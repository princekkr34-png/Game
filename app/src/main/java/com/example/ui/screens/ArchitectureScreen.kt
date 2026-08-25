package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ArchitectureData
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

@Composable
fun ArchitectureScreen(
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf("Database Schema") }
    val sections = listOf(
        "Database Schema",
        "Express.js & Razorpay",
        "Flutter Client",
        "UPI AutoPay Flow",
        "NestJS Controller",
        "FastAPI Backend",
        "DRM & Security"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 100.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountTree,
                contentDescription = "Architecture",
                tint = ElectricViolet,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "System Architecture & Specs",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Full-Stack Blueprints, Schemas & Webhooks",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Section Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sections) { section ->
                val isSelected = selectedSection == section
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSection = section },
                    label = {
                        Text(
                            text = section,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricViolet,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceElevated,
                        labelColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedSection) {
            "Database Schema" -> DatabaseSchemaSection()
            "Express.js & Razorpay" -> CodeBlockSection("Express.js + Razorpay Mandates & Webhooks", ArchitectureData.EXPRESS_JS_BACKEND_CODE)
            "Flutter Client" -> CodeBlockSection("Flutter Reels & Paywall VideoPlayer", ArchitectureData.FLUTTER_REELS_CLIENT_CODE)
            "UPI AutoPay Flow" -> UpiAutoPayFlowSection()
            "NestJS Controller" -> CodeBlockSection("NestJS TypeScript Controller & Webhooks", ArchitectureData.NESTJS_AUTOPAY_CONTROLLER)
            "FastAPI Backend" -> CodeBlockSection("FastAPI Python Endpoints", ArchitectureData.FASTAPI_ENDPOINT_CODE)
            "DRM & Security" -> DrmSecuritySection()
        }
    }
}

@Composable
private fun DatabaseSchemaSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SchemaSummaryCard(name = "users", rows = "UUID, phone, email, role, auth", modifier = Modifier.weight(1f))
            SchemaSummaryCard(name = "upi_mandates", rows = "gateway_mandate_id, UMRN, VPA", modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SchemaSummaryCard(name = "subscriptions", rows = "status, period_end, retries", modifier = Modifier.weight(1f))
            SchemaSummaryCard(name = "payments", rows = "amount_inr, txn_id, status", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Code block
        CodeSnippetBox(code = ArchitectureData.POSTGRESQL_SCHEMA_SQL)
    }
}

@Composable
private fun SchemaSummaryCard(name: String, rows: String, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "DB Table",
                    tint = CyberGold,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = rows,
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun UpiAutoPayFlowSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "UPI AutoPay End-to-End Execution Sequence",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        val steps = listOf(
            Triple(
                "Step 1: ₹1 Token Verification",
                "Mobile app initiates subscription setup -> Backend creates Razorpay Mandate -> User approves in GPay/PhonePe with UPI PIN.",
                AutoPayGreen
            ),
            Triple(
                "Step 2: Webhook Authentication",
                "Razorpay triggers 'subscription.authenticated' webhook -> NPCI assigns UMRN (Unique Mandate Reference Number) -> Status set to ACTIVE_TRIAL.",
                CyberGold
            ),
            Triple(
                "Step 3: Auto-Debit on Day 4",
                "Pre-debit notification sent 24h prior (RBI mandate rule) -> Backend triggers automated ₹299 debit -> Razorpay fires 'subscription.charged'.",
                NeonPink
            ),
            Triple(
                "Step 4: Real-Time Paywall Gating",
                "App streams 4K video using secure DRM Signed URLs only if status is ACTIVE. If debit fails, grace period starts.",
                ElectricViolet
            )
        )

        steps.forEach { (title, desc, color) ->
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, color.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                            .align(Alignment.Top)
                    )
                    Column {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = desc,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrmSecuritySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Video Content Protection & Anti-Piracy Architecture",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        Surface(
            color = SurfaceCard,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. AWS S3 + Cloudflare Stream / Mux Signed URLs",
                    color = CyberGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Raw MP4 reels are transcoded into encrypted HLS/DASH chunks. Playback manifests require an HMAC-SHA256 token signed by the VIP subscription service.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "2. Dynamic Forensic Watermarking",
                    color = NeonPink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Every active subscriber session projects a semi-transparent, floating watermark displaying their Phone Number & User ID to trace screen leakage.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "3. Android FLAG_SECURE Integration",
                    color = AutoPayGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = "The window manager enforces WindowManager.LayoutParams.FLAG_SECURE to prevent OS screenshotting, AirPlay casting, and screen recorder apps from capturing VIP content.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun CodeBlockSection(title: String, code: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))
        CodeSnippetBox(code = code)
    }
}

@Composable
private fun CodeSnippetBox(code: String) {
    Surface(
        color = Color(0xFF08060F),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(14.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
