/*
 * Copyright 2020 Marcel Pinto Biescas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.marcelpinto.permissionktx

import androidx.activity.result.launch
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class MultiplePermissionLauncherTest {

    private lateinit var permissionsStatus: List<PermissionStatus>

    private var onRequirePermissionCalls = 0
    private val onRequireRational = spy<MultiplePermissionsLauncher.(List<Permission>) -> Unit>()
    private val onAlreadyGranted = spy<() -> Unit>()
    private val resultLauncher = spy(EmptyResultLauncher())

    private val permissionTypes = listOf(Permission("one"), Permission("two"))

    @Before
    fun setUp() {
        val fakeChecker = object : PermissionChecker {
            override fun getStatus(type: Permission) = permissionsStatus.first { it.type == type }
        }
        val dummyObserver = object : PermissionObserver {
            override fun getStatusFlow(type: Permission) = emptyFlow<PermissionStatus>()

            override fun refreshStatus() {}
        }
        PermissionProvider.init(fakeChecker, dummyObserver)
    }

    @Test
    fun `test safeLaunch when Revoked with Optional Rational`() {
        val target = MultiplePermissionsLauncher(permissionTypes, resultLauncher)
        permissionsStatus = listOf(
            PermissionStatus.Revoked(
                type = permissionTypes.first(),
                rationale = PermissionRational.OPTIONAL
            ),
            PermissionStatus.Granted(type = permissionTypes[1])
        )

        target.safeLaunch(
            onRequirePermissions = {
                onRequirePermissionCalls++
                true
            },
            onRequireRational = onRequireRational,
            onAlreadyGranted = onAlreadyGranted
        )

        assertThat(onRequirePermissionCalls).isEqualTo(1)
        verify(onRequireRational, never()).invoke(eq(target), any())
        verify(onAlreadyGranted, never()).invoke()
        verify(resultLauncher).launch()
    }

    @Test
    fun `test safeLaunch when first is Revoked and Required Rational`() {
        val target = MultiplePermissionsLauncher(permissionTypes, resultLauncher)
        permissionsStatus = listOf(
            PermissionStatus.Revoked(
                type = permissionTypes.first(),
                rationale = PermissionRational.REQUIRED
            ),
            PermissionStatus.Granted(type = permissionTypes[1])
        )

        target.safeLaunch(
            onRequirePermissions = {
                onRequirePermissionCalls++
                true
            },
            onRequireRational = onRequireRational,
            onAlreadyGranted = onAlreadyGranted
        )

        assertThat(onRequirePermissionCalls).isEqualTo(0)
        verify(onRequireRational).invoke(target, listOf(permissionTypes.first()))
        verify(onAlreadyGranted, never()).invoke()
        verify(resultLauncher, never()).launch()
    }

    @Test
    fun `test safeLaunch when all Revoked and Required Rational`() {
        val target = MultiplePermissionsLauncher(permissionTypes, resultLauncher)
        permissionsStatus = listOf(
            PermissionStatus.Revoked(
                type = permissionTypes.first(),
                rationale = PermissionRational.REQUIRED
            ),
            PermissionStatus.Revoked(
                type = permissionTypes[1],
                rationale = PermissionRational.REQUIRED
            )
        )

        target.safeLaunch(
            onRequirePermissions = {
                onRequirePermissionCalls++
                true
            },
            onRequireRational = onRequireRational,
            onAlreadyGranted = onAlreadyGranted
        )

        assertThat(onRequirePermissionCalls).isEqualTo(0)
        verify(onRequireRational).invoke(target, permissionTypes)
        verify(onAlreadyGranted, never()).invoke()
        verify(resultLauncher, never()).launch()
    }

    @Test
    fun `test safeLaunch when all Granted`() {
        val target = MultiplePermissionsLauncher(permissionTypes, resultLauncher)
        permissionsStatus = listOf(
            PermissionStatus.Granted(type = permissionTypes[0]),
            PermissionStatus.Granted(type = permissionTypes[1])
        )

        target.safeLaunch(
            onRequirePermissions = {
                onRequirePermissionCalls++
                true
            },
            onRequireRational = onRequireRational,
            onAlreadyGranted = onAlreadyGranted
        )

        assertThat(onRequirePermissionCalls).isEqualTo(0)
        verify(onRequireRational, never()).invoke(eq(target), any())
        verify(onAlreadyGranted).invoke()
        verify(resultLauncher, never()).launch()
    }
}