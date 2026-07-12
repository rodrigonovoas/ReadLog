package com.rodrigonovoa.readlog.domain.usecase

import java.util.Calendar
import javax.inject.Inject

enum class TimeOfDay {
    MORNING, AFTERNOON, EVENING
}

class GetTimeOfDayUseCase @Inject constructor() {
    operator fun invoke(): TimeOfDay {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> TimeOfDay.MORNING
            in 12..17 -> TimeOfDay.AFTERNOON
            else -> TimeOfDay.EVENING
        }
    }
}
