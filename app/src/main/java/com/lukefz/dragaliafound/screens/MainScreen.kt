package com.lukefz.dragaliafound.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.composable.AppContainer
import com.lukefz.dragaliafound.composable.SpacedLine
import com.lukefz.dragaliafound.navigation.NavScreens
import com.lukefz.dragaliafound.utils.ApiProvidedValues
import com.lukefz.dragaliafound.utils.Constants

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MainScreen(navController: NavController, model: MainScreenViewModel = viewModel()) {
    val serverUrl by remember { model.customServerUrl }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                /*navigationIcon = {
                    IconButton(
                        onClick = { navController.navigate(NavScreens.Patcher.route) }
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = "About button")
                    }
                }*/
            )
        },
        floatingActionButton = {
            if (model.isPatchable && model.estimateApiUrlLength() <= Constants.URL_MAX_LENGTH && serverUrl.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        ApiProvidedValues.apiUrl = model.customServerUrl.value
                        navController.navigate(NavScreens.Patcher.route)
                    },
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
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                AppContainer(model.originalAppInfo, model.originalAppIcon)

                SpacedLine(2.dp)

                AppContainer(model.patchedAppInfo, model.patchedAppIcon)

                SpacedLine(2.dp)

                OutlinedTextField(
                    modifier = Modifier
                        .padding(
                            top = 4.dp,
                            start = 2.dp,
                            end = 2.dp
                        )
                        .fillMaxWidth(),
                    singleLine = true,
                    value = serverUrl,
                    onValueChange = {
                                    if (it.length <= Constants.URL_MAX_LENGTH)
                                        model.customServerUrl.value = it
                    },
                    isError = model.estimateApiUrlLength() > Constants.URL_MAX_LENGTH,
                    label = { Text(stringResource(R.string.activity_main_custom_server)) }
                )

                /*Spacer(Modifier.size(4.dp))

                Button(
                    modifier = Modifier
                        .padding(
                            top = 4.dp,
                            start = 2.dp,
                            end = 2.dp
                        )
                        .fillMaxWidth(),
                    onClick = { model.clearDeviceAccount() },
                    enabled = model.enableClearDeviceAccountButton,

                ) {
                    Text(stringResource(R.string.utility_clear_device_acount))
                }*/

                SpacedLine(4.dp)

                Column(
                    modifier = Modifier
                        .padding(
                            top = 4.dp,
                            start = 2.dp,
                            end = 2.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.activity_about_credits),
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    Spacer(Modifier.size(4.dp))

                    ClickableText(
                        modifier = Modifier.padding(start = 4.dp),
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(MaterialTheme.colorScheme.primary)) {
                                append(stringResource(R.string.activity_about_credits_based_on))
                            }
                        },
                        onClick = {
                            model.openWebsite(Constants.PROJECT_EARTH_GITHUB_URL)
                        }
                    )

                    Spacer(Modifier.size(4.dp))

                    ClickableText(
                        modifier = Modifier.padding(start = 4.dp),
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(MaterialTheme.colorScheme.primary)) {
                                append(stringResource(R.string.activity_about_credits_app_creator))
                            }
                        },
                        onClick = {
                            model.openWebsite(Constants.GITHUB_URL)
                        }
                    )

                    SpacedLine(8.dp)

                    Text(
                        text = stringResource(R.string.activity_about_special_thanks),
                        style = MaterialTheme.typography.headlineSmall,
                    )

                    Spacer(Modifier.size(4.dp))

                    Text(stringResource(R.string.activity_about_custom_server_creators))
                    Text(stringResource(R.string.activity_about_custom_server_creators_reason))
                }
            }
        }
    )
}