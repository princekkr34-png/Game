package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reels")
data class ReelEntity(
    @PrimaryKey val id: String,
    val title: String,
    val creatorName: String,
    val creatorHandle: String,
    val description: String,
    val tagsCsv: String,
    val category: String,
    val durationSec: Int,
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isExclusiveVip: Boolean,
    val isLiked: Boolean,
    val isBookmarked: Boolean,
    val isFollowingCreator: Boolean,
    val soundTrackTitle: String,
    val soundTrackArtist: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val videoAspectType: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val planId: String,
    val planName: String,
    val priceInr: Int,
    val status: String,
    val mandateId: String,
    val umrn: String,
    val upiVpa: String,
    val upiApp: String,
    val startDate: String,
    val nextBillingDate: String,
    val totalBilledInr: Int,
    val autoDebitDayOfMonth: Int
)

@Entity(tableName = "payment_transactions")
data class PaymentTransactionEntity(
    @PrimaryKey val id: String,
    val subscriptionId: String,
    val amountInr: Int,
    val type: String,
    val status: String,
    val gatewayTxnId: String,
    val paymentMethod: String,
    val formattedDate: String,
    val failureReason: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val authProvider: String,
    val isVipMember: Boolean,
    val role: String
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val reelId: String,
    val userName: String,
    val userHandle: String,
    val commentText: String,
    val timestamp: String,
    val likesCount: Int,
    val isLiked: Boolean
)
