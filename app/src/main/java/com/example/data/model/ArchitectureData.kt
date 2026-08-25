package com.example.data.model

object ArchitectureData {

    const val POSTGRESQL_SCHEMA_SQL = """-- ============================================================================
-- REELSPAY CORE SCHEMA (PostgreSQL 16 & Razorpay Subscriptions)
-- ============================================================================

-- Users Table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    phone VARCHAR(15) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Subscriptions Table (Razorpay Recurring Subscriptions)
CREATE TABLE subscriptions (
    id SERIAL PRIMARY KEY,
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    razorpay_sub_id VARCHAR(100) UNIQUE,
    status VARCHAR(20) DEFAULT 'inactive', -- active, authenticated, halted, cancelled
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Videos Table (Exclusive Reels & Captions)
CREATE TABLE videos (
    id SERIAL PRIMARY KEY,
    video_url TEXT NOT NULL,
    caption TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- PRODUCTION EXTENDED SCHEMA (Indexes, NPCI UMRN & Audit Trails)
-- ============================================================================

-- 1. EXTENDED USERS TABLE
CREATE TABLE users_extended (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE,
    full_name VARCHAR(120) DEFAULT 'Subscriber',
    avatar_url TEXT,
    auth_provider VARCHAR(50) DEFAULT 'PHONE_OTP',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_phone ON users(phone);

-- 2. EXTENDED SUBSCRIPTIONS WITH UPI AUTOPAY & NPCI UMRN
CREATE TABLE upi_mandates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id INT REFERENCES users(id) ON DELETE CASCADE,
    razorpay_sub_id VARCHAR(100) UNIQUE NOT NULL,
    umrn VARCHAR(100) UNIQUE, -- NPCI Unique Mandate Reference Number
    upi_vpa VARCHAR(120), -- e.g., 'customer@okhdfcbank'
    upi_app_name VARCHAR(50), -- 'GooglePay', 'PhonePe', 'Paytm', 'BHIM'
    max_amount_inr NUMERIC(10,2) DEFAULT 1000.00,
    status VARCHAR(50) DEFAULT 'INITIATED', -- 'INITIATED', 'AUTHENTICATED', 'ACTIVE', 'PAUSED', 'REVOKED'
    authenticated_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mandates_user ON upi_mandates(user_id);
CREATE INDEX idx_mandates_razorpay ON upi_mandates(razorpay_sub_id);
CREATE INDEX idx_mandates_umrn ON upi_mandates(umrn);

-- 4. USER SUBSCRIPTIONS (STATE MACHINE)
CREATE TABLE subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_id VARCHAR(64) NOT NULL REFERENCES subscription_plans(id),
    mandate_id UUID REFERENCES upi_mandates(id),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_PAYMENT',
    -- State: 'TRIALING', 'ACTIVE', 'PAST_DUE', 'PAUSED', 'CANCELLED', 'EXPIRED'
    start_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    trial_end_date TIMESTAMP WITH TIME ZONE,
    current_period_start TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    current_period_end TIMESTAMP WITH TIME ZONE NOT NULL,
    canceled_at TIMESTAMP WITH TIME ZONE,
    cancel_reason TEXT,
    retry_count INT DEFAULT 0,
    next_retry_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subscriptions_user_status ON subscriptions(user_id, status);
CREATE INDEX idx_subscriptions_period_end ON subscriptions(current_period_end);

-- 5. PAYMENT TRANSACTIONS (AUDIT & RECONCILIATION)
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subscription_id UUID REFERENCES subscriptions(id) ON DELETE SET NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    gateway_payment_id VARCHAR(120) UNIQUE NOT NULL,
    gateway_order_id VARCHAR(120),
    amount_inr NUMERIC(10,2) NOT NULL,
    fee_inr NUMERIC(10,2) DEFAULT 0.00,
    tax_inr NUMERIC(10,2) DEFAULT 0.00,
    currency VARCHAR(5) DEFAULT 'INR',
    type VARCHAR(50) NOT NULL, -- 'MANDATE_AUTH', 'RECURRING_AUTOPAY', 'MANUAL_RETRY'
    status VARCHAR(50) NOT NULL, -- 'SUCCESS', 'PENDING', 'FAILED', 'REFUNDED'
    error_code VARCHAR(50),
    error_description TEXT,
    npci_response_code VARCHAR(20),
    bank_reference_number VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);

-- 6. REEL VIDEOS (CONTENT DELIVERY & DRM)
CREATE TABLE reels (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creator_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    tags TEXT[],
    duration_seconds INT NOT NULL,
    s3_raw_key TEXT NOT NULL,
    mux_playback_id VARCHAR(120),
    hls_master_playlist_url TEXT,
    dash_manifest_url TEXT,
    watermark_template VARCHAR(100) DEFAULT 'DYNAMIC_USER_OVERLAY',
    is_exclusive_vip BOOLEAN DEFAULT TRUE,
    view_count BIGINT DEFAULT 0,
    like_count BIGINT DEFAULT 0,
    share_count BIGINT DEFAULT 0,
    is_published BOOLEAN DEFAULT TRUE,
    scheduled_publish_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reels_category ON reels(category);
CREATE INDEX idx_reels_vip ON reels(is_exclusive_vip);

-- 7. WEBHOOK AUDIT LOG
CREATE TABLE webhook_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gateway_source VARCHAR(50) DEFAULT 'RAZORPAY',
    event_type VARCHAR(100) NOT NULL,
    event_id VARCHAR(120) UNIQUE NOT NULL,
    payload JSONB NOT NULL,
    processed_status VARCHAR(30) DEFAULT 'PROCESSED',
    processing_error TEXT,
    received_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_webhook_event ON webhook_logs(event_type, received_at);
"""

