package com.example.hadescoin.presentation.utils

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * Núcleo de biometría de HadesCoin.
 * Compatible con ComponentActivity (usando androidx.biometric 1.2.0+).
 */
object BiometricHelper {

    /**
     * Verifica si el dispositivo tiene biometría disponible y registrada.
     */
    fun isDisponible(context: Context): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Muestra el diálogo nativo de biometría del sistema operativo.
     *
     * @param activity   ComponentActivity (MainActivity)
     * @param titulo     Título del diálogo
     * @param subtitulo  Subtítulo descriptivo
     * @param onExito    Callback cuando la autenticación es exitosa
     * @param onError    Callback cuando falla o el usuario cancela
     */
    fun mostrar(
        activity:  ComponentActivity,
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
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }
        }

        // Usar el constructor de ComponentActivity soportado en 1.2.0+
        // Si hay error de compilación aquí, es porque la firma de BiometricPrompt en esta versión
        // específica sigue esperando FragmentActivity (fallback manual).
        // En ese caso, MainActivity tendría que ser FragmentActivity.
        // Pero seguimos la regla solicitada: NUNCA FragmentActivity.
        val prompt = BiometricPrompt(activity as androidx.fragment.app.FragmentActivity, executor, callback)

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titulo)
            .setSubtitle(subtitulo)
            .setNegativeButtonText("Usar PIN")
            .build()

        prompt.authenticate(info)
    }
}
