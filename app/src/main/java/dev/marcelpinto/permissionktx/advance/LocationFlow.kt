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