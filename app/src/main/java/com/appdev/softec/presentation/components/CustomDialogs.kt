package com.appdev.softec.presentation.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.appdev.softec.R

/**
 * A dialog that explains why permissions are needed when users deny them
 *
 * @param permissionName The name of the permission that was denied
 * @param rationale Explanation of why the permission is needed
 * @param onDismiss Called when the user dismisses the dialog
 * @param onRequestPermission Called when the user decides to grant the permission
 */
@Composable
fun PermissionRationalDialog(
    permissionName: String,
    rationale: String,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Permission Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"$permissionName\" permission is required",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = rationale,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Not Now")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}

// Extension to use this dialog for common permissions
object PermissionRationalDialogs {

    @Composable
    fun CameraPermissionDialog(
        onDismiss: () -> Unit,
        onRequestPermission: () -> Unit
    ) {
        PermissionRationalDialog(
            permissionName = "Camera",
            rationale = "The camera permission is needed to scan documents and capture text for your tasks. " +
                    "This helps you quickly create tasks from printed information or handwritten notes.",
            onDismiss = onDismiss,
            onRequestPermission = onRequestPermission
        )
    }

    @Composable
    fun MicrophonePermissionDialog(
        onDismiss: () -> Unit,
        onRequestPermission: () -> Unit
    ) {
        PermissionRationalDialog(
            permissionName = "Microphone",
            rationale = "The microphone permission allows you to create tasks using voice input. " +
                    "This makes it easier to add tasks hands-free while you're on the go.",
            onDismiss = onDismiss,
            onRequestPermission = onRequestPermission
        )
    }
}
