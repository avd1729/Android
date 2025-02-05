package com.example.contentproviders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Wrap your composable in a MaterialTheme and Surface for styling
            Surface(color = MaterialTheme.colorScheme.background) {
                StudentListScreen()  // Calling the StudentListScreen composable
            }
        }
    }
}
