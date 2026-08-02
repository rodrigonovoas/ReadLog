package com.rodrigonovoa.readlog.ui.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.model.BookState
import com.rodrigonovoa.readlog.domain.model.Session
import com.rodrigonovoa.readlog.domain.usecase.GetAnnotationsForSessionUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetBookByIdUseCase
import com.rodrigonovoa.readlog.domain.usecase.GetSessionsForBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val RECENT_SESSIONS_LIMIT = 5
private const val DAY_IN_MILLIS = 24 * 60 * 60 * 1000L

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val getSessionsForBookUseCase: GetSessionsForBookUseCase,
    private val getAnnotationsForSessionUseCase: GetAnnotationsForSessionUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val bookId: Int = savedStateHandle.get<Int>("bookId") ?: -1

    private val _uiState = MutableStateFlow(BookDetailUiState(bookId = bookId))
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    private var bookLoaded = false
    private var sessionsLoaded = false

    init {
        if (bookId != -1) {
            viewModelScope.launch {
                val book = getBookByIdUseCase(bookId)
                if (book != null) {
                    _uiState.update {
                        it.copy(
                            bookTitle = book.title,
                            bookAuthor = book.author,
                            bookState = book.state,
                            readingSinceLabel = formatMillis(dayMonthFormat, book.creationDate),
                            daysReadingCount = calculateDaysReading(
                                book.creationDate,
                                book.statusDate,
                                book.state,
                            ),
                            coverUrl = book.coverUrl,
                        )
                    }
                }
                bookLoaded = true
                if (sessionsLoaded) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
            viewModelScope.launch {
                getSessionsForBookUseCase(bookId).collect { sessions ->
                    updateSessionData(sessions)
                    if (!sessionsLoaded) {
                        sessionsLoaded = true
                        if (bookLoaded) {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun updateSessionData(sessions: List<Session>) {
        val sortedSessions = sessions.sortedByDescending { it.creationDate }

        val recentSessions = sortedSessions.take(RECENT_SESSIONS_LIMIT).map { session ->
            val comment = getAnnotationsForSessionUseCase(session.sessionId)
                .firstOrNull()
                ?.annotation
            BookDetailSession(
                dateLabel = formatMillis(dayMonthFormat, session.creationDate),
                dayLabel = formatMillis(weekdayFormat, session.creationDate).replaceFirstChar {
                    it.uppercase(Locale.getDefault())
                },
                durationLabel = formatDuration(session.time),
                comment = comment,
            )
        }

        _uiState.update {
            it.copy(
                sessionsCount = sessions.size,
                totalTimeLabel = formatDuration(
                    totalSeconds = sessions.sumOf { session -> session.time },
                    minuteUnit = "m.",
                ),
                monthLabel = currentMonthLabel(),
                monthDays = buildMonthDays(sessions),
                recentSessions = recentSessions,
            )
        }
    }

    private fun calculateDaysReading(
        creationDateMillis: Long,
        statusDateMillis: Long?,
        state: BookState,
    ): Int {
        val startOfCreationDay = startOfDay(creationDateMillis)
        val endOfDay = when (state) {
            BookState.COMPLETED,
            BookState.DROPPED,
            BookState.PAUSED -> statusDateMillis?.let { startOfDay(it) } ?: startOfDay(System.currentTimeMillis())
            BookState.IN_PROGRESS -> startOfDay(System.currentTimeMillis())
        }
        val elapsedDays = (endOfDay.timeInMillis - startOfCreationDay.timeInMillis) / DAY_IN_MILLIS
        return (elapsedDays + 1).toInt().coerceAtLeast(1)
    }

    private fun startOfDay(millis: Long): Calendar {
        return Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun currentMonthLabel(): String {
        return formatMillis(monthLabelFormat, System.currentTimeMillis()).replaceFirstChar {
            it.uppercase(Locale.getDefault())
        }
    }

    private fun buildMonthDays(sessions: List<Session>): List<BookDetailMonthDay> {
        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val currentMonth = today.get(Calendar.MONTH)
        val todayOfMonth = today.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH)

        val readDays = sessions.mapNotNull { session ->
            val calendar = Calendar.getInstance().apply { timeInMillis = session.creationDate }
            if (calendar.get(Calendar.YEAR) == currentYear && calendar.get(Calendar.MONTH) == currentMonth) {
                calendar.get(Calendar.DAY_OF_MONTH)
            } else {
                null
            }
        }.toSet()

        return (1..daysInMonth).map { day ->
            val status = when {
                day == todayOfMonth -> BookDetailDayStatus.TODAY
                day in readDays -> BookDetailDayStatus.READ
                else -> BookDetailDayStatus.NONE
            }
            BookDetailMonthDay(day = day, status = status)
        }
    }

    private fun formatDuration(totalSeconds: Long, minuteUnit: String = "min"): String {
        val minutes = totalSeconds / 60
        return if (minutes < 60) {
            "$minutes $minuteUnit"
        } else {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            "%dh %02d$minuteUnit".format(hours, remainingMinutes)
        }
    }

    private companion object {
        val dayMonthFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val weekdayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val monthLabelFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    }
}

private fun formatMillis(format: SimpleDateFormat, millis: Long): String = format.format(Date(millis))
