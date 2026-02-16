package com.floristeriaakasia.backend.model.validation

import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.repository.ProductRepository
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueSlugValidator::class])
annotation class UniqueSlug(
    val message: String = "Slug already exists",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

@Component
class UniqueSlugValidator(
    private val productRepository: ProductRepository
) : ConstraintValidator<UniqueSlug, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?
    ): Boolean {
        if (value == null) return true
        return productRepository.findBySlug(value) == null
    }


}