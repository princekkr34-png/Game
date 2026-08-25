package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelsDao {
    @Query("SELECT * FROM reels ORDER BY createdAt DESC")
    fun getAllReels(): Flow<List<ReelEntity>>

    @Query("SELECT * FROM reels WHERE id = :reelId LIMIT 1")
    suspend fun getReelById(reelId: String): ReelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReels(reels: List<ReelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelEntity)

    @Update
    suspend fun updateReel(reel: ReelEntity)

    @Query("UPDATE reels SET isLiked = :isLiked, likesCount = likesCount + :delta WHERE id = :reelId")
    suspend fun updateLikeStatus(reelId: String, isLiked: Boolean, delta: Int)

    @Query("UPDATE reels SET isBookmarked = :isBookmarked WHERE id = :reelId")
    suspend fun updateBookmarkStatus(reelId: String, isBookmarked: Boolean)

    @Query("UPDATE reels SET isFollowingCreator = :isFollowing WHERE creatorHandle = :creatorHandle")
    suspend fun updateFollowStatus(creatorHandle: String, isFollowing: Boolean)

    @Query("SELECT * FROM subscriptions LIMIT 1")
    fun getActiveSubscription(): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSubscription(subscription: SubscriptionEntity)

    @Query("UPDATE subscriptions SET status = :status WHERE id = :subId")
    suspend fun updateSubscriptionStatus(subId: String, status: String)

    @Query("SELECT * FROM payment_transactions ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentTransactionEntity>)

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(user: UserProfileEntity)

    @Query("SELECT * FROM comments WHERE reelId = :reelId")
    fun getCommentsForReel(reelId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
}
