package com.scholartrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scholartrackapp.data.local.ScholarshipEntity
import com.scholartrackapp.data.repository.ScholarshipRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DiscoverStatus(val isLoading: Boolean = false, val error: String? = null)

class DiscoverViewModel(private val repo: ScholarshipRepository) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _status = MutableStateFlow(DiscoverStatus())
    val status = _status.asStateFlow()

    /** Room list filtered by the search text. */
    val scholarships: StateFlow<List<ScholarshipEntity>> =
        combine(repo.scholarships, _query) { list, q ->
            if (q.isBlank()) list
            else list.filter { it.name.contains(q, true) || it.provider.contains(q, true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { refresh() }

    fun onQueryChange(value: String) { _query.value = value }

    fun refresh() {
        viewModelScope.launch {
            _status.value = DiscoverStatus(isLoading = true)
            val result = repo.refreshScholarships()
            _status.value = DiscoverStatus(
                isLoading = false,
                error = if (result.isFailure) "Couldn't refresh. Check your connection. Showing saved data." else null
            )
        }
    }
}

