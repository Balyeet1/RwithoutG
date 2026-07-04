package com.example.rewardwithoutguilt.more

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rewardwithoutguilt.R
import com.example.rewardwithoutguilt.components.GreetingImageDisplay
import com.example.rewardwithoutguilt.data.GreetingImage
import com.example.rewardwithoutguilt.data.GreetingPreferences
import com.example.rewardwithoutguilt.features.GreetingScreen
import com.example.rewardwithoutguilt.ui.theme.RewardWithoutGuiltTheme
import com.example.rewardwithoutguilt.util.Constants
import com.example.rewardwithoutguilt.util.FileUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGreetingScreen(onSaveSuccess: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { GreetingPreferences(context) }

    val savedGreetingText by prefs.greetingText.collectAsState(initial = null)
    val savedImage by prefs.greetingImage.collectAsState(initial = GreetingImage())

    var greetingText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var imageSize by remember { mutableFloatStateOf(Constants.DEFAULT_IMAGE_SIZE) }
    var showPreview by remember { mutableStateOf(false) }
    var showSlider by remember { mutableStateOf(false) }

    var imageVersion by remember { mutableLongStateOf(0L) }

    val charCount = greetingText.length
    val isTextValid = charCount <= Constants.MAX_GREETING_CHARS

    LaunchedEffect(savedGreetingText) {
        if (savedGreetingText != null && greetingText.isEmpty()) {
            greetingText = savedGreetingText!!
        }
    }

    LaunchedEffect(savedImage) {
        if (savedImage.uri != null && selectedImageUri == null) {
            selectedImageUri = savedImage.uri
        }
        imageSize = savedImage.size
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val localPath = FileUtils.copyUriToInternalStorage(context, uri)
                if (localPath != null) {
                    selectedImageUri = localPath
                    imageVersion = System.currentTimeMillis()
                }
            }
        }
    )

    if (savedGreetingText == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("") },
                    actions = {
                        TextButton(onClick = { showPreview = true }) {
                            Text(
                                stringResource(R.string.preview_greeting),
                                color = if (isTextValid) MaterialTheme.colorScheme.primary else Color.Gray,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        TextButton(
                            onClick = {
                                if (isTextValid) {
                                    scope.launch {
                                        prefs.saveGreeting(
                                            greetingText,
                                            GreetingImage(selectedImageUri, imageSize)
                                        )
                                        onSaveSuccess()
                                    }
                                }
                            },
                            enabled = isTextValid
                        ) {
                            Text(
                                stringResource(R.string.save),
                                color = if (isTextValid) MaterialTheme.colorScheme.primary else Color.Gray,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                GreetingImageDisplay(
                    image = GreetingImage(selectedImageUri, imageSize),
                    version = imageVersion,
                    contentScale = ContentScale.Crop
                )

                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = stringResource(R.string.change_image),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showSlider = !showSlider }) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = stringResource(R.string.resize_image),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                if (showSlider) {
                    Text(text = stringResource(R.string.image_size), style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = imageSize,
                        onValueChange = { imageSize = it },
                        valueRange = Constants.MIN_IMAGE_SIZE..Constants.MAX_IMAGE_SIZE,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = greetingText,
                    onValueChange = { newValue ->
                        if (newValue.length <= Constants.MAX_GREETING_CHARS) {
                            greetingText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.greeting_text_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Text(
                            text = stringResource(
                                R.string.character_count,
                                charCount,
                                Constants.MAX_GREETING_CHARS
                            ),
                            color = if (!isTextValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    isError = !isTextValid
                )
            }
        }
    }

    if (showPreview) {
        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    GreetingScreen(
                        overrideText = greetingText,
                        overrideImage = GreetingImage(selectedImageUri, imageSize),
                        overrideImageVersion = imageVersion,
                        onTimeout = {}
                    )
                    Button(
                        onClick = { showPreview = false },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    ) {
                        Text(stringResource(R.string.close_preview))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditGreetingScreenPreview() {
    RewardWithoutGuiltTheme {
        EditGreetingScreen()
    }
}
