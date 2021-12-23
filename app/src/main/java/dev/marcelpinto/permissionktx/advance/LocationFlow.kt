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

package dev.marcelpinto.permissionktx.advance

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Simple interface that simulates a Location Provider.
 *
 * Normally here you would use the FusedLocationProviderClient or LocationManager.
 * For simplicity we just implement a fake one.
 */
interface LocationFlow {
    fun getLocation(): Flow<String>

    object Fake : LocationFlow {
        override fun getLocation(): Flow<String> = flow {
            var lat = 1.345
            var lng = 12.567
            while (true) {
                emit("$lat, $lng")
                delay(2000)
                lat += 0.1
                lng += 0.1
            }
        }
    }
}