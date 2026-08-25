package com.example.data.repository

import com.example.data.local.PaymentTransactionEntity
import com.example.data.local.ReelsDao
import com.example.data.local.SubscriptionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.model.PaymentTransaction
import com.example.data.model.Subscription
import com.example.data.model.SubscriptionPlan
import com.example.data.model.SubscriptionStatus
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SubscriptionRepository(private val dao: ReelsDao) {

    val availablePlans = listOf(
        SubscriptionPlan(
            id = "plan_monthly_vip_299",
            name = "VIP Monthly AutoPay",
            description = "Uninterrupted access to all exclusive 4K reels & masterclasses",
            initialVerificationFeeInr = 1,
            recurringPriceInr = 299,
            billingInterval = "monthly",
            trialDays = 3,
            badge = "MOST POPULAR",
            features = listOf(
                "3-Day ₹1 Mandate Verification Trial",
                "₹299/month automated debit thereafter",
                "Unlimited 4K VIP Reels & Audio Stems",
                "Offline Caching & DRM Screen Protected",
                "Direct Creator AMA Q&A Access",
                "Pause, Update UPI VPA, or Cancel Anytime"
            )
        ),
        SubscriptionPlan(
            id = "plan_annual_vip_2499",
            name = "VIP Super Annual Pass",
            description = "Best value for power viewers & deep-dive learners (Save 30%)",
            initialVerificationFeeInr = 1,
            recurringPriceInr = 2499,
            billingInterval = "annual",
            trialDays = 7,
            badge = "SAVE 30%",
            features = listOf(
                "7-Day ₹1 Mandate Verification Trial",
                "₹2,499/year automated recurring charge",
                "VIP Discord Community & Backstage Passes",
                "Priority 60fps / HDR Streaming Pipeline",
                "Early access to upcoming Creator Series",
                "Cancel or switch anytime via 1-tap in app"
            )
        )
    )

    val currentSubscription: Flow<Subscription?> = dao.getActiveSubscription().map { entity ->
        entity?.toDomainModel()
    }

    val userProfile: Flow<User> = dao.getUserProfile().map { entity ->
        entity?.toDomainModel() ?: User(
            id = "usr_guest_101",
            name = "Alex Rivera",
            phone = "+91 98765 43210",
            email = "alex.rivera@reelsvip.in",
            isVipMember = true,
            role = "VIP_SUBSCRIBER"
        )
    }

    val paymentHistory: Flow<List<PaymentTransaction>> = dao.getAllPayments().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun seedInitialSubscriptionData() {
        val initialUser = UserProfileEntity(
            id = "usr_99812",
            name = "Alex Rivera",
            phone = "+91 98765 43210",
            email = "alex.rivera@reelsvip.in",
            authProvider = "UPI_MOBILE_OTP",
            isVipMember = true,
            role = "VIP_SUBSCRIBER"
        )
        dao.saveUserProfile(initialUser)

        val initialSub = SubscriptionEntity(
            id = "sub_live_90841",
            userId = "usr_99812",
            planId = "plan_monthly_vip_299",
            planName = "VIP Monthly AutoPay",
            priceInr = 299,
            status = SubscriptionStatus.ACTIVE.name,
            mandateId = "man_razor_881023",
            umrn = "HDFC0001099238472910",
            upiVpa = "alex@okhdfcbank",
            upiApp = "Google Pay",
            startDate = "15 Aug 2026",
            nextBillingDate = "15 Sep 2026",
            totalBilledInr = 300,
            autoDebitDayOfMonth = 15
        )
        dao.saveSubscription(initialSub)

        val initialPayments = listOf(
            PaymentTransactionEntity(
                id = "txn_debit_991823",
                subscriptionId = "sub_live_90841",
                amountInr = 299,
                type = "RECURRING_AUTOPAY_DEBIT",
                status = "SUCCESS",
                gatewayTxnId = "pay_rzp_998120394",
                paymentMethod = "UPI AutoPay (alex@okhdfcbank)",
                formattedDate = "15 Aug 2026, 06:00 AM",
                failureReason = null
            ),
            PaymentTransactionEntity(
                id = "txn_auth_109283",
                subscriptionId = "sub_live_90841",
                amountInr = 1,
                type = "MANDATE_SETUP_TRIAL",
                status = "SUCCESS",
                gatewayTxnId = "pay_rzp_118230192",
                paymentMethod = "UPI AutoPay Mandate Auth (Google Pay)",
                formattedDate = "12 Aug 2026, 09:30 PM",
                failureReason = null
            )
        )
        dao.insertPayments(initialPayments)
    }

    suspend fun setupUpiAutoPayMandate(
        plan: SubscriptionPlan,
        upiVpa: String,
        upiApp: String
    ): Subscription {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val cal = Calendar.getInstance()
        val startDateStr = dateFormat.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, plan.trialDays)
        val nextBillingStr = dateFormat.format(cal.time)

        val mandateId = "man_rzp_${System.currentTimeMillis().toString().takeLast(6)}"
        val randomUmrnDigits = (10000000..99999999).random()
        val umrn = "NPCI${randomUmrnDigits}UPI"

        val newSub = SubscriptionEntity(
            id = "sub_${System.currentTimeMillis()}",
            userId = "usr_99812",
            planId = plan.id,
            planName = plan.name,
            priceInr = plan.recurringPriceInr,
            status = SubscriptionStatus.ACTIVE.name,
            mandateId = mandateId,
            umrn = umrn,
            upiVpa = upiVpa.ifBlank { "user@upi" },
            upiApp = upiApp,
            startDate = startDateStr,
            nextBillingDate = nextBillingStr,
            totalBilledInr = plan.initialVerificationFeeInr,
            autoDebitDayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        dao.saveSubscription(newSub)

        // Record verification ₹1 payment transaction
        val authTxn = PaymentTransactionEntity(
            id = "txn_auth_${System.currentTimeMillis()}",
            subscriptionId = newSub.id,
            amountInr = plan.initialVerificationFeeInr,
            type = "MANDATE_SETUP_TRIAL",
            status = "SUCCESS",
            gatewayTxnId = "pay_rzp_${(10000000..99999999).random()}",
            paymentMethod = "UPI AutoPay Mandate Auth ($upiApp - $upiVpa)",
            formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()),
            failureReason = null
        )
        dao.insertPayment(authTxn)

        // Update user VIP flag
        dao.saveUserProfile(
            UserProfileEntity(
                id = "usr_99812",
                name = "Alex Rivera",
                phone = "+91 98765 43210",
                email = "alex.rivera@reelsvip.in",
                authProvider = "UPI_AUTOPAY",
                isVipMember = true,
                role = "VIP_SUBSCRIBER"
            )
        )

        return newSub.toDomainModel()
    }

    suspend fun cancelSubscription(subId: String) {
        dao.updateSubscriptionStatus(subId, SubscriptionStatus.CANCELLED.name)
    }

    suspend fun reactivateSubscription(subId: String) {
        dao.updateSubscriptionStatus(subId, SubscriptionStatus.ACTIVE.name)
    }

    suspend fun triggerSimulatedAutoDebit(subId: String, amount: Int, forceFailure: Boolean = false) {
        val nowFormatted = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        if (forceFailure) {
            val failedTxn = PaymentTransactionEntity(
                id = "txn_fail_${System.currentTimeMillis()}",
                subscriptionId = subId,
                amountInr = amount,
                type = "RECURRING_AUTOPAY_DEBIT",
                status = "FAILED",
                gatewayTxnId = "pay_rzp_err_${(100000..999999).random()}",
                paymentMethod = "UPI AutoPay e-Mandate",
                formattedDate = nowFormatted,
                failureReason = "NPCI U16: Insufficient balance in customer account for automated debit"
            )
            dao.insertPayment(failedTxn)
            dao.updateSubscriptionStatus(subId, SubscriptionStatus.PAST_DUE.name)
        } else {
            val successTxn = PaymentTransactionEntity(
                id = "txn_auto_${System.currentTimeMillis()}",
                subscriptionId = subId,
                amountInr = amount,
                type = "RECURRING_AUTOPAY_DEBIT",
                status = "SUCCESS",
                gatewayTxnId = "pay_rzp_${(10000000..99999999).random()}",
                paymentMethod = "UPI AutoPay Recurring e-Mandate",
                formattedDate = nowFormatted,
                failureReason = null
            )
            dao.insertPayment(successTxn)
            dao.updateSubscriptionStatus(subId, SubscriptionStatus.ACTIVE.name)
        }
    }

    suspend fun processWebhookEvent(eventType: String, subId: String): String {
        return when (eventType) {
            "subscription.charged" -> {
                triggerSimulatedAutoDebit(subId, 299, forceFailure = false)
                "Status: HTTP 200 OK. Processed recurring charge ₹299. Subscription extended to next billing cycle."
            }
            "payment.failed" -> {
                triggerSimulatedAutoDebit(subId, 299, forceFailure = true)
                "Status: HTTP 200 OK. Auto-debit failed. Subscription set to PAST_DUE. Grace period activated."
            }
            "subscription.cancelled" -> {
                cancelSubscription(subId)
                "Status: HTTP 200 OK. Mandate revoked via NPCI UPI AutoPay. Subscription status set to CANCELLED."
            }
            "subscription.activated" -> {
                reactivateSubscription(subId)
                "Status: HTTP 200 OK. Mandate verified and subscription set to ACTIVE."
            }
            else -> "Status: HTTP 200 OK. Event acknowledged."
        }
    }

    suspend fun updateUserProfile(name: String, phone: String, email: String) {
        val user = UserProfileEntity(
            id = "usr_99812",
            name = name,
            phone = phone,
            email = email,
            authProvider = "MOBILE_OTP",
            isVipMember = true,
            role = "VIP_SUBSCRIBER"
        )
        dao.saveUserProfile(user)
    }
}

private fun SubscriptionEntity.toDomainModel(): Subscription {
    return Subscription(
        id = id,
        userId = userId,
        planId = planId,
        planName = planName,
        priceInr = priceInr,
        initialMandateFeeInr = 1,
        status = runCatching { SubscriptionStatus.valueOf(status) }.getOrDefault(SubscriptionStatus.ACTIVE),
        mandateId = mandateId,
        umrn = umrn,
        upiVpa = upiVpa,
        upiApp = upiApp,
        startDate = startDate,
        nextBillingDate = nextBillingDate,
        totalBilledInr = totalBilledInr,
        autoDebitDayOfMonth = autoDebitDayOfMonth
    )
}

private fun PaymentTransactionEntity.toDomainModel(): PaymentTransaction {
    return PaymentTransaction(
        id = id,
        subscriptionId = subscriptionId,
        amountInr = amountInr,
        type = type,
        status = status,
        gatewayTxnId = gatewayTxnId,
        paymentMethod = paymentMethod,
        formattedDate = formattedDate,
        failureReason = failureReason
    )
}

private fun UserProfileEntity.toDomainModel(): User {
    return User(
        id = id,
        name = name,
        phone = phone,
        email = email,
        isVipMember = isVipMember,
        role = role
    )
}
