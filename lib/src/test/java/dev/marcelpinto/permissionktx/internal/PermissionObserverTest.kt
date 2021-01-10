package dev.marcelpinto.permissionktx.internal

import com.google.common.truth.Truth.assertThat
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

    private val declaredPermissions = listOf("any")

    private lateinit var actualStatus: Permission.Status

    private val checker = object : Permission.Checker {
        override fun getStatus(name: String): Permission.Status {
            return if (name == actualStatus.name) {
                actualStatus
            } else {
                revoked
            }
        }
    }

    private val granted = Permission.Status.Granted(declaredPermissions.first())
    private val revoked = Permission.Status.Revoked(
        declaredPermissions.first(),
        Permission.Rational.OPTIONAL
    )

    @Test
    fun `test when granted then get granted status once`() = testScope.runBlockingTest {
        actualStatus = granted
        val target = PermissionObserver(checker, declaredPermissions)

        var count = 0
        target.getStatusFlow(declaredPermissions.first()).onEach {
            assertThat(++count).isEqualTo(1)
            assertThat(it).isEqualTo(granted)
        }
    }

    @Test
    fun `test when status change then get changed status once`() = testScope.runBlockingTest {
        actualStatus = granted
        val target = PermissionObserver(checker, declaredPermissions)

        var count = 0
        target.getStatusFlow(declaredPermissions.first()).onEach {
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