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

package dev.marcelpibi.permissionktx.compose

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.app.ActivityOptionsCompat
import dev.marcelpinto.permissionktx.MultiplePermissionsLauncher
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.PermissionLauncher

/**
 * A version of [ActivityResultCaller.registerForActivityResult] to use in a composable, that
 * creates a [PermissionLauncher] using the provided [Permission].
 *
 * @see PermissionLauncher
 * @see rememberLauncherForActivityResult
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
@Composable
fun rememberLauncherForPermissionResult(
    type: Permission,
    onResult: (Boolean) -> Unit = {}
): PermissionLauncher = rememberLauncherForPermissionResult(type.name, onResult)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] to use in a composable, that
 * creates a [PermissionLauncher] using the provided [Permission].
 *
 * @see PermissionLauncher
 * @see rememberLauncherForActivityResult
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestPermission
 */
@Composable
fun rememberLauncherForPermissionResult(
    type: String,
    onResult: (Boolean) -> Unit = {}
): PermissionLauncher {
    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )
    // Wrap the generic activity launcher into a permission launcher.
    return remember(resultLauncher) {
        PermissionLauncher(
            type = Permission(type),
            resultLauncher = object : ActivityResultLauncher<Unit>() {
                override fun launch(input: Unit?, options: ActivityOptionsCompat?) {
                    resultLauncher.launch(type, options)
                }

                override fun unregister() {
                    // Not required. Registration is automatically handled by rememberLauncherForActivityResult
                }

                override fun getContract(): ActivityResultContract<Unit, *> {

                    return object : ActivityResultContract<Unit, Unit>() {
                        override fun createIntent(context: Context, input: Unit) =
                            resultLauncher.contract.createIntent(context, type)

                        override fun parseResult(resultCode: Int, intent: Intent?) {
                            resultLauncher.contract.parseResult(resultCode, intent)
                        }
                    }
                }
            }
        )
    }
}

/**
 * A version of [ActivityResultCaller.registerForActivityResult] to use in a composable, that
 * creates a [MultiplePermissionsLauncher] using the provided list of [Permission]s.
 *
 * @see PermissionLauncher
 * @see rememberLauncherForActivityResult
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestMultiplePermissions
 */
@Composable
fun rememberLauncherForPermissionsResult(
    types: Array<Permission>,
    onResult: (Map<Permission, Boolean>) -> Unit = {}
): MultiplePermissionsLauncher =
    rememberLauncherForPermissionsResult(types.map { it.name }.toTypedArray(), onResult)

/**
 * A version of [ActivityResultCaller.registerForActivityResult] to use in a composable, that
 * creates a [MultiplePermissionsLauncher] using the provided list of [Permission]s.
 *
 * @see PermissionLauncher
 * @see rememberLauncherForActivityResult
 * @see ActivityResultCaller.registerForActivityResult
 * @see ActivityResultContracts.RequestMultiplePermissions
 */
@Composable
fun rememberLauncherForPermissionsResult(
    types: Array<String>,
    onResult: (Map<Permission, Boolean>) -> Unit = {}
): MultiplePermissionsLauncher {
    val resultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { resultMap ->
            onResult(resultMap.mapKeys { Permission(it.key) })
        }
    )
    // Wrap the generic activity launcher into a permission launcher.
    return remember(resultLauncher) {
        MultiplePermissionsLauncher(
            types = types.map { Permission(it) },
            resultLauncher = object : ActivityResultLauncher<Unit>() {
                override fun launch(input: Unit?, options: ActivityOptionsCompat?) {
                    resultLauncher.launch(types, options)
                }

                override fun unregister() {
                    // Not required. Registration is automatically handled by rememberLauncherForActivityResult
                }

                override fun getContract(): ActivityResultContract<Unit, *> {

                    return object : ActivityResultContract<Unit, Unit>() {
                        override fun createIntent(context: Context, input: Unit) =
                            resultLauncher.contract.createIntent(context, types)

                        override fun parseResult(resultCode: Int, intent: Intent?) {
                            resultLauncher.contract.parseResult(resultCode, intent)
                        }
                    }
                }
            }
        )
    }
}