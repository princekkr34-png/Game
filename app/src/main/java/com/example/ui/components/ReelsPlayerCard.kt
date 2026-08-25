package com.example.ui.components

import java.util.Locale

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReelVideo
import com.example.ui.theme.AutoPayGreen
import com.example.ui.theme.CyberGold
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReelsPlayerCard(
    reel: ReelVideo,
    isPlaying: Boolean,
    isMuted: Boolean,
    isPaywallLocked: Boolean,
    userWatermarkText: String,
    showWatermark: Boolean,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onLike: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onCommentsClick: () -> Unit,
    onFollowClick: () -> Unit,
    onUnlockVipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var showHeartBurst by remember { mutableStateOf(false) }
    var heartBurstOffset by remember { mutableStateOf(Offset.Zero) }
    val scope = rememberCoroutineScope()

    // Smooth simulated video progress
    val progressAnim = remember { Animatable(0f) }
    LaunchedEffect(isPlaying, isPaywallLocked, reel.id) {
        if (isPlaying && !isPaywallLocked) {
            while (true) {
                progressAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = reel.durationSec * 1000,
                        easing = LinearEasing
                    )
                )
                progressAnim.snapTo(0f)
            }
        } else {
            progressAnim.stop()
        }
    }

    // Audio Disc rotation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(reel.id, isPaywallLocked) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        if (!isPaywallLocked) {
                            heartBurstOffset = offset
                            showHeartBurst = true
                            if (!reel.isLiked) {
                                onLike()
                            }
                            scope.launch {
                                delay(900)
                                showHeartBurst = false
                            }
                        }
                    },
                    onTap = {
                        if (isPaywallLocked) {
                            onUnlockVipClick()
                        } else {
                            onTogglePlay()
                        }
                    }
                )
            }
    ) {
        // 1. High-Fidelity Animated Video Simulation Canvas
        VideoSimulationCanvas(
            reel = reel,
            isPlaying = isPlaying && !isPaywallLocked,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Gradient overlays for crisp text legibility
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
                    )
                )
        )

        // 3. Top Header Bar (VIP Status, Bitrate Badge, DRM Watermark & Audio Mute)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = CyberGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "VIP Reel",
                            tint = CyberGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "EXCLUSIVE VIP",
                            color = CyberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "4K • AV1",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onToggleMute,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .testTag("audio_mute_toggle")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "Audio Mute",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 4. DRM Anti-Screen Recording Dynamic Watermark
        if (showWatermark) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "DRM",
                        tint = Color.White.copy(alpha = 0.45f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "DRM PROTECTED • $userWatermarkText",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // 5. Play / Pause Overlay Icon Indicator
        AnimatedVisibility(
            visible = !isPlaying && !isPaywallLocked,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // 6. Double Tap Heart Burst Animation
        AnimatedVisibility(
            visible = showHeartBurst,
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(
                initialScale = 0.3f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f)
            ),
            exit = fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 1.4f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Liked",
                tint = NeonPink,
                modifier = Modifier.size(100.dp)
            )
        }

        // 7. Right Action Rail (Like, Comment, Bookmark, Share, Audio Disc)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Creator Avatar with Follow Plus Badge
            Box(contentAlignment = Alignment.BottomCenter) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(reel.primaryColorHex), Color(reel.secondaryColorHex))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = reel.creatorName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (!reel.isFollowingCreator) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(NeonPink)
                            .clickable { onFollowClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Like Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onLike() }
            ) {
                Icon(
                    imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (reel.isLiked) NeonPink else Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatCount(reel.likesCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Comments Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onCommentsClick() }
                    .testTag("open_comments_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "Comments",
                    tint = Color.White,
                    modifier = Modifier.size(29.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatCount(reel.commentsCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Bookmark Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onBookmark() }
            ) {
                Icon(
                    imageVector = if (reel.isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (reel.isBookmarked) CyberGold else Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (reel.isBookmarked) "Saved" else "Save",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Share Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onShare() }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = formatCount(reel.sharesCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Spinning Vinyl Audio Stem
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF221F33))
                    .rotate(if (isPlaying && !isPaywallLocked) discRotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(NeonPink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Sound Disc",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        // 8. Bottom Information Overlay (Creator handle, Title, Description, Audio)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(start = 16.dp, end = 12.dp, bottom = 85.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = reel.creatorHandle,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                if (reel.isFollowingCreator) {
                    Surface(
                        color = Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Following",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = reel.title,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = if (isDescriptionExpanded) 4 else 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = reel.description,
                color = TextSecondary,
                fontSize = 13.sp,
                maxLines = if (isDescriptionExpanded) 6 else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { isDescriptionExpanded = !isDescriptionExpanded }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tags
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                reel.tags.take(3).forEach { tag ->
                    Text(
                        text = "#$tag",
                        color = CyberGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Audio Marquee Track Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Audio track",
                    tint = NeonPink,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${reel.soundTrackTitle} • ${reel.soundTrackArtist}",
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 9. Bottom Playback Timeline Bar
        LinearProgressIndicator(
            progress = { progressAnim.value },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter),
            color = NeonPink,
            trackColor = Color.White.copy(alpha = 0.2f)
        )

        // 10. Paywall Gated Screen Blur & Unlock CTA
        if (isPaywallLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { onUnlockVipClick() }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E1A30), Color(0xFF110E1C))
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(CyberGold.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked VIP Reel",
                            tint = CyberGold,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Text(
                        text = "VIP Subscriber Exclusive",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "This exclusive 4K masterclass is locked. Start your 3-day trial for ₹1 with instant UPI AutoPay to unlock the entire vault.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Button(
                        onClick = onUnlockVipClick,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("unlock_paywall_cta")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Start ₹1 Trial with UPI AutoPay",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Text(
                        text = "Auto-renews at ₹299/mo • Cancel anytime with 1 tap",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoSimulationCanvas(
    reel: ReelVideo,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "video_canvas")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val color1 = Color(reel.primaryColorHex)
        val color2 = Color(reel.secondaryColorHex)
        val color3 = Color(0xFF0D0B18)

        // Background gradient
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color1.copy(alpha = 0.6f), color2.copy(alpha = 0.35f), color3),
                center = Offset(width * 0.5f, height * 0.4f),
                radius = width * 0.85f * pulse
            )
        )

        // Geometric dynamic studio waveforms
        val numBars = 18
        val barWidth = width / (numBars * 1.6f)
        for (i in 0 until numBars) {
            val factor = sin(wavePhase + (i * 0.45f))
            val barHeight = (height * 0.12f) * (0.4f + 0.6f * factor.coerceAtLeast(0.1f))
            val x = (i * (barWidth * 1.5f)) + 20f
            val y = (height * 0.52f) - (barHeight / 2f)

            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(color2.copy(alpha = 0.8f), color1.copy(alpha = 0.4f))
                ),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
        }

        // Concentric cinema light rings
        for (ring in 1..3) {
            val ringRadius = (width * 0.25f * ring) * (if (isPlaying) pulse else 1f)
            drawCircle(
                color = Color.White.copy(alpha = 0.04f * ring),
                radius = ringRadius,
                center = Offset(width * 0.5f, height * 0.42f),
                style = Stroke(width = 2f)
            )
        }
    }
}

private val TextTertiary = Color(0xFF88849E)

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
