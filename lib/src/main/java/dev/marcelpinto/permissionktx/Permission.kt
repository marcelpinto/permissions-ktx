/*
 * Copyright 2020 Marcel Pinto Biescas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
