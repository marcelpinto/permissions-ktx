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

package dev.marcelpinto.permissionktx.compose

import android.Manifest
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import dev.marcelpinto.permissionktx.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ComposePermissionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComposeActivity>()

    private val permissionType = Permission(Manifest.permission.CALL_PHONE)

    private var permissionStatus: PermissionStatus = PermissionStatus.Revoked(
        type = permissionType,
        rationale = PermissionRational.OPTIONAL
    )

    @Before
    fun setUp() {
        PermissionProvider.init(
            context = InstrumentationRegistry.getInstrumentation().targetContext,
            checker = object : PermissionChecker {
                override fun getStatus(type: Permission) = permissionStatus
            }
        )
    }

    @Test
    fun testWhenPermissionNotGrantedThenShowHint() {
        composeTargetScreen()

        composeTestRule.onNodeWithText("Call permission required").assertIsDisplayed()
    }

    @Test
    fun testWhenPermissionIsGrantedThenDoNotShowHint() {
        permissionStatus = PermissionStatus.Granted(permissionType)

        composeTargetScreen()

        composeTestRule.onNodeWithText("Call permission required").assertDoesNotExist()
    }

    @Test
    fun testWhenPhoneWithOutRationalRequireThenRequestPermission() {
        permissionStatus = PermissionStatus.Revoked(permissionType, PermissionRational.OPTIONAL)

        composeTargetScreen()
        composeTestRule.onNode(hasClickAction().and(hasSetTextAction().not())).performClick()

        composeTestRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun testWhenPhoneWithRationalRequireThenShowDialog() {
        permissionStatus = PermissionStatus.Revoked(permissionType, PermissionRational.REQUIRED)

        composeTargetScreen()
        composeTestRule.onNode(hasClickAction().and(hasSetTextAction().not())).performClick()

        composeTestRule.onNode(isDialog()).assertIsDisplayed()
    }

    private fun composeTargetScreen() {
        composeTestRule.setContent {
            println("Set content")
            MaterialTheme {
                ComposePermissionScreen {}
            }
        }
    }
}
