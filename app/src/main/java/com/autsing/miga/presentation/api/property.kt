package com.autsing.miga.presentation.api

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
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

fun genNonceV2(): String {
    // 1. 生成 8 个安全随机字节 (对应 JS 的 crypto.randomBytes(8))
    val randomBytes = ByteArray(8)
    SecureRandom().nextBytes(randomBytes)

    // 2. 计算当前分钟数 (对应 parseInt(Date.now() / 60000, 10))
    val timeMinutes = (System.currentTimeMillis() / 60000).toInt()

    // 3. 分配 12 字节的缓冲区 (对应 Buffer.allocUnsafe(12))
    // ByteBuffer 默认就是大端序 (Big-Endian)
    val buffer = ByteBuffer.allocate(12)
    buffer.put(randomBytes)       // 写入前 8 字节
    buffer.putInt(timeMinutes)    // 以大端序写入后 4 字节 (对应 writeInt32BE)

    // 4. 转为 Base64 字符串 (对应 buf.toString("base64"))
    return Base64.getEncoder().encodeToString(buffer.array())
}

fun genSignedNonceV2(secret: String, nonce: String): String {
    // 1. 将 Base64 字符串解码为原始字节数组 (对应 Buffer.from(..., "base64"))
    val s = Base64.getDecoder().decode(secret)
    val n = Base64.getDecoder().decode(nonce)

    // 2. 初始化 SHA-256 MessageDigest
    val digest = MessageDigest.getInstance("SHA-256")

    // 3. 依次更新数据 (对应 crypto.createHash("sha256").update(s).update(n))
    digest.update(s)
    digest.update(n)

    // 4. 完成哈希计算并获取字节数组 (对应 digest())
    val hashBytes = digest.digest()

    // 5. 将结果转为 Base64 字符串 (对应 digest("base64"))
    return Base64.getEncoder().encodeToString(hashBytes)
}

fun genSignatureV2(
    path: String,
    signedNonce: String,
    nonce: String,
    params: Map<String, Any>,
): String {
    val exps = mutableListOf<String>()
    exps.add(path)
    exps.add(signedNonce)
    exps.add(nonce)

    // 1. 将参数按 Key 进行字母表升序排序并格式化为 key=value
    // 使用 TreeMap 可以自动根据 Key 进行自然升序排序
    val sortedParams = params.toSortedMap()
    for ((key, value) in sortedParams) {
        exps.add("$key=$value")
    }

    // 2. 用 & 拼接所有元素 (对应 exps.join("&"))
    val message = exps.joinToString("&")

    // 3. 将 _signedNonce 从 Base64 解码作为 HMAC 密钥 (对应 Buffer.from(_signedNonce, "base64"))
    val secretKeyBytes = Base64.getDecoder().decode(signedNonce)
    val secretKey = SecretKeySpec(secretKeyBytes, "HmacSHA256")

    // 4. 初始化并计算 HMAC-SHA256
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(secretKey)
    val hashBytes = mac.doFinal(message.toByteArray(Charsets.UTF_8))

    // 5. 将结果转为 Base64 字符串 (对应 digest("base64"))
    return Base64.getEncoder().encodeToString(hashBytes)
}
