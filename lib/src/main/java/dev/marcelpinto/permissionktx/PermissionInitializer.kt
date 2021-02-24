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
@ExperimentalCoroutinesApi
class PermissionInitializer : Initializer<PermissionProvider> {

    override fun create(context: Context): PermissionProvider {
        return PermissionProvider.run {
            init(context)
            instance
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}