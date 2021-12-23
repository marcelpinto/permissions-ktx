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

package dev.marcelpinto.permissionktx

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class PermissionTest {

    private lateinit var permissionsStatus: PermissionStatus

    @OptIn(ExperimentalCoroutinesApi::class)
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

    @Test
    fun `test given a permission name that is granted, then isPermissionGranted returns true`() {
        val permissionType = Permission("any")
        permissionsStatus = PermissionStatus.Granted(permissionType)

        assertThat(permissionType.status.isGranted()).isTrue()
    }

    @Test
    fun `test given a permission name that is revoked, then isPermissionGranted returns false`() {
        val permissionType = Permission("any")
        permissionsStatus = PermissionStatus.Revoked(permissionType, PermissionRational.OPTIONAL)

        assertThat(permissionType.status.isGranted()).isFalse()
    }

    @Test
    fun `test getPermissionStatus with a permission name that is revoked with required rational`() {
        val permissionType = Permission("any")
        permissionsStatus = PermissionStatus.Revoked(permissionType, PermissionRational.REQUIRED)

        assertThat(permissionType.status).isEqualTo(permissionsStatus)
    }
}