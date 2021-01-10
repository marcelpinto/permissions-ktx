package dev.marcelpinto.permissionktx

import com.google.common.truth.Truth.assertThat
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.getPermissionStatus
import dev.marcelpinto.permissionktx.isPermissionGranted
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class PermissionKtxTest {

    private lateinit var permissionsStatus: Permission.Status

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
    fun `test given a permission name that is granted, then isPermissionGranted returns true`() {
        val permissionName = "any"
        permissionsStatus = Permission.Status.Granted(permissionName)

        assertThat(permissionName.isPermissionGranted()).isTrue()
    }

    @Test
    fun `test given a permission name that is revoked, then isPermissionGranted returns false`() {
        val permissionName = "any"
        permissionsStatus = Permission.Status.Revoked(permissionName, Permission.Rational.OPTIONAL)

        assertThat(permissionName.isPermissionGranted()).isFalse()
    }

    @Test
    fun `test getPermissionStatus with a permission name that is revoked with required rational`() {
        val permissionName = "any"
        permissionsStatus = Permission.Status.Revoked(permissionName, Permission.Rational.REQUIRED)

        assertThat(permissionName.getPermissionStatus()).isEqualTo(permissionsStatus)
    }
}