package dev.marcelpinto.permissionktx.internal

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.marcelpinto.permissionktx.Permission

internal class PermissionChecker(private val provider: PermissionActivityProvider) : Permission.Checker {

    override fun getStatus(name: String): Permission.Status {
        return when {
            provider.context.hasPermission(name) -> Permission.Status.Granted(name)
            else -> Permission.Status.Revoked(name = name, rationale = getRationale(name))
        }
    }

    private fun getRationale(name: String): Permission.Rational {
        val currentActivity = provider.get()
        return when {
            currentActivity == null -> {
                Permission.Rational.UNDEFINED
            }
            ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, name) -> {
                Permission.Rational.REQUIRED
            }
            else -> {
                Permission.Rational.OPTIONAL
            }
        }
    }

    private fun Context.hasPermission(name: String): Boolean = ContextCompat.checkSelfPermission(
            this,
            name
    ) == PackageManager.PERMISSION_GRANTED
}
