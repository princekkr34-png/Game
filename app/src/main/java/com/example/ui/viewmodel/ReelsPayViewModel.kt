package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AdminAnalytics
import com.example.data.model.PaymentTransaction
import com.example.data.model.ReelComment
import com.example.data.model.ReelVideo
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SubscriptionStatus
import com.example.data.model.User
import com.example.data.model.WebhookSimEvent
import com.example.data.repository.ReelsRepository
import com.example.data.repository.SubscriptionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppNavTab {
    REELS_FEED,
    VIP_VAULT,
    SUBSCRIPTION,
    ADMIN_STUDIO,
    ARCHITECTURE
}

data class ReelsPayUiState(
    val currentTab: AppNavTab = AppNavTab.REELS_FEED,
    val currentReelIndex: Int = 0,
    val isPlaying: Boolean = true,
    val isMuted: Boolean = false,
    val showPaywallModal: Boolean = false,
    val showUpiCheckoutSheet: Boolean = false,
    val showCommentsSheet: Boolean = false,
    val showAuthModal: Boolean = false,
    val showUploadReelModal: Boolean = false,
    val activeCommentsReelId: String? = null,
    val selectedPlan: SubscriptionPlan? = null,
    val upiCheckoutStep: Int = 0, // 0: Idle, 1: Initiating Mandate, 2: Simulating UPI App, 3: Success
    val selectedUpiApp: String = "Google Pay",
    val upiVpaInput: String = "alex@okhdfcbank",
    val drmWatermarkEnabled: Boolean = true,
    val antiScreenCaptureSecure: Boolean = true,
    val adminAnalytics: AdminAnalytics = AdminAnalytics(),
    val webhookLogs: List<WebhookSimEvent> = emptyList(),
    val userNotification: String? = null
)

class ReelsPayViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val reelsRepo = ReelsRepository(db.reelsDao())
    private val subRepo = SubscriptionRepository(db.reelsDao())

    private val _uiState = MutableStateFlow(ReelsPayUiState())
    val uiState: StateFlow<ReelsPayUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    val allReels: StateFlow<List<ReelVideo>> = reelsRepo.allReels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSubscription: StateFlow<Subscription?> = subRepo.currentSubscription
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userProfile: StateFlow<User> = subRepo.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), User())

    val paymentHistory: StateFlow<List<PaymentTransaction>> = subRepo.paymentHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availablePlans = subRepo.availablePlans

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun getCommentsForActiveReel(): Flow<List<ReelComment>> {
        return _uiState.flatMapLatest { state ->
            val reelId = state.activeCommentsReelId
            if (reelId != null) {
                reelsRepo.getCommentsForReel(reelId)
            } else {
                flowOf(emptyList())
            }
        }
    }

    init {
        viewModelScope.launch {
            reelsRepo.seedInitialReelsIfEmpty()
            subRepo.seedInitialSubscriptionData()
            _uiState.value = _uiState.value.copy(selectedPlan = availablePlans.first())
            initSampleWebhookLogs()
        }
    }

    private fun initSampleWebhookLogs() {
        val now = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        _uiState.value = _uiState.value.copy(
            webhookLogs = listOf(
                WebhookSimEvent(
                    id = "wh_01",
                    eventType = "subscription.charged",
                    description = "Recurring ₹299 debit processed successfully via NPCI AutoPay",
                    payloadJson = """{"event":"subscription.charged","sub_id":"sub_live_90841","amount":29900,"umrn":"HDFC0001099238472910"}""",
                    timestamp = now,
                    resultStatus = "PROCESSED (200 OK)"
                ),
                WebhookSimEvent(
                    id = "wh_02",
                    eventType = "mandate.authenticated",
                    description = "₹1 Mandate verified by user with Google Pay UPI PIN",
                    payloadJson = """{"event":"mandate.authenticated","token_id":"tok_99182","status":"active"}""",
                    timestamp = "12 Aug 2026",
                    resultStatus = "PROCESSED (200 OK)"
                )
            )
        )
    }

    fun selectTab(tab: AppNavTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun onReelChanged(index: Int) {
        val reels = allReels.value
        if (index in reels.indices) {
            val reel = reels[index]
            val sub = currentSubscription.value
            val isSubActive = sub != null && (sub.status == SubscriptionStatus.ACTIVE || sub.status == SubscriptionStatus.TRIALING)

            _uiState.value = _uiState.value.copy(
                currentReelIndex = index,
                isPlaying = true
            )

            // Check Paywall gating
            if (reel.isExclusiveVip && !isSubActive) {
                _uiState.value = _uiState.value.copy(
                    isPlaying = false,
                    showPaywallModal = true
                )
            }
        }
    }

    fun togglePlayPause() {
        _uiState.value = _uiState.value.copy(isPlaying = !_uiState.value.isPlaying)
    }

    fun toggleMute() {
        _uiState.value = _uiState.value.copy(isMuted = !_uiState.value.isMuted)
    }

    fun toggleLike(reel: ReelVideo) {
        viewModelScope.launch {
            reelsRepo.toggleLike(reel.id, reel.isLiked)
        }
    }

    fun toggleBookmark(reel: ReelVideo) {
        viewModelScope.launch {
            reelsRepo.toggleBookmark(reel.id, reel.isBookmarked)
            _events.emit(if (!reel.isBookmarked) "Saved to VIP Bookmarks" else "Removed from Bookmarks")
        }
    }

    fun toggleFollowCreator(reel: ReelVideo) {
        viewModelScope.launch {
            reelsRepo.toggleFollowCreator(reel.creatorHandle, reel.isFollowingCreator)
            _events.emit(if (!reel.isFollowingCreator) "Following ${reel.creatorHandle}" else "Unfollowed ${reel.creatorHandle}")
        }
    }

    fun shareReel(reel: ReelVideo) {
        viewModelScope.launch {
            _events.emit("Reel Link copied to clipboard: https://reelsvip.app/r/${reel.id}")
        }
    }

    fun openCommentsSheet(reelId: String) {
        _uiState.value = _uiState.value.copy(
            showCommentsSheet = true,
            activeCommentsReelId = reelId
        )
    }

    fun closeCommentsSheet() {
        _uiState.value = _uiState.value.copy(showCommentsSheet = false)
    }

    fun submitComment(text: String) {
        val reelId = _uiState.value.activeCommentsReelId ?: return
        if (text.isBlank()) return
        val user = userProfile.value
        viewModelScope.launch {
            reelsRepo.postComment(reelId, user.name, text)
            _events.emit("Comment posted!")
        }
    }

    fun openPaywall(plan: SubscriptionPlan? = null) {
        _uiState.value = _uiState.value.copy(
            showPaywallModal = true,
            selectedPlan = plan ?: _uiState.value.selectedPlan ?: availablePlans.first()
        )
    }

    fun dismissPaywall() {
        _uiState.value = _uiState.value.copy(showPaywallModal = false)
    }

    fun selectPlan(plan: SubscriptionPlan) {
        _uiState.value = _uiState.value.copy(selectedPlan = plan)
    }

    fun startUpiCheckout(plan: SubscriptionPlan) {
        _uiState.value = _uiState.value.copy(
            selectedPlan = plan,
            showPaywallModal = false,
            showUpiCheckoutSheet = true,
            upiCheckoutStep = 0
        )
    }

    fun setUpiApp(app: String) {
        _uiState.value = _uiState.value.copy(selectedUpiApp = app)
    }

    fun setUpiVpa(vpa: String) {
        _uiState.value = _uiState.value.copy(upiVpaInput = vpa)
    }

    fun dismissUpiCheckout() {
        _uiState.value = _uiState.value.copy(showUpiCheckoutSheet = false, upiCheckoutStep = 0)
    }

    fun confirmUpiAutoPayMandate() {
        val plan = _uiState.value.selectedPlan ?: availablePlans.first()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(upiCheckoutStep = 1) // Initiating
            delay(1200)
            _uiState.value = _uiState.value.copy(upiCheckoutStep = 2) // Simulating UPI App auth
            delay(1500)
            
            // Success registration
            subRepo.setupUpiAutoPayMandate(
                plan = plan,
                upiVpa = _uiState.value.upiVpaInput,
                upiApp = _uiState.value.selectedUpiApp
            )

            _uiState.value = _uiState.value.copy(
                upiCheckoutStep = 3,
                showPaywallModal = false,
                isPlaying = true
            )
            delay(1000)
            _uiState.value = _uiState.value.copy(showUpiCheckoutSheet = false, upiCheckoutStep = 0)
            _events.emit("₹1 Mandate Verified! VIP Subscription Activated via UPI AutoPay.")
        }
    }

    fun cancelSubscription() {
        val sub = currentSubscription.value ?: return
        viewModelScope.launch {
            subRepo.cancelSubscription(sub.id)
            _events.emit("Subscription cancelled. Auto-debits stopped on NPCI.")
        }
    }

    fun reactivateSubscription() {
        val sub = currentSubscription.value ?: return
        viewModelScope.launch {
            subRepo.reactivateSubscription(sub.id)
            _events.emit("Subscription reactivated!")
        }
    }

    fun toggleDrmWatermark() {
        _uiState.value = _uiState.value.copy(drmWatermarkEnabled = !_uiState.value.drmWatermarkEnabled)
    }

    fun toggleAntiScreenCapture() {
        _uiState.value = _uiState.value.copy(antiScreenCaptureSecure = !_uiState.value.antiScreenCaptureSecure)
    }

    fun simulateWebhookEvent(eventType: String) {
        val sub = currentSubscription.value
        val subId = sub?.id ?: "sub_live_90841"
        viewModelScope.launch {
            val result = subRepo.processWebhookEvent(eventType, subId)
            val now = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
            val newEvent = WebhookSimEvent(
                id = "wh_${System.currentTimeMillis()}",
                eventType = eventType,
                description = result,
                payloadJson = """{"event":"$eventType","subscription_id":"$subId","timestamp":"$now"}""",
                timestamp = now,
                resultStatus = "PROCESSED (200 OK)"
            )
            _uiState.value = _uiState.value.copy(
                webhookLogs = listOf(newEvent) + _uiState.value.webhookLogs
            )
            _events.emit("Webhook event [$eventType] executed!")
        }
    }

    fun uploadNewReel(
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        isExclusiveVip: Boolean,
        soundTitle: String
    ) {
        viewModelScope.launch {
            val colorPairs = listOf(
                Pair(0xFF9333EAL, 0xFFFF007AL),
                Pair(0xFF0D9488L, 0xFF3B82F6L),
                Pair(0xFFEA580CL, 0xFFE11D48L),
                Pair(0xFF2563EBL, 0xFF8B5CF6L)
            ).random()

            reelsRepo.uploadReel(
                title = title,
                description = description,
                category = category,
                tags = tags,
                isExclusiveVip = isExclusiveVip,
                soundTrackTitle = soundTitle.ifBlank { "Original Audio • Studio Master" },
                primaryColorHex = colorPairs.first,
                secondaryColorHex = colorPairs.second
            )
            _uiState.value = _uiState.value.copy(showUploadReelModal = false)
            _events.emit("Reel uploaded & scheduled to VIP pipeline!")
        }
    }

    fun openUploadModal() {
        _uiState.value = _uiState.value.copy(showUploadReelModal = true)
    }

    fun dismissUploadModal() {
        _uiState.value = _uiState.value.copy(showUploadReelModal = false)
    }

    fun openAuthModal() {
        _uiState.value = _uiState.value.copy(showAuthModal = true)
    }

    fun dismissAuthModal() {
        _uiState.value = _uiState.value.copy(showAuthModal = false)
    }

    fun loginWithPhoneOtp(phone: String, otp: String) {
        viewModelScope.launch {
            subRepo.updateUserProfile(
                name = "Alex Rivera",
                phone = phone.ifBlank { "+91 98765 43210" },
                email = "alex.rivera@reelsvip.in"
            )
            _uiState.value = _uiState.value.copy(showAuthModal = false)
            _events.emit("Signed in successfully via OTP!")
        }
    }

    fun loginWithGoogle() {
        viewModelScope.launch {
            subRepo.updateUserProfile(
                name = "Alex Rivera",
                phone = "+91 98765 43210",
                email = "alex.rivera.google@reelsvip.in"
            )
            _uiState.value = _uiState.value.copy(showAuthModal = false)
            _events.emit("Google OAuth verified!")
        }
    }
}
