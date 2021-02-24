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
 * A version of [ActivityResultCaller.registerForActivityResult] for the current Fragment
 * that creates a PermissionLauncher using the provided permission name.
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
 * that creates a PermissionLauncher using the provided permission name.
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
