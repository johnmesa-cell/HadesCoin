package com.example.hadescoin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hadescoin.di.ServiceLocator
import com.example.hadescoin.presentation.navigation.AppNavigation
import com.example.hadescoin.ui.theme.HadesCoinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrar el contexto en ServiceLocator para DataStore (sesion local)
        ServiceLocator.init(applicationContext)

        enableEdgeToEdge()

        setContent {
            HadesCoinTheme {
                AppNavigation()
            }
        }
    }
}
