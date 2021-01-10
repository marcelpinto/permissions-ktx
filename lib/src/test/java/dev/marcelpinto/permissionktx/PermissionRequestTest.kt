package dev.marcelpinto.permissionktx

import androidx.activity.result.launch
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import dev.marcelpinto.permissionktx.Permission
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class PermissionRequestTest {

    private lateinit var permissionsStatus: Permission.Status

    private var onRequirePermissionCalls = 0
    private val onRequireRational = spy<PermissionRequest.() -> Unit>()
    private val onAlreadyGranted = spy<() -> Unit>()
    private val resultLauncher = spy(EmptyResultLauncher())

    @Before
    fun setUp() {
        val fakeChecker = object : Permission.Checker {
            override fun getStatus(name: String) = permissionsStatus
        }
        val dummyObserver = object : Permission.Observer {
            override fun getStatusFlow(name: String) = emptyFlow<Permission.Status>()

            override fun refreshStatus() {}
        }
        Permission.init(fakeChecker, dummyObserver)
    }

    @Test
    fun `test safeLaunch when Revoked and Optional Rational`() {
        val target = PermissionRequest("any", resultLauncher)
        permissionsStatus = Permission.Status.Revoked(
            name = "any",
            rationale = Permission.Rational.OPTIONAL
        )

        target.safeLaunch(
            onRequirePermission = {
                onRequirePermissionCalls++
                true
            },
            onRequireRational = onRequireRational,
            onAlreadyGranted = onAlreadyGranted
        )

        assertThat(onRequirePermissionCalls).isEqualTo(1)
        verify(onRequireRational, never()).invoke(target)
        verify(onAlreadyGranted, never()).invoke()
        verify(resultLauncher).launch()
    }

    @Test
    fun `test safeLaunch when Revoked and Required Rational`() {
        val target = PermissionRequest("any", resultLauncher)
        permissionsStatus = Permission.Status.Revoked(
            name = "any",
            rationale = Permission.Rational.REQUIRED
        )

        target.safeLaunch(
            onRequirePermission = {
                onRequirePermissionCalls++
                true
            },
            onRequireRational = onRequireRational,
            onAlreadyGranted = onAlreadyGranted
        )

        assertThat(onRequirePermissionCalls).isEqualTo(0)
        verify(onRequireRational).invoke(target)
        verify(onAlreadyGranted, never()).invoke()
        verify(resultLauncher, never()).launch()
    }

    @Test
    fun `test safeLaunch when Granted`() {
        val target = PermissionRequest("any", resultLauncher)
        permissionsStatus = Permission.Status.Granted("any")

        target.safeLaunch(
            onRequirePermission = {
                onRequirePermissionCalls++
                true
            },
            onRequireRational = onRequireRational,
            onAlreadyGranted = onAlreadyGranted
        )

        assertThat(onRequirePermissionCalls).isEqualTo(0)
        verify(onRequireRational, never()).invoke(target)
        verify(onAlreadyGranted).invoke()
        verify(resultLauncher, never()).launch()
    }

    @Test
    fun `test safeLaunch when Revoked, Required Rational and manual launch`() {
        val target = PermissionRequest("any", resultLauncher)
        permissionsStatus = Permission.Status.Revoked(
            name = "any",
            rationale = Permission.Rational.OPTIONAL
        )

        target.safeLaunch(
            onRequirePermission = {
                onRequirePermissionCalls++
                // return false to avoid launching the request
                false
            },
            onRequireRational = onRequireRational,
            onAlreadyGranted = onAlreadyGranted
        )

        assertThat(onRequirePermissionCalls).isEqualTo(1)
        verify(onRequireRational, never()).invoke(target)
        verify(onAlreadyGranted, never()).invoke()
        verify(resultLauncher, never()).launch()
    }
}