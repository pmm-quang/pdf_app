package com.example.pdf

import android.app.Application
import com.example.pdf.data.AppContainer
import com.example.pdf.data.DefaultAppContainer

class PdfApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}