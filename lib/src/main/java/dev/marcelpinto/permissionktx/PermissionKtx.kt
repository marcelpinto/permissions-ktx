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

package dev.marcelpinto.permissionktx

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.*
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

/**
 * Check if the permission defined by the String is granted
 */
fun String.isPermissionGranted(): Boolean = Permission.instance.checker.getStatus(this).isGranted()

/**
 * Get the status of the permission defined by the String
 */
fun String.getPermissionStatus(): Permission.Status = Permission.instance.checker.getStatus(this)

/**
 * Get the status of the permission defined by the String
 */
@ExperimentalCoroutinesApi
fun String.observePermissionStatus(): Flow<Permission.Status> =
    Permission.instance.observer.getStatusFlow(this)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Activity
 * that creates a PermissionRequest using the provided permission name.
 *
 * @see PermissionRequest
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun ComponentActivity.registerForPermissionResult(
    permission: String,
    registry: ActivityResultRegistry? = null,
    onResult: (Boolean) -> Unit = {}
): PermissionRequest = PermissionRequest(
    name = permission,
    resultLauncher = registerForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        input = permission,
        registry = registry ?: getPermissionRegistry(),
        callback = createResultCallback(onResult),
    )
)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Fragment
 * that creates a PermissionRequest using the provided permission name.
 *
 * @see PermissionRequest
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun Fragment.registerForPermissionResult(
    permission: String,
    registry: ActivityResultRegistry? = null,
    onResult: (Boolean) -> Unit = {}
): PermissionRequest {
    val resultLauncher = if (registry == null && Permission.instance.registry == null) {
        registerForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            input = permission,
            callback = createResultCallback(onResult),
        )
    } else {
        registerForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            input = permission,
            registry = registry ?: getPermissionRegistry(),
            callback = createResultCallback(onResult),
        )
    }
    return PermissionRequest(
        name = permission,
        resultLauncher = resultLauncher
    )
}

private fun ComponentActivity.getPermissionRegistry() =
    Permission.instance.registry ?: activityResultRegistry

private fun Fragment.getPermissionRegistry() =
    Permission.instance.registry ?: (host as? ActivityResultRegistryOwner)?.activityResultRegistry
    ?: requireActivity().activityResultRegistry

private fun createResultCallback(onResult: (Boolean) -> Unit): (Boolean) -> Unit = {
    Permission.instance.observer.refreshStatus()
    onResult(it)
}

@VisibleForTesting
open class EmptyResultLauncher : ActivityResultLauncher<Unit>() {
    override fun launch(input: Unit?, options: ActivityOptionsCompat?) {}

    override fun unregister() {}

    override fun getContract(): ActivityResultContract<Unit, *> {
        return object : ActivityResultContract<Unit, Unit>() {
            override fun createIntent(context: Context, input: Unit?) = Intent()

            override fun parseResult(resultCode: Int, intent: Intent?) {}
        }
    }
}
