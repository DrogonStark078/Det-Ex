package com.detex.app.service

import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

data class OtpSession(
    val otp: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val attempts: Int = 0,
    val ttlMillis: Long = 5 * 60 * 1000
) {
    fun isExpired(): Boolean = (System.currentTimeMillis() - generatedAt) > ttlMillis
}

sealed class VerificationResult {
    data object Success : VerificationResult()
    data object Expired : VerificationResult()
    data object NotFound : VerificationResult()
    data object MaxAttemptsExceeded : VerificationResult()
    data class Incorrect(val remaining: Int) : VerificationResult()
}

object OtpAuthManager {
    private val sessions = ConcurrentHashMap<String, OtpSession>()
    private val secureRandom = SecureRandom()

    fun generateNewOtp(fullPhoneNumber: String): String {
        val code = 100000 + secureRandom.nextInt(900000)
        val otpString = code.toString()
        sessions[fullPhoneNumber] = OtpSession(otp = otpString)
        return otpString
    }

    fun verifyOtp(fullPhoneNumber: String, enteredOtp: String): VerificationResult {
        val session = sessions[fullPhoneNumber] ?: return VerificationResult.NotFound

        if (session.isExpired()) {
            sessions.remove(fullPhoneNumber)
            return VerificationResult.Expired
        }

        return if (session.otp == enteredOtp.trim()) {
            sessions.remove(fullPhoneNumber)
            VerificationResult.Success
        } else {
            val updated = session.copy(attempts = session.attempts + 1)
            if (updated.attempts >= 3) {
                sessions.remove(fullPhoneNumber)
                VerificationResult.MaxAttemptsExceeded
            } else {
                sessions[fullPhoneNumber] = updated
                VerificationResult.Incorrect(remaining = 3 - updated.attempts)
            }
        }
    }
}
