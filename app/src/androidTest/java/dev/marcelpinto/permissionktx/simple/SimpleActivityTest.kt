package dev.marcelpinto.permissionktx.simple

import android.Manifest
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.marcelpinto.permissionktx.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleActivityTest {

    private var permissionStatus: PermissionStatus = PermissionStatus.Revoked(
        type = Permission(Manifest.permission.ACCESS_COARSE_LOCATION),
        rationale = PermissionRational.OPTIONAL
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // Provide a custom init that returns the values of the defined permissionStatus
        // and when request is launched it returns true or false depending on the permissionStatus
        PermissionProvider.init(
            context = InstrumentationRegistry.getInstrumentation().targetContext,
            checker = object : PermissionChecker {
                override fun getStatus(type: Permission) = permissionStatus
            },
            registry = object : ActivityResultRegistry() {
                override fun <I, O> onLaunch(
                    requestCode: Int,
                    contract: ActivityResultContract<I, O>,
                    input: I,
                    options: ActivityOptionsCompat?
                ) {
                    dispatchResult(requestCode, permissionStatus.isGranted())
                }
            }
        )
    }

    @Test
    fun testGivenPermissionRevokedWhenStartThenLocationIsMissing() {
        launchActivity<SimpleActivity>().use {
            onView(withId(R.id.textview_title)).check(
                matches(withText("Missing location permission"))
            )
        }
    }

    @Test
    fun testGivenPermissionGrantedWhenPressingButtonThenIsFindingLocation() {
        launchActivity<SimpleActivity>().use {
            // Change the value before launching the request to granted
            permissionStatus = PermissionStatus.Granted(permissionStatus.type)
            onView(withId(R.id.button_show_location)).perform(click())

            // The request should return true and update the UI
            onView(withId(R.id.textview_title)).check(
                matches(withText("Finding location..."))
            )
        }
    }
}