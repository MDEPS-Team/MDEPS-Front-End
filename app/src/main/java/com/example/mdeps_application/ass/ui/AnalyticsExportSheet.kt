@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mdeps_application.ass.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mdeps_application.R
import com.example.mdeps_application.ass.application.AnalyticsExportFormat
import com.example.mdeps_application.ass.application.AnalyticsExportRange
import com.example.mdeps_application.ass.application.AnalyticsUiState
import com.example.mdeps_application.ass.application.PortfolioReportExporter
import com.example.mdeps_application.ass.domain.ExecutiveReportSummary
import com.example.mdeps_application.ui.Caption
import com.example.mdeps_application.ui.Vx
import com.example.mdeps_application.ui.VxCard

@Composable
fun AnalyticsExportSheet(
    visible: Boolean,
    uiState: AnalyticsUiState,
    executiveReports: List<ExecutiveReportSummary>,
    onDismiss: () -> Unit,
) {
    if (!visible) return

    val context = LocalContext.current
    val exporter = PortfolioReportExporter(context)
    var range by rememberSaveable { mutableStateOf(AnalyticsExportRange.QUARTER) }
    var format by rememberSaveable { mutableStateOf(AnalyticsExportFormat.CSV) }
    var includeSquad by rememberSaveable { mutableStateOf(true) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Caption(text = stringResource(R.string.ass_export_eyebrow), color = Vx.Blue)
            Text(
                text = stringResource(R.string.ass_export_sheet_title),
                fontWeight = FontWeight.Bold,
            )
            Text(text = stringResource(R.string.ass_export_range_label), fontWeight = FontWeight.SemiBold)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                rangeOptions().forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = range == option,
                        onClick = { range = option },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = rangeOptions().size),
                        label = { Text(stringResource(exportRangeLabelRes(option))) },
                    )
                }
            }
            Text(text = stringResource(R.string.ass_export_format_label), fontWeight = FontWeight.SemiBold)
            formatOptions().forEach { option ->
                val selected = format == option
                VxCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { format = option },
                    containerColor = if (selected) Vx.BlueSoft else Vx.Bg,
                ) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (selected) {
                            androidx.compose.material3.Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Vx.Blue,
                            )
                        }
                        Column(Modifier.padding(start = if (selected) 8.dp else 0.dp)) {
                            Text(stringResource(exportFormatTitleRes(option)), fontWeight = FontWeight.Bold)
                            Text(stringResource(exportFormatDescRes(option)), color = Vx.Muted)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { includeSquad = !includeSquad }) {
                Checkbox(checked = includeSquad, onCheckedChange = { includeSquad = it })
                Text(stringResource(R.string.ass_export_include_squad))
            }
            Text(stringResource(R.string.ass_export_security_note), color = Vx.Muted)
            if (executiveReports.isNotEmpty()) {
                Text(stringResource(R.string.ass_export_server_reports), fontWeight = FontWeight.SemiBold)
                executiveReports.take(5).forEach { report ->
                    VxCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(report.title, fontWeight = FontWeight.Bold)
                            Text(report.subtitle, color = Vx.Muted)
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.ass_export_server_reports_empty), color = Vx.Muted)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.ass_export_cancel))
                }
                Button(
                    onClick = {
                        val uri = exporter.export(uiState, range, format, includeSquad)
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/*"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(share, context.getString(R.string.ass_export_chooser)))
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.ass_export_download))
                }
            }
        }
    }
}

private fun rangeOptions() = listOf(
    AnalyticsExportRange.LAST_15_DAYS,
    AnalyticsExportRange.QUARTER,
    AnalyticsExportRange.SEMESTER,
)

private fun formatOptions() = listOf(
    AnalyticsExportFormat.CSV,
    AnalyticsExportFormat.PDF_SUMMARY,
)

private fun exportRangeLabelRes(range: AnalyticsExportRange): Int = when (range) {
    AnalyticsExportRange.LAST_15_DAYS -> R.string.ass_export_range_15d
    AnalyticsExportRange.QUARTER -> R.string.ass_export_range_quarter
    AnalyticsExportRange.SEMESTER -> R.string.ass_export_range_semester
}

private fun exportFormatTitleRes(format: AnalyticsExportFormat): Int = when (format) {
    AnalyticsExportFormat.CSV -> R.string.ass_export_format_csv_title
    AnalyticsExportFormat.PDF_SUMMARY -> R.string.ass_export_format_pdf_title
}

private fun exportFormatDescRes(format: AnalyticsExportFormat): Int = when (format) {
    AnalyticsExportFormat.CSV -> R.string.ass_export_format_csv_desc
    AnalyticsExportFormat.PDF_SUMMARY -> R.string.ass_export_format_pdf_desc
}