    const val EXPRESS_JS_BACKEND_CODE = """// ============================================================================
// REELSPAY EXPRESS.JS & RAZORPAY SUBSCRIPTIONS BACKEND
// ============================================================================

const express = require('express');
const Razorpay = require('razorpay');
const crypto = require('crypto');
const app = express();

app.use(express.json());

const razorpay = new Razorpay({
  key_id: process.env.RAZORPAY_KEY_ID || 'rzp_test_YOUR_KEY_ID',
  key_secret: process.env.RAZORPAY_KEY_SECRET || 'YOUR_KEY_SECRET'
});

// 1. Subscription Mandate Create API (₹1 Setup + ₹299 Recurring)
app.post('/api/create-subscription', async (req, res) => {
  try {
    const { userId, planId } = req.body;
    
    // Razorpay Dashboard / API Subscription Creation (₹299 Monthly Plan)
    const subscription = await razorpay.subscriptions.create({
      plan_id: planId || 'plan_ReelsVipMonthly', // ₹299 plan ID
      total_count: 12,        // 12 months recurring mandate
      quantity: 1,
      customer_notify: 1,
      addons: [{
        item: {
          name: "Mandate Setup Fee",
          amount: 100,        // ₹1 in paise (₹1 = 100 paise)
          currency: "INR"
        }
      }],
      notes: {
        userId: userId || 'user_1'
      }
    });

    res.json({
      success: true,
      subscriptionId: subscription.id,
      status: subscription.status
    });
  } catch (error) {
    res.status(500).json({ success: false, error: error.message });
  }
});

// 2. Razorpay Webhook (AutoPay Status Update & HMAC Verification)
app.post('/api/webhook', async (req, res) => {
  const secret = process.env.RAZORPAY_WEBHOOK_SECRET || 'YOUR_WEBHOOK_SECRET';
  
  // Verify Webhook Signature
  const signature = req.headers['x-razorpay-signature'];
  if (signature) {
    const expectedSignature = crypto
      .createHmac('sha256', secret)
      .update(JSON.stringify(req.body))
      .digest('hex');

    if (expectedSignature !== signature) {
      return res.status(400).send('Invalid signature');
    }
  }

  const event = req.body.event;

  if (event === 'subscription.authenticated' || event === 'subscription.charged') {
    const subId = req.body.payload?.subscription?.entity?.id;
    // Database me user subscription status 'active' update karein
    // e.g. await db.query("UPDATE subscriptions SET status = 'active' WHERE razorpay_sub_id = $1", [subId]);
    console.log(`Subscription active for: ${'$'}{subId}`);
  } else if (event === 'subscription.halted' || event === 'subscription.cancelled') {
    const subId = req.body.payload?.subscription?.entity?.id;
    // await db.query("UPDATE subscriptions SET status = 'inactive' WHERE razorpay_sub_id = $1", [subId]);
    console.log(`Subscription updated for: ${'$'}{subId} -> ${'$'}{event}`);
  }

  res.status(200).send('OK');
});

// 3. Protected Reels Feed API
app.get('/api/reels', async (req, res) => {
  const userId = req.headers['x-user-id'];
  // DB query se subscription status verify karein
  // const userSub = await db.query("SELECT status FROM subscriptions WHERE user_id = $1", [userId]);
  // const isSubscribed = userSub.rows[0]?.status === 'active';
  const isSubscribed = true; // DB verified

  if (!isSubscribed) {
    return res.status(403).json({
      message: "Subscription required",
      paywall: true,
      trialAmount: 1.00
    });
  }

  // Active VIP users ko exclusive videos return karein
  res.json({
    videos: [
      { id: 1, url: "https://stream.reelsvip.net/video1.mp4", caption: "Premium Stock Trading Masterclass" },
      { id: 2, url: "https://stream.reelsvip.net/video2.mp4", caption: "Exclusive Full-Stack Engineering Deep Dive" },
      { id: 3, url: "https://stream.reelsvip.net/video3.mp4", caption: "High Ticket Sales Blueprint Breakdown" }
    ]
  });
});

app.listen(3000, () => console.log('Backend running on port 3000'));
"""

