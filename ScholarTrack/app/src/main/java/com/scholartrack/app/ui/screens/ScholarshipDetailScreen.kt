package com.scholartrack.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scholartrack.app.ui.viewmodel.AppViewModelProvider
import com.scholartrack.app.ui.viewmodel.ScholarshipDetailViewModel
import com.scholartrack.app.util.deadlineLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarshipDetailScreen(
    onBack: () -> Unit,
    onTrack: (Int) -> Unit,
    vm: ScholarshipDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val s by vm.scholarship.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Scholarship details") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            }
        )
    }) { padding ->
        val item = s
        if (item == null) {
            Box(Modifier.padding(padding)) { CenteredBox { CircularProgressIndicator() } }
        } else {
            Column(
                Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(item.name, style = MaterialTheme.typography.headlineSmall)
                Text(item.provider, style = MaterialTheme.typography.titleMedium)
                Text("Amount: ${item.amount}")
                Text("Deadline: ${deadlineLabel(item.deadline)}")
                Text(item.description)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { onTrack(item.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Track this scholarship")
                }
                if (item.link.isNotBlank()) {
                    OutlinedButton(onClick = { uriHandler.openUri(item.link) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Open website")
                    }
                }
            }
        }
    }
}
