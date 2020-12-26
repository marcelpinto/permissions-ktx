package dev.marcelpinto.permissionktx

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment(R.layout.fragment_main) {

    private val locationPermissionRequest =
            registerForPermissionResult(Manifest.permission.ACCESS_FINE_LOCATION) { granted ->
                showLocation(granted)
            }

    private var isFinding = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                                view,
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
        with(requireView()) {
            val button = findViewById<Button>(R.id.button_show_location)
            val title = findViewById<TextView>(R.id.textview_title)
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
}