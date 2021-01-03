package dev.marcelpinto.permissionktx.compose

import android.Manifest
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import dev.marcelpinto.permissionktx.EmptyResultLauncher
import dev.marcelpinto.permissionktx.Permission
import dev.marcelpinto.permissionktx.PermissionRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ComposePermissionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComposeActivity>()

    private val permissionName = Manifest.permission.CALL_PHONE

    private val permissionRequest: PermissionRequest = PermissionRequest(
        name = permissionName,
        resultLauncher = EmptyResultLauncher
    )

    private var permissionStatus: Permission.Status = Permission.Status.Revoked(
        name = permissionName,
        rationale = Permission.Rational.OPTIONAL
    )

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Permission.init(
            context = InstrumentationRegistry.getInstrumentation().targetContext,
            checker = object : Permission.Checker {
                override fun getStatus(name: String) = permissionStatus
            }
        )
    }

    @Test
    fun testWhenPermissionNotGrantedThenShowHint() {
        val phoneNumber = mutableStateOf("")

        compose(phoneNumber)

        composeTestRule.onNodeWithText("Call permission required").assertIsDisplayed()
    }

    @Test
    fun testWhenPermissionIsGrantedThenDoNotShowHint() {
        val phoneNumber = mutableStateOf("")
        permissionStatus = Permission.Status.Granted(permissionName)

        compose(phoneNumber)

        composeTestRule.onNodeWithText("Call permission required").assertDoesNotExist()
    }

    @Test
    fun testWhenPhoneWithOutRationalRequireThenRequestPermission() {
        val phoneNumber = mutableStateOf("1111")
        permissionStatus = Permission.Status.Revoked(permissionName, Permission.Rational.OPTIONAL)

        compose(phoneNumber)
        composeTestRule.onNode(hasClickAction().and(hasSetTextAction().not())).performClick()

        composeTestRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun testWhenPhoneWithRationalRequireThenShowDialog() {
        val phoneNumber = mutableStateOf("1111")
        permissionStatus = Permission.Status.Revoked(permissionName, Permission.Rational.REQUIRED)

        compose(phoneNumber)
        composeTestRule.onNode(hasClickAction().and(hasSetTextAction().not())).performClick()

        composeTestRule.onNode(isDialog()).assertIsDisplayed()
    }

    private fun compose(phoneNumber: MutableState<String>) {
        composeTestRule.setContent {
            println("Set content")
            MaterialTheme {
                ComposePermissionScreen(
                    phoneNumber = phoneNumber,
                    permissionRequest = permissionRequest,
                    onCall = { }
                )
            }
        }
    }
}
