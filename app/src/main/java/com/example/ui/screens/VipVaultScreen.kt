package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReelVideo
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionStatus
import com.example.ui.theme.AutoPayGreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun VipVaultScreen(
    reels: List<ReelVideo>,
    currentSubscription: Subscription?,
    onSelectReel: (ReelVideo) -> Unit,
    onUnlockVipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf(
        "All",
        "Technology",
        "Finance & Wealth",
        "Filmmaking & VFX",
        "Aerospace & Science",
        "Culinary Arts"
    )

    val isSubActive = currentSubscription != null && (
        currentSubscription.status == SubscriptionStatus.ACTIVE ||
        currentSubscription.status == SubscriptionStatus.TRIALING
    )

    val filteredReels = reels.filter {
        val matchesCat = selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)
        val matchesQuery = searchQuery.isBlank() ||
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.creatorName.contains(searchQuery, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
        matchesCat && matchesQuery
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Vault",
                        tint = CyberGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "VIP Vault",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Curated Masterclasses & 4K Series",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Surface(
                color = if (isSubActive) AutoPayGreen.copy(alpha = 0.2f) else NeonPink.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isSubActive) AutoPayGreen.copy(alpha = 0.5f) else NeonPink.copy(alpha = 0.5f)),
                modifier = Modifier.clickable {
                    if (!isSubActive) onUnlockVipClick()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isSubActive) Icons.Default.Star else Icons.Default.Lock,
                        contentDescription = "Status",
                        tint = if (isSubActive) AutoPayGreen else NeonPink,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (isSubActive) "VIP ACTIVE" else "GET VIP ₹1",
                        color = if (isSubActive) AutoPayGreen else NeonPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by topic, tag, or creator...", color = TextSecondary.copy(alpha = 0.6f), fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = TextSecondary
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricViolet,
                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag("vault_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonPink,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceElevated,
                        labelColor = TextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) NeonPink else Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of Video Cards
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredReels) { reel ->
                VipReelGridCard(
                    reel = reel,
                    isLocked = reel.isExclusiveVip && !isSubActive,
                    onClick = {
                        if (reel.isExclusiveVip && !isSubActive) {
                            onUnlockVipClick()
                        } else {
                            onSelectReel(reel)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun VipReelGridCard(
    reel: ReelVideo,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vault_card_${reel.id}")
    ) {
        Column {
            // Simulated Thumbnail Preview with duration & lock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(reel.primaryColorHex).copy(alpha = 0.85f),
                                Color(reel.secondaryColorHex).copy(alpha = 0.65f),
                                Color(0xFF0F0B1A)
                            )
                        )
                    )
            ) {
                // Top VIP Badge
                if (reel.isExclusiveVip) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "VIP",
                                tint = CyberGold,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "VIP",
                                color = CyberGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Duration badge
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "0:${reel.durationSec}",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Center Lock or Play Icon
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.PlayArrow,
                        contentDescription = if (isLocked) "Locked" else "Play",
                        tint = if (isLocked) CyberGold else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Card Body Details
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = reel.title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = reel.creatorName,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Likes",
                            tint = NeonPink,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = formatCountShort(reel.likesCount),
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = reel.category,
                            color = TextSecondary,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatCountShort(count: Int): String {
    return when {
        count >= 1000 -> String.format(Locale.getDefault(), "%.1fk", count / 1000.0)
        else -> count.toString()
    }
}