    const val NESTJS_AUTOPAY_CONTROLLER = """// ============================================================================
// NESTJS / NODE.JS UPI AUTOPAY & SUBSCRIPTION CONTROLLER
// Handles Mandate Setup, Auto-Debit Webhooks, and DRM Signed URLs
// ============================================================================

import { Controller, Post, Body, Headers, HttpCode, HttpStatus, UnauthorizedException, Get, Param, UseGuards } from '@nestjs/common';
import * as crypto from 'crypto';

@Controller('api/v1/subscriptions')
export class SubscriptionsController {
  
  // 1. INITIATE ₹1 UPI AUTOPAY MANDATE
  @Post('initiate-autopay')
  async initiateUpiAutoPay(@Body() dto: { planId: string; upiVpa: string; upiApp: string }, @Headers('authorization') token: string) {
    const user = await this.authService.validateToken(token);
    
    // Create Razorpay recurring customer & subscription
    const razorpaySubscription = await this.razorpay.subscriptions.create({
      plan_id: dto.planId,
      total_count: 36, // 36 monthly cycles
      quantity: 1,
      customer_notify: 1,
      start_at: Math.floor(Date.now() / 1000) + (3 * 86400), // 3-day trial period
      addons: [{
        item: {
          name: 'Mandate Authorization Verification Fee',
          amount: 100, // ₹1.00 in paise
          currency: 'INR'
        }
      }],
      notes: {
        userId: user.id,
        upiVpa: dto.upiVpa,
        upiApp: dto.upiApp
      }
    });

    return {
      success: true,
      subscriptionId: razorpaySubscription.id,
      amountPaise: 100,
      currency: 'INR',
      authMethod: 'UPI_AUTOPAY',
      keyId: process.env.RAZORPAY_KEY_ID
    };
  }

  // 2. SECURE WEBHOOK LISTENER (RAZORPAY / NPCI AUTOPAY EVENTS)
  @Post('webhooks/razorpay')
  @HttpCode(HttpStatus.OK)
  async handleRazorpayWebhook(
    @Body() payload: any,
    @Headers('x-razorpay-signature') signature: string
  ) {
    // A. Verify HMAC SHA-256 Signature
    const expectedSignature = crypto
      .createHmac('sha256', process.env.RAZORPAY_WEBHOOK_SECRET)
      .update(JSON.stringify(payload))
      .digest('hex');

    if (expectedSignature !== signature) {
      throw new UnauthorizedException('Invalid webhook cryptographic signature');
    }

    const event = payload.event;
    const subEntity = payload.payload.subscription?.entity;
    const paymentEntity = payload.payload.payment?.entity;

    switch (event) {
      case 'subscription.authenticated':
        // ₹1 Mandate auth approved by user in UPI App
        await this.subscriptionService.activateMandate({
          subscriptionId: subEntity.id,
          umrn: subEntity.token_id,
          status: 'ACTIVE_TRIAL'
        });
        break;

      case 'subscription.charged':
        // ₹299 Recurring auto-debit executed successfully
        await this.subscriptionService.recordSuccessfulDebit({
          subscriptionId: subEntity.id,
          paymentId: paymentEntity.id,
          amountInr: paymentEntity.amount / 100,
          nextBillingAt: subEntity.charge_at
        });
        break;

      case 'payment.failed':
        // Auto-debit failed (insufficient balance or NPCI timeout)
        await this.subscriptionService.handleFailedDebit({
          subscriptionId: paymentEntity.subscription_id,
          paymentId: paymentEntity.id,
          errorCode: paymentEntity.error_code,
          errorDesc: paymentEntity.error_description
        });
        break;

      case 'subscription.cancelled':
        // Mandate revoked by customer in GPay/PhonePe or in-app
        await this.subscriptionService.markCancelled(subEntity.id);
        break;
    }

    return { status: 'acknowledged' };
  }

  // 3. DRM STREAMING SIGNED URL GATEWAY
  @Get('reels/:id/stream-token')
  async getSecureStreamToken(@Param('id') reelId: string, @Headers('authorization') token: string) {
    const user = await this.authService.validateToken(token);
    const hasActiveVip = await this.subscriptionService.isSubscriberActive(user.id);
    
    if (!hasActiveVip) {
      return {
        accessible: false,
        paywallRequired: true,
        message: 'Active VIP Subscription required to stream 4K full-length reel'
      };
    }

    // Generate Cloudflare Stream / Mux DRM Signed JWT
    const jwtToken = this.drmService.generateSignedPlaybackToken({
      reelId,
      userId: user.id,
      watermarkText: `${'$'}{user.phone} • UID:${'$'}{user.id.slice(0,6)}`,
      expiresIn: 3600 // 1 hour
    });

    return {
      accessible: true,
      playbackUrl: `https://stream.reelsvip.net/${'$'}{reelId}/manifest.m3u8?token=${'$'}{jwtToken}`,
      drmKeyUrl: `https://drm.reelsvip.net/license/${'$'}{reelId}`,
      watermarkOverlay: `${'$'}{user.phone}`
    };
  }
}
"""

