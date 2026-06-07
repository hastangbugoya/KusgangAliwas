package com.example.kusgangaliwas.ui.split

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kusgangaliwas.data.local.entity.SplitTemplateEntity
import com.example.kusgangaliwas.domain.repository.SplitTemplateRepository
import com.example.kusgangaliwas.domain.usecase.split.CreateSplitTemplateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SplitListUiState(
    val splits: List<SplitTemplateEntity> = emptyList(),
    val deletedSplits: List<SplitTemplateEntity> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class SplitListViewModel @Inject constructor(
    private val splitTemplateRepository: SplitTemplateRepository,
    private val createSplitTemplateUseCase: CreateSplitTemplateUseCase,
) : ViewModel() {

    val uiState: StateFlow<SplitListUiState> =
        splitTemplateRepository
            .observeAllSplits()
            .map { allSplits ->
                SplitListUiState(
                    splits = allSplits
                        .filter { split -> split.isActive }
                        .sortedBy { split -> split.name.lowercase() },
                    deletedSplits = allSplits
                        .filterNot { split -> split.isActive }
                        .sortedBy { split -> split.name.lowercase() },
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SplitListUiState(),
            )

    fun createSplit(name: String) {
        viewModelScope.launch {
            runCatching {
                createSplitTemplateUseCase(name = name)
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun deleteSplit(splitId: Long) {
        viewModelScope.launch {
            runCatching {
                splitTemplateRepository.softDeleteSplit(
                    splitId = splitId,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }

    fun restoreSplit(splitId: Long) {
        viewModelScope.launch {
            runCatching {
                splitTemplateRepository.restoreSplit(
                    splitId = splitId,
                    updatedAtEpochMillis = System.currentTimeMillis(),
                )
            }.onFailure { error ->
                error.printStackTrace()
            }
        }
    }
}