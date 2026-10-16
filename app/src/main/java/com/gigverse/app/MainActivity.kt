package com.gigverse.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gigverse.app.ui.navigation.GigverseAppRoot
import com.gigverse.app.ui.theme.MyApplicationTheme
import com.gigverse.app.ui.viewmodel.GigverseViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: GigverseViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      enableEdgeToEdge()
    } catch (e: Throwable) {
      Log.e("MainActivity", "Edge-to-edge configuration error", e)
    }

    try {
      setContent {
        val isDarkTheme by viewModel.isDarkTheme.collectAsState()
        MyApplicationTheme(darkTheme = isDarkTheme) {
          GigverseAppRoot(viewModel = viewModel)
        }
      }
    } catch (t: Throwable) {
      Log.e("MainActivity", "Error in setContent UI root", t)
    }
  }
}

