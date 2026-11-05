package com.example.mdeps_application.pto.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.mdeps_application.R

object PtoPdfShare {

    fun launchPdfShareOrView(context: Context, pdfUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = PDF_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserTitle = context.getString(R.string.pto_export_pdf_chooser_title)
        val chooser = Intent.createChooser(shareIntent, chooserTitle)

        if (shareIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
            return
        }

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(pdfUri, PDF_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (viewIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(viewIntent)
            return
        }

        throw ActivityNotFoundException(context.getString(R.string.pto_export_pdf_no_app))
    }

    private const val PDF_MIME_TYPE = "application/pdf"
}
