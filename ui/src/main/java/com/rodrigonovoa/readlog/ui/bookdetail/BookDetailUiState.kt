package com.rodrigonovoa.readlog.ui.bookdetail

data class BookDetailUiState(
    val bookId: Int = 0,
    val bookTitle: String = "",
    val bookAuthor: String = "",
    val readingSinceLabel: String = "",
    val sessionsCount: Int = 0,
    val totalTimeLabel: String = "",
    val daysReadingCount: Int = 0,
    val monthLabel: String = "",
    val monthDays: List<BookDetailMonthDay> = emptyList(),
    val recentSessions: List<BookDetailSession> = emptyList(),
)

enum class BookDetailDayStatus { NONE, READ, TODAY }

data class BookDetailMonthDay(
    val day: Int,
    val status: BookDetailDayStatus = BookDetailDayStatus.NONE,
)

data class BookDetailSession(
    val dateLabel: String,
    val dayLabel: String,
    val durationLabel: String,
    val comment: String? = null,
)

val sampleBookDetailUiState = BookDetailUiState(
    bookId = 1,
    bookTitle = "Cien años de soledad",
    bookAuthor = "Gabriel García Márquez",
    readingSinceLabel = "6 jun",
    sessionsCount = 18,
    totalTimeLabel = "14h 32m",
    daysReadingCount = 12,
    monthLabel = "Julio 2026",
    monthDays = (1..30).map { day ->
        val status = when (day) {
            16 -> BookDetailDayStatus.TODAY
            in setOf(2, 3, 6, 7, 8, 11, 12, 15) -> BookDetailDayStatus.READ
            else -> BookDetailDayStatus.NONE
        }
        BookDetailMonthDay(day = day, status = status)
    },
    recentSessions = listOf(
        BookDetailSession(
            dateLabel = "15 jul",
            dayLabel = "Miércoles",
            durationLabel = "42 min",
            comment = "El realismo mágico de este capítulo me ha dejado sin palabras.",
        ),
        BookDetailSession(
            dateLabel = "12 jul",
            dayLabel = "Domingo",
            durationLabel = "1h 05min",
        ),
        BookDetailSession(
            dateLabel = "11 jul",
            dayLabel = "Sábado",
            durationLabel = "28 min",
            comment = "Buonaparte otra vez... me está costando seguir el hilo familiar.",
        ),
    ),
)
