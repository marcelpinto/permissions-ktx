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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Test

class PermissionProviderTest {


    private val fakeChecker = object : PermissionChecker {
        override fun getStatus(type: Permission) = PermissionStatus.Granted(Permission("any"))
    }

    private val dummyObserver = object : PermissionObserver {
        override fun getStatusFlow(type: Permission) = emptyFlow<PermissionStatus>()
        override fun refreshStatus() {}
    }

    @Test
    fun `test isInitialized when PermissionProvider is initialized`() {
        assertThat(PermissionProvider.isInitialized()).isFalse()
        PermissionProvider.init(fakeChecker, dummyObserver)
        assertThat(PermissionProvider.isInitialized()).isTrue()
    }
}
