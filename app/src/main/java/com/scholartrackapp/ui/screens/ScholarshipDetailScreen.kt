package com.scholartrackapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scholartrackapp.ui.viewmodel.AppViewModelProvider
import com.scholartrackapp.ui.viewmodel.ScholarshipDetailViewModel
import com.scholartrackapp.util.deadlineLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScholarshipDetailScreen(
    onBack: () -> Unit,
    onTrack: (Int) -> Unit,
    vm: ScholarshipDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val scholarship by vm.scholarship.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scholarship details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val s = scholarship
        if (s == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(s.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    s.provider,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text(s.location) })
                    AssistChip(onClick = {}, label = { Text(s.level) })
                    AssistChip(onClick = {}, label = { Text("Ages ${s.minAge}-${s.maxAge}") })
                }

                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Award: ${s.amount}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        Text("Deadline: ${s.deadline} (${deadlineLabel(s.deadline)})", style = MaterialTheme.typography.bodyMedium)
                        if (s.link.isNotBlank()) {
                            Text(
                                text = "Portal: ${s.link}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Text("About this opportunity", style = MaterialTheme.typography.titleMedium)
                Text(
                    s.description.ifBlank { "No additional description provided." },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { onTrack(s.id) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Track this scholarship")
                }

                if (s.link.isNotBlank()) {
                    OutlinedButton(
                        onClick = {
                            val rawUrl = s.link.trim()
                            val formattedUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                                rawUrl
                            } else {
                                "https://$rawUrl"
                            }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Unable to open browser for $formattedUrl", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open official website")
                    }
                }
            }
        }
    }
}
