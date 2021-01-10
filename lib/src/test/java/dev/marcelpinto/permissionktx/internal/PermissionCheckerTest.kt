package dev.marcelpinto.permissionktx.internal

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.marcelpinto.permissionktx.Permission
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.lang.reflect.Field
import java.lang.reflect.Modifier


@RunWith(JUnit4::class)
class PermissionCheckerTest {

    private val context = mock<Context>()
    private val activity = mock<Activity>()

    private val provider = PermissionActivityProvider(context)

    @Before
    fun setUp() {
        uglyReflectionToSetSdkInt()
    }

    @Test
    fun `test when permission is granted return Status Granted`() {
        val status = getScenarioStatus(granted = true, showRational = false, createActivity = false)

        assertThat(status).isEqualTo(Permission.Status.Granted("any"))
    }

    @Test
    fun `test when permission is revoked and should Not show rational return Status Revoked with Optional Rational`() {
        val status = getScenarioStatus(granted = false, showRational = false, createActivity = true)

        assertThat(status).isEqualTo(
            Permission.Status.Revoked(
                name = "any",
                rationale = Permission.Rational.OPTIONAL
            )
        )
    }

    @Test
    fun `test when permission is revoked and should show rational return Status Revoked with Required Rational`() {
        val status = getScenarioStatus(granted = false, showRational = true, createActivity = true)

        assertThat(status).isEqualTo(
            Permission.Status.Revoked(
                name = "any",
                rationale = Permission.Rational.REQUIRED
            )
        )
    }

    @Test
    fun `test when permission is revoked and no activity yet return Status Revoked with Undefined Rational`() {
        val status = getScenarioStatus(granted = false, showRational = true, createActivity = false)

        assertThat(status).isEqualTo(
            Permission.Status.Revoked(
                name = "any",
                rationale = Permission.Rational.UNDEFINED
            )
        )
    }

    private fun getScenarioStatus(
        granted: Boolean,
        showRational: Boolean,
        createActivity: Boolean
    ): Permission.Status {
        val target = PermissionChecker(provider)
        whenever(context.checkPermission(any(), any(), any())).thenReturn(
            if (granted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        )
        whenever(activity.shouldShowRequestPermissionRationale(any())).thenReturn(showRational)
        if (createActivity) {
            provider.onActivityCreated(activity, null)
        }
        return target.getStatus("any")
    }

    private fun uglyReflectionToSetSdkInt() {
        val field = Build.VERSION::class.java.getField("SDK_INT")
        field.isAccessible = true
        val modifiersField: Field = Field::class.java.getDeclaredField("modifiers")
        modifiersField.isAccessible = true
        modifiersField.setInt(field, field.modifiers and Modifier.FINAL.inv())

        field[null] = 23
    }
}