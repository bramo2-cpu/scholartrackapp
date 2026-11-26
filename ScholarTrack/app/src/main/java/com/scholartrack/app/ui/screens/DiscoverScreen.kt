package com.scholartrack.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scholartrack.app.data.local.ScholarshipEntity
import com.scholartrack.app.ui.viewmodel.AppViewModelProvider
import com.scholartrack.app.ui.viewmodel.DiscoverViewModel
import com.scholartrack.app.util.deadlineLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onOpen: (Int) -> Unit,
    vm: DiscoverViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val list by vm.scholarships.collectAsStateWithLifecycle()
    val status by vm.status.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("Discover scholarships") }) }) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = vm::onQueryChange,
                label = { Text("Search by name or provider") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            status.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }
            when {
                list.isEmpty() && status.isLoading -> CenteredBox { CircularProgressIndicator() }          // loading state
                list.isEmpty() -> CenteredBox {                                                             // empty / error state
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No scholarships to show.", textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = vm::refresh) { Text("Retry") }
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(list, key = { it.id }) { s -> ScholarshipCard(s) { onOpen(s.id) } }
                }
            }
        }
    }
}

@Composable
private fun ScholarshipCard(s: ScholarshipEntity, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(s.name, style = MaterialTheme.typography.titleMedium)
            Text(s.provider, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text("Amount: ${s.amount}", style = MaterialTheme.typography.bodySmall)
            Text("Deadline: ${deadlineLabel(s.deadline)}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
