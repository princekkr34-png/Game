package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AdminAnalytics
import com.example.data.model.WebhookSimEvent
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AdminStudioScreen(
    analytics: AdminAnalytics,
    webhookLogs: List<WebhookSimEvent>,
    showUploadModal: Boolean,
    onOpenUploadModal: () -> Unit,
    onDismissUploadModal: () -> Unit,
    onUploadReel: (title: String, desc: String, cat: String, tags: List<String>, isVip: Boolean, sound: String) -> Unit,
    onSimulateWebhook: (eventType: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // Top Header
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
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Studio",
                        tint = NeonPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Admin & Creator Studio",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Subscriber Analytics & Video Delivery Pipeline",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onOpenUploadModal,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("open_upload_modal_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Upload",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text("New Reel", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 1. KPI Analytics Cards Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Active VIP Subscribers",
                    value = formatNumber(analytics.activeSubscribersCount),
                    subtext = "+${analytics.monthlyGrowthRate}% this month",
                    icon = Icons.Default.Groups,
                    accentColor = AutoPayGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Monthly MRR (INR)",
                    value = "₹${formatNumber(analytics.mrrInr)}",
                    subtext = "Via UPI AutoPay debits",
                    icon = Icons.Default.Money,
                    accentColor = CyberGold,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "₹1 Trial Conversion",
                    value = "${analytics.trialMandateConversionPercent}%",
                    subtext = "Auto-debited on Day 4",
                    icon = Icons.Default.TrendingUp,
                    accentColor = ElectricViolet,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Monthly Churn Rate",
                    value = "${analytics.churnRatePercent}%",
                    subtext = "Mandates cancelled",
                    icon = Icons.Default.Sensors,
                    accentColor = NeonPink,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Real-Time Webhook Simulator (Test AutoPay state machine)
        Text(
            text = "Live UPI Webhook Event Simulator",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Trigger Razorpay / NPCI gateway events to test real-time state machine transitions and paywall access.",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Simulate Gateway Webhook Dispatch:",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSimulateWebhook("subscription.charged") },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AutoPayGreen.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f).testTag("webhook_charged_btn")
                    ) {
                        Text("₹299 Auto-Debit", color = AutoPayGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onSimulateWebhook("payment.failed") },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f).testTag("webhook_failed_btn")
                    ) {
                        Text("Debit Failed", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSimulateWebhook("subscription.cancelled") },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CyberGold.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f).testTag("webhook_cancel_btn")
                    ) {
                        Text("Mandate Revoked", color = CyberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onSimulateWebhook("subscription.activated") },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.6f)),
                        modifier = Modifier.weight(1f).testTag("webhook_activate_btn")
                    ) {
                        Text("Re-Activate VIP", color = ElectricViolet, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Webhook Audit Logs Inspector
        Text(
            text = "Webhook Audit Logs (${webhookLogs.size})",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            webhookLogs.take(4).forEach { log ->
                WebhookLogRow(log = log)
            }
        }
    }

    if (showUploadModal) {
        UploadReelDialog(
            onDismiss = onDismissUploadModal,
            onUpload = onUploadReel
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtext,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun WebhookLogRow(log: WebhookSimEvent) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AutoPayGreen)
                    )
                    Text(
                        text = log.eventType,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = log.timestamp,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.description,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = log.payloadJson,
                color = CyberGold.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UploadReelDialog(
    onDismiss: () -> Unit,
    onUpload: (title: String, desc: String, cat: String, tags: List<String>, isVip: Boolean, sound: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technology") }
    var tagsInput by remember { mutableStateOf("ai,quantum,future,tech") }
    var soundTitle by remember { mutableStateOf("Cybernetic Waveform Audio") }
    var isExclusiveVip by remember { mutableStateOf(true) }

    val categories = listOf("Technology", "Finance & Wealth", "Filmmaking & VFX", "Aerospace & Science", "Culinary Arts")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(26.dp))
                .background(SurfaceDark)
                .border(BorderStroke(1.dp, NeonPink.copy(alpha = 0.5f)), RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Upload & Schedule VIP Reel",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reel Title", color = TextSecondary) },
                    placeholder = { Text("e.g. Quantum Computing 101 Masterclass", color = TextSecondary.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("upload_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes", color = TextSecondary) },
                    placeholder = { Text("Detailed breakdown of key takeaways...", color = TextSecondary.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("upload_desc_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("Tags (comma separated)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // VIP exclusive toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Gate Behind VIP Paywall", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Requires active UPI AutoPay subscription", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isExclusiveVip,
                        onCheckedChange = { isExclusiveVip = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NeonPink)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onUpload(
                                    title,
                                    description,
                                    category,
                                    tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                    isExclusiveVip,
                                    soundTitle
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("publish_reel_button")
                    ) {
                        Text("Publish 4K Reel", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun formatNumber(num: Any): String {
    return runCatching {
        NumberFormat.getNumberInstance(Locale.US).format(num)
    }.getOrDefault(num.toString())
}
