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

/**
 * Defines the rational status when a [Permission] status is [PermissionStatus.Revoked]
 */
enum class PermissionRational {
    /**
     * The permission requires further explanation
     */
    REQUIRED,

    /**
     * The permission can be requested directly without further explanation
     */
    OPTIONAL,

    /**
     * Could not be determined. This can happen when checking the status before the first
     * activity is created.
     */
    UNDEFINED
}