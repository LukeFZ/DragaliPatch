package com.lukefz.dragaliafound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.lukefz.dragaliafound.navigation.NavGraph
import com.lukefz.dragaliafound.ui.theme.DragaliPatchTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Fix for OSDetection in apktool
        System.setProperty(
            "sun.arch.data.model",
            if (System.getProperty("os.arch")?.contains("64") == true) "64" else "32"
        )

        setContent {
            DragaliPatchTheme {
                // A surface container using the 'background' color from the theme
                Surface {
                    val controller = rememberNavController()
                    NavGraph(controller = controller)
                }
            }
        }
    }
}