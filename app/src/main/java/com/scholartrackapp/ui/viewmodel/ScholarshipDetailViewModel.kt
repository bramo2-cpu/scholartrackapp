package com.scholartrackapp.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scholartrackapp.data.local.ScholarshipEntity
import com.scholartrackapp.data.repository.ScholarshipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScholarshipDetailViewModel(
    handle: SavedStateHandle,
    repo: ScholarshipRepository
) : ViewModel() {

    private val id: Int = checkNotNull(handle["id"]) // navigation argument

    private val _scholarship = MutableStateFlow<ScholarshipEntity?>(null)
    val scholarship: StateFlow<ScholarshipEntity?> = _scholarship

    init {
        viewModelScope.launch { _scholarship.value = repo.getScholarship(id) }
    }
}

