package com.scholartrackapp.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scholartrackapp.data.local.ApplicationStatus
import com.scholartrackapp.ui.viewmodel.AppViewModelProvider
import com.scholartrackapp.ui.viewmodel.ApplicationFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationFormScreen(
    onBack: () -> Unit,
    vm: ApplicationFormViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val categories = listOf("Scholarship", "Bursary", "Internship", "Attachment")

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEdit) "Edit application" else "Track opportunity") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Opportunity Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = state.category == cat,
                        onClick = { vm.onCategoryChange(cat) },
                        label = { Text(cat) }
                    )
                }
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = vm::onTitleChange,
                label = { Text("Opportunity Title *") },
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.provider,
                onValueChange = vm::onProviderChange,
                label = { Text("Company / Sponsor / Provider *") },
                isError = state.providerError != null,
                supportingText = state.providerError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.school,
                onValueChange = vm::onSchoolChange,
                label = { Text("Your School / University *") },
                placeholder = { Text("e.g. Kenyatta University / Strathmore") },
                isError = state.schoolError != null,
                supportingText = state.schoolError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.course,
                onValueChange = vm::onCourseChange,
                label = { Text("Your Course / Program *") },
                placeholder = { Text("e.g. BSc. Software Engineering") },
                isError = state.courseError != null,
                supportingText = state.courseError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.requirements,
                onValueChange = vm::onRequirementsChange,
                label = { Text("Required Documents & Checklist") },
                placeholder = { Text("e.g. School Intro Letter, CV, Transcripts, National ID") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.deadline,
                onValueChange = vm::onDeadlineChange,
                label = { Text("Deadline (yyyy-MM-dd) *") },
                placeholder = { Text("2027-03-31") },
                isError = state.deadlineError != null,
                supportingText = state.deadlineError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Application Status", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ApplicationStatus.entries.forEach { st ->
                    FilterChip(
                        selected = state.status == st,
                        onClick = { vm.onStatusChange(st) },
                        label = { Text(st.label) }
                    )
                }
            }

            OutlinedTextField(
                value = state.notes,
                onValueChange = vm::onNotesChange,
                label = { Text("Additional Notes / Portal Login / Ref No.") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            if (state.formError != null) {
                Text(state.formError!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(if (state.isEdit) "Update Application" else "Save Application")
            }
        }
    }
}
