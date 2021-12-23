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

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Automatically wire the permission checker and observer with the application lifecycle
 *
 * Do not call PermissionInitializer.create directly unless it's required for testing or
 * some custom initialization.
 */
@Keep
@OptIn(ExperimentalCoroutinesApi::class)
class PermissionInitializer : Initializer<PermissionProvider> {

    override fun create(context: Context): PermissionProvider {
        return PermissionProvider.run {
            init(context)
            instance
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}