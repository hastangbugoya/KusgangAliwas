package com.example.kusgangaliwas.ui.split

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kusgangaliwas.R
import com.example.kusgangaliwas.ui.theme.KaPalette

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

    val filteredSplits = remember(uiState.splits, localSearchQuery) {
        val query = localSearchQuery.trim()

        if (query.isBlank()) {
            uiState.splits
        } else {
            uiState.splits.filter { split ->
                split.name.contains(query, ignoreCase = true) ||
                        split.notes?.contains(query, ignoreCase = true) == true
            }
        }
    }

    if (showAddSplitSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showAddSplitSheet = false
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Splits",
                        fontWeight = FontWeight.Bold,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            showAddSplitSheet = true
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.plus),
                            contentDescription = "Add split",
                            tint = KaPalette.Amber,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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

            KaRootSectionCard(
                title = "Saved splits",
                count = filteredSplits.size,
                accentColor = KaPalette.Amber,
                modifier = Modifier.weight(1f),
            ) {
                if (uiState.splits.isEmpty()) {
                    EmptySplitState(
                        onAddClick = {
                            showAddSplitSheet = true
                        },
                    )
                } else if (filteredSplits.isEmpty()) {
                    Text(
                        text = "No splits match your search.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = filteredSplits,
                            key = { it.id },
                        ) { split ->
                            SplitRowCard(
                                name = split.name,
                                notes = split.notes,
                                onClick = {
                                    onSplitClick(split.id)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KaRootSectionCard(
    title: String,
    count: Int? = null,
    accentColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = accentColor,
                    ),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Spacer(
                        modifier = Modifier
                            .width(5.dp)
                            .padding(vertical = 18.dp),
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )

                count?.let {
                    Text(
                        text = it.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun SplitRowCard(
    name: String,
    notes: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (!notes.isNullOrBlank()) {
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EmptySplitState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "No splits yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.plus),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Add split")
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Add split",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Create a saved workout template you can schedule, start, or reuse later.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        OutlinedTextField(
            value = splitName,
            onValueChange = onSplitNameChange,
            label = {
                Text("Split name")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Icon(
                painter = painterResource(R.drawable.plus),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Save split")
        }
    }
}
