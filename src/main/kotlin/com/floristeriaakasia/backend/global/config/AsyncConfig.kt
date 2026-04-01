package com.floristeriaakasia.backend.global.config

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import org.slf4j.LoggerFactory


@Configuration
@EnableAsync
class AsyncConfig : AsyncConfigurer {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Bean(name = ["taskExecutor"])
    override fun getAsyncExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 20
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("async-general-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(60)
        executor.initialize()
        return executor
    }

    @Bean(name = ["imageExecutor"])
    fun imageTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 10
        executor.queueCapacity = 50
        executor.setThreadNamePrefix("async-image-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(120)
        executor.initialize()
        return executor
    }

    @Bean(name = ["analyticsExecutor"])
    fun analyticsTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 2
        executor.maxPoolSize = 5
        executor.queueCapacity = 200
        executor.setThreadNamePrefix("async-analytics-")
        executor.initialize()
        return executor
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return AsyncUncaughtExceptionHandler { throwable, method, params ->
            logger.error(
                "Async method '{}' threw exception: {}",
                method.name,
                throwable.message,
                throwable
            )
            logger.error("Method parameters: {}", params.joinToString())
        }
    }
}
