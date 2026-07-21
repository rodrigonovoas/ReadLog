package com.rodrigonovoa.readlog.domain.usecase

import java.text.Normalizer
import javax.inject.Inject

class GenerateUsernameUseCase @Inject constructor() {
    operator fun invoke(displayName: String?, userId: String): String {
        val slug = displayName
            ?.let { Normalizer.normalize(it, Normalizer.Form.NFD) }
            ?.replace(Regex("\\p{M}"), "")
            ?.lowercase()
            ?.trim()
            ?.replace(Regex("\\s+"), "_")
            ?.replace(Regex("[^a-z0-9_]"), "")
            ?.trim('_')
            .orEmpty()

        return slug.ifEmpty { "user${userId.take(6)}" }
    }
}
