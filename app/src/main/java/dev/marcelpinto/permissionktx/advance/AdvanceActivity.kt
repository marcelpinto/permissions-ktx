package dev.marcelpinto.permissionktx.advance

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dev.marcelpinto.permissionktx.*
import kotlinx.coroutines.flow.collect

/**
 * Advance sample shows how you can use the observe pattern to keep all the permission logic
 * in the view model while handling the request/rational flow in the activity/View and start/stop
 * an API (in this case the location manager) in a reactive way based on the permission status.
 */
class AdvanceActivity : AppCompatActivity(R.layout.advance_activity) {

    // Create the PermissionLauncher by registering the required permission
    // Note that we don't care about the result since the ViewModel is tracking permission status
    // changes.
    private val locationPermissionLauncher: PermissionLauncher = registerForPermissionResult(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val viewModel: AdvanceViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdvanceViewModel(LocationFlow.Fake) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val locationText = findViewById<TextView>(R.id.locationText)
        val actionButton = findViewById<FloatingActionButton>(R.id.locationFab)
        val missingHint = findViewById<Button>(R.id.locationMissingPermissionHint)
        val rationalView = findViewById<ViewGroup>(R.id.locationRational)
        val rationalConfirmButton = findViewById<Button>(R.id.locationRationalConfirm)
        val rationalDeclineButton = findViewById<Button>(R.id.locationRationalDecline)

        actionButton.setOnClickListener {
            viewModel.onLocationClick()
        }
        missingHint.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }
        rationalConfirmButton.setOnClickListener {
            viewModel.onRationalConfirmed()
        }
        rationalDeclineButton.setOnClickListener {
            viewModel.onRationalDeclined()
        }

        // Update view based on ViewModel output
        viewModel.getViewData().observe(this) { viewData ->
            locationText.text = viewData.location
            missingHint.isGone = !viewData.showPermissionHint
            rationalView.isGone = !viewData.showRational
        }

        // Single event flow triggered by ViewModel when an action needs to be done
        // in this case launch the permission flow
        lifecycleScope.launchWhenResumed {
            viewModel.getEventData().collect { eventData ->
                when (eventData) {
                    AdvanceEventData.RequestPermission -> locationPermissionLauncher.launch()
                }
            }
        }
    }
}