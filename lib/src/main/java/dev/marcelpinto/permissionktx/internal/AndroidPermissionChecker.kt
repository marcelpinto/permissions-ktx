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

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.PermissionChecker
import dev.marcelpinto.permissionktx.PermissionRational
import dev.marcelpinto.permissionktx.PermissionStatus

internal class AndroidPermissionChecker(private val provider: AndroidActivityProvider) :
    PermissionChecker {

    override fun getStatus(type: Permission): PermissionStatus {
        return when {
            provider.context.hasPermission(type.name) -> PermissionStatus.Granted(type)
            else -> PermissionStatus.Revoked(type = type, rationale = getRationale(type.name))
        }
    }

    private fun getRationale(name: String): PermissionRational {
        val currentActivity = provider.get()
        return when {
            currentActivity == null -> {
                PermissionRational.UNDEFINED
            }
            try {
                ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, name)
            } catch (e: IllegalArgumentException) {
                return PermissionRational.UNDEFINED
            } -> {
                PermissionRational.REQUIRED
            }
            else -> {
                PermissionRational.OPTIONAL
            }
        }
    }

    private fun Context.hasPermission(name: String): Boolean = ContextCompat.checkSelfPermission(
        this,
        name
    ) == PackageManager.PERMISSION_GRANTED
}
