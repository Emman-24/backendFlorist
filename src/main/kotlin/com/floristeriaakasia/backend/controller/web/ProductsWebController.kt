package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.exception.ResourceNotFoundException
import com.floristeriaakasia.backend.model.Product
import com.floristeriaakasia.backend.model.dto.ProductCreateRequest
import com.floristeriaakasia.backend.model.dto.ProductUpdateRequest
import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import com.floristeriaakasia.backend.service.ProductSeoService
import com.floristeriaakasia.backend.service.ProductService
import com.floristeriaakasia.backend.service.TagService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.math.BigDecimal
import jakarta.validation.constraints.NotNull

@Controller
@RequestMapping("/admin/products")
class ProductsWebController(
    private val productService: ProductService,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubcategoryRepository,
    private val tagService: TagService,
    private val productSeoService: ProductSeoService
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) subcategoryId: Long?,
        @RequestParam(required = false) status: Boolean?,
        @RequestParam(required = false) featured: Boolean?,
        @RequestParam(required = false) seasonal: Boolean?,
        model: Model
    ): String {
        var products = when {
            categoryId != null -> productService.findByCategory(categoryId)
            subcategoryId != null -> productService.findBySubCategory(subcategoryId)
            else -> productService.findAll()
        }

        if (status != null) {
            products = products.filter { it.status == status }
        }
        if (featured == true) {
            products = products.filter { it.featured }
        }
        if (seasonal == true) {
            products = products.filter { it.seasonal }
        }

        val categories = categoryRepository.findAll().sortedBy { it.position }
        val subcategories = subCategoryRepository.findAll().sortedBy { it.position }
        val stats = productService.getStats()

        model.addAttribute("products", products)
        model.addAttribute("categories", categories)
        model.addAttribute("subcategories", subcategories)
        model.addAttribute("stats", stats)
        model.addAttribute("selectedCategoryId", categoryId)
        model.addAttribute("selectedSubcategoryId", subcategoryId)
        model.addAttribute("selectedStatus", status)
        model.addAttribute("featuredOnly", featured)
        model.addAttribute("seasonalOnly", seasonal)

        return "pages/products/list"
    }

    @GetMapping("/new")
    fun showCreateForm(
        model: Model,
        redirectAttributes: RedirectAttributes
    ): String {
        val categories = categoryRepository.findByStatus(true)
        if (categories.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debes crear al menos una categoría primero")
            return "redirect:/admin/categories"
        }

        val subcategories = subCategoryRepository.findAll()
        if (subcategories.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debes crear al menos una subcategoría primero")
            return "redirect:/admin/subcategories"
        }

        val tags = tagService.findAllActive()

        model.addAttribute("product", ProductCreateRequest())
        model.addAttribute("categories", categories)
        model.addAttribute("subcategories", subcategories)
        model.addAttribute("tags", tags)
        model.addAttribute("isNew", true)

        return "pages/products/form"
    }

    @PostMapping("/save")
    fun createProduct(
        @Valid @ModelAttribute request: ProductCreateRequest,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", request)
            model.addAttribute("categories", categoryRepository.findByStatus(true))
            model.addAttribute("subcategories", subCategoryRepository.findAll())
            model.addAttribute("tags", tagService.findAllActive())
            model.addAttribute("isNew", true)
            return "pages/products/form"
        }

        try {
            val category = categoryRepository.findById(request.categoryId!!).orElse(null)
            val subCategory = subCategoryRepository.findById(request.subcategoryId!!)
                .orElseThrow { IllegalArgumentException("Subcategory not found") }

            val product = Product(
                title = request.title,
                route = request.route,
                price = request.price,
                stockStatus = request.stockStatus,
                seasonal = request.seasonal ?: false,
                featured = request.featured ?: false,
                facebookUrl = request.facebookUrl ?: "",
                instagramUrl = request.instagramUrl ?: "",
                status = request.status ?: true
            ).apply {
                this.category = category
                this.subCategory = subCategory
            }

            val saved = productService.save(
                product = product,
                tagIds = request.tagIds,
                generateSeo = true
            )
            redirectAttributes.addFlashAttribute("success", "Producto creado exitosamente")
            return "redirect:/admin/products"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al crear el producto: ${e.message}")
            println(e.message)
            return "redirect:/admin/products/new"
        }

    }

    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Long,
        model: Model
    ): String {
        val product = productService.findById(id) ?: throw ResourceNotFoundException("Producto no encontrado")

        val categories = categoryRepository.findByStatus(true)
        val subcategories = subCategoryRepository.findAll()
        val tags = tagService.findAllActive()

        val updateRequest = ProductUpdateRequest(
            title = product.title,
            route = product.route,
            price = product.price,
            categoryId = product.category.id,
            subcategoryId = product.subCategory.id,
            stockStatus = product.stockStatus,
            seasonal = product.seasonal,
            featured = product.featured,
            facebookUrl = product.facebookUrl,
            instagramUrl = product.instagramUrl,
            status = product.status,
            tagIds = product.tags.mapNotNull { it.id }
        )

        model.addAttribute("product", updateRequest)
        model.addAttribute("productId", id)
        model.addAttribute("categories", categories)
        model.addAttribute("subcategories", subcategories)
        model.addAttribute("tags", tags)
        model.addAttribute("descriptions", product.descriptions.sortedBy { it.position })
        model.addAttribute("gallery", product.gallery.sortedBy { it.position })
        model.addAttribute("isNew", false)

        return "pages/products/form"
    }

    @PostMapping("/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @ModelAttribute("product") request: ProductUpdateRequest,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes,
        model: Model
    ): String {
        if (bindingResult.hasErrors()) {
            model.addAttribute("product", request)
            model.addAttribute("productId", id)
            model.addAttribute("categories", categoryRepository.findByStatus(true))
            model.addAttribute("subcategories", subCategoryRepository.findAll())
            model.addAttribute("tags", tagService.findAllActive())
            model.addAttribute("isNew", false)
            return "pages/products/form"
        }

        try {
            val product = productService.findById(id) ?: throw ResourceNotFoundException("Producto no encontrado")
            
            val category = request.categoryId?.let { 
                categoryRepository.findById(it)
                    .orElseThrow { IllegalArgumentException("Category not found") }
            }
            
            val subCategory = request.subcategoryId?.let {
                subCategoryRepository.findById(it).orElseThrow { IllegalArgumentException("Subcategory not found") }
            }

            product.apply {
                title = request.title
                route = request.route
                price = request.price
                stockStatus = request.stockStatus
                seasonal = request.seasonal ?: false
                featured = request.featured ?: false
                facebookUrl = request.facebookUrl ?: ""
                instagramUrl = request.instagramUrl ?: ""
                status = request.status ?: true
                this.category = category!!
                this.subCategory = subCategory!!
            }

            productService.save(
                product = product,
                tagIds = request.tagIds,
                generateSeo = request.updateSeo
            )
            
            redirectAttributes.addFlashAttribute("success", "Producto actualizado exitosamente")
            return "redirect:/admin/products"
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el producto: ${e.message}")
            return "redirect:/admin/products/edit/$id"
        }
    }

    @GetMapping("/{id}")
    fun showProduct(
        @PathVariable id: Long,
        model: Model
    ): String {
        val details = productService.getProductWithDetails(id) ?: throw RuntimeException("Producto no encontrado")

        model.addAttribute("product", details.product)
        model.addAttribute("gallery", details.gallery)
        model.addAttribute("variants", details.variants)
        model.addAttribute("descriptions", details.descriptions)
        model.addAttribute("tags", details.tags)
        model.addAttribute("reviews", details.reviews)
        model.addAttribute("averageRating", details.averageRating)
        model.addAttribute("reviewCount", details.reviewCount)

        return "pages/products/details"
    }


    @PostMapping("/delete/{id}")
    fun deleteProduct(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.deleteById(id)
            redirectAttributes.addFlashAttribute("success", "Producto eliminado exitosamente")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: ${e.message}")
        }
        return "redirect:/admin/products"
    }

    @PostMapping("/{id}/toggle-status")
    fun toggleStatus(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.toggleStatus(id)
            redirectAttributes.addFlashAttribute("success", "Estado actualizado")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error: ${e.message}")
        }
        return "redirect:/admin/products/$id"
    }

    @PostMapping("/{id}/toggle-featured")
    fun toogleFeatured(
        @PathVariable id: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.toggleFeatured(id)
            redirectAttributes.addFlashAttribute("success", "Producto destacado actualizado")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
        }
        return "redirect:/admin/products/$id"
    }

    @PostMapping("/{id}/images")
    fun uploadImage(
        @PathVariable id: Long,
        @RequestParam file: MultipartFile,
        @RequestParam(required = false) altText: String?,
        @RequestParam(defaultValue = "false") isPrimary: Boolean,
        @RequestParam(defaultValue = "false") seasonal: Boolean,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.uploadImage(id, file, altText, isPrimary, seasonal)
            redirectAttributes.addFlashAttribute("success", "Imagen cargada exitosamente")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error al cargar la imagen: ${e.message}")
        }
        return "redirect:/admin/products/$id"

    }

    @PostMapping("/{id}/images/{imageId}/delete")
    fun deleteImage(
        @PathVariable id: Long,
        @PathVariable imageId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.deleteImage(imageId)
            redirectAttributes.addFlashAttribute("success", "Imagen eliminada exitosamente")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
        }
        return "redirect:/admin/products/$id"
    }

    @PostMapping("/{id}/images/{imageId}/set-primary")
    fun setPrimaryImage(
        @PathVariable id: Long,
        @PathVariable imageId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.setPrimaryImage(imageId)
            redirectAttributes.addFlashAttribute("success", "Imagen principal actualizada exitosamente")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
        }
        return "redirect:/admin/products/$id"
    }

    @PostMapping("/{id}/descriptions")
    fun addDescription(
        @PathVariable id: Long,
        @RequestParam paragraph: String,
        @RequestParam(required = false) position: Int?,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.addDescription(id, paragraph, position)
            redirectAttributes.addFlashAttribute("success", "Descripción añadida")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error: ${e.message}")
        }
        return "redirect:/admin/products/$id"
    }

    @PostMapping("/{id}/descriptions/{descId}/delete")
    fun deleteDescription(
        @PathVariable id: Long,
        @PathVariable descId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.deleteDescription(descId)
            redirectAttributes.addFlashAttribute("success", "Descripción eliminada")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", e.message)
        }
        return "redirect:/admin/products/$id"
    }

    @PostMapping("/{id}/variants")
    fun addVariant(
        @PathVariable id: Long,
        @RequestParam variantType: String,
        @RequestParam name: String,
        @RequestParam priceAdjustment: BigDecimal,
        @RequestParam(required = false) description: String?,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.addVariant(id, variantType, name, priceAdjustment, description)
            redirectAttributes.addFlashAttribute("success", "Variante añadida")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error: ${e.message}")
        }
        return "redirect:/admin/products/$id"
    }

    @PostMapping("/{id}/variants/{variantId}/delete")
    fun deleteVariant(
        @PathVariable id: Long,
        @PathVariable variantId: Long,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            productService.deleteVariant(variantId)
            redirectAttributes.addFlashAttribute("success", "Variante eliminada")
        } catch (e: Exception) {
            redirectAttributes.addFlashAttribute("error", "Error: ${e.message}")
        }
        return "redirect:/admin/products/$id"
    }

}