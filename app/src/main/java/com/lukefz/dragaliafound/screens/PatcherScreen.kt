package com.lukefz.dragaliafound.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.work.WorkManager
import com.lukefz.dragaliafound.R
import com.lukefz.dragaliafound.patcher.PatcherWorker

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatcherScreen(controller: NavController, model: PatcherScreenViewModel = viewModel()) {
    val workerStates by WorkManager.getInstance(LocalContext.current).getWorkInfosForUniqueWorkLiveData(PatcherWorker.Tag).observeAsState(mutableListOf())
    val textScroll = rememberScrollState()

    if (workerStates.size > 0) {
        val workerState = workerStates[0]
        model.updateFromWorker(workerState)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = model.currentProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "Patch progress"
    )

    LaunchedEffect(Unit) {
        model.startWorker()
    }

    LaunchedEffect(workerStates) {
        textScroll.scrollTo(textScroll.maxValue)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (model.hasFailed)
                        Text(stringResource(id = R.string.activity_patcher_title_failed))
                    else if (model.hasFinished)
                        Text(stringResource(id = R.string.activity_patcher_completed))
                    else
                        Text(stringResource(id = R.string.activity_patcher_title))
                },
            )
        },
        floatingActionButton = {
            if (model.hasFailed || model.hasFinished) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (model.hasFailed) {
                            model.shareLog(model.logString)
                        } else {
                            model.installPatchedApp()
                        }
                    },
                    icon = {
                        if (model.hasFailed)
                            Icon(Icons.Filled.Share, "Share the error")
                        else
                            Icon(Icons.Filled.Done, "Install the patched app")

                    },
                    text = {
                        if (model.hasFailed)
                            Text(stringResource(R.string.patcher_share_error))
                        else
                            Text(stringResource(R.string.install))
                        //else
                        //    Text(stringResource(R.string.dialog_cancel))
                    }
                )
            }
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
                        model.logString,
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .verticalScroll(textScroll)
                    )
                }
            }

            Spacer(Modifier.requiredHeight(16.dp))

            if (!model.hasFailed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            bottom = 16.dp
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(model.currentStep)
                    Spacer(Modifier.size(4.dp))
                    LinearProgressIndicator(
                        progress = animatedProgress,
                    )
                }
            }
        }
    }
}