package com.example.kotlinproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.kotlinproject.models.UserPreferences
import com.example.kotlinproject.ui.theme.KotlinProjectTheme

class MainActivity : ComponentActivity() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        userPreferences = UserPreferences(applicationContext)

        setContent {
            KotlinProjectTheme {
                MainScreen(userPreferences = userPreferences)
            }
        }
    }
}
