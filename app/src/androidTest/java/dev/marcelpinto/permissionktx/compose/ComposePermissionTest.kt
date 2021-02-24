package dev.marcelpinto.permissionktx.compose

import android.Manifest
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import dev.marcelpinto.permissionktx.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class ComposePermissionTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComposeActivity>()

    private val permissionType = Permission(Manifest.permission.CALL_PHONE)

    private val permissionLauncher: PermissionLauncher = PermissionLauncher(
        type = permissionType,
        resultLauncher = EmptyResultLauncher()
    )

    private var permissionStatus: PermissionStatus = PermissionStatus.Revoked(
        type = permissionType,
        rationale = PermissionRational.OPTIONAL
    )

    @ExperimentalCoroutinesApi
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
        val phoneNumber = mutableStateOf("")

        compose(phoneNumber)

        composeTestRule.onNodeWithText("Call permission required").assertIsDisplayed()
    }

    @Test
    fun testWhenPermissionIsGrantedThenDoNotShowHint() {
        val phoneNumber = mutableStateOf("")
        permissionStatus = PermissionStatus.Granted(permissionType)

        compose(phoneNumber)

        composeTestRule.onNodeWithText("Call permission required").assertDoesNotExist()
    }

    @Test
    fun testWhenPhoneWithOutRationalRequireThenRequestPermission() {
        val phoneNumber = mutableStateOf("1111")
        permissionStatus = PermissionStatus.Revoked(permissionType, PermissionRational.OPTIONAL)

        compose(phoneNumber)
        composeTestRule.onNode(hasClickAction().and(hasSetTextAction().not())).performClick()

        composeTestRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun testWhenPhoneWithRationalRequireThenShowDialog() {
        val phoneNumber = mutableStateOf("1111")
        permissionStatus = PermissionStatus.Revoked(permissionType, PermissionRational.REQUIRED)

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
                    permissionLauncher = permissionLauncher,
                    onCall = { }
                )
            }
        }
    }
}
