@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.gro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mdeps_application.R
import com.example.mdeps_application.gro.application.GroViewModel
import com.example.mdeps_application.gro.domain.Risk
import com.example.mdeps_application.gro.domain.RiskSeverity
import com.example.mdeps_application.gro.domain.RiskStatus
import com.example.mdeps_application.ui.theme.MDEPS_APPLICATIONTheme

private val SwipeResolveBackgroundGreen = Color(0xFF2E7D32)

@Composable
fun RiskMatrixScreen(
    viewModel: GroViewModel,
    projectId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CollectGroUiEvents(viewModel = viewModel)
    val risks by viewModel.risksState.collectAsStateWithLifecycle()
    val navigateBackDescription = stringResource(R.string.gro_a11y_navigate_back)
    val seedDemoDescription = stringResource(R.string.gro_a11y_seed_demo_data)

    RiskMatrixContent(
        risks = risks,
        onRiskSwiped = viewModel::resolveRisk,
        onNavigateBack = onNavigateBack,
        onSeedDemoData = { viewModel.addCorporateDummyData(projectId) },
        navigateBackDescription = navigateBackDescription,
        seedDemoDescription = seedDemoDescription,
        modifier = modifier,
    )
}

@Composable
internal fun RiskMatrixContent(
    risks: List<Risk>,
    onRiskSwiped: (Risk) -> Unit,
    onNavigateBack: () -> Unit,
    onSeedDemoData: () -> Unit,
    navigateBackDescription: String,
    seedDemoDescription: String,
    modifier: Modifier = Modifier,
) {
    val swipeToResolveDescription = stringResource(R.string.gro_a11y_swipe_to_resolve)
    val swipeResolveIconDescription = stringResource(R.string.gro_a11y_swipe_resolve_icon)
    val riskCardDescription = stringResource(R.string.gro_a11y_risk_card)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.gro_risks_title),
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics { contentDescription = navigateBackDescription },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = navigateBackDescription,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSeedDemoData,
                        modifier = Modifier.semantics { contentDescription = seedDemoDescription },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = seedDemoDescription,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSeedDemoData,
                modifier = Modifier.semantics { contentDescription = seedDemoDescription },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = seedDemoDescription,
                )
            }
        },
    ) { innerPadding ->
        if (risks.isEmpty()) {
            Text(
                text = stringResource(R.string.gro_empty_risks),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(risks, key = { it.id }) { risk ->
                    SwipeableRiskRow(
                        risk = risk,
                        swipeToResolveDescription = swipeToResolveDescription,
                        swipeResolveIconDescription = swipeResolveIconDescription,
                        riskCardDescription = riskCardDescription,
                        onRiskSwiped = onRiskSwiped,
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeableRiskRow(
    risk: Risk,
    swipeToResolveDescription: String,
    swipeResolveIconDescription: String,
    riskCardDescription: String,
    onRiskSwiped: (Risk) -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onRiskSwiped(risk)
                true
            } else {
                false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = swipeToResolveDescription },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = SwipeResolveBackgroundGreen,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = swipeResolveIconDescription,
                    tint = Color.White,
                )
            }
        },
        content = {
            RiskCard(
                risk = risk,
                cardContentDescription = riskCardDescription,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun RiskMatrixScreenPreview() {
    MDEPS_APPLICATIONTheme {
        RiskMatrixContent(
            risks = listOf(
                Risk(
                    id = 1L,
                    projectId = 1L,
                    title = "Budget overrun",
                    description = "Forecasted spend exceeds approved cap for Q3.",
                    severity = RiskSeverity.CRITICAL,
                    status = RiskStatus.IDENTIFIED,
                ),
            ),
            onRiskSwiped = {},
            onNavigateBack = {},
            onSeedDemoData = {},
            navigateBackDescription = "Back",
            seedDemoDescription = "Seed",
        )
    }
}
