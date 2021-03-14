/*
 * Copyright 2020 Marcel Pinto Biescas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.marcelpinto.permissionktx.PermissionLauncher

@Composable
fun ComposePermissionScreen(
    phoneNumber: MutableState<String>,
    permissionLauncher: PermissionLauncher,
    onCall: (String) -> Unit
) {
    // Collect the permission status with Compose collectAsState extension
    val callPermission by permissionLauncher.type.statusFlow.collectAsState(
        initial = permissionLauncher.type.status
    )
    var openRational by rememberSaveable { mutableStateOf(false) }

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
                permissionLauncher.safeLaunch(
                    onRequireRational = {
                        openRational = true
                    },
                    onAlreadyGranted = {
                        onCall(phoneNumber.value)
                    }
                )
            },
            content = { Icon(imageVector = Icons.Rounded.Call, contentDescription = "Call icon") }
        )

        if (openRational) {
            AlertDialog(
                onDismissRequest = {
                    openRational = false
                },
                title = {
                    Text(text = "PermissionProvider required to call")
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
                            permissionLauncher.launch()
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
