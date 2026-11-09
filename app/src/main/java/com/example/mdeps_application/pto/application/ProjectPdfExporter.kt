package com.example.mdeps_application.pto.application

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.mdeps_application.R
import com.example.mdeps_application.pto.domain.Project
import com.example.mdeps_application.pto.domain.ProjectStatus
import com.example.mdeps_application.pto.domain.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ProjectPdfExporter(
    private val appContext: Context,
) {

    suspend fun exportProjectReport(
        project: Project,
        tasks: List<Task>,
    ): Uri = withContext(Dispatchers.IO) {
        val pdfDirectory = File(appContext.cacheDir, PDF_DIRECTORY).apply { mkdirs() }
        val pdfFile = File(pdfDirectory, "project_${project.id}_${System.currentTimeMillis()}.pdf")

        val document = PdfDocument()
        var pageNumber = 1
        var currentPage = startPage(document, pageNumber)
        var canvas = currentPage.canvas
        var cursorY = PAGE_MARGIN_TOP

        cursorY = drawHeader(canvas, project, cursorY)
        cursorY = drawProjectDetails(canvas, project, cursorY)
        cursorY = drawTasksHeader(canvas, cursorY)

        tasks.forEach { task ->
            if (cursorY > PAGE_HEIGHT - PAGE_MARGIN_BOTTOM) {
                document.finishPage(currentPage)
                pageNumber += 1
                currentPage = startPage(document, pageNumber)
                canvas = currentPage.canvas
                cursorY = PAGE_MARGIN_TOP
            }
            cursorY = drawTaskLine(canvas, task, cursorY)
        }

        document.finishPage(currentPage)
        FileOutputStream(pdfFile).use { outputStream ->
            document.writeTo(outputStream)
        }
        document.close()

        FileProvider.getUriForFile(
            appContext,
            fileProviderAuthority(appContext),
            pdfFile,
        )
    }

    private fun startPage(document: PdfDocument, pageNumber: Int): PdfDocument.Page {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        return document.startPage(pageInfo)
    }

    private fun drawHeader(canvas: Canvas, project: Project, startY: Float): Float {
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 24f
            isFakeBoldText = true
        }
        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f
        }

        var y = startY
        canvas.drawText(appContext.getString(R.string.pto_pdf_report_title), PAGE_MARGIN_LEFT, y, titlePaint)
        y += 32f
        canvas.drawText(project.name, PAGE_MARGIN_LEFT, y, titlePaint)
        y += 24f
        canvas.drawText(
            "${appContext.getString(R.string.pto_pdf_status_label)}: ${project.status.toLocalizedLabel(appContext)}",
            PAGE_MARGIN_LEFT,
            y,
            subtitlePaint,
        )
        return y + 28f
    }

    private fun drawProjectDetails(canvas: Canvas, project: Project, startY: Float): Float {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
        }

        var y = startY
        canvas.drawText(appContext.getString(R.string.pto_pdf_description_label), PAGE_MARGIN_LEFT, y, labelPaint)
        y += 18f
        project.description.lines().forEach { line ->
            canvas.drawText(line, PAGE_MARGIN_LEFT, y, bodyPaint)
            y += 16f
        }
        return y + 8f
    }

    private fun drawTasksHeader(canvas: Canvas, startY: Float): Float {
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 16f
            isFakeBoldText = true
        }
        canvas.drawText(appContext.getString(R.string.pto_pdf_tasks_header), PAGE_MARGIN_LEFT, startY, headerPaint)
        return startY + 24f
    }

    private fun drawTaskLine(canvas: Canvas, task: Task, startY: Float): Float {
        val taskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
        }
        val completionLabel = if (task.isCompleted) {
            appContext.getString(R.string.pto_pdf_task_completed)
        } else {
            appContext.getString(R.string.pto_pdf_task_pending)
        }
        val evidenceLabel = if (task.evidencePhotoUri.isNullOrBlank()) {
            appContext.getString(R.string.pto_pdf_evidence_no)
        } else {
            appContext.getString(R.string.pto_pdf_evidence_yes)
        }

        val line = "• ${task.title} — $completionLabel — $evidenceLabel"
        canvas.drawText(line, PAGE_MARGIN_LEFT, startY, taskPaint)
        return startY + 18f
    }

    private fun ProjectStatus.toLocalizedLabel(context: Context): String =
        when (this) {
            ProjectStatus.PLANNING -> context.getString(R.string.pto_status_planning)
            ProjectStatus.IN_PROGRESS -> context.getString(R.string.pto_status_in_progress)
            ProjectStatus.AT_RISK -> context.getString(R.string.pto_status_at_risk)
            ProjectStatus.COMPLETED -> context.getString(R.string.pto_status_completed)
        }

    companion object {
        private const val PDF_DIRECTORY = "pdfs"
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val PAGE_MARGIN_LEFT = 40f
        private const val PAGE_MARGIN_TOP = 56f
        private const val PAGE_MARGIN_BOTTOM = 56f

        fun fileProviderAuthority(context: Context): String =
            "${context.packageName}.fileprovider"
    }
}
