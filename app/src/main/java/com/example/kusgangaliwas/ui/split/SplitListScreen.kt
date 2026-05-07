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

@Composable
fun SplitListRoute(
    onBackClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onSplitClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplitListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SplitListScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onOverflowClick = onOverflowClick,
        onCreateSplit = viewModel::createSplit,
        onSplitClick = onSplitClick,
        modifier = modifier,
    )
}

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

    Scaffold(
        modifier = modifier,
        topBar = {
            KusgangTopBar(
                title = "Splits",
                onBackClick = onBackClick,
                onOverflowClick = onOverflowClick,
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
            SharpCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Add split")

                    OutlinedTextField(
                        value = newSplitName,
                        onValueChange = { newSplitName = it },
                        label = { Text("Split name") },
                        singleLine = true,
                    )

                    OutlinedButton(
                        onClick = {
                            val cleaned = newSplitName.trim()
                            if (cleaned.isNotBlank()) {
                                onCreateSplit(cleaned)
                                newSplitName = ""
                            }
                        },
                    ) {
                        Text("Add")
                    }
                }
            }

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
                        items = uiState.splits,
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