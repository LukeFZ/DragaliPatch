package com.lukefz.dragaliafound

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.lukefz.dragaliafound.navigation.NavGraph
import com.lukefz.dragaliafound.patcher.PatcherWorker
import com.lukefz.dragaliafound.ui.theme.DragaliPatchTheme

class MainActivity : ComponentActivity() {
    private var initial = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!initial) {
            WorkManager.getInstance(applicationContext).cancelAllWorkByTag(PatcherWorker.Tag)
            initial = true
        }

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