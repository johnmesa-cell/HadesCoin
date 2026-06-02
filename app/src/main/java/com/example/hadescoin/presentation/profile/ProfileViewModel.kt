package com.example.hadescoin.presentation.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.hadescoin.R
import com.example.hadescoin.core.Constants
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.domain.model.AppUser
import com.example.hadescoin.domain.repository.SessionRepository
import com.example.hadescoin.domain.usecase.CreateNotificationUseCase
import com.example.hadescoin.domain.usecase.GenerateVerificationCodeUseCase
import com.example.hadescoin.domain.usecase.GetUnreadNotificationsCountUseCase
import com.example.hadescoin.domain.usecase.GetUserProfileUseCase
import com.example.hadescoin.domain.usecase.QueueNotificationEmailUseCase
import com.example.hadescoin.domain.usecase.UpdateUserNicknameUseCase
import com.example.hadescoin.domain.usecase.UpdateUserPinUseCase
import com.example.hadescoin.domain.usecase.ValidateVerificationCodeUseCase
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class ProfileViewModel @JvmOverloads constructor(
    application: Application,
    private val getUserProfileUseCase:  GetUserProfileUseCase           = ServiceLocator.provideGetUserProfileUseCase(),
    private val updatePinUseCase:       UpdateUserPinUseCase            = ServiceLocator.provideUpdateUserPinUseCase(),
    private val updateNicknameUseCase:  UpdateUserNicknameUseCase       = ServiceLocator.provideUpdateUserNicknameUseCase(),
    private val generateCodeUseCase:    GenerateVerificationCodeUseCase = ServiceLocator.provideGenerateVerificationCodeUseCase(),
    private val validateCodeUseCase:    ValidateVerificationCodeUseCase = ServiceLocator.provideValidateVerificationCodeUseCase(),
    private val createNotificationUseCase:          CreateNotificationUseCase          = ServiceLocator.provideCreateNotificationUseCase(),
    private val getUnreadNotificationsCountUseCase: GetUnreadNotificationsCountUseCase = ServiceLocator.provideGetUnreadNotificationsCountUseCase(),
    private val queueNotificationEmailUseCase:      QueueNotificationEmailUseCase      = ServiceLocator.provideQueueNotificationEmailUseCase(),
    private val sessionRepository:                  SessionRepository                  = ServiceLocator.provideSessionRepository()
) : AndroidViewModel(application) {

    private val _user = MutableLiveData<AppUser?>()
    val user: LiveData<AppUser?> = _user

    private val _cargando = MutableLiveData(false)
    val cargando: LiveData<Boolean> = _cargando

    private val _mensajeExito = MutableLiveData<String?>()
    val mensajeExito: LiveData<String?> = _mensajeExito

    private val _mensajeError = MutableLiveData<String?>()
    val mensajeError: LiveData<String?> = _mensajeError

    private val _codigoGenerado = MutableLiveData<String?>()
    val codigoGenerado: LiveData<String?> = _codigoGenerado

    private val _codigoValidado = MutableLiveData(false)
    val codigoValidado: LiveData<Boolean> = _codigoValidado

    private val _notificacionesNoLeidas = MutableLiveData(0)
    val notificacionesNoLeidas: LiveData<Int> = _notificacionesNoLeidas

    // ── Biometría ────────────────────────────────────────────────────────────
    private val _biometriaActiva = MutableLiveData(sessionRepository.isBiometriaActiva(sessionRepository.getPhone()))
    val biometriaActiva: LiveData<Boolean> = _biometriaActiva

    private fun timeoutMsg() = getApplication<Application>().getString(R.string.error_timeout_message)

    fun setBiometriaActiva(activa: Boolean) {
        val phone = sessionRepository.getPhone()
        if (phone.isNotBlank()) {
            sessionRepository.setBiometriaActiva(phone, activa)
            _biometriaActiva.value = activa
        }
    }
    // ───────────────────────────────────────────────────────────────

    private var phoneParaReset: String = ""

    // ── Cargar perfil ───────────────────────────────────────────────────
    fun cargarPerfil(phoneNumber: String) {
        _user.value           = null
        _notificacionesNoLeidas.value = 0
        _mensajeError.value   = null
        _mensajeExito.value   = null
        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    _user.value = getUserProfileUseCase(phoneNumber)
                    cargarNoLeidas(phoneNumber)
                }
            } catch (e: TimeoutCancellationException) {
                _mensajeError.value = timeoutMsg()
            } catch (e: Exception) {
                _mensajeError.value = "Error al cargar el perfil"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Cambiar PIN ───────────────────────────────────────────────────
    fun cambiarPin(phoneNumber: String, pinActual: String, pinNuevo: String, confirmacion: String) {
        val userVal = _user.value ?: return
        when {
            pinActual != userVal.pin                                -> { _mensajeError.value = "El PIN actual es incorrecto"; return }
            pinNuevo.length != 4 || !pinNuevo.all { it.isDigit() } -> { _mensajeError.value = "El nuevo PIN debe tener exactamente 4 dígitos"; return }
            pinNuevo != confirmacion                               -> { _mensajeError.value = "La confirmación no coincide"; return }
            pinNuevo == pinActual                                  -> { _mensajeError.value = "El nuevo PIN no puede ser igual al anterior"; return }
            esPinObvio(pinNuevo)                                   -> { _mensajeError.value = "El PIN es muy sencillo. Usa uno más seguro."; return }
        }
        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    if (updatePinUseCase(phoneNumber, pinNuevo)) {
                        _mensajeExito.value = "PIN actualizado correctamente"
                        registrarNotificacionPerfil(phoneNumber, "PIN actualizado", "Tu PIN de seguridad fue actualizado correctamente.", "SECURITY")
                        cargarPerfil(phoneNumber)
                    } else {
                        _mensajeError.value = "No se pudo actualizar el PIN"
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _mensajeError.value = timeoutMsg()
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Recuperación de PIN ───────────────────────────────────────────────
    fun generarCodigoVerificacion(phoneNumber: String, documentNumber: String) {
        if (phoneNumber.isBlank() || documentNumber.isBlank()) { _mensajeError.value = "Teléfono y cédula son requeridos"; return }
        phoneParaReset = phoneNumber
        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    val code = generateCodeUseCase(phoneNumber, documentNumber)
                    if (code != null) _codigoGenerado.value = code
                    else _mensajeError.value = "No se encontró un usuario con esos datos."
                }
            } catch (e: TimeoutCancellationException) {
                _mensajeError.value = timeoutMsg()
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun validarCodigo(code: String) {
        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    val ok = validateCodeUseCase(phoneParaReset, code)
                    if (ok) _codigoValidado.value = true
                    else _mensajeError.value = "Código incorrecto."
                }
            } catch (e: TimeoutCancellationException) {
                _mensajeError.value = timeoutMsg()
            } catch (e: Exception) {
                _mensajeError.value = "Error al validar: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetearPin(nuevoPin: String) {
        if (nuevoPin.length != 4 || !nuevoPin.all { it.isDigit() }) { _mensajeError.value = "El nuevo PIN debe tener exactamente 4 dígitos"; return }
        if (esPinObvio(nuevoPin)) { _mensajeError.value = "El PIN es muy sencillo. Usa uno más seguro."; return }
        val phone = phoneParaReset.ifBlank { _user.value?.phoneNumber ?: return }
        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    if (updatePinUseCase(phone, nuevoPin)) {
                        _mensajeExito.value = "PIN actualizado correctamente"
                        registrarNotificacionPerfil(phone, "PIN restablecido", "Tu PIN fue restablecido mediante verificación.", "SECURITY")
                        _codigoGenerado.value = null
                        _codigoValidado.value = false
                        cargarPerfil(phone)
                    } else {
                        _mensajeError.value = "No se pudo actualizar el PIN"
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _mensajeError.value = timeoutMsg()
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    // ── Apodo ─────────────────────────────────────────────────────────────
    fun actualizarApodo(phoneNumber: String, nuevoApodo: String) {
        if (nuevoApodo.isBlank()) { _mensajeError.value = "El apodo no puede estar vacío"; return }
        viewModelScope.launch {
            _cargando.value = true
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    if (updateNicknameUseCase(phoneNumber, nuevoApodo)) {
                        _mensajeExito.value = "Apodo actualizado"
                        registrarNotificacionPerfil(phoneNumber, "Perfil actualizado", "Tu apodo fue actualizado a $nuevoApodo.", "PROFILE")
                        cargarPerfil(phoneNumber)
                    } else {
                        _mensajeError.value = "No se pudo actualizar el apodo"
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _mensajeError.value = timeoutMsg()
            } catch (e: Exception) {
                _mensajeError.value = "Error: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun clearMessages() {
        _mensajeExito.value   = null
        _mensajeError.value   = null
        _codigoGenerado.value = null
        _codigoValidado.value = false
    }

    fun cargarNoLeidas(phoneNumber: String) {
        viewModelScope.launch {
            try {
                withTimeout(Constants.NETWORK_TIMEOUT_MS) {
                    _notificacionesNoLeidas.value = getUnreadNotificationsCountUseCase(phoneNumber)
                }
            } catch (e: TimeoutCancellationException) {
                // Notificaciones son secundarias
            } catch (_: Exception) { 
                _notificacionesNoLeidas.value = 0 
            }
        }
    }

    private suspend fun registrarNotificacionPerfil(phoneNumber: String, title: String, message: String, type: String) {
        createNotificationUseCase(phoneNumber = phoneNumber, title = title, message = message, type = type)
        val email = _user.value?.email.orEmpty()
        if (email.isNotBlank()) {
            queueNotificationEmailUseCase(phoneNumber = phoneNumber, toEmail = email, subject = "HadesCoin - $title", body = message)
        }
    }

    private fun esPinObvio(pin: String) =
        listOf("1234","0000","1111","2222","3333","4444","5555","6666","7777","8888","9999","4321").contains(pin)
}
