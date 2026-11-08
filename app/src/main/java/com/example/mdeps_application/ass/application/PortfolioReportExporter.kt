package com.example.mdeps_application.ass.application

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.mdeps_application.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AnalyticsExportFormat {
    CSV,
    PDF_SUMMARY,
}

enum class AnalyticsExportRange {
    LAST_15_DAYS,
    QUARTER,
    SEMESTER,
}

class PortfolioReportExporter(
    private val context: Context,
) {
    fun export(
        uiState: AnalyticsUiState,
        range: AnalyticsExportRange,
        format: AnalyticsExportFormat,
        includeSquadBreakdown: Boolean,
    ): Uri {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = when (format) {
            AnalyticsExportFormat.CSV -> "vantage_pmo_portfolio_$stamp.csv"
            AnalyticsExportFormat.PDF_SUMMARY -> "vantage_pmo_portfolio_$stamp.txt"
        }
        val file = File(context.cacheDir, fileName)
        val rangeLabel = when (range) {
            AnalyticsExportRange.LAST_15_DAYS -> "last_15_days"
            AnalyticsExportRange.QUARTER -> "quarter"
            AnalyticsExportRange.SEMESTER -> "semester"
        }
        val header = context.getString(R.string.ass_export_file_header, rangeLabel)
        val body = buildString {
            appendLine(header)
            appendLine("efficiency,${uiState.operativeEfficiency}")
            appendLine("spi,${uiState.schedulePerformanceIndex}")
            appendLine("cpi,${uiState.costPerformanceIndex}")
            appendLine("global_progress,${uiState.globalProgress}")
            appendLine("critical_risks,${uiState.activeCriticalRisks}")
            appendLine("projects_total,${uiState.totalProjects}")
            appendLine("projects_on_track,${uiState.projectsOnTrack}")
            appendLine("projects_at_risk,${uiState.projectsAtRisk}")
            appendLine("projects_moderate,${uiState.projectsModerateExposure}")
            appendLine("open_tasks,${uiState.openTasks}")
            if (includeSquadBreakdown) {
                appendLine("squad_pto_tasks,${uiState.completedTasks}/${uiState.openTasks + uiState.completedTasks}")
                appendLine("squad_gro_risks,${uiState.activeCriticalRisks}")
            }
            uiState.projectsWithHighRisks.forEach { alert ->
                appendLine("risk_project,${alert.projectId},${alert.projectName},${alert.activeHighRiskCount}")
            }
        }
        file.writeText(body)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }
}
