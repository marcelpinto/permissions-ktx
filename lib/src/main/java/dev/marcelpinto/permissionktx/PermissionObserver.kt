package dev.marcelpinto.permissionktx

import kotlinx.coroutines.flow.Flow

/**
 * Observes permission status changes
 */
interface PermissionObserver {

    /**
     * @param type the permission name
     * @return a flow with the PermissionProvider.Status of the given permission name
     */
    fun getStatusFlow(type: Permission): Flow<PermissionStatus>

    /**
     * Request the observer to refresh the status of the permissions
     *
     * Note: this is called on lifecycle changes or on permission request results
     */
    fun refreshStatus()
}