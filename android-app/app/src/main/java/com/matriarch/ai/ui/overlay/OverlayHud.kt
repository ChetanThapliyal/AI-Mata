package com.matriarch.ai.ui.overlay

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun OverlayHud(
    currentThought: String,
    isConnected: Boolean,
    isMayaSpeaking: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Top Left: Status Dot (Connection)
        StatusIndicator(
            isConnected = isConnected,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Top Right: Timer
        SessionTimer(
            modifier = Modifier.align(Alignment.TopEnd)
        )

        // Center: Matriarch "Eye"
        MatriarchEye(
            modifier = Modifier.align(Alignment.Center)
        )

        // Bottom Center: Thought Bubble & Speaking Indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            if (isMayaSpeaking) {
                SpeakingIndicator()
                Spacer(modifier = Modifier.height(8.dp))
            }
            ThoughtBubble(
                text = currentThought
            )
        }
    }
}

@Composable
fun SpeakingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF7C4DFF).copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Simple waveform mock
        repeat(3) { index ->
            val scale = if (index == 1) pulse else (1.5f - pulse)
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(4.dp)
                    .height((8 * scale).dp)
                    .background(Color.White, CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Maya is speaking...",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusIndicator(isConnected: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color = if (isConnected) Color(0xFF00E676) else Color(0xFFFF3D00)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(if (isConnected) pulse else 1f)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (isConnected) "LIVE" else "OFFLINE",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SessionTimer(modifier: Modifier = Modifier) {
    var ticks by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            ticks++
        }
    }

    val minutes = ticks / 60
    val seconds = ticks % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Text(
        text = timeString,
        color = Color.White.copy(alpha = 0.8f),
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun MatriarchEye(modifier: Modifier = Modifier) {
    // Subtle crosshair / target to indicate vision tracking
    Box(
        modifier = modifier
            .size(120.dp)
            .background(Color.Transparent)
            .blur(2.dp)
    ) {
        // Just a decorative minimalist bracket
        Text(
            text = "[   ]",
            color = Color(0xFF7C4DFF).copy(alpha = 0.3f), // Deep purple accent
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun ThoughtBubble(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.DarkGray.copy(alpha = 0.4f))
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
