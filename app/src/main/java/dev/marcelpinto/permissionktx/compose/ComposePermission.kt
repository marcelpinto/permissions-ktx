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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.PermissionStatus
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private fun startCall(context: Context, phone: String) {
    val phoneCallUri = Uri.parse("tel:$phone")
    val phoneCallIntent = Intent(Intent.ACTION_CALL).also {
        it.data = phoneCallUri
    }
    context.startActivity(phoneCallIntent)
}

@Composable
fun ComposePermissionScreen() {
    val permissions = listOf(
        Permission(Manifest.permission.CALL_PHONE),
        Permission(Manifest.permission.ACCESS_FINE_LOCATION)
    )

    val allGrantedState by combine(flows = permissions.map { it.statusFlow },
        transform = { permissionsStatuses ->
            permissionsStatuses.map { it is PermissionStatus.Granted }
        }).map { !it.contains(false) }
        .collectAsState(initial = !permissions.map { it.status is PermissionStatus.Granted }
            .contains(false))

    val phoneNumber = rememberSaveable { mutableStateOf("") }

    var openRational by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    val requestPerm = composePermissionRequest(permissions = permissions,
        onRequireRational = {
            openRational = true
        },
        onAlreadyGranted = {
            startCall(context, phoneNumber.value)
        })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
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
        if (!allGrantedState) {
            Text(text = "Call permission required", color = Color.Magenta, fontSize = 12.sp)
        }
        FloatingActionButton(
            modifier = Modifier.padding(16.dp),
            onClick = {
                // permissionLauncher.launch()
                // When calling we can use the safe launch to follow the permission flow
                // in case there is a need for further explanation we can open the rational dialog
                // if it was already granted simply invoke the callback
                requestPerm(false)
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

                            requestPerm(true)
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
