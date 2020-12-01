/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.marcelpibi.permissionktx

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

/**
 * Check if the permission defined by the String is granted
 */
fun String.isPermissionGranted(): Boolean = Permission.permissionChecker.getStatus(this).isGranted()

/**
 * Get the status of the permission defined by the String
 */
fun String.getPermissionStatus(): Permission.Status = Permission.permissionChecker.getStatus(this)

/**
 * Get the status of the permission defined by the String
 */
@ExperimentalCoroutinesApi
fun String.observePermissionStatus(): Flow<Permission.Status> =
    Permission.permissionObserver.getStatusFlow(this)

/**
 * A version of [ActivityResultCaller.registerForActivityResult]
 * that creates a PermissionRequest using the provided permission name.
 *
 * @see PermissionRequest
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun AppCompatActivity.registerForPermissionResult(
    permission: String,
    onResult: (Boolean) -> Unit
): PermissionRequest = PermissionRequest(
    name = permission,
    resultLauncher = this.registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        permission
    ) {
        @Suppress("EXPERIMENTAL_API_USAGE")
        Permission.permissionObserver.refreshStatus()
        onResult(it)
    }
)

fun Fragment.registerForPermissionResult(
    permission: String,
    onResult: (Boolean) -> Unit
): PermissionRequest = PermissionRequest(
    name = permission,
    resultLauncher = this.registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        permission
    ) {
        @Suppress("EXPERIMENTAL_API_USAGE")
        Permission.permissionObserver.refreshStatus()
        onResult(it)
    }
)

