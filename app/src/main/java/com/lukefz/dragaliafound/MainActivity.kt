package com.lukefz.dragaliafound

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.twotone.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.lukefz.dragaliafound.ui.theme.DragaliPatchTheme
import com.lukefz.dragaliafound.utils.Constants
import com.lukefz.dragaliafound.utils.StorageUtil

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

    var customServerUrl = Constants.DEFAULT_CUSTOM_URL

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val storage = StorageUtil(applicationContext)

        var isPatchable = false
        var originalAppInfo: String
        var originalAppIcon: Drawable? = null
        var patchedAppInfo: String
        var patchedAppIcon: Drawable? = null

        try {
            val originalApp = packageManager.getPackageInfo(Constants.PACKAGE_NAME, 0)
            originalAppInfo = getString(R.string.activity_app_version_info, originalApp.versionName, originalApp.versionCode).plus("\n")
            if (originalApp.versionCode == Constants.SUPPORTED_PACKAGE_VERSION) {
                isPatchable = true
            }

            originalAppInfo = originalAppInfo.plus(getString(R.string.activity_app_is_patchable, isPatchable.toString()))
            originalAppIcon = originalApp.applicationInfo.loadIcon(packageManager)
        } catch (ex: Exception) {
            isPatchable = false
            originalAppInfo = "\nOriginal game not installed!\n"
        }

        try {
            val patchedApp = packageManager.getPackageInfo(Constants.PATCHED_PACKAGE_NAME, 0)
            patchedAppInfo = getString(R.string.activity_app_version_info, patchedApp.versionName, patchedApp.versionCode)
            patchedAppIcon = patchedApp.applicationInfo.loadIcon(packageManager)
        } catch (_: Exception) {
            patchedAppInfo = "\nPatched app not installed!\n"
        }



        setContent {
            DragaliPatchTheme {
                // A surface container using the 'background' color from the theme
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(getString(R.string.app_name)) },
                            navigationIcon = {
                                IconButton(
                                    onClick = { /*TODO*/ }
                                ) {
                                    Icon(Icons.Filled.Info, contentDescription = "About button")
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        if (isPatchable) {
                            ExtendedFloatingActionButton(
                                onClick = {/* TODO */},
                                icon = { Icon(Icons.Filled.PlayArrow, "Start button")},
                                text = { Text(getString(R.string.activity_patcher_step_patch))},
                            )
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End,
                    content = { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {

                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(originalAppInfo)

                                if (originalAppIcon != null) {
                                    Image(
                                        rememberDrawablePainter(drawable = originalAppIcon),
                                        contentDescription = "Dragalia Lost Icon")
                                }
                            }

                            Spacer(modifier = Modifier.size(2.dp))
                            Divider(thickness = 1.dp)
                            Spacer(modifier = Modifier.size(2.dp))

                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(patchedAppInfo)

                                if (patchedAppIcon != null) {
                                    Image(
                                        rememberDrawablePainter(drawable = patchedAppIcon),
                                        contentDescription = "Patched App Icon")
                                }
                            }

                            Spacer(modifier = Modifier.size(2.dp))
                            Divider(thickness = 1.dp)
                            Spacer(modifier = Modifier.size(2.dp))

                            OutlinedTextField(
                                value = customServerUrl,
                                onValueChange = { customServerUrl = it },
                                label = { Text(getString(R.string.activity_main_custom_server))}
                            )
                    }
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Column {
        Row() {
            Image(imageVector = Icons.TwoTone.Build, contentDescription = "icon")
            Column() {
                Text("text 1")
                Text("text 2")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    DragaliPatchTheme {
        Greeting("Android")
    }
}