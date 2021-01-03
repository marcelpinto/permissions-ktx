package dev.marcelpinto.permissionktx.compose

import androidx.activity.result.launch
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.marcelpinto.permissionktx.PermissionRequest
import dev.marcelpinto.permissionktx.getPermissionStatus
import dev.marcelpinto.permissionktx.observePermissionStatus

@Composable
fun ComposePermissionScreen(
    phoneNumber: MutableState<String>,
    permissionRequest: PermissionRequest,
    onCall: (String) -> Unit
) {
    // Collect the permission status with Compose collectAsState extension
    val callPermission by permissionRequest.name.observePermissionStatus().collectAsState(
        initial = permissionRequest.name.getPermissionStatus()
    )
    var openRational by savedInstanceState { false }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = phoneNumber.value,
            onValueChange = { phoneNumber.value = it },
            label = { Text(text = "Phone number") },
            placeholder = { Text(text = "Who to call today?") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        // If permission is not granted we can show a small hint
        if (!callPermission.isGranted()) {
            Text(text = "Call permission required", color = Color.Magenta, fontSize = 12.sp)
        }
        FloatingActionButton(
            modifier = Modifier.padding(16.dp),
            onClick = {
                // When calling we can use the safe launch to follow the permission flow
                // in case there is a need for further explanation we can open the rational dialog
                // if it was already granted simply invoke the callback
                permissionRequest.safeLaunch(
                    onRequireRational = {
                        openRational = true
                    },
                    onAlreadyGranted = {
                        onCall(phoneNumber.value)
                    }
                )
            },
            content = { Icon(Icons.Rounded.Call) }
        )

        if (openRational) {
            AlertDialog(
                onDismissRequest = {
                    openRational = false
                },
                title = {
                    Text(text = "Permission required to call")
                },
                text = {
                    Text("In order to establish the call we require the \"Call\" permission.\nPlease grant it to continue")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            openRational = false

                            // When the user confirm the rational then launch directly the permission
                            // request with the launch extension
                            permissionRequest.launch()
                        },
                        content = {
                            Text("Continue")
                        }
                    )
                },
                dismissButton = {
                    Button(onClick = { openRational = false }, content = { Text("Not now") })
                }
            )
        }
    }
}
