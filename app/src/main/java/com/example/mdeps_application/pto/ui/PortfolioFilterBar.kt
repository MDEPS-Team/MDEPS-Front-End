package com.example.mdeps_application.pto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectStatus

enum class PortfolioFilter {
    ALL,
    AT_RISK,
    IN_PROGRESS,
}

fun List<Project>.filterByPortfolio(filter: PortfolioFilter): List<Project> =
    when (filter) {
        PortfolioFilter.ALL -> this
        PortfolioFilter.AT_RISK -> filter { it.status == ProjectStatus.AT_RISK }
        PortfolioFilter.IN_PROGRESS -> filter { it.status == ProjectStatus.IN_PROGRESS }
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PortfolioFilterBar(
    selectedFilter: PortfolioFilter,
    onFilterSelected: (PortfolioFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PortfolioFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            PortfolioFilter.ALL -> stringResource(R.string.portfolio_filter_all)
                            PortfolioFilter.AT_RISK -> stringResource(R.string.portfolio_filter_at_risk)
                            PortfolioFilter.IN_PROGRESS -> stringResource(R.string.portfolio_filter_in_progress)
                        },
                    )
                },
            )
        }
    }
}
