package com.capstone.safehito.util

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.provider.MediaStore
import android.widget.Toast
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

suspend fun generateMedicalPDF(context: Context, record: com.capstone.safehito.ui.Record) {
    val pdfDocument = PdfDocument()
    val pageWidth = 595
    val pageHeight = 842
    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    // ===== TEXT STYLES =====
    val titlePaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = 26f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    val subtitlePaint = Paint().apply {
        isAntiAlias = true
        color = Color.DKGRAY
        textSize = 16f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
    }

    val headerPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = 15f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }

    val bodyPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = 14f
        textAlign = Paint.Align.LEFT
        typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
    }

    val borderPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    // ===== BORDER & HEADER =====
    canvas.drawRect(RectF(20f, 20f, pageWidth - 20f, pageHeight - 20f), borderPaint)
    canvas.drawText("SafeHito Fish Health Report", pageWidth / 2f, 60f, titlePaint)
    canvas.drawText("Diagnostic Certificate", pageWidth / 2f, 85f, subtitlePaint)

    // ===== LOGO =====
    try {
        val logo = BitmapFactory.decodeResource(context.resources, com.capstone.safehito.R.drawable.logo)
        val scaledLogo = Bitmap.createScaledBitmap(logo, 50, 50, true)
        canvas.drawBitmap(scaledLogo, 40f, 30f, null)
    } catch (_: Exception) {}

    // ===== DATE FORMAT =====
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val formattedTime = try {
        if (record.timestamp < 1000000000000L)
            sdf.format(Date(record.timestamp * 1000))
        else
            sdf.format(Date(record.timestamp))
    } catch (_: Exception) {
        "Invalid date"
    }

    // ===== DIAGNOSIS DETAILS =====
    val (scientificName, description) = when {
        record.result.contains("cotton", true) -> "Saprolegnia spp." to "Cotton-like fungal growth on skin or fins."
        record.result.contains("whitepatch", true) -> "Possible early fungal infection" to "Flat white patches that may indicate fungal infection."
        record.result.contains("reddish", true) -> "Possible secondary infection" to "Reddish areas from irritation or bacterial co-infection."
        record.result.contains("ulcer", true) -> "Skin ulcer / necrotic lesion" to "Open sores often linked to advanced fungal or bacterial infection."
        record.result.contains("fungal", true) -> "Various fungal species" to "General fungal infection signs."
        record.result.contains("healthy", true) -> "No pathogens detected" to "Fish appear healthy."
        record.result.contains("no fish", true) -> "No fish detected" to "No visible specimen."
        else -> "Unknown" to "No further details available."
    }

    var y = 130f

    // ===== DATE =====
    canvas.drawText("Date Issued", 40f, y, headerPaint)
    y += 18f
    canvas.drawText(formattedTime, 40f, y, bodyPaint)
    y += 35f

    // ===== CERTIFICATION STATEMENT =====
    canvas.drawText("Certification Statement", 40f, y, headerPaint)
    y += 30f

    val certStatement = "I hereby certify that the findings below are based on AI-assisted " +
            "image analysis of the submitted sample. This may be suggestive of a fungal infection " +
            "(e.g., Saprolegnia) but is not definitive without laboratory confirmation."

    // Wrap text properly if it's long
    val certLines = certStatement.split(" ")
    var line = ""
    for (word in certLines) {
        val testLine = if (line.isEmpty()) word else "$line $word"
        if (bodyPaint.measureText(testLine) < 500) {
            line = testLine
        } else {
            canvas.drawText(line, 40f, y, bodyPaint)
            y += 20f
            line = word
        }
    }
    if (line.isNotEmpty()) {
        canvas.drawText(line, 40f, y, bodyPaint)
        y += 40f
    }


    // ===== DIAGNOSIS =====
    canvas.drawText("Diagnosis", 40f, y, headerPaint)
    y += 22f
    canvas.drawText("Result:  ${record.result}", 40f, y, bodyPaint)
    y += 18f
    canvas.drawText("Confidence Level:  ${(record.confidence * 100).toInt()}%", 40f, y, bodyPaint)
    y += 40f

    // ===== SCIENTIFIC NAME =====
    canvas.drawText("Lesion Name", 40f, y, headerPaint)
    y += 22f
    canvas.drawText(scientificName, 40f, y, bodyPaint)
    y += 40f

    // ===== DESCRIPTION =====
    canvas.drawText("Description", 40f, y, headerPaint)
    y += 22f
    canvas.drawText(description, 40f, y, bodyPaint)
    y += 40f

    // ===== IMAGE SECTION =====
    try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(record.image_url)
            .allowHardware(false)
            .build()
        val result = loader.execute(request)
        val drawable = (result as? SuccessResult)?.drawable
        val bitmap = (drawable as? BitmapDrawable)?.bitmap

        if (bitmap != null) {
            val maxWidth = 280
            val maxHeight = 180

            val ratio = minOf(
                maxWidth / bitmap.width.toFloat(),
                maxHeight / bitmap.height.toFloat()
            )

            val scaledWidth = (bitmap.width * ratio).toInt()
            val scaledHeight = (bitmap.height * ratio).toInt()

            val resized = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

            val roundedBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
            val canvasRound = Canvas(roundedBitmap)
            val paint = Paint().apply {
                isAntiAlias = true
                shader = BitmapShader(resized, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
            val rect = RectF(0f, 0f, scaledWidth.toFloat(), scaledHeight.toFloat())
            canvasRound.drawRoundRect(rect, 12f, 12f, paint)

            canvas.drawBitmap(roundedBitmap, 40f, y, null)
            y += scaledHeight + 40f
        }

    } catch (e: Exception) {
        canvas.drawText("⚠️ Failed to load image: ${e.message}", 40f, y, bodyPaint)
        y += 30f
    }

    // ===== SIGNATURE =====
    y += 30f
    canvas.drawLine(60f, y, 260f, y, bodyPaint)
    canvas.drawText("Authorized Signature", 60f, y + 20f, bodyPaint)

    pdfDocument.finishPage(page)

    // ===== SAVE PDF =====
    val filename = "SafeHito_Report_${System.currentTimeMillis()}.pdf"
    try {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/SafeHito")
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {
            resolver.openOutputStream(it)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "✅ PDF saved!", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "❌ Failed to create file.", Toast.LENGTH_LONG).show()
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    } finally {
        pdfDocument.close()
    }
}
