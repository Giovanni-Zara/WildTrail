package com.wildtrail.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wildtrail.app.WildTrailApp
import com.wildtrail.app.data.repository.AuthRepository
import com.wildtrail.app.data.repository.AuthState
import com.wildtrail.app.data.repository.HikeLogRepository
import com.wildtrail.app.domain.model.HikeLog
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExploreUiState(
    val query: String = "",
    val results: List<HikeLog> = emptyList(),
    val featured: List<HikeLog> = emptyList(),
    val likedHikeIds: Set<String> = emptySet(),
    val currentUserUid: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModel(
    private val hikeLogRepository: HikeLogRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val results = MutableStateFlow<List<HikeLog>>(emptyList())

    private val currentUidFlow = authRepository.authState
        .map { (it as? AuthState.SignedIn)?.user?.firebaseUid }

    private val likedHikeIds = currentUidFlow.flatMapLatest { uid ->
        if (uid == null) flowOf(emptySet()) else hikeLogRepository.observeMyLikedHikeIds(uid)
    }

    val uiState: StateFlow<ExploreUiState> = combine(
        query,
        results,
        hikeLogRepository.observePublicFeed(20),
        likedHikeIds,
        currentUidFlow,
    ) { q, r, featured, liked, uid ->
        ExploreUiState(
            query = q,
            results = r,
            featured = featured,
            likedHikeIds = liked,
            currentUserUid = uid,
        )
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

    suspend fun refresh() {
        runCatching { hikeLogRepository.refresh() }
    }

    fun toggleLike(hike: HikeLog) {
        val uid = uiState.value.currentUserUid ?: return
        viewModelScope.launch {
            runCatching {
                hikeLogRepository.setLiked(uid, hike.hikeId, hike.hikeId !in uiState.value.likedHikeIds)
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WildTrailApp)
                ExploreViewModel(
                    hikeLogRepository = app.container.hikeLogRepository,
                    authRepository = app.container.authRepository,
                )
            }
        }
    }
}
