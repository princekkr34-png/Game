package com.example.data.repository

import com.example.data.local.CommentEntity
import com.example.data.local.ReelEntity
import com.example.data.local.ReelsDao
import com.example.data.model.ReelComment
import com.example.data.model.ReelVideo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReelsRepository(private val dao: ReelsDao) {

    val allReels: Flow<List<ReelVideo>> = dao.getAllReels().map { entities ->
        entities.map { it.toDomainModel() }
    }

    fun getCommentsForReel(reelId: String): Flow<List<ReelComment>> = dao.getCommentsForReel(reelId).map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun toggleLike(reelId: String, currentLiked: Boolean) {
        val newLiked = !currentLiked
        val delta = if (newLiked) 1 else -1
        dao.updateLikeStatus(reelId, newLiked, delta)
    }

    suspend fun toggleBookmark(reelId: String, currentBookmarked: Boolean) {
        dao.updateBookmarkStatus(reelId, !currentBookmarked)
    }

    suspend fun toggleFollowCreator(creatorHandle: String, currentFollowing: Boolean) {
        dao.updateFollowStatus(creatorHandle, !currentFollowing)
    }

    suspend fun postComment(reelId: String, userName: String, text: String) {
        val comment = CommentEntity(
            id = "cmt_${System.currentTimeMillis()}",
            reelId = reelId,
            userName = userName,
            userHandle = "@${userName.lowercase().replace(" ", "")}",
            commentText = text,
            timestamp = "Just now",
            likesCount = 0,
            isLiked = false
        )
        dao.insertComment(comment)
    }

    suspend fun uploadReel(
        title: String,
        description: String,
        category: String,
        tags: List<String>,
        isExclusiveVip: Boolean,
        soundTrackTitle: String,
        primaryColorHex: Long,
        secondaryColorHex: Long
    ) {
        val entity = ReelEntity(
            id = "reel_custom_${System.currentTimeMillis()}",
            title = title,
            creatorName = "You (VIP Studio)",
            creatorHandle = "@you_official",
            description = description,
            tagsCsv = tags.joinToString(","),
            category = category,
            durationSec = 28,
            likesCount = 0,
            commentsCount = 0,
            sharesCount = 0,
            isExclusiveVip = isExclusiveVip,
            isLiked = false,
            isBookmarked = false,
            isFollowingCreator = true,
            soundTrackTitle = soundTrackTitle,
            soundTrackArtist = "VIP Audio Vault",
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            videoAspectType = ((1..5).random())
        )
        dao.insertReel(entity)
    }

    suspend fun seedInitialReelsIfEmpty() {
        val initialReels = listOf(
            ReelEntity(
                id = "reel_01",
                title = "Behind the Silicon: How Next-Gen AI Microchips are Fabricated in 3nm Cleanrooms",
                creatorName = "TechVanguard VIP",
                creatorHandle = "@techvanguard",
                description = "Deep dive into extreme ultraviolet (EUV) photolithography and atomic layer deposition. Exclusive backstage look at Taiwan fab facilities. #tech #deeptech #semiconductors",
                tagsCsv = "deeptech,semiconductors,ai_hardware,engineering",
                category = "Technology",
                durationSec = 45,
                likesCount = 28420,
                commentsCount = 1420,
                sharesCount = 6840,
                isExclusiveVip = true,
                isLiked = true,
                isBookmarked = true,
                isFollowingCreator = true,
                soundTrackTitle = "Cybernetic Pulse 120BPM",
                soundTrackArtist = "Synthia Beats",
                primaryColorHex = 0xFF7C3AED, // Violet
                secondaryColorHex = 0xFFFF007A, // Neon Pink
                videoAspectType = 1
            ),
            ReelEntity(
                id = "reel_02",
                title = "Masterclass: High-Frequency Algorithmic Market Making & Order Book Dynamics",
                creatorName = "QuantMaster Class",
                creatorHandle = "@quant_elite",
                description = "Understanding liquidity pools, latency arbitrage, and VWAP execution strategies with real exchange level-3 data feeds. #finance #quant #investing",
                tagsCsv = "finance,quant,trading,wealth",
                category = "Finance & Wealth",
                durationSec = 52,
                likesCount = 41200,
                commentsCount = 2310,
                sharesCount = 11200,
                isExclusiveVip = true,
                isLiked = false,
                isBookmarked = true,
                isFollowingCreator = false,
                soundTrackTitle = "Financial Flow • Minimalist Keys",
                soundTrackArtist = "WallStreet Audio",
                primaryColorHex = 0xFF059669, // Emerald
                secondaryColorHex = 0xFF0284C7, // Sky Blue
                videoAspectType = 2
            ),
            ReelEntity(
                id = "reel_03",
                title = "Cinematography Breakdown: Crafting 70mm Anamorphic Flare in Unreal Engine 5.6",
                creatorName = "CinemaCraft Studios",
                creatorHandle = "@cinemacraft",
                description = "Raytracing volumetric lighting and focal falloff setups that top Hollywood VFX teams use for virtual production stages. #cinematography #vfx #unrealengine",
                tagsCsv = "filmmaking,vfx,cgi,design",
                category = "Filmmaking & VFX",
                durationSec = 38,
                likesCount = 19500,
                commentsCount = 890,
                sharesCount = 4300,
                isExclusiveVip = true,
                isLiked = false,
                isBookmarked = false,
                isFollowingCreator = true,
                soundTrackTitle = "Interstellar Strings & Brass",
                soundTrackArtist = "Orchestral Void",
                primaryColorHex = 0xFFEA580C, // Amber Red
                secondaryColorHex = 0xFF4F46E5, // Indigo
                videoAspectType = 3
            ),
            ReelEntity(
                id = "reel_04",
                title = "Aerospace Engineering: How Scramjet Hypersonic Propulsion Defies Atmospheric Drag",
                creatorName = "AeroSpace Frontiers",
                creatorHandle = "@aerospace_vip",
                description = "Shockwave boundary layer interactions and supersonic combustion physics at Mach 7+. Thermal protection tile breakdown. #science #aerospace #physics",
                tagsCsv = "physics,aerospace,engineering,future",
                category = "Aerospace & Science",
                durationSec = 40,
                likesCount = 33800,
                commentsCount = 1840,
                sharesCount = 8900,
                isExclusiveVip = true,
                isLiked = true,
                isBookmarked = false,
                isFollowingCreator = false,
                soundTrackTitle = "Mach 7 Kinetic Waves",
                soundTrackArtist = "Orbital Sound Lab",
                primaryColorHex = 0xFF2563EB, // Electric Blue
                secondaryColorHex = 0xFFD946EF, // Magenta
                videoAspectType = 4
            ),
            ReelEntity(
                id = "reel_05",
                title = "Private Kitchen: 3-Star Michelin Sous-Vide Wagyu A5 & Truffle Glaze Secret",
                creatorName = "Chef Pierre Laurent",
                creatorHandle = "@chef_pierre_vip",
                description = "Reverse searing at 54°C, bone marrow reduction, and black summer truffle shaving technique. Restaurant grade plating. #culinary #luxury #chef",
                tagsCsv = "culinary,luxury,gourmet,chef",
                category = "Culinary Arts",
                durationSec = 35,
                likesCount = 52900,
                commentsCount = 3120,
                sharesCount = 14200,
                isExclusiveVip = true,
                isLiked = false,
                isBookmarked = false,
                isFollowingCreator = false,
                soundTrackTitle = "Parisian Jazz Cafe Accordion",
                soundTrackArtist = "Le Gourmet Trio",
                primaryColorHex = 0xFFD97706, // Golden Honey
                secondaryColorHex = 0xFFB91C1C, // Deep Crimson
                videoAspectType = 5
            )
        )
        dao.insertReels(initialReels)

        // Seed initial comments
        val sampleComments = listOf(
            CommentEntity("c1", "reel_01", "Rohan Mehta", "@rohan_m", "The depth on the cleanroom EUV optics explanation is insane! Worth every rupee of the subscription.", "2h ago", 45, true),
            CommentEntity("c2", "reel_01", "Priya Sharma", "@priya_tech", "Are you planning an episode on ASML high-NA machines next?", "4h ago", 19, false),
            CommentEntity("c3", "reel_02", "Vikram Rathore", "@vikram_trade", "The order book imbalance metric works wonders in real-time execution!", "1d ago", 88, true),
            CommentEntity("c4", "reel_03", "Ananya Rao", "@ananya_dp", "Unreal 5.6 lumen subsurface scattering is magic. Great breakdown!", "3d ago", 24, false)
        )
        for (c in sampleComments) {
            dao.insertComment(c)
        }
    }
}

private fun ReelEntity.toDomainModel(): ReelVideo {
    return ReelVideo(
        id = id,
        title = title,
        creatorName = creatorName,
        creatorHandle = creatorHandle,
        description = description,
        tags = if (tagsCsv.isBlank()) emptyList() else tagsCsv.split(","),
        category = category,
        durationSec = durationSec,
        likesCount = likesCount,
        commentsCount = commentsCount,
        sharesCount = sharesCount,
        isExclusiveVip = isExclusiveVip,
        isLiked = isLiked,
        isBookmarked = isBookmarked,
        isFollowingCreator = isFollowingCreator,
        soundTrackTitle = soundTrackTitle,
        soundTrackArtist = soundTrackArtist,
        primaryColorHex = primaryColorHex,
        secondaryColorHex = secondaryColorHex,
        videoAspectType = videoAspectType
    )
}

private fun CommentEntity.toDomainModel(): ReelComment {
    return ReelComment(
        id = id,
        reelId = reelId,
        userName = userName,
        userHandle = userHandle,
        commentText = commentText,
        timestamp = timestamp,
        likesCount = likesCount,
        isLiked = isLiked
    )
}
