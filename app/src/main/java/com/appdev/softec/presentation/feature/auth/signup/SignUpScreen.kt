package com.appdev.softec.presentation.feature.auth.signup


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel = hiltViewModel(),
    navigateToLogin: () -> Unit,
    navigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.updateImageUri(it)
            // Here you could add code to convert URI to ByteArray if needed
            // Example: convertUriToByteArray(context, uri)?.let { bytes -> viewModel.updateImageBytes(bytes) }
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            navigateToHome()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(uiState.errorMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))
//
//                // Profile Image Picker
//                Box(
//                    modifier = Modifier
//                        .size(120.dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.surface)
//                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
//                        .clickable { imagePickerLauncher.launch("image/*") },
//                    contentAlignment = Alignment.Center
//                ) {
//                    if (uiState.imageUri != null) {
//                        GlideImage(
//                            model = uiState.imageUri,
//                            contentDescription = "Profile Image",
//                            contentScale = ContentScale.Crop,
//                            modifier = Modifier.fillMaxSize()
//                        )
//                    } else {
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = "Add Profile Picture",
//                            tint = MaterialTheme.colorScheme.primary,
//                            modifier = Modifier.size(40.dp)
//                        )
//                    }
//                }
//
//                Text(
//                    text = "Add Profile Picture",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.primary,
//                    modifier = Modifier.padding(top = 8.dp)
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))

                // Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Name Field
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { viewModel.updateName(it) },
                            label = { Text("Full Name") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Name Icon"
                                )
                            },
                            isError = uiState.isNameError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Email Field
                        OutlinedTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateEmail(it) },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Email Icon"
                                )
                            },
                            isError = uiState.isEmailError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password Field
                        OutlinedTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.updatePassword(it) },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Password Icon"
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.togglePasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.isPasswordVisible)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Password Visibility"
                                    )
                                }
                            },
                            visualTransformation = if (uiState.isPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            isError = uiState.isPasswordError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirm Password Field
                        OutlinedTextField(
                            value = uiState.confirmPassword,
                            onValueChange = { viewModel.updateConfirmPassword(it) },
                            label = { Text("Confirm Password") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Confirm Password Icon"
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { viewModel.toggleConfirmPasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.isConfirmPasswordVisible)
                                            Icons.Default.Visibility
                                        else
                                            Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Confirm Password Visibility"
                                    )
                                }
                            },
                            visualTransformation = if (uiState.isConfirmPasswordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            isError = uiState.isConfirmPasswordError,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Sign Up Button
                        Button(
                            onClick = { viewModel.signUp() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    "SIGN UP",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Suggestion
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Login",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { navigateToLogin() }
                    )
                }
            }
        }
    }
}