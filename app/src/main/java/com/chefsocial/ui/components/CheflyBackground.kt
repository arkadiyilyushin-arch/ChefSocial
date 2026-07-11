package com.chefsocial.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chefsocial.ui.theme.CheflySalmon
import com.chefsocial.ui.theme.CheflyTerracotta
import com.chefsocial.ui.theme.CheflyTerracottaDark

@Composable
fun CheflyBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CheflySalmon,
                        CheflyTerracotta,
                        CheflyTerracottaDark,
                    ),
                ),
            ),
    ) {
        DecorativeCircle(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-60).dp, y = 80.dp),
            alpha = 0.18f,
        )
        DecorativeCircle(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 120.dp),
            alpha = 0.14f,
        )
        DecorativeCircle(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomStart)
                .offset(x = 30.dp, y = (-100).dp),
            alpha = 0.12f,
        )
        Text(
            text = "🥚",
            fontSize = 28.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-48).dp, y = 200.dp),
        )
        Text(
            text = "🌿",
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 24.dp, y = (-80).dp),
        )
        content()
    }
}

@Composable
private fun DecorativeCircle(
    modifier: Modifier = Modifier,
    alpha: Float,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = alpha)),
    )
}
