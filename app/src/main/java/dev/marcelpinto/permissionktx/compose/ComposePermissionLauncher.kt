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

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityOptionsCompat
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.PermissionProvider
import dev.marcelpinto.permissionktx.PermissionRational
import dev.marcelpinto.permissionktx.PermissionStatus

internal class ComposePermissionLauncher(
    private val type: Permission,
    private val resultLauncher: ActivityResultLauncher<String>
) : ActivityResultLauncher<String>() {
    override fun launch(input: String?, options: ActivityOptionsCompat?) {
        resultLauncher.launch(type.name, options)
    }

    override fun unregister() {
        resultLauncher.unregister()
    }

    override fun getContract(): ActivityResultContract<String, *> = resultLauncher.contract
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

    val requestPerm = {
        forced: Boolean ->
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

    ComposePermissionLauncher(permission, activityResultLauncher)

    return requestPerm
}
