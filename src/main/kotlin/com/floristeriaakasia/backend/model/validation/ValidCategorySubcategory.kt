package com.floristeriaakasia.backend.model.validation

import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidCategorySubcategoryValidator::class])
annotation class ValidCategorySubcategory(
    val message: String = "Subcategory must belong to the specified category",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)


@Component
class ValidCategorySubcategoryValidator(
    private val categoryRepository: CategoryRepository,
    private val subcategoryRepository: SubcategoryRepository
) : ConstraintValidator<ValidCategorySubcategory, Any> {
    override fun isValid(
        value: Any?,
        context: ConstraintValidatorContext?
    ): Boolean {
        if (value == null) return true

        val categoryId = getProperty(value, "categoryId") as? Long ?: return false
        val subcategoryId = getProperty(value, "subcategoryId") as? Long ?: return false

        val subcategory = subcategoryRepository.findByIdOrNull(subcategoryId) ?: return false

        return subcategory.category.id == categoryId
    }

    private fun getProperty(obj: Any, propertyName: String): Any? {
        return obj::class.memberProperties
            .find { it.name == propertyName }
            ?.call(obj)
    }

}