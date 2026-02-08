package com.example.pdf.ui.settings

import android.app.Activity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.example.pdf.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity

    // Get the current language from the app's configuration. This is the most reliable method.
    val currentLanguage = ConfigurationCompat.getLocales(context.resources.configuration)[0]?.language ?: "en"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_language_settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                LanguageItem(
                    language = stringResource(R.string.language_english),
                    // English is selected if the current language is not Vietnamese.
                    isSelected = currentLanguage != "vi",
                ) {
                    val appLocale = LocaleListCompat.forLanguageTags("en")
                    AppCompatDelegate.setApplicationLocales(appLocale)
                    activity?.recreate()
                }
            }
            item {
                LanguageItem(
                    language = stringResource(R.string.language_vietnamese),
                    isSelected = currentLanguage == "vi",
                ) {
                    val appLocale = LocaleListCompat.forLanguageTags("vi")
                    AppCompatDelegate.setApplicationLocales(appLocale)
                    activity?.recreate()
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(language: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = language,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null // Decorative icon, indicates selection
            )
        }
    }
}
