package com.floristeriaakasia.backend.feature.tag

import com.floristeriaakasia.backend.config.RepositoryTestBase
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TagRepositoryTest : RepositoryTestBase() {

    @Autowired
    private lateinit var tagRepository: TagRepository

    private lateinit var tag: Tag
    private lateinit var tag2: Tag

    @BeforeEach
    fun setUp() {
        tag = Tag(text = "Rosas", route = "rosas", description = "Rosas are beautiful flowers")
        tag2 = Tag(text = "Tulips", route = "tulips", description = "Tulips are beautiful flowers")
        tagRepository.saveAll(listOf(tag, tag2))
    }

    @Test
    fun `findByRoute returns the matching tag`() {
        assertEquals(tag, tagRepository.findByRoute("rosas"))
        assertEquals(tag2, tagRepository.findByRoute("tulips"))
    }

    @Test
    fun `findByRoute returns null when no tag has that route`() {
        assertNull(tagRepository.findByRoute("does-not-exist"))
    }

}