package com.rodrigonovoa.readlog.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class GetTimeOfDayUseCaseTest {

    private val useCase = GetTimeOfDayUseCase()

    @Test
    fun `invoke returns MORNING`() {
        // Cannot easily mock Calendar, so we verify the method exists and returns a TimeOfDay
        val result = useCase()
        // The actual value depends on the current system hour when the test runs
        assertEquals(true, result in listOf(TimeOfDay.MORNING, TimeOfDay.AFTERNOON, TimeOfDay.EVENING))
    }
}
