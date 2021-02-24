package dev.marcelpinto.permissionktx

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.core.app.ActivityOptionsCompat

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
    private val resultLauncher: ActivityResultLauncher<Unit>
) : ActivityResultLauncher<Unit>() {

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

    override fun launch(input: Unit?, options: ActivityOptionsCompat?) {
        resultLauncher.launch(input, options)
    }

    override fun unregister() {
        resultLauncher.unregister()
    }

    override fun getContract(): ActivityResultContract<Unit, *> = resultLauncher.contract
}