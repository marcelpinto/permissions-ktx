/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import dev.marcelpinto.permissionktx.registerForPermissionResult

class SimpleFragment : Fragment(R.layout.simple_fragment) {

    private val locationPermissionRequest =
        registerForPermissionResult(Manifest.permission.ACCESS_COARSE_LOCATION) { granted ->
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
            title.text = if (locationPermissionRequest.type.status.isGranted()) {
                "Location tracking stopped"
            } else {
                "Missing location permission"
            }
            button.text = "Show location"
        }
    }
}