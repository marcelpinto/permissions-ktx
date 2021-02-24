package dev.marcelpinto.permissionktx

/**
 * Checks the status of a given permission name.
 */
interface PermissionChecker {

    /**
     * @param type the permission name
     * @return the PermissionProvider.Status of the given permission name
     */
    fun getStatus(type: Permission): PermissionStatus
}