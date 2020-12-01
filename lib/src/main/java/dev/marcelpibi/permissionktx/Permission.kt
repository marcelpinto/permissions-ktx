package dev.marcelpibi.permissionktx

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.startup.Initializer
import dev.marcelpibi.permissionktx.internal.PermissionActivityProvider
import dev.marcelpibi.permissionktx.internal.PermissionChecker
import dev.marcelpibi.permissionktx.internal.PermissionObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Used to hold the reference to check and observe application permission status.
 *
 * @see Permission.Status
 * @see getPermissionStatus
 * @see observePermissionStatus
 * @see registerForPermissionResult
 */
object Permission {

    internal lateinit var permissionChecker: PermissionChecker

    @ExperimentalCoroutinesApi
    internal lateinit var permissionObserver: PermissionObserver

    /**
     * Defines the status of a given permission
     */
    sealed class Status {

        /**
         * The actual name of the permission
         */
        abstract val name: String

        /**
         * Defines a permission with the given name as Granted by the user
         */
        data class Granted(override val name: String) : Status()

        /**
         * Defines a permission with the given name as Revoked/denied by the user and if the
         * permission requires further rational
         *
         * @see Activity.shouldShowRequestPermissionRationale
         */
        data class Revoked(override val name: String, val rationale: Rational) : Status()

        /**
         * Shortcut to check if the permission status is granted
         */
        fun isGranted() = this is Granted
    }

    /**
     * Defines if a permission rational status
     */
    enum class Rational {
        /**
         * The permission requires further explanation
         */
        REQUIRED,

        /**
         * The permission can be requested directly without further explanation
         */
        OPTIONAL,

        /**
         * Could not determinate This can happen when checking the status before the first activity is created.
         */
        UNDEFINED
    }

    /**
     * Automatically wire the permission checker and observer with the application lifecycle
     *
     * Do not call PermissionInitializer.create directly unless it's required for testing or
     * some custom initialization.
     */
    @ExperimentalCoroutinesApi
    class PermissionInitializer : Initializer<Permission> {

        override fun create(context: Context): Permission {
            (context.applicationContext as Application).run {
                val provider = PermissionActivityProvider(this)
                permissionChecker = PermissionChecker(provider)
                permissionObserver = PermissionObserver(
                    packageManager.getPackageInfo(
                        packageName,
                        PackageManager.GET_PERMISSIONS
                    ).requestedPermissions.toList()
                )
                provider.onRefresh = permissionObserver::refreshStatus
                registerActivityLifecycleCallbacks(provider)
            }
            return Permission
        }

        override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

    }
}