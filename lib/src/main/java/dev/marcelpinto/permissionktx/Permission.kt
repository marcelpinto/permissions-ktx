package dev.marcelpinto.permissionktx

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import androidx.activity.result.ActivityResultRegistry
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.startup.Initializer
import dev.marcelpinto.permissionktx.internal.PermissionActivityProvider
import dev.marcelpinto.permissionktx.internal.PermissionChecker
import dev.marcelpinto.permissionktx.internal.PermissionObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

/**
 * Used to hold the reference to check and observe application permission status.
 *
 * @see Permission.Status
 * @see getPermissionStatus
 * @see observePermissionStatus
 * @see registerForPermissionResult
 */
class Permission @ExperimentalCoroutinesApi constructor(
    val checker: Checker,
    val observer: Observer,
    internal val registry: ActivityResultRegistry?
) {

    companion object {

        internal lateinit var instance: Permission
            private set

        /**
         * Initialize the Permission instance and wire the components to check and observe
         * Permission status changes.
         *
         * Note: by default this is called automatically via PermissionInitializer
         */
        @ExperimentalCoroutinesApi
        fun init(context: Context) {
            check(!::instance.isInitialized) {
                "Permission instance was already initialized, if you are calling this method manually ensure you disabled the PermissionInitializer"
            }
            init(context, null, null, null)
        }

        /**
         * Init method that allows to provide custom parameters for testing purposes.
         */
        @VisibleForTesting
        @ExperimentalCoroutinesApi
        fun init(
            context: Context,
            checker: Checker? = null,
            observer: Observer? = null,
            registry: ActivityResultRegistry? = null
        ) {
            val permissionChecker =
                checker ?: PermissionChecker(PermissionActivityProvider(context).also {
                    (context.applicationContext as Application).registerActivityLifecycleCallbacks(
                        it
                    )
                })
            val permissionObserver = observer ?: PermissionObserver(
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_PERMISSIONS
                ).requestedPermissions.toList()
            ).also {
                // Wire to lifecycle ensuring its done in the main thread
                Handler(context.mainLooper).post {
                    ProcessLifecycleOwner.get().lifecycle.addObserver(it)
                }
            }
            instance = Permission(permissionChecker, permissionObserver, registry)
        }

        /**
         * Init method for unit testing that simply creates the Permission instance with
         * the given parameters without wiring or need of Context
         */
        @VisibleForTesting
        @ExperimentalCoroutinesApi
        fun init(checker: Checker, observer: Observer) {
            instance = Permission(checker, observer, null)
        }
    }

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
         * Could not be determined. This can happen when checking the status before the first
         * activity is created.
         */
        UNDEFINED
    }

    /**
     * Checks the status of a given permission name.
     */
    interface Checker {

        /**
         * @param name the permission name
         * @return the Permission.Status of the given permission name
         */
        fun getStatus(name: String): Status
    }

    /**
     * Observes permission status changes
     */
    interface Observer {

        /**
         * @param name the permission name
         * @return a flow with the Permission.Status of the given permission name
         */
        fun getStatusFlow(name: String): Flow<Status>

        /**
         * Request the observer to refresh the status of the permissions
         *
         * Note: this is called on lifecycle changes or on permission request results
         */
        fun refreshStatus()
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
            init(context)
            return instance
        }

        override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
    }
}