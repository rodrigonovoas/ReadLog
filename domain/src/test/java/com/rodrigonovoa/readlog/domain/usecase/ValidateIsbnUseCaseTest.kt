package com.rodrigonovoa.readlog.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateIsbnUseCaseTest {

    private val useCase = ValidateIsbnUseCase()

    @Test
    fun `empty isbn is invalid`() {
        assertFalse(useCase(""))
    }

    @Test
    fun `blank isbn is invalid`() {
        assertFalse(useCase("   "))
    }

    @Test
    fun `isbn with non numeric characters is invalid`() {
        assertFalse(useCase("978-1-23-456789-0"))
    }

    @Test
    fun `valid 13 digit isbn is valid`() {
        assertTrue(useCase("9781234567890"))
    }

    @Test
    fun `valid 13 digit isbn with 978 prefix is valid`() {
        assertTrue(useCase("9780441172719"))
    }

    @Test
    fun `valid 13 digit isbn with 979 prefix is valid`() {
        assertTrue(useCase("9791234567890"))
    }

    @Test
    fun `invalid 13 digit isbn prefix is invalid`() {
        assertFalse(useCase("9771234567890"))
    }

    @Test
    fun `valid 10 digit isbn is valid`() {
        assertTrue(useCase("1234567890"))
    }

    @Test
    fun `9 digit isbn is invalid`() {
        assertFalse(useCase("123456789"))
    }

    @Test
    fun `14 digit isbn is invalid`() {
        assertFalse(useCase("12345678901234"))
    }
}
