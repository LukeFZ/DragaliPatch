package com.lukefz.dragaliafound.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.composable.AppContainer
import com.lukefz.dragaliafound.composable.SpacedLine
import com.lukefz.dragaliafound.navigation.NavScreens
import com.lukefz.dragaliafound.utils.Constants

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(navController: NavController, model: MainScreenViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigate(NavScreens.Patcher.route) }
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = "About button")
                    }
                }
            )
        },
        floatingActionButton = {
            if (model.isPatchable) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(NavScreens.Patcher.route) },
                    icon = { Icon(Icons.Filled.PlayArrow, "Start button") },
                    text = { Text(stringResource(R.string.activity_patcher_step_patch)) },
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

                AppContainer(model.originalAppInfo, model.originalAppIcon)

                SpacedLine(2.dp)

                AppContainer(model.patchedAppInfo, model.patchedAppIcon)

                SpacedLine(2.dp)

                OutlinedTextField(
                    modifier = Modifier.padding(
                        top = 4.dp,
                        start = 2.dp,
                        end = 2.dp
                    ),
                    singleLine = true,
                    value = model.customServerUrl,
                    onValueChange = {
                                    if (it.length <= Constants.URL_MAX_LENGTH)
                                        model.customServerUrl = it
                    },
                    label = { Text(stringResource(R.string.activity_main_custom_server)) }
                )
            }
        }
    )
}