package com.sagrd.mentorly.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object CertificatePdfGenerator {

    fun generateAndSharePdf(
        context: Context,
        studentName: String,
        courseTitle: String,
        enrollmentId: String,
        completionDate: String
    ) {
        val width = 1200
        val height = 850
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        canvas.drawColor(Color.rgb(250, 250, 252))

        val paint = Paint().apply {
            isAntiAlias = true
        }

        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(2, 136, 209)
        paint.strokeWidth = 8f
        canvas.drawRoundRect(RectF(40f, 40f, width - 40f, height - 40f), 24f, 24f, paint)

        paint.color = Color.rgb(217, 119, 6)
        paint.strokeWidth = 2f
        canvas.drawRoundRect(RectF(55f, 55f, width - 55f, height - 55f), 16f, 16f, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.CENTER

        paint.color = Color.rgb(2, 136, 209)
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText("MENTORLY LMS", width / 2f, 115f, paint)

        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 42f
        paint.isFakeBoldText = true
        canvas.drawText("CERTIFICADO DE FINALIZACIÓN", width / 2f, 185f, paint)

        paint.color = Color.rgb(217, 119, 6)
        paint.strokeWidth = 3f
        canvas.drawLine(width / 2f - 180f, 210f, width / 2f + 180f, 210f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText("El presente certificado se otorga con honor y distinción a:", width / 2f, 270f, paint)

        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 46f
        paint.isFakeBoldText = true
        canvas.drawText(studentName.ifBlank { "Estudiante de Mentorly" }, width / 2f, 345f, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1.5f
        canvas.drawLine(width / 2f - 260f, 365f, width / 2f + 260f, 365f, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 22f
        paint.isFakeBoldText = false
        canvas.drawText(
            "Por haber completado satisfactoriamente el 100% de los requisitos académicos,",
            width / 2f,
            425f,
            paint
        )
        canvas.drawText(
            "evaluaciones y entregas del curso certificado:",
            width / 2f,
            460f,
            paint
        )

        paint.color = Color.rgb(2, 136, 209)
        paint.textSize = 34f
        paint.isFakeBoldText = true
        canvas.drawText(courseTitle.ifBlank { "Curso Mentorly" }, width / 2f, 530f, paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 18f
        paint.isFakeBoldText = false
        val formattedDate = if (completionDate.isNotBlank()) "Fecha de emisión: $completionDate" else "Acreditación Oficial Mentorly"
        canvas.drawText(formattedDate, width / 2f, 600f, paint)

        val cleanEnrollmentId = if (enrollmentId.isNotBlank()) "ID de Acreditación: $enrollmentId" else ""
        if (cleanEnrollmentId.isNotBlank()) {
            paint.textSize = 15f
            paint.color = Color.rgb(148, 163, 184)
            canvas.drawText(cleanEnrollmentId, width / 2f, 630f, paint)
        }

        paint.color = Color.rgb(148, 163, 184)
        paint.strokeWidth = 1.5f
        canvas.drawLine(200f, 720f, 460f, 720f, paint)
        canvas.drawLine(width - 460f, 720f, width - 200f, 720f, paint)

        paint.textSize = 17f
        paint.color = Color.rgb(71, 85, 105)
        paint.isFakeBoldText = true
        canvas.drawText("Comité Académico", 330f, 745f, paint)
        canvas.drawText("Mentorly Platform", width - 330f, 745f, paint)

        paint.textSize = 14f
        paint.color = Color.rgb(148, 163, 184)
        paint.isFakeBoldText = false
        canvas.drawText("Aprobación Digital", 330f, 768f, paint)
        canvas.drawText("Certificación Oficial", width - 330f, 768f, paint)

        pdfDocument.finishPage(page)

        val sanitizedTitle = courseTitle.take(20).replace(Regex("[^a-zA-Z0-9]"), "_")
        val fileName = "Certificado_${sanitizedTitle}_${enrollmentId.take(6)}.pdf"
        val file = File(context.cacheDir, fileName)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Certificado - $courseTitle")
            putExtra(Intent.EXTRA_TEXT, "¡He completado con éxito el curso '$courseTitle' en Mentorly! Adjunto mi certificado oficial.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(shareIntent, "Compartir Certificado Digital")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
