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