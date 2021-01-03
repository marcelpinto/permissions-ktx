package dev.marcelpinto.permissionktx.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.getPermissionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull

/**
 * Class that provides a Flow with the status of the given permission
 *
 * Note: instead of using it directly use the ktx extensions.
 */
@ExperimentalCoroutinesApi
internal class PermissionObserver(
    private val declaredPermissions: List<String>
) : Permission.Observer, LifecycleEventObserver {

    private val stateFlow: MutableStateFlow<List<Permission.Status>> by lazy {
        MutableStateFlow(getPermissionsState())
    }

    /**
     * @param name a permission name to check the status
     * @return a flow that emits a Permission.Status everytime it changes
     */
    override fun getStatusFlow(name: String): Flow<Permission.Status> {
        require(declaredPermissions.contains(name)) {
            "Permission $name not declared in the AndroidManifest"
        }
        return stateFlow.mapNotNull { permissions ->
            permissions.firstOrNull { state -> state.name == name }
        }.distinctUntilChanged()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            ON_CREATE -> refreshStatus()
            ON_RESUME -> refreshStatus()
            else -> return
        }
    }

    override fun refreshStatus() {
        stateFlow.value = getPermissionsState()
    }

    private fun getPermissionsState() = declaredPermissions.map {
        it.getPermissionStatus()
    }
}