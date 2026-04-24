package com.example.hadescoin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hadescoin.presentation.navigation.AppNavHost
import com.example.hadescoin.ui.theme.HadesCoinTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HadesCoinTheme {
                // Punto de entrada de la navegación
                AppNavHost()
            }
        }
    }
}
/**
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HadesCoinTheme {
        Greeting("Android")
    }
}
 */