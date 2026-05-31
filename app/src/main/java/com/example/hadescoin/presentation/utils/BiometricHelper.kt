package com.example.hadescoin.presentation.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Núcleo de biometría de HadesCoin.
 *
 * Encapsula toda la lógica de BiometricPrompt de AndroidX.
 * Se reutiliza desde LoginView, WithdrawCodeDialog, TransferView y ProfileView
 * sin duplicar código.
 *
 * Uso:
 *   val disponible = BiometricHelper.isDisponible(context)
 *   BiometricHelper.mostrar(
 *       activity  = requireActivity() as FragmentActivity,
 *       titulo    = "Confirmar retiro",
 *       subtitulo = "Usa tu huella para autorizar",
 *       onExito   = { /* continuar */ },
 *       onError   = { msg -> /* mostrar error o fallback a PIN */ }
 *   )
 */
object BiometricHelper {

    /**
     * Verifica si el dispositivo tiene biometría disponible y registrada.
     * Retorna true solo si hay hardware Y al menos una huella/cara enrollada.
     */
    fun isDisponible(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Muestra el diálogo nativo de biometría del sistema operativo.
     *
     * @param activity   FragmentActivity requerida por BiometricPrompt
     * @param titulo     Título del diálogo (ej: "Iniciar sesión")
     * @param subtitulo  Subtítulo descriptivo (ej: "Usa tu huella para entrar")
     * @param onExito    Callback cuando la autenticación es exitosa
     * @param onError    Callback cuando falla o el usuario cancela — recibe el mensaje de error
     */
    fun mostrar(
        activity:  FragmentActivity,
        titulo:    String,
        subtitulo: String,
        onExito:   () -> Unit,
        onError:   (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onExito()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                // Código 10 = el usuario canceló manualmente → no es un error real
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }
            override fun onAuthenticationFailed() {
                // Huella no reconocida — el sistema ya muestra el mensaje nativo,
                // no necesitamos hacer nada adicional aquí
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titulo)
            .setSubtitle(subtitulo)
            .setNegativeButtonText("Usar PIN")
            .build()

        prompt.authenticate(info)
    }
}
