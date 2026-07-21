package com.rodrigonovoa.readlog.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class GenerateUsernameUseCaseTest {

    private val useCase = GenerateUsernameUseCase()

    @Test
    fun `generates lowercase slug with underscores from display name`() {
        val result = useCase("Elena Marín", "uid-123456")

        assertEquals("elena_marin", result)
    }

    @Test
    fun `strips accents and non alphanumeric characters`() {
        val result = useCase("José Ñúñez!!", "uid-123456")

        assertEquals("jose_nunez", result)
    }

    @Test
    fun `falls back to userId based username when display name is null`() {
        val result = useCase(null, "abcdef123456")

        assertEquals("userabcdef", result)
    }

    @Test
    fun `falls back to userId based username when display name is blank`() {
        val result = useCase("   ", "abcdef123456")

        assertEquals("userabcdef", result)
    }
}
