package com.floristeriaakasia.backend.controller

import com.floristeriaakasia.backend.repository.BannerRepository
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/banners")
class BannerController(private val bannerRepository: BannerRepository) {

    @RequestMapping
    fun getAllBanners() = bannerRepository.findAll()

}