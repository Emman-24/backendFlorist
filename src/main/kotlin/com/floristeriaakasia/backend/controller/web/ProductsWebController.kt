package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.repository.CategoryRepository
import com.floristeriaakasia.backend.repository.SubcategoryRepository
import com.floristeriaakasia.backend.service.ProductSeoService
import com.floristeriaakasia.backend.service.ProductService
import com.floristeriaakasia.backend.service.TagService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/admin/products")
class ProductsWebController(
    private val productService: ProductService,
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubcategoryRepository,
    private val tagService: TagService,
    private val productSeoService: ProductSeoService
) {
//
//    @GetMapping
//    fun list(
//        @RequestParam(required = false) categoryId: Long?,
//        @RequestParam(required = false) subcategoryId: Long?,
//        @RequestParam(required = false) status: Boolean?,
//        @RequestParam(required = false) featured: Boolean?,
//        @RequestParam(required = false) seasonal: Boolean?,
//        model: Model
//    ): String {
//        var products = when {
//            categoryId != null -> productService.findByCategory(categoryId)
//            subcategoryId != null -> productService.findBySubCategory(subcategoryId)
//            else -> productService.findAll()
//        }
//
//        if (status != null) {
//            products = products.filter { it.status == status }
//        }
//        if (featured == true) {
//            products = products.filter { it.featured }
//        }
//        if (seasonal == true) {
//            products = products.filter { it.seasonal }
//        }
//
//        val categories = categoryRepository.findAll().sortedBy { it.position }
//        val subcategories = subCategoryRepository.findAll().sortedBy { it.position }
//        val stats = productService.getStats()
//
//        model.addAttribute("products", products)
//        model.addAttribute("categories", categories)
//        model.addAttribute("subcategories", subcategories)
//        model.addAttribute("stats", stats)
//        model.addAttribute("selectedCategoryId", categoryId)
//        model.addAttribute("selectedSubcategoryId", subcategoryId)
//        model.addAttribute("selectedStatus", status)
//        model.addAttribute("featuredOnly", featured)
//        model.addAttribute("seasonalOnly", seasonal)
//
//        return "pages/products/list"
//    }
//
//    @GetMapping("/new")
//    fun showCreateForm(
//        model: Model,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        val categories = categoryRepository.findByStatus(true)
//        if (categories.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "Debes crear al menos una categoría primero")
//            return "redirect:/categories"
//        }
//
//        val subcategories = subCategoryRepository.findAll()
//        if (subcategories.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "Debes crear al menos una subcategoría primero")
//            return "redirect:/subcategories"
//        }
//
//        val newProduct = Product(
//            title = "",
//            route = "",
//            price = BigDecimal.ZERO,
//            status = true
//        )
//
//        val tags = tagService.findAllActive()
//
//        model.addAttribute("product", newProduct)
//        model.addAttribute("categories", categories)
//        model.addAttribute("subcategories", subcategories)
//        model.addAttribute("tags", tags)
//        model.addAttribute("isNew", true)
//
//        return "pages/products/form"
//    }
//
//    @PostMapping("/save")
//    fun create(
//        @Valid @ModelAttribute product: Product,
//        @RequestParam("category.id") categoryId: Long,
//        @RequestParam("subCategory.id") subcategoryId: Long,
//        @RequestParam(required = false) tagIds: List<Long>?,
//        @RequestParam(required = false) mainImage: MultipartFile?,
//        bindingResult: BindingResult,
//        redirectAttributes: RedirectAttributes,
//        model: Model
//    ):String{
//        val category = categoryRepository.findById(categoryId).orElse(null)
//        val subCategory = subCategoryRepository.findById(subcategoryId).orElse(null)
//
//        if (category == null) {
//            bindingResult.rejectValue("category", "error.product", "Categoría no encontrada")
//        } else {
//            product.category = category
//        }
//
//        if (subCategory == null) {
//            bindingResult.rejectValue("subCategory", "error.product", "Subcategoría no encontrada")
//        } else {
//            product.subCategory = subCategory
//        }
//
//        val existingProduct = productService.findByRoute(product.route)
//        if (existingProduct != null) {
//            bindingResult.rejectValue("route", "error.product", "Ya existe un producto con esta ruta")
//        }
//
//        if (bindingResult.hasErrors()) {
//            model.addAttribute("product", product)
//            model.addAttribute("categories", categoryRepository.findByStatus(true))
//            model.addAttribute("subcategories", subCategoryRepository.findAll())
//            model.addAttribute("tags", tagService.findAllActive())
//            model.addAttribute("isNew", true)
//            return "redirect:/products/form"
//        }
//
//        try {
//            val savedProduct = productService.save(product, tagIds)
//            if (mainImage != null && !mainImage.isEmpty) {
//                productService.uploadImage(
//                    productId = savedProduct.id!!,
//                    file = mainImage,
//                    altText = "${savedProduct.title} - Floristería Akasia - Pereira - Colombia",
//                    isPrimary = true
//                )
//            }
//            redirectAttributes.addFlashAttribute("success", "Producto creado exitosamente")
//            return "redirect:/products"
//
//        }catch (e: ResourceNotFoundException){
//            redirectAttributes.addFlashAttribute("error", "Error al crear el producto: ${e.message}")
//            return "redirect:/products/form"
//        }
//    }
//
//    @PostMapping("/delete/{id}")
//    fun delete(
//        @PathVariable id: Long,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        try {
//            productService.deleteById(id)
//            redirectAttributes.addFlashAttribute("success", "Producto eliminado exitosamente")
//        } catch (e: Exception) {
//            redirectAttributes.addFlashAttribute("error", "Error al eliminar: ${e.message}")
//        }
//        return "redirect:/products"
//    }
//
//    @PostMapping("/toggle-status/{id}")
//    fun toggleStatus(
//        @PathVariable id: Long,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        try {
//            val product = productService.toggleStatus(id)
//            val statusText = if (product.status) "activado" else "desactivado"
//            redirectAttributes.addFlashAttribute("success", "Producto $statusText exitosamente")
//        } catch (e: Exception) {
//            redirectAttributes.addFlashAttribute("error", "Error: ${e.message}")
//        }
//
//        return "redirect:/products"
//    }
//
//    @PostMapping("/toggle-featured/{id}")
//    fun toggleFeatured(
//        @PathVariable id: Long,
//        redirectAttributes: RedirectAttributes
//    ): String {
//        try {
//            val product = productService.toggleFeatured(id)
//            val featuredText = if (product.featured) "marcado como destacado" else "removido de destacados"
//            redirectAttributes.addFlashAttribute("success", "Producto $featuredText exitosamente")
//        } catch (e: Exception) {
//            redirectAttributes.addFlashAttribute("error", "Error: ${e.message}")
//        }
//
//        return "redirect:/products"
//    }

}