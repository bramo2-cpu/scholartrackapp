package com.scholartrackapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scholartrackapp.data.local.ScholarshipEntity
import com.scholartrackapp.ui.viewmodel.AppViewModelProvider
import com.scholartrackapp.ui.viewmodel.DiscoverViewModel
import com.scholartrackapp.util.deadlineLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onOpen: (Int) -> Unit,
    vm: DiscoverViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val list by vm.scholarships.collectAsStateWithLifecycle()
    val filters by vm.filters.collectAsStateWithLifecycle()
    val refreshing by vm.refreshing.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()

    val categories = listOf("All", "Scholarship", "Bursary", "Internship", "Attachment")
    val locations = listOf("All", "Kenya", "Africa", "UK", "USA", "Global")
    val levels = listOf("All", "High School", "Undergraduate", "Postgraduate")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Opportunities") },
                actions = {
                    IconButton(onClick = vm::refresh, enabled = !refreshing) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync from web")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filters.query,
                    onValueChange = vm::onQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search scholarships, attachments...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    trailingIcon = {
                        if (filters.query.isNotEmpty()) {
                            IconButton(onClick = { vm.onQueryChange("") }) {
                                Icon(Icons.Default.Clear, "Clear search")
                            }
                        }
                    },
                    singleLine = true
                )

                OutlinedTextField(
                    value = filters.ageInput,
                    onValueChange = vm::onAgeChange,
                    modifier = Modifier.width(84.dp),
                    label = { Text("Age") },
                    placeholder = { Text("21") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Category Filter Chips (Scholarships, Bursaries, Internships, Attachments)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = filters.category == cat,
                        onClick = { vm.onCategoryChange(cat) },
                        label = { Text(if (cat == "All") "All Types" else "${cat}s") }
                    )
                }
            }

            // Location Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                locations.forEach { loc ->
                    FilterChip(
                        selected = filters.location == loc,
                        onClick = { vm.onLocationChange(loc) },
                        label = { Text(if (loc == "All") "All Locations" else loc) }
                    )
                }
            }

            // Education Level Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                levels.forEach { lvl ->
                    FilterChip(
                        selected = filters.level == lvl,
                        onClick = { vm.onLevelChange(lvl) },
                        label = { Text(if (lvl == "All") "All Levels" else lvl) }
                    )
                }
            }

            if (refreshing) LinearProgressIndicator(Modifier.fillMaxWidth())

            when {
                list.isEmpty() && refreshing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                list.isEmpty() -> Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        error ?: "No opportunities match your current filters.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = vm::clearFilters) { Text("Reset Filters") }
                        Button(onClick = vm::refresh) { Text("Sync Web") }
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SuggestionChip(onClick = {}, label = { Text(s.category) })
                Text(deadlineLabel(s.deadline), style = MaterialTheme.typography.labelMedium)
            }
            Text(s.name, style = MaterialTheme.typography.titleMedium)
            Text(
                s.provider,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = {}, label = { Text(s.location) })
                AssistChip(onClick = {}, label = { Text(s.level) })
                AssistChip(onClick = {}, label = { Text("Ages ${s.minAge}-${s.maxAge}") })
            }
            Text(
                s.amount,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
