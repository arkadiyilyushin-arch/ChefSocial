package com.chefsocial

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chefsocial.sync.scheduleBackgroundSync
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.localization.rememberAppStrings
import com.chefsocial.ui.navigation.AppNavigation
import com.chefsocial.ui.theme.ChefSocialTheme
import com.chefsocial.ui.viewmodel.ChefViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        scheduleBackgroundSync(applicationContext)

        setContent {
            val viewModel: ChefViewModel = viewModel()
            val language by viewModel.language.collectAsState()
            val strings = rememberAppStrings(language)

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) {}

            androidx.compose.runtime.LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            ChefSocialTheme {
                CompositionLocalProvider(LocalAppStrings provides strings) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
