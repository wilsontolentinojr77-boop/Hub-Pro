package com.example.vault.security

import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

object TotpGenerator {

    private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    /**
     * Generate a random 16-character Base32 Secret Key for 2FA TOTP setup.
     */
    fun generateSecretKey(length: Int = 16): String {
        val random = SecureRandom()
        val sb = StringBuilder(length)
        for (i in 0 until length) {
            sb.append(BASE32_CHARS[random.nextInt(BASE32_CHARS.length)])
        }
        return sb.toString()
    }

    /**
     * Generates a 6-digit TOTP code for the given secret key and time window.
     */
    fun generateTotpCode(secretKey: String, timeSeconds: Long = System.currentTimeMillis() / 1000L, timeStepSeconds: Int = 30): String {
        try {
            val decodedKey = decodeBase32(secretKey)
            if (decodedKey.isEmpty()) return "000000"

            val timeIndex = timeSeconds / timeStepSeconds
            val buffer = ByteBuffer.allocate(8).putLong(timeIndex).array()

            val mac = Mac.getInstance("HmacSHA1")
            val keySpec = SecretKeySpec(decodedKey, "HmacSHA1")
            mac.init(keySpec)
            val hmac = mac.doFinal(buffer)

            val offset = (hmac[hmac.size - 1] and 0x0F).toInt()
            val binary = ((hmac[offset].toInt() and 0x7F) shl 24) or
                    ((hmac[offset + 1].toInt() and 0xFF) shl 16) or
                    ((hmac[offset + 2].toInt() and 0xFF) shl 8) or
                    (hmac[offset + 3].toInt() and 0xFF)

            val otp = binary % 1000000
            return String.format("%06d", otp)
        } catch (e: Exception) {
            e.printStackTrace()
            return "000000"
        }
    }

    /**
     * Verify if the code provided by the user matches the current, previous, or next 30-second window.
     */
    fun verifyTotpCode(secretKey: String, userCode: String): Boolean {
        val cleanUserCode = userCode.trim()
        if (cleanUserCode.length != 6) return false

        val nowSeconds = System.currentTimeMillis() / 1000L
        // Allow time drift tolerance (+/- 1 time step)
        val windows = listOf(nowSeconds - 30L, nowSeconds, nowSeconds + 30L)
        return windows.any { time ->
            generateTotpCode(secretKey, time) == cleanUserCode
        }
    }

    /**
     * Returns remaining seconds in the current 30-second TOTP cycle.
     */
    fun getRemainingSeconds(): Int {
        val currentSeconds = (System.currentTimeMillis() / 1000L) % 30L
        return (30 - currentSeconds).toInt()
    }

    /**
     * Formats OTP Auth URI for scanning into Google Authenticator or Authy.
     */
    fun getOtpAuthUri(accountName: String, secretKey: String, issuer: String = "AppVault"): String {
        return "otpauth://totp/$issuer:$accountName?secret=$secretKey&issuer=$issuer&algorithm=SHA1&digits=6&period=30"
    }

    /**
     * Generates a list of 5 single-use 6-digit emergency backup codes.
     */
    fun generateBackupCodes(count: Int = 5): List<String> {
        val random = SecureRandom()
        val codes = mutableListOf<String>()
        for (i in 0 until count) {
            val code = String.format("%06d", random.nextInt(1000000))
            codes.add(code)
        }
        return codes
    }

    private fun decodeBase32(base32: String): ByteArray {
        val cleanKey = base32.uppercase().replace("[^A-Z2-7]".toRegex(), "")
        if (cleanKey.isEmpty()) return ByteArray(0)

        val out = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0

        for (char in cleanKey) {
            val valIndex = BASE32_CHARS.indexOf(char)
            if (valIndex < 0) continue
            buffer = (buffer shl 5) or valIndex
            bitsLeft += 5
            if (bitsLeft >= 8) {
                out.add((buffer shr (bitsLeft - 8)).toByte())
                bitsLeft -= 8
            }
        }
        return out.toByteArray()
    }
}