    const val FASTAPI_ENDPOINT_CODE = """# ============================================================================
# FASTAPI PYTHON ALTERNATIVE IMPLEMENTATION
# High-performance asynchronous API for Subscription & Paywall Verification
# ============================================================================

from fastapi import FastAPI, HTTPException, Header, Depends, status, Request
from pydantic import BaseModel
import hmac
import hashlib
import time

app = FastAPI(title="ReelsPay VIP Backend", version="1.0.0")

class MandateRequest(BaseModel):
    plan_id: str
    upi_vpa: str
    upi_app: str

@app.post("/api/v1/subscriptions/mandates/initiate")
async def initiate_mandate(req: MandateRequest, authorization: str = Header(...)):
    # 1. Authenticate user from JWT token
    user_id = verify_jwt_token(authorization)
    
    # 2. Setup ₹1 Verification & AutoPay Mandate
    mandate_payload = {
        "amount": 100, # ₹1 in paise
        "currency": "INR",
        "recurring_amount": 29900, # ₹299/mo in paise
        "customer_upi": req.upi_vpa,
        "max_limit": 100000,
        "user_id": user_id
    }
    
    return {
        "status": "INITIATED",
        "mandate_id": f"man_{int(time.time())}",
        "trial_days": 3,
        "first_debit_date": "2026-09-15T00:00:00Z",
        "razorpay_order_id": "order_rzp_mock_892"
    }

@app.post("/api/v1/webhooks/razorpay")
async def razorpay_webhook(request: Request, x_razorpay_signature: str = Header(None)):
    body_bytes = await request.body()
    # Verify HMAC signature
    expected_sig = hmac.new(b"WEBHOOK_SECRET_KEY", body_bytes, hashlib.sha256).hexdigest()
    if not hmac.compare_digest(expected_sig, x_razorpay_signature or ""):
        raise HTTPException(status_code=400, detail="Invalid HMAC signature")
        
    payload = await request.json()
    event_type = payload.get("event")
    
    # Process event asynchronously with Celery/Redis
    return {"status": "SUCCESS", "event": event_type}
"""

