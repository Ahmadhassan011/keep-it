@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notes.ui.theme.NotesTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotesTheme {
                SplashScreen()
            }
        }
    }
}

@Composable
fun SplashScreen() {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(1500)),
        exit = fadeOut(animationSpec = tween(1500))
    ) {
        NoteTakingApp()
    }
}

@Composable
fun NoteTakingApp() {
    var notes by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var showDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backgroundColor = Color(0xFF212121) // Dark Gray Background
    val textColor = Color.White
    val primaryColor = Color(0xFFFFA000) // Orange accent color

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp)
                    .background(backgroundColor)
                    .padding(16.dp)
            ) {
                Text("Keep Notes", fontSize = 24.sp, color = textColor, modifier = Modifier.padding(8.dp))
                Divider(color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                DrawerItem("Home", Icons.Default.Home, onClick = { scope.launch { drawerState.close() } }, textColor)
                DrawerItem("About", Icons.Default.Info, onClick = { scope.launch { drawerState.close() } }, textColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text("A simple note-taking app inspired by Google Keep. Easily add, delete, and manage your notes.", color = textColor)
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text("Keep Notes", color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = textColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = primaryColor
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note", tint = Color.White)
                }
            },
            containerColor = backgroundColor
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .background(backgroundColor)
            ) {
                if (notes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No Notes Available", color = Color.Gray)
                    }
                } else {
                    Column {
                        notes.forEachIndexed { index, (title, content) ->
                            NoteCard(title, content, index, primaryColor) { notes = notes - notes[index] }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddNoteDialog(
            onDismiss = { showDialog = false },
            onSave = { title, content ->
                if (title.isNotBlank() || content.isNotBlank()) {
                    notes = notes + (title to content)
                }
                showDialog = false
            },
            primaryColor = primaryColor
        )
    }
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit, primaryColor: Color) {
    var title by remember { mutableStateOf(TextFieldValue()) }
    var content by remember { mutableStateOf(TextFieldValue()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(title.text, content.text) }, colors = ButtonDefaults.textButtonColors(contentColor = primaryColor)) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("New Note", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

@Composable
fun NoteCard(title: String, content: String, index: Int, primaryColor: Color, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable { },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (index % 2 == 0) primaryColor else Color(0xFF64B5F6))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = content, fontSize = 14.sp, color = Color.Black)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Note", tint = Color.Black)
            }
        }
    }
}

@Composable
fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit, textColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = label, tint = textColor, modifier = Modifier.padding(end = 12.dp))
        Text(label, fontSize = 16.sp, color = textColor)
    }
}

@Preview(showBackground = true)
@Composable
fun NoteTakingAppPreview() {
    NotesTheme {
        NoteTakingApp()
    }
}
