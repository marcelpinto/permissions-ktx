package dev.marcelpinto.permissionktx.simple

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import dev.marcelpinto.permissionktx.R
import dev.marcelpinto.permissionktx.isPermissionGranted
import dev.marcelpinto.permissionktx.registerForPermissionResult

class SimpleFragment : Fragment(R.layout.simple_fragment) {

    private val locationPermissionRequest =
        registerForPermissionResult(Manifest.permission.ACCESS_FINE_LOCATION) { granted ->
            showLocation(granted)
        }

    private var isFinding = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showLocation(false)
        val button = view.findViewById<Button>(R.id.button_show_location)
        button.setOnClickListener {
            if (isFinding) {
                showLocation(false)
                return@setOnClickListener
            }
            locationPermissionRequest.safeLaunch(
                onRequirePermission = {
                    showLocation(false)
                    true
                },
                onRequireRational = {
                    Snackbar.make(
                        requireView(),
                        "I need location permission, trust me :)",
                        Snackbar.LENGTH_LONG
                    ).setAction("Ok") {
                        launch()
                    }.show()
                },
                onAlreadyGranted = {
                    showLocation(true)
                }
            )
        }
    }

    private fun showLocation(show: Boolean) {
        val title = requireView().findViewById<TextView>(R.id.textview_title)
        val button = requireView().findViewById<Button>(R.id.button_show_location)

        isFinding = show
        if (show) {
            title.text = "Finding location..."
            button.text = "Stop location"
        } else {
            title.text = if (locationPermissionRequest.name.isPermissionGranted()) {
                "Location tracking stopped"
            } else {
                "Missing location permission"
            }
            button.text = "Show location"
        }
    }
}