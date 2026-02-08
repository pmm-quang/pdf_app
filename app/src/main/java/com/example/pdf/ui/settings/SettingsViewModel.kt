package com.example.pdf.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.pdf.PdfApplication
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class SettingsViewModel : ViewModel() {

    fun getSignedInAccount(context: Context) = GoogleSignIn.getLastSignedInAccount(context)

    fun signOut(context: Context) {
        val application = context.applicationContext as PdfApplication
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)

        googleSignInClient.signOut().addOnCompleteListener {
            // Clear the service to indicate the user is signed out
            application.container.googleDriveService = null
        }
    }
}