package com.floristeriaakasia.backend.util

import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.Logger

object LoggingUtils {

    fun Logger.logSuccess(
        operation: String,
        vararg details: Pair<String, Any?>
    ) {
        this.info(
            operation,
            kv("operation", operation),
            kv("status", "success"),
            *details.map { kv(it.first, it.second) }.toTypedArray()
        )
    }

    fun Logger.logFailure(
        operation: String,
        error: Throwable,
        vararg details: Pair<String, Any?>
    ) {
        this.error(
            "$operation failed",
            kv("operation", operation),
            kv("status", "failure"),
            kv("errorType", error.javaClass.simpleName),
            kv("errorMessage", error.message),
            *details.map { kv(it.first, it.second) }.toTypedArray()
        )
    }

    fun Logger.logPerformance(
        operation: String,
        durationMs: Long,
        vararg details: Pair<String, Any?>
    ) {
        val level = when {
            durationMs > 1000 -> "SLOW"
            durationMs > 500 -> "MODERATE"
            else -> "FAST"
        }

        this.info(
            "$operation completed",
            kv("operation", operation),
            kv("durationMs", durationMs),
            kv("performanceLevel", level),
            *details.map { kv(it.first, it.second) }.toTypedArray()
        )
    }

}