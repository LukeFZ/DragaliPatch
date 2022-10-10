package com.lukefz.dragaliafound.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    val logText by remember { model.logMessages }
    val currentStep by remember { model.currentStep }
    val progress by remember { model.currentProgress }

    val hasFailed by remember { model.hasFailed }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Scaffold(
        floatingActionButton = {
            if (hasFailed) {
                ExtendedFloatingActionButton(
                    onClick = { controller.navigate(NavScreens.Main.route) },
                    icon = { Icon(Icons.Filled.ArrowBack, "Back arrow") },
                    text = { Text(stringResource(R.string.patcher_back)) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(
                    top = 8.dp,
                    start = 6.dp,
                    end = 6.dp,
                    bottom = 8.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .weight(weight = 1f, fill = false)
            ) {
                Card(shape = RoundedCornerShape(4.dp)) {
                    Text(logText)
                }
            }

            Spacer(Modifier.requiredHeight(24.dp))
            //SpacedLine(width = 4.dp)

            if (!hasFailed) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(currentStep)
                    LinearProgressIndicator(
                        progress = animatedProgress,
                    )
                }
            }
        }
    }
}