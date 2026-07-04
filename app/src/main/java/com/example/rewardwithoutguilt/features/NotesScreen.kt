package com.example.rewardwithoutguilt.features

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.data.Note
import com.example.rewardwithoutguilt.data.NotePreferences
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(onAddNote: (String?) -> Unit, onEditNote: (String) -> Unit) {
    val context = LocalContext.current
    val notePrefs = remember { NotePreferences(context) }
    val notesOpt by notePrefs.notes.collectAsState(initial = null)
    val persistentCategory by notePrefs.selectedCategory.collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    NotesScreenContent(
        notes = notesOpt ?: emptyList(),
        persistentCategory = persistentCategory,
        onAddNote = onAddNote,
        onEditNote = onEditNote,
        onCategoryChanged = { scope.launch { notePrefs.updateSelectedCategory(it) } },
        onDeleteNote = { scope.launch { notePrefs.deleteNote(it) } },
        isLoading = notesOpt == null
    )
}

@Composable
fun NotesScreenContent(
    notes: List<Note>, persistentCategory: String?, onAddNote: (String?) -> Unit,
    onEditNote: (String) -> Unit, onCategoryChanged: (String) -> Unit,
    onDeleteNote: (String) -> Unit, isLoading: Boolean = false
) {
    var selectedNoteId by remember { mutableStateOf<String?>(null) }
    val allNotesLabel = stringResource(R.string.all_notes)
    val catPersonal = stringResource(R.string.note_category_personal)
    val catWork = stringResource(R.string.note_category_work)
    val catIdeas = stringResource(R.string.note_category_ideas)
    val catOthers = stringResource(R.string.note_category_others)
    val categories = remember(allNotesLabel, catPersonal, catWork, catIdeas, catOthers) {
        listOf(allNotesLabel, catPersonal, catWork, catIdeas, catOthers)
    }
    val selectedCategory = persistentCategory ?: allNotesLabel
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(persistentCategory) {
        persistentCategory?.let { cat ->
            val index = categories.indexOf(cat)
            if (index != -1) listState.scrollToItem((index - 1).coerceAtLeast(0))
        }
    }

    val filteredNotes = remember(notes, selectedCategory, allNotesLabel) {
        if (selectedCategory == allNotesLabel) notes else notes.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize().clickable(
                enabled = selectedNoteId != null,
                onClick = { selectedNoteId = null },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyRow(
                    state = listState,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(categories) { index, category ->
                        val isSelected = category == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onCategoryChanged(category)
                                scope.launch { listState.animateScrollToItem((index - 1).coerceAtLeast(0)) }
                            },
                            label = { Text(category) },
                            leadingIcon = { Icon(if (category == allNotesLabel) Icons.Default.Home else Icons.Default.Folder, null, modifier = Modifier.size(18.dp)) },
                            shape = RoundedCornerShape(100.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = null
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                        Text(stringResource(R.string.nav_notes), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        AnimatedContent(
                            targetState = selectedCategory,
                            transitionSpec = { fadeIn(animationSpec = tween(220, delayMillis = 90)) togetherWith fadeOut(animationSpec = tween(90)) },
                            label = "CategoryBreadcrumb"
                        ) { target -> Text(target, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) }
                    }

                    if (isLoading) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        AnimatedContent(
                            targetState = filteredNotes,
                            transitionSpec = { fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) },
                            modifier = Modifier.fillMaxSize(),
                            label = "NotesGridAnimation"
                        ) { current ->
                            if (current.isEmpty()) {
                                Box(Modifier.fillMaxSize(), Alignment.Center) {
                                    Text(if (selectedCategory == allNotesLabel) stringResource(R.string.no_notes_yet) else stringResource(R.string.no_notes_in_category), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)) {
                                    items(current, key = { it.id }) { note ->
                                        NoteItem(note = note, isSelected = selectedNoteId == note.id, onClick = { if (selectedNoteId != null) selectedNoteId = if (selectedNoteId == note.id) null else note.id else onEditNote(note.id) }, onLongClick = { selectedNoteId = note.id }, onDelete = { onDeleteNote(note.id); selectedNoteId = null })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedNoteId == null) {
            FloatingActionButton(
                onClick = { onAddNote(if (selectedCategory == allNotesLabel) null else selectedCategory) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
            ) { Icon(Icons.Default.Add, stringResource(R.string.add_note_description)) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(note: Note, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp).combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Text(note.title.ifEmpty { stringResource(R.string.untitled_note) }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                if (isSelected) IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Delete, stringResource(R.string.delete_description), tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(8.dp))
            Text(note.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Surface(shape = RoundedCornerShape(100.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), modifier = Modifier.wrapContentSize()) {
                Text(note.category.uppercase(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    RewardWithoutGuiltTheme {
        NotesScreenContent(
            notes = listOf(
                Note("1", "Drink Water", "Remember to drink 2L water today", "PERSONAL"),
                Note("2", "App Idea", "Build a productivity app with rewards", "IDEAS")
            ),
            persistentCategory = null, onAddNote = {}, onEditNote = {}, onCategoryChanged = {}, onDeleteNote = {}
        )
    }
}
