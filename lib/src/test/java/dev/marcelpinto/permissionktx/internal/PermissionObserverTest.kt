package dev.marcelpinto.permissionktx.internal

import com.google.common.truth.Truth.assertThat
import dev.marcelpinto.permissionktx.PermissionChecker
import dev.marcelpinto.permissionktx.PermissionRational
import dev.marcelpinto.permissionktx.PermissionStatus
import dev.marcelpinto.permissionktx.Permission
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@ExperimentalCoroutinesApi
@RunWith(JUnit4::class)
class PermissionObserverTest {

    private val testScope = TestCoroutineScope()

    private val permissionType = Permission("any")

    private val declaredPermissions = listOf(permissionType.name)

    private lateinit var actualStatus: PermissionStatus

    private val checker = object : PermissionChecker {
        override fun getStatus(type: Permission): PermissionStatus {
            return if (type == actualStatus.type) {
                actualStatus
            } else {
                revoked
            }
        }
    }

    private val granted = PermissionStatus.Granted(permissionType)
    private val revoked = PermissionStatus.Revoked(
        permissionType,
        PermissionRational.OPTIONAL
    )

    @Test
    fun `test when granted then get granted status once`() = testScope.runBlockingTest {
        actualStatus = granted
        val target = AndroidPermissionObserver(checker, declaredPermissions)

        var count = 0
        target.getStatusFlow(permissionType).onEach {
            assertThat(++count).isEqualTo(1)
            assertThat(it).isEqualTo(granted)
        }
    }

    @Test
    fun `test when status change then get changed status once`() = testScope.runBlockingTest {
        actualStatus = granted
        val target = AndroidPermissionObserver(checker, declaredPermissions)

        var count = 0
        target.getStatusFlow(permissionType).onEach {
            when (++count) {
                1 -> assertThat(it).isEqualTo(granted)
                2 -> assertThat(it).isEqualTo(revoked)
                else -> throw IllegalStateException()
            }
        }

        actualStatus = revoked
        target.refreshStatus()
    }
}