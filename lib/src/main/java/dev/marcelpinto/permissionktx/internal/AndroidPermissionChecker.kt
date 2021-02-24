package dev.marcelpinto.permissionktx.internal

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.marcelpinto.permissionktx.*

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
            ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, name) -> {
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
