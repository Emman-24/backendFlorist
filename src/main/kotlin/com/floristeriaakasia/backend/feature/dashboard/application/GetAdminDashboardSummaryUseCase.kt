package com.floristeriaakasia.backend.feature.dashboard.application

data class AdminDashboardSummary(
    val totalArrangements: Long,
    val availableArrangements: Long,
    val totalCategories: Long,
    val totalFaqs: Long,
    val activeFaqs: Long,
    val totalTags: Long
)

interface GetAdminDashboardSummaryUseCase {
    fun execute(): AdminDashboardSummary
}