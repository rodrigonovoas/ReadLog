package com.rodrigonovoa.readlog.ui.addbook

sealed interface ScanError {
    data object Network : ScanError
    data object NotFound : ScanError
    data object Unknown : ScanError
}
