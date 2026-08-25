package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AuthModal
import com.example.ui.components.CommentsBottomSheet
import com.example.ui.components.PaywallModal
import com.example.ui.components.UpiCheckoutSheet
import com.example.ui.screens.AdminStudioScreen
import com.example.ui.screens.ArchitectureScreen
import com.example.ui.screens.ReelsFeedScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.screens.VipVaultScreen
import com.example.ui.theme.AutoPayGreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonPink
import com.example.ui.theme.ReelsPayTheme
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.ReelsPayViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ReelsPayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ReelsPayTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Dynamically sync Android FLAG_SECURE for anti-screen recording
                LaunchedEffect(uiState.antiScreenCaptureSecure) {
                    if (uiState.antiScreenCaptureSecure) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }

                ReelsPayMainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ReelsPayMainApp(viewModel: ReelsPayViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allReels by viewModel.allReels.collectAsStateWithLifecycle()
    val currentSub by viewModel.currentSubscription.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val paymentHistory by viewModel.paymentHistory.collectAsStateWithLifecycle()
    val currentComments by viewModel.getCommentsForActiveReel().collectAsStateWithLifecycle(initialValue = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            ReelsBottomNavigationBar(
                currentTab = uiState.currentTab,
                onTabSelected = { tab -> viewModel.selectTab(tab) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (uiState.currentTab == AppNavTab.REELS_FEED) 0.dp else innerPadding.calculateBottomPadding())
        ) {
            when (uiState.currentTab) {
                AppNavTab.REELS_FEED -> {
                    ReelsFeedScreen(
                        reels = allReels,
                        currentSubscription = currentSub,
                        currentUser = userProfile,
                        isPlaying = uiState.isPlaying,
                        isMuted = uiState.isMuted,
                        showWatermark = uiState.drmWatermarkEnabled,
                        currentReelIndex = uiState.currentReelIndex,
                        onReelChanged = { index -> viewModel.onReelChanged(index) },
                        onTogglePlay = { viewModel.togglePlayPause() },
                        onToggleMute = { viewModel.toggleMute() },
                        onLike = { reel -> viewModel.toggleLike(reel) },
                        onBookmark = { reel -> viewModel.toggleBookmark(reel) },
                        onShare = { reel -> viewModel.shareReel(reel) },
                        onCommentsClick = { reelId -> viewModel.openCommentsSheet(reelId) },
                        onFollowClick = { reel -> viewModel.toggleFollowCreator(reel) },
                        onUnlockVipClick = { viewModel.openPaywall() }
                    )
                }

                AppNavTab.VIP_VAULT -> {
                    VipVaultScreen(
                        reels = allReels,
                        currentSubscription = currentSub,
                        onSelectReel = { reel ->
                            val index = allReels.indexOfFirst { it.id == reel.id }
                            if (index != -1) {
                                viewModel.onReelChanged(index)
                                viewModel.selectTab(AppNavTab.REELS_FEED)
                            }
                        },
                        onUnlockVipClick = { viewModel.openPaywall() }
                    )
                }

                AppNavTab.SUBSCRIPTION -> {
                    SubscriptionScreen(
                        currentSubscription = currentSub,
                        currentUser = userProfile,
                        paymentHistory = paymentHistory,
                        showWatermark = uiState.drmWatermarkEnabled,
                        antiScreenCapture = uiState.antiScreenCaptureSecure,
                        onToggleWatermark = { viewModel.toggleDrmWatermark() },
                        onToggleAntiScreenCapture = { viewModel.toggleAntiScreenCapture() },
                        onOpenPaywall = { viewModel.openPaywall() },
                        onCancelSubscription = { viewModel.cancelSubscription() },
                        onReactivateSubscription = { viewModel.reactivateSubscription() },
                        onOpenAuthModal = { viewModel.openAuthModal() }
                    )
                }

                AppNavTab.ADMIN_STUDIO -> {
                    AdminStudioScreen(
                        analytics = uiState.adminAnalytics,
                        webhookLogs = uiState.webhookLogs,
                        showUploadModal = uiState.showUploadReelModal,
                        onOpenUploadModal = { viewModel.openUploadModal() },
                        onDismissUploadModal = { viewModel.dismissUploadModal() },
                        onUploadReel = { title, desc, cat, tags, isVip, sound ->
                            viewModel.uploadNewReel(title, desc, cat, tags, isVip, sound)
                        },
                        onSimulateWebhook = { eventType ->
                            viewModel.simulateWebhookEvent(eventType)
                        }
                    )
                }

                AppNavTab.ARCHITECTURE -> {
                    ArchitectureScreen()
                }
            }

            // Paywall Modal Overlay
            if (uiState.showPaywallModal) {
                PaywallModal(
                    plans = viewModel.availablePlans,
                    selectedPlan = uiState.selectedPlan,
                    onSelectPlan = { plan -> viewModel.selectPlan(plan) },
                    onProceedToCheckout = { plan -> viewModel.startUpiCheckout(plan) },
                    onDismiss = { viewModel.dismissPaywall() }
                )
            }

            // UPI AutoPay Checkout Bottom Sheet
            if (uiState.showUpiCheckoutSheet) {
                UpiCheckoutSheet(
                    plan = uiState.selectedPlan ?: viewModel.availablePlans.first(),
                    selectedUpiApp = uiState.selectedUpiApp,
                    upiVpa = uiState.upiVpaInput,
                    checkoutStep = uiState.upiCheckoutStep,
                    onSelectUpiApp = { app -> viewModel.setUpiApp(app) },
                    onVpaChange = { vpa -> viewModel.setUpiVpa(vpa) },
                    onConfirmMandate = { viewModel.confirmUpiAutoPayMandate() },
                    onDismiss = { viewModel.dismissUpiCheckout() }
                )
            }

            // Comments Bottom Sheet
            if (uiState.showCommentsSheet) {
                CommentsBottomSheet(
                    comments = currentComments,
                    onPostComment = { text -> viewModel.submitComment(text) },
                    onDismiss = { viewModel.closeCommentsSheet() }
                )
            }

            // Authentication Modal
            if (uiState.showAuthModal) {
                AuthModal(
                    onLoginWithPhoneOtp = { phone, otp -> viewModel.loginWithPhoneOtp(phone, otp) },
                    onLoginWithGoogle = { viewModel.loginWithGoogle() },
                    onDismiss = { viewModel.dismissAuthModal() }
                )
            }
        }
    }
}

