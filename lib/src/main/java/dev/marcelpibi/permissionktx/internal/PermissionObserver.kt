package dev.marcelpibi.permissionktx.internal

import dev.marcelpibi.permissionktx.Permission
import dev.marcelpibi.permissionktx.getPermissionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

@ExperimentalCoroutinesApi
internal class PermissionObserver(private val declaredPermissions: List<String>) {

    private val stateFlow = MutableStateFlow(getPermissionsState())

    fun getStatusFlow(name: String): Flow<Permission.Status> {
        require(declaredPermissions.contains(name)) {
            "Permission $name not declared in the AndroidManifest"
        }
        return stateFlow.mapNotNull { permissions ->
            permissions.firstOrNull { state -> state.name == name }
        }.distinctUntilChanged()
    }

    fun refreshStatus() {
        stateFlow.value = getPermissionsState()
    }

    private fun getPermissionsState() = declaredPermissions.map {
        it.getPermissionStatus()
    }
}