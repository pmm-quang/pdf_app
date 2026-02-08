package com.example.pdf

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.pdf.ui.MainScreen
import com.example.pdf.ui.theme.PdfTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PdfTheme {
                MainScreen()
            }
        }
    }
}
