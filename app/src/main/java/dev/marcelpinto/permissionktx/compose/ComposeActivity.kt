package dev.marcelpinto.permissionktx.compose

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.platform.setContent
import dev.marcelpinto.permissionktx.PermissionRequest
import dev.marcelpinto.permissionktx.registerForPermissionResult

/**
 * The compose example, shows a simple dialer app that follows the permission flow by:
 *   - Giving hint to user when permission is missing
 *   - Showing a rational if required (based on the status)
 *   - Requesting the permission
 *   - Launching the call once permission is granted
 */
class ComposeActivity : AppCompatActivity() {

    // Register the permission request in the parent activity. This ensure that the state is handle
    // properly even after activity recreation.
    //
    // The callback is called after permission request result.
    private val permissionRequest: PermissionRequest =
        registerForPermissionResult(Manifest.permission.CALL_PHONE) { granted ->
            if (granted) {
                startCall(phoneNumber.value)
            }
        }

    // Keeping the variable here allows us to use it in the permission request callback and ensures
    // it's restored on activity restart because we use savedInstanceState below.
    private lateinit var phoneNumber: MutableState<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colors.background) {
                    phoneNumber = savedInstanceState { "" }
                    ComposePermissionScreen(phoneNumber, permissionRequest) { phone ->
                        // in case the permission was already granted we can call startCall directly
                        startCall(phone)
                    }
                }
            }
        }
    }

    private fun startCall(phone: String) {
        val phoneCallUri = Uri.parse("tel:$phone")
        val phoneCallIntent = Intent(Intent.ACTION_CALL).also {
            it.data = phoneCallUri
        }
        startActivity(phoneCallIntent)
    }
}