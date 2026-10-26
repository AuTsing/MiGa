package com.autsing.miga.presentation.api

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random.Default.nextInt

fun genDeviceId(): String {
    val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-"
    val value = buildString(16) {
        repeat(16) {
            append(chars[nextInt(chars.length)])
        }
    }

    return value
}

fun genNonce(length: Int = 16): String {
    val chars = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val value = (1..length)
        .map { chars.random() }
        .joinToString("")

    return value
}

fun genSignedNonce(secret: String, nonce: String): String {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest.update(Base64.getDecoder().decode(secret))
    messageDigest.update(Base64.getDecoder().decode(nonce))
    val value = Base64.getEncoder().encodeToString(messageDigest.digest())

    return value
}

fun genSignature(
    uri: String,
    signedNonce: String,
    nonce: String,
    data: String,
): String {
    val hmac = Mac.getInstance("HmacSHA256")
    val sign = "$uri&$signedNonce&$nonce&data=$data"
    val key = SecretKeySpec(Base64.getDecoder().decode(signedNonce), "HmacSHA256")
    hmac.init(key)
    val value = Base64.getEncoder().encodeToString(hmac.doFinal(sign.toByteArray(Charsets.UTF_8)))

    return value
}
