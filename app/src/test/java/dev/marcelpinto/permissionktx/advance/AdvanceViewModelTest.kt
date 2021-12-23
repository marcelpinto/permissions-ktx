/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.marcelpinto.permissionktx.advance

import android.Manifest
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import dev.marcelpinto.permissionktx.*
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
 * Test to showcase how unit test a ViewModel that uses the PermissionProvider API.
 *
 * The important bit is the PermissionProvider.init with the custom checker and observer to allow you
 * to define and change the permission status.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdvanceViewModelTest {

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

    private val testScope = TestCoroutineScope(testDispatcher)

    private val actualFlow = MutableStateFlow("1,1")

    private val locationFlow = object : LocationFlow {
        override fun getLocation(): Flow<String> = actualFlow
    }

    private var permissionStatus = MutableStateFlow<PermissionStatus>(
        PermissionStatus.Revoked(
            type = Permission(Manifest.permission.ACCESS_FINE_LOCATION),
            rationale = PermissionRational.OPTIONAL
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        val checker = object : PermissionChecker {
            override fun getStatus(type: Permission) = permissionStatus.value
        }
        val observer = object : PermissionObserver {
            override fun getStatusFlow(type: Permission) = permissionStatus

            override fun refreshStatus() {
                permissionStatus.value = permissionStatus.value
            }
        }
        PermissionProvider.init(checker, observer)
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
            permissionStatus.value = PermissionStatus.Revoked(
                permissionStatus.value.type,
                PermissionRational.REQUIRED
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
            permissionStatus.value = PermissionStatus.Granted(permissionStatus.value.type)

            target.getViewData().value!!.run {
                assertThat(location).isEqualTo(actualFlow.value)
                assertThat(showPermissionHint).isFalse()
                assertThat(showRational).isFalse()
            }
        }
}