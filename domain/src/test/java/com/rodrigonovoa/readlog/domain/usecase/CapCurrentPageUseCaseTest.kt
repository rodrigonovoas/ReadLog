package com.rodrigonovoa.readlog.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CapCurrentPageUseCaseTest {

    private val useCase = CapCurrentPageUseCase()

    @Test
    fun `empty string returns empty`() {
        assertEquals("", useCase("", 100))
    }

    @Test
    fun `valid currentPage below max returns unchanged`() {
        assertEquals("50", useCase("50", 100))
    }

    @Test
    fun `currentPage equal to max returns unchanged`() {
        assertEquals("100", useCase("100", 100))
    }

    @Test
    fun `currentPage above max is capped`() {
        assertEquals("100", useCase("150", 100))
    }

    @Test
    fun `non numeric string returns unchanged`() {
        assertEquals("abc", useCase("abc", 100))
    }

    @Test
    fun `null maxPages returns unchanged`() {
        assertEquals("50", useCase("50", null))
    }
}
