package com.scholartrackapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scholartrackapp.data.local.ApplicationEntity
import com.scholartrackapp.data.repository.ScholarshipRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ApplicationsViewModel(private val repo: ScholarshipRepository) : ViewModel() {

    /** null = still loading, empty list = empty state. */
    val applications: StateFlow<List<ApplicationEntity>?> =
        repo.applications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun delete(app: ApplicationEntity) {
        viewModelScope.launch { repo.deleteApplication(app) }
    }
}

