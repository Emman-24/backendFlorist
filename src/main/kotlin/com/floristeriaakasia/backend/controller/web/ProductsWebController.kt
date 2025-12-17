package com.floristeriaakasia.backend.controller.web

import com.floristeriaakasia.backend.model.dto.product.ProductCreateRequest
import com.floristeriaakasia.backend.service.CategoryService
import com.floristeriaakasia.backend.service.ProductService
import com.floristeriaakasia.backend.service.SubcategoryService
import com.floristeriaakasia.backend.service.TagService
import jakarta.validation.Valid
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
@RequestMapping("/products")
class ProductsWebController(
    private val categoryService: CategoryService,
    private val subcategoryService: SubcategoryService,
    private val productService: ProductService,
    private val tagService: TagService
) {
    @GetMapping
    fun showProductList(model: Model): String {
        val categories = categoryService.findAll()
        model.addAttribute("categoriesForDropdown", categories)
        model.addAttribute("subcategoriesForDropdown", subcategoryService.findAll())
        model.addAttribute("tagsForDropdown", tagService.findActiveStatus())
        model.addAttribute(
            "productCreateRequest",
            ProductCreateRequest(
                categoryId = 0L,
                subCategoryId = 0L,
                route = "",
                status = true,
                text = "",
                description = "",
                price = null,
                facebookUrl = "",
                instagramUrl = "",
                tagIds = mutableListOf()
            )
        )
        model.addAttribute("products", productService.findAll())
        return "pages/products/list"
    }

    @GetMapping("/edit/{id}")
    fun showEditForm(
        @PathVariable id: Long,
        model: Model
    ): String {
        model.addAttribute("productCreateRequest", productService.finByRequestId(id))
        model.addAttribute("id", id)
        model.addAttribute("categoriesForDropdown", categoryService.findAll())
        model.addAttribute("subcategoriesForDropdown", subcategoryService.findAll())
        model.addAttribute("tagsForDropdown", tagService.findActiveStatus())

        return "pages/products/edit"
    }

    @PostMapping
    fun createProduct(
        @Valid @ModelAttribute("productCreateRequest") request: ProductCreateRequest,
        @RequestParam("file") file: MultipartFile,
        bindingResult: BindingResult,
        model: Model
    ): String {
        try {
            productService.create(request, file)
        } catch (_: Exception) {
            return "pages/products/list"
        }
        return "redirect:/products"
    }

    @PostMapping("/update/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @ModelAttribute("productCreateRequest") request: ProductCreateRequest,
        @RequestParam("file") file: MultipartFile,
        bindingResult: BindingResult,
        model: Model
    ): String {
        return try {
            productService.update(id, request, file)
            "redirect:/products"
        } catch (_: Exception) {
            model.addAttribute("categoriesForDropdown", categoryService.findAll())
            model.addAttribute("subcategoriesForDropdown", subcategoryService.findAll())
            model.addAttribute("id", id)
            "pages/products/edit"
        }

    }

    @PostMapping("/delete/{id}")
    fun deleteProduct(
        @PathVariable id: Long
    ): String {
        productService.deleteById(id)
        return "redirect:/products"
    }


}