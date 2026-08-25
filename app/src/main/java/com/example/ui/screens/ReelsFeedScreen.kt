package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReelVideo
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionStatus
import com.example.data.model.User
import com.example.ui.components.ReelsPlayerCard
import com.example.ui.theme.BackgroundDark

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsFeedScreen(
    reels: List<ReelVideo>,
    currentSubscription: Subscription?,
    currentUser: User,
    isPlaying: Boolean,
    isMuted: Boolean,
    showWatermark: Boolean,
    currentReelIndex: Int,
    onReelChanged: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    onToggleMute: () -> Unit,
    onLike: (ReelVideo) -> Unit,
    onBookmark: (ReelVideo) -> Unit,
    onShare: (ReelVideo) -> Unit,
    onCommentsClick: (String) -> Unit,
    onFollowClick: (ReelVideo) -> Unit,
    onUnlockVipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (reels.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading VIP Reels...",
                color = Color.White,
                fontSize = 16.sp
            )
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = currentReelIndex.coerceIn(0, reels.size - 1),
        pageCount = { reels.size }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onReelChanged(page)
        }
    }

    LaunchedEffect(currentReelIndex) {
        if (currentReelIndex != pagerState.currentPage && currentReelIndex in 0 until reels.size) {
            pagerState.scrollToPage(currentReelIndex)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        key = { reels[it].id }
    ) { page ->
        val reel = reels[page]
        val isPageActive = pagerState.currentPage == page
        val isSubActive = currentSubscription != null && (
            currentSubscription.status == SubscriptionStatus.ACTIVE ||
            currentSubscription.status == SubscriptionStatus.TRIALING
        )
        val isPaywallLocked = reel.isExclusiveVip && !isSubActive

        ReelsPlayerCard(
            reel = reel,
            isPlaying = isPageActive && isPlaying && !isPaywallLocked,
            isMuted = isMuted,
            isPaywallLocked = isPaywallLocked,
            userWatermarkText = "${currentUser.phone} • UID:${currentUser.id.takeLast(5)}",
            showWatermark = showWatermark,
            onTogglePlay = onTogglePlay,
            onToggleMute = onToggleMute,
            onLike = { onLike(reel) },
            onBookmark = { onBookmark(reel) },
            onShare = { onShare(reel) },
            onCommentsClick = { onCommentsClick(reel.id) },
            onFollowClick = { onFollowClick(reel) },
            onUnlockVipClick = onUnlockVipClick
        )
    }
}
