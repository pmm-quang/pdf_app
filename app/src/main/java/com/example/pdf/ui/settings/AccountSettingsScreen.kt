package com.example.pdf.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pdf.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen() {
    val viewModel: SettingsViewModel = viewModel()
    val context = LocalContext.current
    var signedInAccount by remember { mutableStateOf(viewModel.getSignedInAccount(context)) }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                signedInAccount = task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                // Handle sign-in error
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.title_account_settings)) })
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            signedInAccount?.let { account ->
                AccountInfo(account, onSignOut = {
                    viewModel.signOut(context)
                    signedInAccount = null
                }, onSwitchAccount = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        signInLauncher.launch(googleSignInClient.signInIntent)
                    }
                })
            } ?: run {
                SignInPrompt { signInLauncher.launch(googleSignInClient.signInIntent) }
            }
        }
    }
}

@Composable
private fun AccountInfo(account: GoogleSignInAccount, onSignOut: () -> Unit, onSwitchAccount: () -> Unit) {
    Text(stringResource(R.string.account_signed_in_as, account.email ?: ""))
    Row(Modifier.padding(top = 16.dp)) {
        Button(onClick = onSignOut) {
            Text(stringResource(R.string.account_sign_out))
        }
        Button(modifier = Modifier.padding(start = 8.dp), onClick = onSwitchAccount) {
            Text(stringResource(R.string.account_switch))
        }
    }
}

@Composable
private fun SignInPrompt(onSignIn: () -> Unit) {
    Text(stringResource(R.string.account_not_signed_in))
    Button(modifier = Modifier.padding(top = 16.dp), onClick = onSignIn) {
        Text(stringResource(R.string.account_sign_in))
    }
}