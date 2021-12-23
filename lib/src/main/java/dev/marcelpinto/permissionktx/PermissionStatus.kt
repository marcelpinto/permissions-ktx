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