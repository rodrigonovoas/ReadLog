package com.rodrigonovoa.readlog.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateReadingProgressUseCaseTest {

    private val useCase = CalculateReadingProgressUseCase()

    @Test
    fun `empty currentPage returns zero`() {
        assertEquals(0, useCase("", "100"))
    }

    @Test
    fun `zero currentPage returns zero`() {
        assertEquals(0, useCase("0", "100"))
    }

    @Test
    fun `valid progress is calculated`() {
        assertEquals(67, useCase("231", "340"))
    }

    @Test
    fun `currentPage equal to pages returns 100`() {
        assertEquals(100, useCase("100", "100"))
    }

    @Test
    fun `non numeric currentPage returns zero`() {
        assertEquals(0, useCase("abc", "100"))
    }

    @Test
    fun `non numeric pages returns zero`() {
        assertEquals(0, useCase("50", "abc"))
    }

    @Test
    fun `zero pages returns zero`() {
        assertEquals(0, useCase("0", "0"))
    }

    @Test
    fun `negative pages returns zero`() {
        assertEquals(0, useCase("0", "-10"))
    }
}
