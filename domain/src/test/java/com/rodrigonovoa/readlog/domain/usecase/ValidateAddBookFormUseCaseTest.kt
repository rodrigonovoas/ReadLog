package com.rodrigonovoa.readlog.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateAddBookFormUseCaseTest {

    private val useCase = ValidateAddBookFormUseCase()

    @Test
    fun `empty title is invalid`() {
        assertFalse(useCase("", "100", "50"))
    }

    @Test
    fun `blank title is invalid`() {
        assertFalse(useCase("   ", "100", "50"))
    }

    @Test
    fun `non numeric pages is invalid`() {
        assertFalse(useCase("Title", "abc", "50"))
    }

    @Test
    fun `zero pages is invalid`() {
        assertFalse(useCase("Title", "0", "50"))
    }

    @Test
    fun `negative pages is invalid`() {
        assertFalse(useCase("Title", "-1", "50"))
    }

    @Test
    fun `empty currentPage is valid`() {
        assertTrue(useCase("Title", "100", ""))
    }

    @Test
    fun `non numeric currentPage is invalid`() {
        assertFalse(useCase("Title", "100", "abc"))
    }

    @Test
    fun `negative currentPage is invalid`() {
        assertFalse(useCase("Title", "100", "-1"))
    }

    @Test
    fun `currentPage greater than pages is invalid`() {
        assertFalse(useCase("Title", "100", "150"))
    }

    @Test
    fun `currentPage equal to pages is valid`() {
        assertTrue(useCase("Title", "100", "100"))
    }

    @Test
    fun `currentPage less than pages is valid`() {
        assertTrue(useCase("Title", "100", "50"))
    }

    @Test
    fun `zero currentPage is valid`() {
        assertTrue(useCase("Title", "100", "0"))
    }
}
