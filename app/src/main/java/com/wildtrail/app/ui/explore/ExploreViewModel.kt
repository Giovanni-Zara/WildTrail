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
import com.wildtrail.app.domain.model.HikeFilter
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
import kotlin.math.roundToInt

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
    /** Visibility of the expandable filter menu (toggled by the ⋮ button). */
    val isFilterMenuOpen: Boolean = false,
    /** Live values shown by the filter menu's sliders/chips (not yet applied). */
    val draftFilter: HikeFilter = HikeFilter(),
    /** Criteria actually in effect — committed only by "Apply". */
    val appliedFilter: HikeFilter = HikeFilter(),
    val results: List<HikeLog> = emptyList(),
    val featured: List<HikeLog> = emptyList(),
    /** True when a search query or an active filter is in effect: the screen
     *  then shows [results] instead of the [featured] feed. */
    val showingResults: Boolean = false,
    val likedHikeIds: Set<String> = emptySet(),
    val currentUserUid: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModel(
    private val hikeLogRepository: HikeLogRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    // ---- Control state (single source of truth, survives rotation) ------
    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(SortOption.RECENT)
    private val isFilterMenuOpen = MutableStateFlow(false)
    private val draftFilter = MutableStateFlow(HikeFilter())
    private val appliedFilter = MutableStateFlow(HikeFilter())

    /** The filtered/searched results — refreshed only by [refreshResults]. */
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

    /** All control flows merged into one so the public [uiState] combine stays
     *  within combine()'s 5-source typed overload. */
    private data class Controls(
        val query: String,
        val sort: SortOption,
        val isFilterMenuOpen: Boolean,
        val draftFilter: HikeFilter,
        val appliedFilter: HikeFilter,
    )

    private val controls = combine(
        query,
        sort,
        isFilterMenuOpen,
        draftFilter,
        appliedFilter,
    ) { q, s, open, draft, applied -> Controls(q, s, open, draft, applied) }

    val uiState: StateFlow<ExploreUiState> = combine(
        controls,
        enrichedResults,
        featured,
        likedHikeIds,
        currentUidFlow,
    ) { c, r, featuredHikes, liked, uid ->
        ExploreUiState(
            query = c.query,
            sort = c.sort,
            isFilterMenuOpen = c.isFilterMenuOpen,
            draftFilter = c.draftFilter,
            appliedFilter = c.appliedFilter,
            results = sortHikes(r, c.sort),
            featured = sortHikes(featuredHikes, c.sort),
            showingResults = c.query.isNotBlank() || c.appliedFilter.isActive(),
            likedHikeIds = liked,
            currentUserUid = uid,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ExploreUiState(),
    )

    // ---- Events (hoisted from the UI) -----------------------------------

    fun onQueryChanged(q: String) {
        query.value = q
        refreshResults()
    }

    fun onSortChanged(option: SortOption) {
        sort.value = option
    }

    /** The ⋮ button only flips the menu's visibility — never filters. */
    fun onToggleFilterMenu() {
        isFilterMenuOpen.value = !isFilterMenuOpen.value
    }

    fun onDistanceChange(range: ClosedFloatingPointRange<Float>) {
        draftFilter.value = draftFilter.value.copy(
            minKm = range.start,
            maxKm = range.endInclusive,
        )
    }

    fun onElevationChange(range: ClosedFloatingPointRange<Float>) {
        draftFilter.value = draftFilter.value.copy(
            minElevation = range.start.roundToInt(),
            maxElevation = range.endInclusive.roundToInt(),
        )
    }

    fun onDifficultyToggle(level: Int) {
        val current = draftFilter.value.difficulties
        draftFilter.value = draftFilter.value.copy(
            difficulties = if (level in current) current - level else current + level,
        )
    }

    /** Reset the *draft* to defaults — does not itself filter (only Apply does). */
    fun onResetFilters() {
        draftFilter.value = HikeFilter()
    }

    /** The only trigger of the actual filtering: commit the draft and query. */
    fun onApplyFilters() {
        appliedFilter.value = draftFilter.value
        isFilterMenuOpen.value = false
        refreshResults()
    }

    /**
     * Recompute [results] from the current query + applied filter via the
     * repository. When neither is in effect we clear them so the screen falls
     * back to the reactive [featured] feed.
     */
    private fun refreshResults() {
        val q = query.value
        val filter = appliedFilter.value
        if (q.isBlank() && !filter.isActive()) {
            results.value = emptyList()
            return
        }
        viewModelScope.launch {
            results.value = hikeLogRepository.filter(q, filter)
        }
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
