package com.autsing.miga.presentation.api

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random.Default.nextInt

fun Map<String, String?>.genCookie(): String {
    return map { "${it.key}=${it.value ?: ""}" }.joinToString("; ")
}

fun genDeviceId(): String {
    val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-"
    val value = buildString(16) {
        repeat(16) {
            append(chars[nextInt(chars.length)])
        }
    }

    return value
}

fun genPassO(): String {
    val value = buildString(16) {
        repeat(16) {
            append("0123456789abcdef".random())
        }
    }

    return value
}

private fun randomHex(length: Int): String {
    val chars = "0123456789ABCDEF"
    val value = buildString(length) {
        repeat(length) {
            append(chars.random())
        }
    }

    return value
}

fun genUserAgent(passO: String): String {
    val uaId1 = randomHex(40)
    val uaId2 = randomHex(32)
    val uaId3 = randomHex(32)
    val uaId4 = randomHex(40)

    val localeCountry = "CN"
    val ua = "Android-15-11.0.701-Xiaomi-23046RP50C-OS2.0.212.0.VMYCNXM-" +
            "$uaId1-$localeCountry-" +
            "$uaId3-$uaId2-SmartHome-MI_APP_STORE-$uaId1|$uaId4|$passO-64"

    return ua
}

fun genNonceV2(): String {
    val randomBytes = ByteArray(8)
    SecureRandom().nextBytes(randomBytes)
    val timeMinutes = (System.currentTimeMillis() / 60000).toInt()
    val buffer = ByteBuffer.allocate(12)
    buffer.put(randomBytes)
    buffer.putInt(timeMinutes)
    val nonce = Base64.getEncoder().encodeToString(buffer.array())

    return nonce
}

fun genSignedNonceV2(secret: String, nonce: String): String {
    val s = Base64.getDecoder().decode(secret)
    val n = Base64.getDecoder().decode(nonce)
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(s)
    digest.update(n)
    val hashBytes = digest.digest()
    val signedNonce = Base64.getEncoder().encodeToString(hashBytes)

    return signedNonce
}

fun genSignatureV2(
    path: String,
    signedNonce: String,
    nonce: String,
    params: Map<String, String>,
): String {
    val exps = mutableListOf<String>()
    exps.add(path)
    exps.add(signedNonce)
    exps.add(nonce)

    val sortedParams = params.toSortedMap()
    for ((key, value) in sortedParams) {
        exps.add("$key=$value")
    }

    val message = exps.joinToString("&")
    val secretKeyBytes = Base64.getDecoder().decode(signedNonce)
    val secretKey = SecretKeySpec(secretKeyBytes, "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secretKey)
    val hashBytes = mac.doFinal(message.toByteArray(Charsets.UTF_8))
    val signature = Base64.getEncoder().encodeToString(hashBytes)

    return signature
}