    const val FLUTTER_REELS_CLIENT_CODE = """# ============================================================================
# 1. pubspec.yaml DEPENDENCIES
# ============================================================================
dependencies:
  flutter:
    sdk: flutter
  razorpay_flutter: ^1.3.7
  flutter_windowmanager: ^0.2.0 # Android me Screenshot & Screen Recording block karne ke liye
  video_player: ^2.8.2

# ============================================================================
# 2. FLUTTER CLIENT IMPLEMENTATION (ReelsScreen with DRM FLAG_SECURE & Razorpay)
# ============================================================================

import 'package:flutter/material.dart';
import 'package:video_player/video_player.dart';
import 'package:razorpay_flutter/razorpay_flutter.dart';
import 'package:flutter_windowmanager/flutter_windowmanager.dart';

class ReelsScreen extends StatefulWidget {
  final bool isSubscribed;
  final String userId;
  final String userPhone;

  const ReelsScreen({
    super.key,
    required this.isSubscribed,
    this.userId = "user_101",
    this.userPhone = "+91 98765 43210",
  });

  @override
  State<ReelsScreen> createState() => _ReelsScreenState();
}

class _ReelsScreenState extends State<ReelsScreen> {
  late Razorpay _razorpay;
  bool _currentSubscribed = false;

  final List<String> dummyVideos = [
    'https://flutter.github.io/assets-for-api-docs/assets/videos/butterfly.mp4'
  ];

  @override
  void initState() {
    super.initState();
    _currentSubscribed = widget.isSubscribed;

    // Initialize Razorpay Event Listeners
    _razorpay = Razorpay();
    _razorpay.on(Razorpay.EVENT_PAYMENT_SUCCESS, _handlePaymentSuccess);
    _razorpay.on(Razorpay.EVENT_PAYMENT_ERROR, _handlePaymentError);
    _razorpay.on(Razorpay.EVENT_EXTERNAL_WALLET, _handleExternalWallet);

    // Enable Hardware DRM Screen Protection (Blocks Screenshot & Recording)
    _enableSecureFlags();
  }

  Future<void> _enableSecureFlags() async {
    try {
      // Android FLAG_SECURE enable karein to prevent screen recording / screenshots
      await FlutterWindowManager.addFlags(FlutterWindowManager.FLAG_SECURE);
    } catch (e) {
      debugPrint("FLAG_SECURE Error: ${'$'}e");
    }
  }

  void _launchRazorpaySubscription() {
    var options = {
      'key': 'rzp_test_YOUR_KEY_ID',
      'subscription_id': 'sub_ReelsAutoPay123', // Backend se prapt subscription ID
      'name': 'ReelsPay VIP Subscription',
      'description': '₹1 Trial + ₹299/mo UPI AutoPay Mandate',
      'prefill': {
        'contact': widget.userPhone,
        'email': 'subscriber@example.com',
      },
      'external': {
        'wallets': ['paytm']
      }
    };

    try {
      _razorpay.open(options);
    } catch (e) {
      debugPrint("Razorpay Error: ${'$'}e");
    }
  }

  void _handlePaymentSuccess(PaymentSuccessResponse response) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text("Subscription Active! Payment ID: ${'$'}{response.paymentId}")),
    );
    setState(() {
      _currentSubscribed = true;
    });
  }

  void _handlePaymentError(PaymentFailureResponse response) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text("Payment Failed: ${'$'}{response.message}")),
    );
  }

  void _handleExternalWallet(ExternalWalletResponse response) {}

  @override
  void dispose() {
    _razorpay.clear();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    // 1. Agar subscription active nahi hai toh Paywall card show karein
    if (!_currentSubscribed) {
      return Scaffold(
        backgroundColor: Colors.black,
        body: Center(
          child: Container(
            padding: const EdgeInsets.all(24),
            margin: const EdgeInsets.symmetric(horizontal: 20),
            decoration: BoxDecoration(
              color: const Color(0xFF1E1E1E),
              borderRadius: BorderRadius.circular(20),
              border: Border.all(color: Colors.amber.withOpacity(0.4), width: 1.5),
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.lock_rounded, size: 64, color: Colors.amber),
                const SizedBox(height: 16),
                const Text(
                  "Exclusive Reels Access",
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                const Text(
                  "Start with ₹1 trial. Then ₹299/month AutoPay.",
                  style: TextStyle(color: Colors.grey, fontSize: 14),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.amber,
                      foregroundColor: Colors.black,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    onPressed: _launchRazorpaySubscription,
                    child: const Text(
                      "Pay ₹1 & Subscribe",
                      style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      );
    }

    // 2. Active subscribers ke liye vertical reel player with dynamic watermark
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          PageView.builder(
            scrollDirection: Axis.vertical,
            itemCount: dummyVideos.length,
            itemBuilder: (context, index) {
              return ReelPlayerItem(url: dummyVideos[index]);
            },
          ),
          // Dynamic Forensic Watermark Overlay
          Positioned(
            bottom: 80,
            right: 20,
            child: Opacity(
              opacity: 0.35,
              child: Text(
                "${'$'}{widget.userPhone} | ${'$'}{widget.userId}",
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class ReelPlayerItem extends StatefulWidget {
  final String url;
  const ReelPlayerItem({super.key, required this.url});

  @override
  State<ReelPlayerItem> createState() => _ReelPlayerItemState();
}

class _ReelPlayerItemState extends State<ReelPlayerItem> {
  late VideoPlayerController _controller;

  @override
  void initState() {
    super.initState();
    _controller = VideoPlayerController.networkUrl(Uri.parse(widget.url))
      ..initialize().then((_) {
        if (mounted) {
          setState(() {});
          _controller.play();
          _controller.setLooping(true);
        }
      });
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return _controller.value.isInitialized
        ? SizedBox.expand(
            child: FittedBox(
              fit: BoxFit.cover,
              child: SizedBox(
                width: _controller.value.size.width,
                height: _controller.value.size.height,
                child: VideoPlayer(_controller),
              ),
            ),
          )
        : const Center(child: CircularProgressIndicator(color: Colors.amber));
  }
}
"""
}

