package com.wildtrail.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.domain.model.HikeLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val query: String = "",
    val results: List<HikeLog> = emptyList(),
    val featured: List<HikeLog> = emptyList(),
)

class ExploreViewModel(
    private val hikeLogRepository: HikeLogRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val results = MutableStateFlow<List<HikeLog>>(emptyList())

    val uiState: StateFlow<ExploreUiState> = combine(
        query,
        results,
        hikeLogRepository.observePublicFeed(20),
    ) { q, r, featured ->
        ExploreUiState(query = q, results = r, featured = featured)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ExploreUiState(),
    )

    fun onQueryChanged(q: String) {
        query.value = q
        if (q.isBlank()) {
            results.value = emptyList()
            return
        }
        viewModelScope.launch {
            results.value = hikeLogRepository.search(q)
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WildTrailApp)
                ExploreViewModel(
                    hikeLogRepository = app.container.hikeLogRepository,
                )
            }
        }
    }
}
