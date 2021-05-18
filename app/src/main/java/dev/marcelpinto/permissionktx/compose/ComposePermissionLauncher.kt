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

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityOptionsCompat
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.PermissionProvider
import dev.marcelpinto.permissionktx.PermissionRational
import dev.marcelpinto.permissionktx.PermissionStatus

private fun createMultipleResultCallback(
    onResult: (Map<Permission, Boolean>) -> Unit
): (Map<String, Boolean>) -> Unit = { map ->
    PermissionProvider.instance.observer.refreshStatus()
    onResult(map.mapKeys { Permission(it.key) })
}

@Composable
internal fun composePermissionRequest(
    permissions: List<Permission>,
    options: ActivityOptionsCompat? = null,
    onRequirePermission: (List<Permission>) -> Boolean = { true },
    onRequireRational: (List<Permission>) -> Unit,
    onAlreadyGranted: () -> Unit = {},
    onResult: (Map<Permission, Boolean>) -> Unit = {},
): (Boolean) -> Unit {

    val activityResultLauncher = composeRegisterForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = createMultipleResultCallback(onResult)
    )

    val requestPerm = { forced: Boolean ->
        if (forced) {
            activityResultLauncher.launch(permissions.map { it.name }.toTypedArray(), options)
        } else {
            val (_, revoked) = permissions.partition { it.status.isGranted() }
            val requireRational = revoked.filter {
                (it.status as PermissionStatus.Revoked).rationale != PermissionRational.OPTIONAL
            }
            when {
                revoked.isEmpty() -> onAlreadyGranted()
                requireRational.isNotEmpty() -> onRequireRational(requireRational)
                onRequirePermission(revoked) ->
                    activityResultLauncher.launch(
                        permissions.map { it.name }.toTypedArray(),
                        options
                    )
            }
        }
    }

    return requestPerm
}

internal fun createResultCallback(onResult: (Boolean) -> Unit): (Boolean) -> Unit = {
    PermissionProvider.instance.observer.refreshStatus()
    onResult(it)
}

@Composable
internal fun composePermissionRequest(
    permission: Permission,
    options: ActivityOptionsCompat? = null,
    onRequirePermission: () -> Boolean = { true },
    onRequireRational: () -> Unit,
    onAlreadyGranted: () -> Unit = {},
    onResult: (Boolean) -> Unit = {},
): (Boolean) -> Unit {

    val activityResultLauncher = composeRegisterForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = createResultCallback(onResult)
    )

    val requestPerm = { forced: Boolean ->
        if (forced) {
            activityResultLauncher.launch(permission.name, options)
        } else {
            when (val state = permission.status) {
                is PermissionStatus.Granted -> onAlreadyGranted()
                is PermissionStatus.Revoked -> if (state.rationale != PermissionRational.OPTIONAL) {
                    onRequireRational()
                } else if (onRequirePermission()) {
                    activityResultLauncher.launch(permission.name, options)
                }
            }
        }
    }

    return requestPerm
}
