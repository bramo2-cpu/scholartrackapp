package com.scholartrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scholartrackapp.data.local.ScholarshipEntity
import com.scholartrackapp.data.repository.ScholarshipRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DiscoverFilters(
    val query: String = "",
    val location: String = "All",
    val ageInput: String = "",
    val level: String = "All"
)

class DiscoverViewModel(private val repo: ScholarshipRepository) : ViewModel() {

    private val _filters = MutableStateFlow(DiscoverFilters())
    val filters: StateFlow<DiscoverFilters> = _filters.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val scholarships: StateFlow<List<ScholarshipEntity>> =
        combine(repo.scholarships, _filters) { list, f ->
            val q = f.query.trim().lowercase()
            val age = f.ageInput.trim().toIntOrNull()

            list.filter { s ->
                val matchesQuery = q.isEmpty() ||
                    s.name.lowercase().contains(q) ||
                    s.provider.lowercase().contains(q) ||
                    s.description.lowercase().contains(q) ||
                    s.location.lowercase().contains(q)

                val matchesLocation = f.location == "All" ||
                    s.location.equals(f.location, ignoreCase = true) ||
                    (f.location == "Kenya" && (s.location.contains("Kenya", true) || s.location.contains("Africa", true) || s.location.equals("Global", true)))

                val matchesAge = age == null || (age in s.minAge..s.maxAge)

                val matchesLevel = f.level == "All" ||
                    s.level.equals("All", ignoreCase = true) ||
                    s.level.contains(f.level, ignoreCase = true)

                matchesQuery && matchesLocation && matchesAge && matchesLevel
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun onQueryChange(v: String) = _filters.update { it.copy(query = v) }
    fun onLocationChange(v: String) = _filters.update { it.copy(location = v) }
    fun onAgeChange(v: String) = _filters.update { it.copy(ageInput = v.filter { ch -> ch.isDigit() }.take(2)) }
    fun onLevelChange(v: String) = _filters.update { it.copy(level = v) }
    fun clearFilters() = _filters.value = DiscoverFilters()

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            _error.value = null
            val result = repo.refreshScholarships()
            _refreshing.value = false
            result.onFailure {
                _error.value = "Could not reach web feed. Showing cached scholarships."
            }
        }
    }
}
