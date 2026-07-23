package com.chefsocial

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chefsocial.data.AppDatabase
import com.chefsocial.sync.scheduleBackgroundSync
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.localization.rememberAppStrings
import com.chefsocial.ui.navigation.AppNavigation
import com.chefsocial.ui.theme.ChefSocialTheme
import com.chefsocial.ui.viewmodel.ChefViewModel
import com.chefsocial.util.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private sealed interface StartupState {
    data object Loading : StartupState
    data object Ready : StartupState
    data object Error : StartupState
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        runCatching { scheduleBackgroundSync(applicationContext) }

        setContent {
            var startupState by remember { mutableStateOf<StartupState>(StartupState.Loading) }
            var retryToken by remember { mutableStateOf(0) }

            LaunchedEffect(retryToken) {
                startupState = StartupState.Loading
                startupState = withContext(Dispatchers.IO) {
                    if (runCatching { AppDatabase.get(applicationContext) }.isSuccess) {
                        StartupState.Ready
                    } else {
                        StartupState.Error
                    }
                }
            }

            when (startupState) {
                StartupState.Loading -> ChefSocialTheme { StartupLoadingScreen() }
                StartupState.Error -> ChefSocialTheme {
                    StartupErrorScreen(
                        onRetry = {
                            AppDatabase.resetAndRecreate(applicationContext)
                            retryToken++
                        },
                    )
                }
                StartupState.Ready -> ChefSocialApp()
            }
        }
    }
}

@Composable
private fun ChefSocialApp() {
    val viewModel: ChefViewModel = viewModel()
    val language by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val strings = rememberAppStrings(language)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    ChefSocialTheme(themeMode = themeMode) {
        CompositionLocalProvider(LocalAppStrings provides strings) {
            AppNavigation(viewModel = viewModel)
        }
    }
}

@Composable
private fun StartupLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun StartupErrorScreen(onRetry: () -> Unit) {
    val strings = rememberAppStrings(AppLanguage.RU)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = strings.startupErrorTitle,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = strings.startupErrorBody,
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onRetry) {
            Text(strings.startupRetry)
        }
    }
}
