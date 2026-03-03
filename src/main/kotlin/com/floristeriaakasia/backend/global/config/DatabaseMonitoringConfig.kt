package com.floristeriaakasia.backend.global.config

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class DatabaseMonitoringConfig(
    private val dataSource: DataSource
): HealthIndicator {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun health(): Health {
        return try {
            if (dataSource is HikariDataSource) {
                val pool = dataSource.hikariPoolMXBean

                val activeConnections = pool?.activeConnections ?: 0
                val idleConnections = pool?.idleConnections ?: 0
                val totalConnections = pool?.totalConnections ?: 0
                val threadsAwaitingConnection = pool?.threadsAwaitingConnection ?: 0

                val utilizationPercent = if (totalConnections > 0) {
                    (activeConnections.toDouble() / totalConnections.toDouble()) * 100
                } else 0.0

                if (utilizationPercent > 80) {
                    logger.warn(
                        "High database pool utilization: $utilizationPercent% " +
                                "(Active: $activeConnections, Total: $totalConnections)"
                    )
                }

                if (threadsAwaitingConnection > 0) {
                    logger.warn(
                        "Threads waiting for database connection: $threadsAwaitingConnection"
                    )
                }

                Health.up()
                    .withDetail("pool.active", activeConnections)
                    .withDetail("pool.idle", idleConnections)
                    .withDetail("pool.total", totalConnections)
                    .withDetail("pool.waiting", threadsAwaitingConnection)
                    .withDetail("pool.utilization", "${"%.2f".format(utilizationPercent)}%")
                    .build()
            } else {
                Health.unknown()
                    .withDetail("reason", "Not using HikariCP")
                    .build()
            }
        } catch (e: Exception) {
            logger.error("Error checking database pool health", e)
            Health.down()
                .withDetail("error", e.message)
                .build()
        }
    }
}