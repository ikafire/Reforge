package io.github.ikafire.reforge.feature.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.ikafire.reforge.core.domain.model.TemplateFolder

@Composable
fun TemplateListSection(
    uiState: TemplateListUiState,
    onTemplateClick: (String) -> Unit,
    onStartFromTemplate: (String) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onDuplicateTemplate: (String) -> Unit,
    onCreateTemplate: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
) {
    var showCreateTemplateDialog by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Templates", style = MaterialTheme.typography.titleMedium)
            Row {
                TextButton(onClick = { showCreateFolderDialog = true }) {
                    Text("New Folder")
                }
                TextButton(onClick = { showCreateTemplateDialog = true }) {
                    Text("New Template")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Folders with templates
        uiState.folders.forEach { folder ->
            FolderSection(
                folder = folder,
                templates = uiState.templatesByFolder[folder.id] ?: emptyList(),
                onTemplateClick = onTemplateClick,
                onStartFromTemplate = onStartFromTemplate,
                onDeleteTemplate = onDeleteTemplate,
                onDuplicateTemplate = onDuplicateTemplate,
                onDeleteFolder = onDeleteFolder,
            )
        }

        // Unfiled templates
        val unfiled = uiState.templatesByFolder[null] ?: emptyList()
        if (unfiled.isNotEmpty()) {
            Text(
                text = "Unfiled",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 4.dp),
            )
            unfiled.forEach { twe ->
                TemplateCard(
                    template = twe,
                    onTemplateClick = onTemplateClick,
                    onStart = onStartFromTemplate,
                    onDelete = onDeleteTemplate,
                    onDuplicate = onDuplicateTemplate,
                )
            }
        }

        if (uiState.templatesByFolder.values.flatten().isEmpty()) {
            Text(
                text = "No templates yet. Create one to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }

    if (showCreateTemplateDialog) {
        CreateNameDialog(
            title = "Create Template",
            label = "Template name",
            onConfirm = {
                onCreateTemplate(it)
                showCreateTemplateDialog = false
            },
            onDismiss = { showCreateTemplateDialog = false },
        )
    }

    if (showCreateFolderDialog) {
        CreateNameDialog(
            title = "Create Folder",
            label = "Folder name",
            onConfirm = {
                onCreateFolder(it)
                showCreateFolderDialog = false
            },
            onDismiss = { showCreateFolderDialog = false },
        )
    }
}

@Composable
private fun FolderSection(
    folder: TemplateFolder,
    templates: List<TemplateWithExercises>,
    onTemplateClick: (String) -> Unit,
    onStartFromTemplate: (String) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onDuplicateTemplate: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = folder.name,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("Delete Folder") },
                onClick = {
                    onDeleteFolder(folder.id)
                    showMenu = false
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
            )
        }
    }

    templates.forEach { twe ->
        TemplateCard(
            template = twe,
            onTemplateClick = onTemplateClick,
            onStart = onStartFromTemplate,
            onDelete = onDeleteTemplate,
            onDuplicate = onDuplicateTemplate,
            modifier = Modifier.padding(start = 24.dp),
        )
    }
}

@Composable
private fun TemplateCard(
    template: TemplateWithExercises,
    onTemplateClick: (String) -> Unit,
    onStart: (String) -> Unit,
    onDelete: (String) -> Unit,
    onDuplicate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onTemplateClick(template.template.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(template.template.name, style = MaterialTheme.typography.titleSmall)
                val exerciseNames = template.exercises.take(3).mapNotNull { it.second?.name }
                if (exerciseNames.isNotEmpty()) {
                    Text(
                        text = exerciseNames.joinToString(", ") + if (template.exercises.size > 3) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            TextButton(onClick = { onStart(template.template.id) }) {
                Text("Start")
            }
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Duplicate") },
                    onClick = {
                        onDuplicate(template.template.id)
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        onDelete(template.template.id)
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun CreateNameDialog(
    title: String,
    label: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
