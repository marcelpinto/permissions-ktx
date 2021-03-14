package dev.marcelpinto.permissionktx

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.marcelpinto.permissionktx.advance.AdvanceActivity
import dev.marcelpinto.permissionktx.compose.ComposeActivity
import dev.marcelpinto.permissionktx.multiple.MultipleActivity
import dev.marcelpinto.permissionktx.simple.SimpleActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colors.background) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            start(SimpleActivity::class.java)
                        }) {
                            Text("Simple Sample")
                        }
                        Button(onClick = {
                            start(MultipleActivity::class.java)
                        }) {
                            Text("Multiple Permissions Sample")
                        }
                        Button(onClick = {
                            start(AdvanceActivity::class.java)
                        }) {
                            Text("Advance Sample")
                        }
                        Button(onClick = {
                            start(ComposeActivity::class.java)
                        }) {
                            Text("Compose Sample")
                        }
                    }
                }
            }
        }
    }

    private fun start(clazz: Class<*>) {
        startActivity(Intent(this@MainActivity, clazz))
    }
}