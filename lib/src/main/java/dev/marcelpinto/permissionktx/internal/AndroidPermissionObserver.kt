/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.marcelpinto.permissionktx.internal

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.PermissionChecker
import dev.marcelpinto.permissionktx.PermissionObserver
import dev.marcelpinto.permissionktx.PermissionStatus
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
@OptIn(ExperimentalCoroutinesApi::class)
internal class AndroidPermissionObserver(
    private val checker: PermissionChecker,
    private val declaredPermissions: List<String>
) : PermissionObserver, LifecycleEventObserver {

    private val stateFlow: MutableStateFlow<List<PermissionStatus>> by lazy {
        MutableStateFlow(getPermissionsState())
    }

    /**
     * @param type a permission name to check the status
     * @return a flow that emits a PermissionProvider.Status everytime it changes
     */
    override fun getStatusFlow(type: Permission): Flow<PermissionStatus> {
        require(declaredPermissions.contains(type.name)) {
            "PermissionProvider $type not declared in the AndroidManifest"
        }
        return stateFlow.mapNotNull { permissions ->
            permissions.firstOrNull { state -> state.type == type }
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

    private fun getPermissionsState() = declaredPermissions.map { name ->
        checker.getStatus(Permission(name))
    }
}