@Composable
fun ReelsBottomNavigationBar(
    currentTab: AppNavTab,
    onTabSelected: (AppNavTab) -> Unit
) {
    Surface(
        color = Color(0xFF0F0D1A).copy(alpha = 0.95f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets.navigationBars,
            modifier = Modifier.height(68.dp)
        ) {
            NavigationBarItem(
                selected = currentTab == AppNavTab.REELS_FEED,
                onClick = { onTabSelected(AppNavTab.REELS_FEED) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == AppNavTab.REELS_FEED) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle,
                        contentDescription = "Reels Feed",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Reels", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.REELS_FEED) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonPink,
                    selectedTextColor = NeonPink,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = NeonPink.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_reels_tab")
            )

            NavigationBarItem(
                selected = currentTab == AppNavTab.VIP_VAULT,
                onClick = { onTabSelected(AppNavTab.VIP_VAULT) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == AppNavTab.VIP_VAULT) Icons.Filled.Explore else Icons.Outlined.Explore,
                        contentDescription = "VIP Vault",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Vault", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.VIP_VAULT) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyberGold,
                    selectedTextColor = CyberGold,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = CyberGold.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_vault_tab")
            )

            NavigationBarItem(
                selected = currentTab == AppNavTab.SUBSCRIPTION,
                onClick = { onTabSelected(AppNavTab.SUBSCRIPTION) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == AppNavTab.SUBSCRIPTION) Icons.Filled.CreditCard else Icons.Outlined.CreditCard,
                        contentDescription = "AutoPay",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("AutoPay", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.SUBSCRIPTION) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AutoPayGreen,
                    selectedTextColor = AutoPayGreen,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = AutoPayGreen.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_autopay_tab")
            )

            NavigationBarItem(
                selected = currentTab == AppNavTab.ADMIN_STUDIO,
                onClick = { onTabSelected(AppNavTab.ADMIN_STUDIO) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == AppNavTab.ADMIN_STUDIO) Icons.Filled.Analytics else Icons.Outlined.Analytics,
                        contentDescription = "Studio",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Studio", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.ADMIN_STUDIO) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonPink,
                    selectedTextColor = NeonPink,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = NeonPink.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_studio_tab")
            )

            NavigationBarItem(
                selected = currentTab == AppNavTab.ARCHITECTURE,
                onClick = { onTabSelected(AppNavTab.ARCHITECTURE) },
                icon = {
                    Icon(
                        imageVector = if (currentTab == AppNavTab.ARCHITECTURE) Icons.Filled.AccountTree else Icons.Outlined.AccountTree,
                        contentDescription = "Architecture",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text("Arch", fontSize = 11.sp, fontWeight = if (currentTab == AppNavTab.ARCHITECTURE) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ElectricViolet,
                    selectedTextColor = ElectricViolet,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = ElectricViolet.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_arch_tab")
            )
        }
    }
}
