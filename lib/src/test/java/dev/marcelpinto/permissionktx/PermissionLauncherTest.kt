package dev.marcelpinto.permissionktx

import androidx.activity.result.launch
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class PermissionLauncherTest {

    private lateinit var permissionsStatus: PermissionStatus

    private var onRequirePermissionCalls = 0
    private val onRequireRational = spy<PermissionLauncher.() -> Unit>()
    private val onAlreadyGranted = spy<() -> Unit>()
    private val resultLauncher = spy(EmptyResultLauncher())

    @Before
    fun setUp() {
        val fakeChecker = object : PermissionChecker {
            override fun getStatus(type: Permission) = permissionsStatus
        }
        val dummyObserver = object : PermissionObserver {
            override fun getStatusFlow(type: Permission) = emptyFlow<PermissionStatus>()

            override fun refreshStatus() {}
        }
        PermissionProvider.init(fakeChecker, dummyObserver)
    }

    private val permissionType = Permission("any")

    @Test
    fun `test safeLaunch when Revoked and Optional Rational`() {
        val target = PermissionLauncher(permissionType, resultLauncher)
        permissionsStatus = PermissionStatus.Revoked(
            type = permissionType,
            rationale = PermissionRational.OPTIONAL
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
        val target = PermissionLauncher(permissionType, resultLauncher)
        permissionsStatus = PermissionStatus.Revoked(
            type = permissionType,
            rationale = PermissionRational.REQUIRED
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
        val target = PermissionLauncher(permissionType, resultLauncher)
        permissionsStatus = PermissionStatus.Granted(permissionType)

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
        val target = PermissionLauncher(permissionType, resultLauncher)
        permissionsStatus = PermissionStatus.Revoked(
            type = permissionType,
            rationale = PermissionRational.OPTIONAL
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