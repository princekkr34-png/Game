package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PaymentTransaction
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionStatus
import com.example.data.model.User
import com.example.ui.theme.AutoPayGreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SubscriptionScreen(
    currentSubscription: Subscription?,
    currentUser: User,
    paymentHistory: List<PaymentTransaction>,
    showWatermark: Boolean,
    antiScreenCapture: Boolean,
    onToggleWatermark: () -> Unit,
    onToggleAntiScreenCapture: () -> Unit,
    onOpenPaywall: () -> Unit,
    onCancelSubscription: () -> Unit,
    onReactivateSubscription: () -> Unit,
    onOpenAuthModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSubActive = currentSubscription != null && (
        currentSubscription.status == SubscriptionStatus.ACTIVE ||
        currentSubscription.status == SubscriptionStatus.TRIALING
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // Top Profile Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(NeonPink, ElectricViolet))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.name.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Column {
                    Text(
                        text = currentUser.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentUser.phone} • ${currentUser.email}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onOpenAuthModal,
                modifier = Modifier
                    .size(36.dp)
                    .background(SurfaceElevated, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.SwitchAccount,
                    contentDescription = "Switch Account",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Subscription Card
        if (currentSubscription != null) {
            ActiveSubscriptionCard(
                subscription = currentSubscription,
                onCancel = onCancelSubscription,
                onReactivate = onReactivateSubscription,
                onUpgrade = onOpenPaywall
            )
        } else {
            NoSubscriptionBanner(onSubscribe = onOpenPaywall)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security & Anti-Screen Recording Section
        Text(
            text = "Video DRM & Content Security",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Watermark Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dynamic Phone Watermark",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Embeds UID and phone number overlay across 4K stream to prevent content leaks",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                    Switch(
                        checked = showWatermark,
                        onCheckedChange = { onToggleWatermark() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonPink
                        ),
                        modifier = Modifier.testTag("watermark_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Anti-Screen Capture Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FLAG_SECURE Anti-Screen Capture",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Blocks hardware screenshots & third-party screen video recorders",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                    Switch(
                        checked = antiScreenCapture,
                        onCheckedChange = { onToggleAntiScreenCapture() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AutoPayGreen
                        ),
                        modifier = Modifier.testTag("anti_capture_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // AutoPay Payment History Ledger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AutoPay Transaction Ledger",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = AutoPayGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "NPCI Verified",
                    color = AutoPayGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (paymentHistory.isEmpty()) {
                Text(
                    text = "No payment history yet.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            } else {
                paymentHistory.forEach { txn ->
                    PaymentTransactionRow(txn = txn)
                }
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionCard(
    subscription: Subscription,
    onCancel: () -> Unit,
    onReactivate: () -> Unit,
    onUpgrade: () -> Unit
) {
    val isStatusActive = subscription.status == SubscriptionStatus.ACTIVE || subscription.status == SubscriptionStatus.TRIALING

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.5.dp,
            if (isStatusActive) AutoPayGreen.copy(alpha = 0.6f) else ErrorRed.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Plan Title & Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = subscription.planName,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${subscription.priceInr} / month • Auto-debits on day ${subscription.autoDebitDayOfMonth}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = if (isStatusActive) AutoPayGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isStatusActive) AutoPayGreen else ErrorRed)
                ) {
                    Text(
                        text = subscription.status.name,
                        color = if (isStatusActive) AutoPayGreen else ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mandate metadata grid
            Surface(
                color = Color.Black.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("NPCI UMRN Token", color = TextSecondary, fontSize = 11.sp)
                        Text(subscription.umrn, color = CyberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active UPI VPA", color = TextSecondary, fontSize = 11.sp)
                        Text("${subscription.upiApp} (${subscription.upiVpa})", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Next Auto-Debit Date", color = TextSecondary, fontSize = 11.sp)
                        Text(subscription.nextBillingDate, color = AutoPayGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Max Mandate Limit", color = TextSecondary, fontSize = 11.sp)
                        Text("₹${subscription.maxMandateDebitLimitInr}", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isStatusActive) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_sub_button")
                    ) {
                        Text("Cancel Mandate", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onReactivate,
                        colors = ButtonDefaults.buttonColors(containerColor = AutoPayGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reactivate VIP", color = Color(0xFF071F11), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Change Plan", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NoSubscriptionBanner(onSubscribe: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = CyberGold,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "No Active VIP Subscription",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Subscribe today with UPI AutoPay to unlock 10,000+ exclusive 4K masterclass reels.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
                onClick = onSubscribe,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start ₹1 Trial with UPI AutoPay", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaymentTransactionRow(txn: PaymentTransaction) {
    val isSuccess = txn.status == "SUCCESS"
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isSuccess) AutoPayGreen.copy(alpha = 0.15f) else ErrorRed.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                        contentDescription = txn.status,
                        tint = if (isSuccess) AutoPayGreen else ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = if (txn.type == "MANDATE_SETUP_TRIAL") "Mandate Verification Auth" else "AutoPay Recurring Debit",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = txn.formattedDate,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Txn: ${txn.gatewayTxnId}",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${txn.amountInr}",
                    color = if (isSuccess) Color.White else ErrorRed,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = txn.status,
                    color = if (isSuccess) AutoPayGreen else ErrorRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
