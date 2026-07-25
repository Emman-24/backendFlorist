package com.floristeriaakasia.backend.feature.dashboard.application

import com.floristeriaakasia.backend.feature.category.CategoryRepository
import com.floristeriaakasia.backend.feature.faq.FaqRepository
import com.floristeriaakasia.backend.feature.floralArrangment.domain.FloralArrangementRepository
import com.floristeriaakasia.backend.feature.tag.TagRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetAdminDashboardSummaryUseCaseImpl(
    private val floralArrangementRepository: FloralArrangementRepository,
    private val categoryRepository: CategoryRepository,
    private val faqRepository: FaqRepository,
    private val tagRepository: TagRepository
) : GetAdminDashboardSummaryUseCase {

    override fun execute(): AdminDashboardSummary = AdminDashboardSummary(
        totalArrangements = floralArrangementRepository.count(),
        availableArrangements = floralArrangementRepository.countByIsAvailable(true),
        totalCategories = categoryRepository.count(),
        totalFaqs = faqRepository.count(),
        activeFaqs = faqRepository.countByStatus(true),
        totalTags = tagRepository.count()
    )

}