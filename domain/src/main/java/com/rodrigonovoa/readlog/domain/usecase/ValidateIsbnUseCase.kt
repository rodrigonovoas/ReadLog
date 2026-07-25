package com.rodrigonovoa.readlog.domain.usecase

import javax.inject.Inject

class ValidateIsbnUseCase @Inject constructor() {

    operator fun invoke(isbn: String): Boolean {
        if (!isbn.matches(Regex("\\d+"))) {
            return false
        }
        return when (isbn.length) {
            13 -> isbn.matches(Regex("(978|979)\\d{10}"))
            10 -> isbn.matches(Regex("\\d{9}[0-9]"))
            else -> false
        }
    }
}
