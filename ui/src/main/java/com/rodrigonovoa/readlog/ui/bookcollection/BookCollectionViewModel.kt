package com.rodrigonovoa.readlog.ui.bookcollection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rodrigonovoa.readlog.domain.model.Book
import com.rodrigonovoa.readlog.domain.usecase.GetBooksUseCase
import com.rodrigonovoa.readlog.domain.usecase.InsertMockBooksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookCollectionViewModel @Inject constructor(
    private val getBooksUseCase: GetBooksUseCase,
    private val insertMockBooksUseCase: InsertMockBooksUseCase,
) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    init {
        viewModelScope.launch {
            insertMockBooksUseCase()
            getBooksUseCase().collect { bookList ->
                _books.value = bookList
            }
        }
    }
}
