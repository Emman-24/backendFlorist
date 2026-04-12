package com.floristeriaakasia.backend.global.security

import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class JwtTokenBlacklist {

    private val log = LoggerFactory.getLogger(javaClass)

    private val blacklist = Caffeine.newBuilder()
        .maximumSize(50_000)
        .expireAfterWrite(24, TimeUnit.HOURS)
        .recordStats()
        .build<String, Long>()

    fun revoke(rawToken: String, ttlMs: Long) {
        if (ttlMs <= 0) return
        val key = rawToken.hashCode().toString()
        blacklist.put(key, System.currentTimeMillis() + ttlMs)
        log.info("SEC_EVENT=TOKEN_REVOKED ttlMs={}", ttlMs)
    }

    fun isBlacklisted(rawToken: String): Boolean {
        val key = rawToken.hashCode().toString()
        val expiryMs = blacklist.getIfPresent(key) ?: return false
        return System.currentTimeMillis() < expiryMs
    }
}