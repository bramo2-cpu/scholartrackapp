package com.scholartrackapp.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.scholartrackapp.ScholarTrackApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { AuthViewModel(app().container.repository) }
        initializer { DiscoverViewModel(app().container.repository) }
        initializer { ApplicationsViewModel(app().container.repository) }
        initializer { ScholarshipDetailViewModel(createSavedStateHandle(), app().container.repository) }
        initializer { ApplicationFormViewModel(createSavedStateHandle(), app().container.repository) }
    }
}

private fun CreationExtras.app(): ScholarTrackApp = this[APPLICATION_KEY] as ScholarTrackApp
