package com.example.hadescoin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.presentation.navigation.AppNavigation
import com.example.hadescoin.ui.theme.HadesCoinTheme

/**
 * MainActivity extiende FragmentActivity (en lugar de ComponentActivity) para que
 * BiometricPrompt de AndroidX funcione correctamente desde cualquier Composable.
 *
 * FragmentActivity extiende ComponentActivity — 100% compatible con Jetpack Compose.
 * No se pierde ninguna funcionalidad existente.
 */
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ServiceLocator.init(applicationContext)

        enableEdgeToEdge()

        setContent {
            HadesCoinTheme {
                AppNavigation()
            }
        }
    }
}
