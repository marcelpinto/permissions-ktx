package dev.marcelpinto.permissionktx

import kotlinx.coroutines.flow.Flow

/**
 * Inline class that defines a Permission that avoids creating a new instance but ensures type safety
 *
 * e.g: Permission(Manifest.permission.CAMERA)
 */
inline class Permission(val name: String) {

    /**
     * Get the [PermissionStatus] of the given type
     */
    val status: PermissionStatus
        get() = PermissionProvider.instance.getStatus(this)

    /**
     * A flow that emits everytime the permission status of the given type changes
     *
     * @return a new [Flow] of [PermissionStatus] from the given type
     */
    val statusFlow: Flow<PermissionStatus>
        get() = PermissionProvider.instance.getStatusFlow(this)
}
