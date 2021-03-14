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

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.core.app.ActivityOptionsCompat

/**
 * Define a PermissionResultLauncher that extends from the [ActivityResultLauncher]
 *
 * @see PermissionLauncher
 * @see MultiplePermissionsLauncher
 */
abstract class PermissionResultLauncher(
    private val resultLauncher: ActivityResultLauncher<Unit>
) : ActivityResultLauncher<Unit>() {

    override fun launch(input: Unit?, options: ActivityOptionsCompat?) {
        resultLauncher.launch(input, options)
    }

    override fun unregister() {
        resultLauncher.unregister()
    }

    override fun getContract(): ActivityResultContract<Unit, *> = resultLauncher.contract
}

/**
 * Define a register ActivityResultContracts.RequestPermission for the given permission type that
 * should be used when launching the permission flow.
 *
 * Call [PermissionLauncher.safeLaunch] to follow the recommended flow when requesting permissions,
 * otherwise you can use directly call [ActivityResultLauncher.launch] method.
 *
 * ```kotlin
 * locationPermission.safeLaunch(
 *      onRequireRational = {
 *          // i.e show a dialog on onPositiveClick call PermissionLauncher.launch()
 *      }
 * )
 * ```
 */
class PermissionLauncher(
    val type: Permission,
    resultLauncher: ActivityResultLauncher<Unit>
) : PermissionResultLauncher(resultLauncher) {

    /**
     * Call this method to safely launch the permission flow before executing any API/Method
     * that requires the given permission.
     *
     * @param options Additional options for how the Activity should be started.
     *
     * @param onRequirePermission called when the permission is not granted and the permission
     * request should be launched. Return true to directly launch the permission or false to
     * manually handle it
     *
     * @param onRequireRational called when the permission is not granted and further clarification
     * is required via a "rational" UI. Once the rational is shown you can call
     * PermissionLauncher.launch directly
     *
     * @param onAlreadyGranted is called if the permission was already granted, thus is safely to
     * call an API/Method that requires the given permission
     *
     * @see ActivityResultLauncher.launch
     */
    fun safeLaunch(
        options: ActivityOptionsCompat? = null,
        onRequirePermission: PermissionLauncher.() -> Boolean = { true },
        onRequireRational: PermissionLauncher.() -> Unit,
        onAlreadyGranted: () -> Unit = {}
    ) {
        when (val state = type.status) {
            is PermissionStatus.Granted -> onAlreadyGranted()
            is PermissionStatus.Revoked -> if (state.rationale != PermissionRational.OPTIONAL) {
                onRequireRational()
            } else if (onRequirePermission()) {
                launch(options)
            }
        }
    }
}

/**
 * Variant of [PermissionLauncher] used when requesting multiple permissions at the same time.
 *
 * @see safeLaunch
 * @see PermissionLauncher
 */
class MultiplePermissionsLauncher(
    val types: List<Permission>,
    resultLauncher: ActivityResultLauncher<Unit>
) : PermissionResultLauncher(resultLauncher) {

    /**
     * Call this method to safely launch the permission flow before executing any API/Method
     * that requires the given permissions.
     *
     * @param options Additional options for how the Activity should be started.
     *
     * @param onRequirePermissions called when at least on permission is not granted and the permission
     * request should be launched. Return true to directly launch the permission or false to
     * manually handle it
     *
     * @param onRequireRational called when at least one of the permissions is not granted and
     * further clarification is required via a "rational" UI. Once the rational is shown you can call
     * PermissionLauncher.launch directly
     *
     * @param onAlreadyGranted is called if all the permission were already granted, thus is safely to
     * call an API/Method that requires the given permission
     *
     * @see ActivityResultLauncher.launch
     */
    fun safeLaunch(
        options: ActivityOptionsCompat? = null,
        onRequirePermissions: MultiplePermissionsLauncher.(List<Permission>) -> Boolean = { true },
        onRequireRational: MultiplePermissionsLauncher.(List<Permission>) -> Unit,
        onAlreadyGranted: () -> Unit = {}
    ) {
        val (_, revoked) = types.partition { it.status.isGranted() }
        val requireRational = revoked.filter {
            (it.status as PermissionStatus.Revoked).rationale != PermissionRational.OPTIONAL
        }
        when {
            revoked.isEmpty() -> onAlreadyGranted()
            requireRational.isNotEmpty() -> onRequireRational(requireRational)
            onRequirePermissions(revoked) -> launch(options)
        }
    }
}