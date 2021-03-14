package dev.marcelpinto.permissionktx.multiple

import android.Manifest
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import dev.marcelpinto.permissionktx.R
import dev.marcelpinto.permissionktx.areGranted
import dev.marcelpinto.permissionktx.getRevoked
import dev.marcelpinto.permissionktx.registerForMultiplePermissionResult

class MultipleActivity : AppCompatActivity(R.layout.simple_fragment) {

    private val locationPermissionsRequest =
        registerForMultiplePermissionResult(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) { results ->
            showLocation(!results.containsValue(false))
        }

    private var isFinding = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLocation(false)
        val button = findViewById<Button>(R.id.button_show_location)
        button.setOnClickListener {
            if (isFinding) {
                showLocation(false)
                return@setOnClickListener
            }
            locationPermissionsRequest.safeLaunch(
                onRequirePermissions = {
                    showLocation(false)
                    true
                },
                onRequireRational = {
                    Snackbar.make(
                        findViewById(R.id.root_view),
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
        val title = findViewById<TextView>(R.id.textview_title)
        val button = findViewById<Button>(R.id.button_show_location)

        isFinding = show
        if (show) {
            title.text = "Finding location..."
            button.text = "Stop location"
        } else {
            title.text = if (locationPermissionsRequest.types.areGranted()) {
                "Location tracking stopped"
            } else {
                val missing = locationPermissionsRequest.types.getRevoked().joinToString(separator = "\n") {
                    it.name
                }
                "Missing location permission:\n\n$missing"
            }
            button.text = "Show location"
        }
    }
}