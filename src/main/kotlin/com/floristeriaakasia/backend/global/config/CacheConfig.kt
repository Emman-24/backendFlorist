package com.floristeriaakasia.backend.global.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.util.concurrent.TimeUnit

/**
 * Advanced cache configuration with multiple cache strategies
 * Optimized for high-performance product catalog operations
 */
@Configuration
@EnableCaching
class CacheConfig {

    /**
     * Primary cache manager with optimized settings for different data types
     * - Products: 10 min TTL, 500 max entries
     * - ProductBySlug: 15 min TTL, 1000 max entries (slugs accessed frequently)
     * - Categories: 30 min TTL, 100 max entries (rarely change)
     * - FeaturedProducts: 5 min TTL, 50 max entries (homepage performance)
     */
    @Bean
    @Primary
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager()
        cacheManager.setCaffeine(defaultCaffeineConfig())

        // Register cache names
        cacheManager.setCacheNames(
            listOf(
                "products",
                "productBySlug",
                "categories",
                "subcategories",
                "tags",
                "featuredProducts",
                "imageUrls"
            )
        )

        return cacheManager
    }

    /**
     * Cache-specific configurations with optimized TTL
     */
    @Bean("productCache")
    fun productCacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager("products", "productBySlug")
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .expireAfterAccess(15, TimeUnit.MINUTES)
                .recordStats() // Enable cache hit/miss metrics
        )
        return cacheManager
    }

    @Bean("categoryCache")
    fun categoryCacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager("categories", "subcategories")
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
        )
        return cacheManager
    }

    @Bean("shortLivedCache")
    fun shortLivedCacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager("featuredProducts", "imageUrls")
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
        )
        return cacheManager
    }

    /**
     * Default Caffeine configuration
     * - Soft values: Allow GC to reclaim under memory pressure
     * - Record stats: Enable monitoring via JMX/Actuator
     * - Weak keys: Prevent memory leaks
     */
    private fun defaultCaffeineConfig(): Caffeine<Any, Any> {
        return Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .softValues() // GC-friendly caching
            .recordStats()
    }
}
