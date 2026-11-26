package com.scholartrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scholartrack.app.data.local.ApplicationEntity
import com.scholartrack.app.data.local.ApplicationStatus
import com.scholartrack.app.ui.viewmodel.AppViewModelProvider
import com.scholartrack.app.ui.viewmodel.ApplicationsViewModel
import com.scholartrack.app.util.deadlineLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    vm: ApplicationsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val apps by vm.applications.collectAsStateWithLifecycle()
    var toDelete by remember { mutableStateOf<ApplicationEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My applications") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) { Icon(Icons.Default.Add, "Add application") }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val list = apps
            when {
                list == null -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                list.isEmpty() -> Text(
                    "No applications yet.\nTap + or track a scholarship from Discover.",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    textAlign = TextAlign.Center
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(list, key = { it.id }) { app ->
                        ApplicationCard(app, onEdit = { onEdit(app.id) }, onDelete = { toDelete = app })
                    }
                }
            }
        }
    }

    toDelete?.let { app ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Delete application?") },
            text = { Text("\"${app.title}\" will be removed permanently.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(app); toDelete = null }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ApplicationCard(app: ApplicationEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    val statusLabel = runCatching { ApplicationStatus.valueOf(app.status).label }.getOrDefault(app.status)
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(app.title, style = MaterialTheme.typography.titleMedium)
                Text(app.provider, style = MaterialTheme.typography.bodyMedium)
                Text("Deadline: ${deadlineLabel(app.deadline)}", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(4.dp))
                AssistChip(onClick = {}, label = { Text(statusLabel) })
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete") }
        }
    }
}
