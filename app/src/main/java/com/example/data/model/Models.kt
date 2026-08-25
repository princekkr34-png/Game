package com.example.data.model

data class User(
    val id: String = "usr_99812",
    val name: String = "Alex Rivera",
    val phone: String = "+91 98765 43210",
    val email: String = "alex.rivera@reelsvip.in",
    val avatarUrl: String = "",
    val authProvider: String = "MOBILE_OTP",
    val isVipMember: Boolean = true,
    val role: String = "VIP_SUBSCRIBER" // USER, VIP_SUBSCRIBER, CREATOR, ADMIN
)

enum class SubscriptionStatus {
    ACTIVE,
    TRIALING,
    PENDING_AUTOPAY,
    PAST_DUE,
    CANCELLED,
    NONE
}

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val initialVerificationFeeInr: Int = 1,
    val recurringPriceInr: Int = 299,
    val billingInterval: String = "monthly", // monthly, annual
    val trialDays: Int = 3,
    val badge: String = "MOST POPULAR",
    val features: List<String> = listOf(
        "Full access to 10,000+ 4K exclusive VIP Reels",
        "Zero ads & uninterrupted vertical playback",
        "Offline caching & high-bitrate audio tracks",
        "Instant creator direct Q&A and masterclasses",
        "UPI AutoPay automated billing (cancel anytime)"
    )
)

data class Subscription(
    val id: String = "sub_live_90841",
    val userId: String = "usr_99812",
    val planId: String = "plan_monthly_vip_299",
    val planName: String = "VIP Monthly AutoPay",
    val priceInr: Int = 299,
    val initialMandateFeeInr: Int = 1,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val mandateId: String = "man_razor_881023",
    val umrn: String = "HDFC0001099238472910", // Unique Mandate Reference Number (NPCI)
    val upiVpa: String = "alex@okhdfcbank",
    val upiApp: String = "Google Pay",
    val startDate: String = "15 Aug 2026",
    val nextBillingDate: String = "15 Sep 2026",
    val maxMandateDebitLimitInr: Int = 1000,
    val totalBilledInr: Int = 300,
    val autoDebitDayOfMonth: Int = 15
)

data class PaymentTransaction(
    val id: String,
    val subscriptionId: String,
    val amountInr: Int,
    val type: String, // "MANDATE_SETUP_TRIAL", "RECURRING_AUTOPAY_DEBIT", "RETRY_DEBIT"
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val gatewayTxnId: String,
    val paymentMethod: String,
    val formattedDate: String,
    val failureReason: String? = null
)

data class ReelVideo(
    val id: String,
    val title: String,
    val creatorName: String,
    val creatorHandle: String,
    val creatorAvatar: String = "",
    val description: String,
    val tags: List<String>,
    val category: String,
    val durationSec: Int = 30,
    val likesCount: Int = 14200,
    val commentsCount: Int = 890,
    val sharesCount: Int = 3200,
    val isExclusiveVip: Boolean = true,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val isFollowingCreator: Boolean = false,
    val soundTrackTitle: String = "Original Audio • Cinematic Waveform",
    val soundTrackArtist: String = "CyberSound Studio",
    val cdnProvider: String = "Cloudflare Stream + AWS S3 Edge",
    val streamBitrate: String = "1080p (4.2 Mbps AV1)",
    val primaryColorHex: Long = 0xFF8B5CF6,
    val secondaryColorHex: Long = 0xFFFF007A,
    val videoAspectType: Int = 1 // 1: Cyber Neon, 2: Sunset Velvet, 3: Electric Emerald, 4: Deep Cosmos, 5: Golden Sunrise
)

data class ReelComment(
    val id: String,
    val reelId: String,
    val userName: String,
    val userHandle: String,
    val commentText: String,
    val timestamp: String,
    val likesCount: Int = 12,
    val isLiked: Boolean = false
)

data class AdminAnalytics(
    val activeSubscribersCount: Int = 42850,
    val mrrInr: Long = 12812150L,
    val churnRatePercent: Double = 3.2,
    val trialMandateConversionPercent: Double = 78.4,
    val failedAutoDebitsCount: Int = 142,
    val totalReelsPublished: Int = 1240,
    val dailyActiveViewers: Int = 94200,
    val monthlyGrowthRate: Double = 24.6
)

data class WebhookSimEvent(
    val id: String,
    val eventType: String,
    val description: String,
    val payloadJson: String,
    val timestamp: String,
    val resultStatus: String
)
