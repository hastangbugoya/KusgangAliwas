package com.example.kusgangaliwas.ui.split

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.kusgangaliwas.ui.common.KusgangTopBar
import com.example.kusgangaliwas.ui.common.SectionHeader
import com.example.kusgangaliwas.ui.common.SharpCard
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBar



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitListScreen(
    uiState: SplitListUiState,
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onCreateSplit: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSplitClick: (Long) -> Unit,
) {
    var newSplitName by remember { mutableStateOf("") }
    var localSearchQuery by remember { mutableStateOf("") }
    var showAddSplitSheet by remember { mutableStateOf(false) }

    if (showAddSplitSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSplitSheet = false
            },
        ) {
            AddSplitSheetContent(
                splitName = newSplitName,
                onSplitNameChange = {
                    newSplitName = it
                },
                onSave = {
                    val cleaned = newSplitName
                        .trim()
                        .replaceFirstChar { character ->
                            character.uppercase()
                        }

                    if (cleaned.isNotBlank()) {
                        onCreateSplit(cleaned)

                        newSplitName = ""
                        showAddSplitSheet = false
                    }
                },
            )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text("Splits")
                },
                actions = {
                    IconButton(
                        onClick = {
                            showAddSplitSheet = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add split",
                        )
                    }

                    IconButton(
                        onClick = onOverflowClick,
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = localSearchQuery,
                onValueChange = {
                    localSearchQuery = it
                },
                label = {
                    Text("Search splits")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (uiState.splits.isEmpty()) {
                    item {
                        SharpCard {
                            Text("No splits yet.")
                        }
                    }
                } else {
                    items(
                        items = uiState.splits.filter { split ->
                            if (localSearchQuery.isBlank()) {
                                true
                            } else {
                                split.name.contains(
                                    localSearchQuery.trim(),
                                    ignoreCase = true,
                                )
                            }
                        },
                        key = { it.id },
                    ) { split ->
                        SharpCard(
                            modifier = Modifier.clickable {
                                onSplitClick(split.id)
                            },
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = split.name)

                                if (!split.notes.isNullOrBlank()) {
                                    Text(text = split.notes)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddSplitSheetContent(
    splitName: String,
    onSplitNameChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Add split",
        )

        OutlinedTextField(
            value = splitName,
            onValueChange = onSplitNameChange,
            label = {
                Text("Split name")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        androidx.compose.material3.OutlinedButton(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save split")
        }
    }
}