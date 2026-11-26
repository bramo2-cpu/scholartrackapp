package com.scholartrack.app.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scholartrack.app.data.local.ApplicationStatus
import com.scholartrack.app.ui.viewmodel.AppViewModelProvider
import com.scholartrack.app.ui.viewmodel.ApplicationFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationFormScreen(
    onBack: () -> Unit,
    vm: ApplicationFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(s.saved) { if (s.saved) onBack() }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(if (s.isEdit) "Edit application" else "New application") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            }
        )
    }) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = s.title, onValueChange = vm::onTitleChange,
                label = { Text("Scholarship / application title") },
                isError = s.titleError != null,
                supportingText = { s.titleError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.provider, onValueChange = vm::onProviderChange,
                label = { Text("Provider") },
                isError = s.providerError != null,
                supportingText = { s.providerError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = s.deadline, onValueChange = vm::onDeadlineChange,
                label = { Text("Deadline (yyyy-MM-dd)") },
                isError = s.deadlineError != null,
                supportingText = { s.deadlineError?.let { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Status", style = MaterialTheme.typography.labelLarge)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ApplicationStatus.entries.forEach { st ->
                    FilterChip(
                        selected = s.status == st,
                        onClick = { vm.onStatusChange(st) },
                        label = { Text(st.label) }
                    )
                }
            }

            OutlinedTextField(
                value = s.notes, onValueChange = vm::onNotesChange,
                label = { Text("Notes (documents needed, contacts...)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            s.formError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(onClick = vm::save, modifier = Modifier.fillMaxWidth()) {
                Text(if (s.isEdit) "Save changes" else "Add application")
            }
        }
    }
}
