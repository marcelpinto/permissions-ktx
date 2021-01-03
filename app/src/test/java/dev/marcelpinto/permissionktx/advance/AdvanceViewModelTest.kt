package dev.marcelpinto.permissionktx.advance

import android.Manifest
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import dev.marcelpinto.permissionktx.Permission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

/**
 * Test to showcase how unit test a ViewModel that uses the Permission API.
 *
 * The important bit is the Permission.init with the custom checker and observer to allow you
 * to define and change the permission status.
 */
@ExperimentalCoroutinesApi
class AdvanceViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private val testScope = TestCoroutineScope(testDispatcher)

    private val actualFlow = MutableStateFlow("1,1")

    private val locationFlow = object : LocationFlow {
        override fun getLocation(): Flow<String> = actualFlow
    }

    private var permissionStatus = MutableStateFlow<Permission.Status>(
        Permission.Status.Revoked(
            name = Manifest.permission.ACCESS_FINE_LOCATION,
            rationale = Permission.Rational.OPTIONAL
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val checker = object : Permission.Checker {
            override fun getStatus(name: String) = permissionStatus.value
        }
        val observer = object : Permission.Observer {
            override fun getStatusFlow(name: String) = permissionStatus

            override fun refreshStatus() {
                permissionStatus.value = permissionStatus.value
            }
        }
        Permission.init(checker, observer)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testScope.cleanupTestCoroutines()
    }

    @Test
    fun testGivenPermissionRevokedWhenStartThenNoLocation() = testScope.runBlockingTest {
        val target = AdvanceViewModel(locationFlow)

        target.getViewData().value!!.run {
            assertThat(location).isEqualTo("Location Disabled")
            assertThat(showPermissionHint).isFalse()
            assertThat(showRational).isFalse()
        }
    }

    @Test
    fun testGivenPermissionRevokedWithoutRationalWhenLocationClickThenRequestPermission() =
        testScope.runBlockingTest {
            val target = AdvanceViewModel(locationFlow)
            var eventData: AdvanceEventData? = null
            target.getEventData().onEach { eventData = it }.launchIn(testScope)

            target.onLocationClick()

            target.getViewData().value!!.run {
                assertThat(location).isEqualTo("Location Disabled")
                assertThat(showPermissionHint).isTrue()
                assertThat(showRational).isFalse()
            }
            assertThat(eventData).isEqualTo(AdvanceEventData.RequestPermission)
        }

    @Test
    fun testGivenPermissionRevokedWithRationalWhenLocationClickThenShowRational() =
        testScope.runBlockingTest {
            permissionStatus.emit(
                Permission.Status.Revoked(
                    permissionStatus.value.name,
                    Permission.Rational.REQUIRED
                )
            )
            val target = AdvanceViewModel(locationFlow)

            target.onLocationClick()

            target.getViewData().value!!.run {
                assertThat(location).isEqualTo("Location Disabled")
                assertThat(showPermissionHint).isTrue()
                assertThat(showRational).isTrue()
            }
        }

    @Test
    fun testGivenPermissionRevokedWhenLocationClickAfterPermissionGrantedThenShowLocation() =
        testScope.runBlockingTest {
            val target = AdvanceViewModel(locationFlow)

            target.onLocationClick()
            permissionStatus.emit(Permission.Status.Granted(permissionStatus.value.name))

            target.getViewData().value!!.run {
                assertThat(location).isEqualTo(actualFlow.value)
                assertThat(showPermissionHint).isFalse()
                assertThat(showRational).isFalse()
            }
        }
}