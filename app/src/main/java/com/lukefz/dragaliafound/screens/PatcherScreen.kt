package com.lukefz.dragaliafound.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.navigation.NavScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatcherScreen(controller: NavController, model: PatcherScreenViewModel = viewModel()) {
    val state = model.state

    val animatedProgress by animateFloatAsState(
        targetValue = state.currentProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    LaunchedEffect(true) {
        model.startPatch()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.hasFailed)
                        Text(stringResource(id = R.string.activity_patcher_title_failed))
                    else
                        Text(stringResource(id = R.string.activity_patcher_title))
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { controller.navigate(NavScreens.Main.route) },
                icon = { Icon(Icons.Filled.ArrowBack, "Back arrow") },
                text = {
                    if (state.hasFailed)
                        Text(stringResource(R.string.patcher_back))
                    else
                        Text(stringResource(R.string.dialog_cancel))
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(weight = 1f, fill = false)
                    .fillMaxWidth()
                    .height(900.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(720.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        state.logMessages,
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }

            Spacer(Modifier.requiredHeight(16.dp))
            //SpacedLine(width = 4.dp)

            if (!state.hasFailed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            bottom = 16.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.currentStep)
                    Spacer(Modifier.size(4.dp))
                    LinearProgressIndicator(
                        progress = animatedProgress,
                    )
                }
            }
        }
    }
}