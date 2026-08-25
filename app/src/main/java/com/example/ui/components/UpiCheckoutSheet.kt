package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SubscriptionPlan
import com.example.ui.theme.AutoPayGreen
import com.example.ui.theme.AutoPayGreenDark
import com.example.ui.theme.CyberGold
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonPink
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun UpiCheckoutSheet(
    plan: SubscriptionPlan,
    selectedUpiApp: String,
    upiVpa: String,
    checkoutStep: Int, // 0: Idle, 1: Initiating, 2: Simulating UPI App, 3: Success
    onSelectUpiApp: (String) -> Unit,
    onVpaChange: (String) -> Unit,
    onConfirmMandate: () -> Unit,
    onDismiss: () -> Unit
) {
    val upiApps = listOf(
        Pair("Google Pay", "gpay"),
        Pair("PhonePe", "phonepe"),
        Pair("Paytm UPI", "paytm"),
        Pair("BHIM UPI", "bhim")
    )

    Dialog(
        onDismissRequest = {
            if (checkoutStep == 0) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF19142E),
                            Color(0xFF100C1F),
                            Color(0xFF0A0714)
                        )
                    )
                )
                .border(
                    BorderStroke(1.5.dp, AutoPayGreen.copy(alpha = 0.5f)),
                    RoundedCornerShape(28.dp)
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = AutoPayGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "NPCI UPI AutoPay",
                                color = AutoPayGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "Secure e-Mandate",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    if (checkoutStep == 0) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price & Mandate Breakdown Card
                Surface(
                    color = Color(0xFF1E1838),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = plan.name,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "3-Day Trial + Auto-debit",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${plan.initialVerificationFeeInr}",
                                    color = AutoPayGreen,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Payable Today",
                                    color = AutoPayGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mandate lifecycle timeline details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Recurring Charge",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "₹${plan.recurringPriceInr} / month",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "First Auto-Debit On",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "After ${plan.trialDays} Days",
                                    color = CyberGold,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Processing Step Animator (If ongoing)
                AnimatedVisibility(visible = checkoutStep > 0) {
                    Surface(
                        color = Color(0xFF120E24),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, AutoPayGreen.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (checkoutStep) {
                                1 -> {
                                    CircularProgressIndicator(
                                        color = AutoPayGreen,
                                        modifier = Modifier.size(44.dp),
                                        strokeWidth = 3.5.dp
                                    )
                                    Text(
                                        text = "1. Creating Mandate Token...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Connecting to Razorpay & NPCI UPI Gateway",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                2 -> {
                                    CircularProgressIndicator(
                                        color = NeonPink,
                                        modifier = Modifier.size(44.dp),
                                        strokeWidth = 3.5.dp
                                    )
                                    Text(
                                        text = "2. Authorizing in $selectedUpiApp...",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Authenticating ₹1 e-mandate via UPI PIN intent",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                                3 -> {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = AutoPayGreen,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Mandate Active & UMRN Generated!",
                                        color = AutoPayGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "VIP access unlocked across all 4K Reels",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // If step is 0 (Selection phase)
                if (checkoutStep == 0) {
                    Text(
                        text = "Select UPI App for AutoPay e-Mandate",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // UPI App Grid
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        upiApps.forEach { (appName, appKey) ->
                            val isSelected = selectedUpiApp == appName
                            Surface(
                                onClick = { onSelectUpiApp(appName) },
                                color = if (isSelected) Color(0xFF261D42) else Color(0xFF161228),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) AutoPayGreen else Color.White.copy(alpha = 0.08f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Smartphone,
                                            contentDescription = appName,
                                            tint = if (isSelected) AutoPayGreen else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = appName,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = AutoPayGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // UPI ID / VPA Input
                    OutlinedTextField(
                        value = upiVpa,
                        onValueChange = onVpaChange,
                        label = { Text("UPI ID / VPA", color = TextSecondary, fontSize = 12.sp) },
                        placeholder = { Text("e.g. mobile@okhdfcbank", color = TextSecondary.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AutoPayGreen,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upi_vpa_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Button
                    Button(
                        onClick = onConfirmMandate,
                        colors = ButtonDefaults.buttonColors(containerColor = AutoPayGreen),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_autopay_mandate_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = "AutoPay",
                                tint = Color(0xFF071F11),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Authorize ₹1 Mandate in $selectedUpiApp",
                                color = Color(0xFF071F11),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "NPCI Secured",
                            tint = AutoPayGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "256-bit Encrypted • Direct RBI/NPCI e-Mandate Framework",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
