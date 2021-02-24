package dev.marcelpinto.permissionktx

/**
 * Defines the status of a given [Permission]
 */
sealed class PermissionStatus {

    /**
     * The given [Permission] from this status
     */
    abstract val type: Permission

    /**
     * Defines that the status of the given [Permission] is Granted by the user
     */
    data class Granted(override val type: Permission) : PermissionStatus()

    /**
     * Defines that the status of the given [Permission] is Revoked/denied by the user with its
     * [PermissionRational] state
     */
    data class Revoked(override val type: Permission, val rationale: PermissionRational) : PermissionStatus()

    /**
     * @return true if the [PermissionStatus] is granted
     */
    fun isGranted() = this is Granted
}