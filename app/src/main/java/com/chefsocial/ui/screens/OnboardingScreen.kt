package com.chefsocial.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.chefsocial.R
import com.chefsocial.ui.components.CheflyBackground
import com.chefsocial.ui.localization.LocalAppStrings
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.theme.cheflyCardColors
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val body: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val strings = LocalAppStrings.current
    val pages = listOf(
        OnboardingPage(R.drawable.onboarding_welcome, strings.onboardingWelcomeTitle, strings.onboardingWelcomeBody),
        OnboardingPage(R.drawable.onboarding_feed, strings.onboardingFeedTitle, strings.onboardingFeedBody),
        OnboardingPage(R.drawable.onboarding_share, strings.onboardingShareTitle, strings.onboardingShareBody),
    )
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    CheflyBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = strings.appTitle,
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(28.dp),
                colors = cheflyCardColors(),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Image(
                            painter = painterResource(pages[page].imageRes),
                            contentDescription = pages[page].title,
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(160.dp),
                            contentScale = ContentScale.Fit,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = pages[page].title,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = pages[page].body,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OnboardingPageIndicator(
                pageCount = pages.size,
                currentPage = pagerState.currentPage,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (pagerState.currentPage < pages.lastIndex) {
                Text(
                    text = strings.onboardingSwipeHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onComplete()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CheflyTerracotta,
                    contentColor = Color.White,
                ),
            ) {
                Text(
                    if (pagerState.currentPage == pages.lastIndex) strings.onboardingStart
                    else strings.onboardingNext,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            Box(
                modifier = Modifier
                    .size(if (selected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) Color.White else Color.White.copy(alpha = 0.45f),
                    ),
            )
        }
    }
}
