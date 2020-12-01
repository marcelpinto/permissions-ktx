package dev.marcelpibi.permissionktx

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.core.app.ActivityOptionsCompat

/**
 * Define a register ActivityResultContracts.RequestPermission for the given permission name that
 * should be used when launching the permission flow.
 *
 * Call launch to follow the recommended flow when requesting permissions, otherwise
 * you can use the other launch method from the ktx versions.
 *
 * ```kotlin
 * locationPermission.launch(
 *      onRequireRational = {
 *          // i.e show a dialog on onPositiveClick call PermissionRequest.launch()
 *      }
 * )
 * ```
 */
class PermissionRequest(
    val name: String,
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
     * PermissionRequest.launch directly
     *
     * @param onAlreadyGranted is called if the permission was already granted, thus is safely to
     * call an API/Method that requires the given permission
     *
     * @see ActivityResultLauncher.launch
     */
    fun safeLaunch(
        options: ActivityOptionsCompat? = null,
        onRequirePermission: PermissionRequest.() -> Boolean = { true },
        onRequireRational: PermissionRequest.() -> Unit,
        onAlreadyGranted: () -> Unit = {}
    ) {
        when (val state = name.getPermissionStatus()) {
            is Permission.Status.Granted -> onAlreadyGranted()
            is Permission.Status.Revoked -> if (state.rationale != Permission.Rational.OPTIONAL) {
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