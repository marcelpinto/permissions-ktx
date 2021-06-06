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

package dev.marcelpinto.permissionktx.compose

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface

/**
 * The compose example, shows a simple dialer app that follows the permission flow by:
 *   - Giving hint to user when permission is missing
 *   - Showing a rational if required (based on the status)
 *   - Requesting the permission
 *   - Launching the call once permission is granted
 */
class ComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colors.background) {
                    ComposePermissionScreen(this::startCall)
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