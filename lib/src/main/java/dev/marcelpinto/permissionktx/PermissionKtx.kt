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


/**
 * Checks if all permissions are granted.
 */
fun List<Permission>.areGranted() = isEmpty() || all { it.status.isGranted() }

/**
 * Gets all the revoked permissions from the list
 */
fun List<Permission>.getRevoked() = filter { !it.status.isGranted() }

/**
 * Gets all the granted permissions from the list
 */
fun List<Permission>.getGranted() = filter { it.status.isGranted() }

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Activity
 * that creates a PermissionLauncher using the provided permission name.
 *
 * @see PermissionLauncher
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun ComponentActivity.registerForPermissionResult(
    type: String,
    registry: ActivityResultRegistry? = null,
    onResult: (Boolean) -> Unit = {}
): PermissionLauncher = registerForPermissionResult(Permission(type), registry, onResult)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Activity
 * that creates a PermissionLauncher using the provided permission name.
 *
 * @see PermissionLauncher
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun ComponentActivity.registerForPermissionResult(
    type: Permission,
    registry: ActivityResultRegistry? = null,
    onResult: (Boolean) -> Unit = {}
): PermissionLauncher = PermissionLauncher(
    type = type,
    resultLauncher = registerForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        input = type.name,
        registry = registry ?: getPermissionRegistry(),
        callback = createResultCallback(onResult),
    )
)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Activity
 * that creates a [MultiplePermissionsLauncher] using the list of provided permission names.
 *
 * @see PermissionLauncher
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun ComponentActivity.registerForMultiplePermissionResult(
    types: Array<String>,
    registry: ActivityResultRegistry? = null,
    onResult: (Map<Permission, Boolean>) -> Unit = {}
): MultiplePermissionsLauncher = MultiplePermissionsLauncher(
    types = types.map { Permission(it) },
    resultLauncher = registerForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        input = types,
        registry = registry ?: getPermissionRegistry(),
        callback = createMultipleResultCallback(onResult),
    )
)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Activity
 * that creates a [MultiplePermissionsLauncher] using the list of provided [Permission]s.
 *
 * @see PermissionLauncher
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun ComponentActivity.registerForMultiplePermissionResult(
    types: Array<Permission>,
    registry: ActivityResultRegistry? = null,
    onResult: (Map<Permission, Boolean>) -> Unit = {}
): MultiplePermissionsLauncher = MultiplePermissionsLauncher(
    types = types.toList(),
    resultLauncher = registerForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        input = types.map { it.name }.toTypedArray(),
        registry = registry ?: getPermissionRegistry(),
        callback = createMultipleResultCallback(onResult),
    )
)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Fragment
 * that creates a [PermissionLauncher] using the provided permission name.
 *
 * @see PermissionLauncher
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun Fragment.registerForPermissionResult(
    type: String,
    registry: ActivityResultRegistry? = null,
    onResult: (Boolean) -> Unit = {}
): PermissionLauncher = registerForPermissionResult(Permission(type), registry, onResult)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Fragment
 * that creates a [PermissionLauncher] using the provided [Permission].
 *
 * @see PermissionLauncher
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun Fragment.registerForPermissionResult(
    type: Permission,
    registry: ActivityResultRegistry? = null,
    onResult: (Boolean) -> Unit = {}
): PermissionLauncher {
    val resultLauncher = if (registry == null && PermissionProvider.instance.registry == null) {
        registerForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            input = type.name,
            callback = createResultCallback(onResult),
        )
    } else {
        registerForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            input = type.name,
            registry = registry ?: getPermissionRegistry(),
            callback = createResultCallback(onResult),
        )
    }
    return PermissionLauncher(
        type = type,
        resultLauncher = resultLauncher
    )
}

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Fragment
 * that creates a [MultiplePermissionsLauncher] using the list of provided permissions name.
 *
 * @see PermissionLauncher
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun Fragment.registerForMultiplePermissionResult(
    types: Array<String>,
    registry: ActivityResultRegistry? = null,
    onResult: (Map<Permission, Boolean>) -> Unit = {}
): MultiplePermissionsLauncher = registerForMultiplePermissionResult(
    types.map { Permission(it) }.toTypedArray(),
    registry,
    onResult
)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Fragment
 * that creates a [MultiplePermissionsLauncher] using the list of provided [Permission].
 *
 * @see PermissionLauncher
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
fun Fragment.registerForMultiplePermissionResult(
    types: Array<Permission>,
    registry: ActivityResultRegistry? = null,
    onResult: (Map<Permission, Boolean>) -> Unit = {}
): MultiplePermissionsLauncher {
    val input = types.map { it.name }.toTypedArray()
    val resultLauncher = if (registry == null && PermissionProvider.instance.registry == null) {
        registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            input = input,
            callback = createMultipleResultCallback(onResult),
        )
    } else {
        registerForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            input = input,
            registry = registry ?: getPermissionRegistry(),
            callback = createMultipleResultCallback(onResult),
        )
    }
    return MultiplePermissionsLauncher(
        types = types.toList(),
        resultLauncher = resultLauncher
    )
}

private fun ComponentActivity.getPermissionRegistry() =
    PermissionProvider.instance.registry ?: activityResultRegistry

private fun Fragment.getPermissionRegistry() =
    PermissionProvider.instance.registry
        ?: (host as? ActivityResultRegistryOwner)?.activityResultRegistry
        ?: requireActivity().activityResultRegistry

private fun createResultCallback(onResult: (Boolean) -> Unit): (Boolean) -> Unit = {
    PermissionProvider.instance.observer.refreshStatus()
    onResult(it)
}

private fun createMultipleResultCallback(
    onResult: (Map<Permission, Boolean>) -> Unit
): (Map<String, Boolean>) -> Unit = { map ->
    PermissionProvider.instance.observer.refreshStatus()
    onResult(map.mapKeys { Permission(it.key) })
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
