package com.example.kusgangaliwas.domain.usecase.split

import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository

/**
 * Creates a reusable split template.
 *
 * A split defines WHAT the session roadmap suggests, not WHEN it happens.
 */
class CreateSplitTemplateUseCase(
    private val splitTemplateRepository: SplitTemplateRepository,
) {

    suspend operator fun invoke(
        name: String,
        notes: String? = null,
        suggestedDaysBeforeRepeat: Int? = null,
        nowEpochMillis: Long = System.currentTimeMillis(),
    ): Long {
        val cleanedName = name.trim()

        require(cleanedName.isNotBlank()) {
            "Split name cannot be blank."
        }

        require(suggestedDaysBeforeRepeat == null || suggestedDaysBeforeRepeat >= 0) {
            "Suggested days before repeat cannot be negative."
        }

        return splitTemplateRepository.insertSplit(
            SplitTemplateEntity(
                name = cleanedName,
                notes = notes?.trim()?.takeIf { it.isNotBlank() },
                suggestedDaysBeforeRepeat = suggestedDaysBeforeRepeat,
                isActive = true,
                createdAtEpochMillis = nowEpochMillis,
                updatedAtEpochMillis = nowEpochMillis,
            )
        )
    }
}