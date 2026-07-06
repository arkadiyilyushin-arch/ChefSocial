package com.chefsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chefsocial.ui.navigation.AppNavigation
import com.chefsocial.ui.theme.ChefSocialTheme
import com.chefsocial.ui.viewmodel.ChefViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ChefSocialTheme {
                val viewModel: ChefViewModel = viewModel()
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
