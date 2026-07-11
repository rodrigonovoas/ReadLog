package com.rodrigonovoa.readlog.ui.fakes

import com.rodrigonovoa.readlog.domain.usecase.InsertMockBooksUseCase

class FakeInsertMockBooksUseCase : InsertMockBooksUseCase(
    bookRepository = FakeBookRepository()
) {
    var invoked = false

    override suspend fun invoke() {
        invoked = true
    }
}
