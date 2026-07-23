package com.chefsocial.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.chefsocial.ui.components.CheflyBackButton
import com.chefsocial.ui.components.CheflyScaffold
import com.chefsocial.ui.components.NewsArticleContent
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.theme.cheflySurfaceTopBarColors
import com.chefsocial.ui.viewmodel.ChefViewModel
import com.chefsocial.util.shareNews

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    viewModel: ChefViewModel,
    newsId: Long,
    onBack: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val post by viewModel.observeNewsPost(newsId).collectAsState()

    CheflyScaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.newsTitle) },
                navigationIcon = { CheflyBackButton(onClick = onBack) },
                colors = cheflySurfaceTopBarColors(),
            )
        },
        bottomBar = {
            val article = post
            if (article != null) {
                Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
                    OutlinedButton(
                        onClick = { shareNews(context, article) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Text(strings.shareNews, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
    ) { padding ->
        val article = post
        if (article == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(color = CheflyTerracotta)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                NewsArticleContent(post = article)
            }
        }
    }
}
