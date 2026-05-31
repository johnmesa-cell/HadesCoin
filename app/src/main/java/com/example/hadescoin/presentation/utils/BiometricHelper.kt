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
 * IMPORTANTE: BiometricPrompt de AndroidX requiere una FragmentActivity.
 * ComponentActivity (base de MainActivity en Compose) NO extiende FragmentActivity.
 *
 * Solución: MainActivity debe extender FragmentActivity en lugar de ComponentActivity.
 * FragmentActivity extiende ComponentActivity, por lo que es 100% compatible con Compose.
 *
 * Uso desde cualquier Composable:
 *   val activity = LocalContext.current as FragmentActivity
 *   BiometricHelper.mostrar(activity, ...)
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
     * @param activity   FragmentActivity — MainActivity ya la extiende tras el fix
     * @param titulo     Título del diálogo
     * @param subtitulo  Subtítulo descriptivo
     * @param onExito    Callback cuando la autenticación es exitosa
     * @param onError    Callback cuando falla o el usuario cancela
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
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    onError(errString.toString())
                }
            }
            override fun onAuthenticationFailed() {
                // El sistema ya muestra el feedback nativo, no se hace nada adicional
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
