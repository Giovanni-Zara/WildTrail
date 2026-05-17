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
import com.wildtrail.app.data.repository.UserRepository
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

/** Sort orders offered by the chips under the Explore search bar. */
enum class SortOption(val label: String) {
    RECENT("Most recent"),
    MOST_LIKED("Most liked"),
    TOP_RATED("Top rated"),
    LONGEST("Longest"),
}

data class ExploreUiState(
    val query: String = "",
    val sort: SortOption = SortOption.RECENT,
    val results: List<HikeLog> = emptyList(),
    val featured: List<HikeLog> = emptyList(),
    val likedHikeIds: Set<String> = emptySet(),
    val currentUserUid: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModel(
    private val hikeLogRepository: HikeLogRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(SortOption.RECENT)
    private val results = MutableStateFlow<List<HikeLog>>(emptyList())

    // Splice each creator's live profile picture onto the cards so previews
    // show the same avatar the hike-detail screen does.
    private val featured = userRepository.withLiveCreatorPictures(
        hikeLogRepository.observePublicFeed(20),
    )
    private val enrichedResults = userRepository.withLiveCreatorPictures(results)

    private val currentUidFlow = authRepository.authState
        .map { (it as? AuthState.SignedIn)?.user?.firebaseUid }

    private val likedHikeIds = currentUidFlow.flatMapLatest { uid ->
        if (uid == null) flowOf(emptySet()) else hikeLogRepository.observeMyLikedHikeIds(uid)
    }

    // query + sort merged so the whole combine stays at 5 typed sources.
    private val controls = combine(query, sort) { q, s -> q to s }

    val uiState: StateFlow<ExploreUiState> = combine(
        controls,
        enrichedResults,
        featured,
        likedHikeIds,
        currentUidFlow,
    ) { (q, s), r, featuredHikes, liked, uid ->
        ExploreUiState(
            query = q,
            sort = s,
            results = sortHikes(r, s),
            featured = sortHikes(featuredHikes, s),
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

    fun onSortChanged(option: SortOption) {
        sort.value = option
    }

    private fun sortHikes(list: List<HikeLog>, sort: SortOption): List<HikeLog> = when (sort) {
        SortOption.RECENT -> list.sortedByDescending { it.endedAt }
        SortOption.MOST_LIKED -> list.sortedByDescending { it.likesCount }
        SortOption.TOP_RATED -> list.sortedWith(
            compareByDescending<HikeLog> { it.averageRating }.thenByDescending { it.reviewCount },
        )
        SortOption.LONGEST -> list.sortedByDescending { it.lengthKm }
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
                    userRepository = app.container.userRepository,
                )
            }
        }
    }
}
