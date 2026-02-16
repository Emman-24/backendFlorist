package com.floristeriaakasia.backend.config

import com.cloudinary.Cloudinary
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class HealthConfig(
    private val dataSource: DataSource
) : HealthIndicator {
    override fun health(): Health {
        return try {
            dataSource.connection.use { conn ->
                val stmt = conn.createStatement()
                stmt.executeQuery("SELECT 1")
                Health.up()
                    .withDetail("database", "MySQL")
                    .withDetail("validationQuery", "SELECT 1")
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message)
                .build()
        }
    }
}

@Component
class CloudinaryHealthIndicator(
    private val cloudinary: Cloudinary
) : HealthIndicator {

    override fun health(): Health {
        return try {
            cloudinary.api().ping(emptyMap<Any, Any>())
            Health.up().build()
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message)
                .build()
        }
    }
}