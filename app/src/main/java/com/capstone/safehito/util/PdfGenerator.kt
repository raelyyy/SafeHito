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

    val titlePaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = 24f
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

    val bodyPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        textSize = 14f
        textAlign = Paint.Align.LEFT
    }

    val borderPaint = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    canvas.drawRect(RectF(20f, 20f, pageWidth - 20f, pageHeight - 20f), borderPaint)
    canvas.drawText("SafeHito Fish Health Report", pageWidth / 2f, 60f, titlePaint)
    canvas.drawText("Diagnostic Certificate", pageWidth / 2f, 85f, subtitlePaint)

    try {
        val logo = BitmapFactory.decodeResource(context.resources, com.capstone.safehito.R.drawable.logo)
        val scaledLogo = Bitmap.createScaledBitmap(logo, 50, 50, true)
        canvas.drawBitmap(scaledLogo, 40f, 30f, null)
    } catch (_: Exception) {}

    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    val formattedTime = try {
        if (record.timestamp < 1000000000000L)
            sdf.format(Date(record.timestamp * 1000))
        else
            sdf.format(Date(record.timestamp))
    } catch (_: Exception) {
        "Invalid date"
    }

    val diagnosisDetails = when {
        record.result.contains("cotton", true) -> "Saprolegnia spp. – Cotton-like fungal growth on skin or fins"
        record.result.contains("reddish", true) -> "Bacterial infection – Reddish patches or skin lesions"
        record.result.contains("whitepatch", true) -> "White Patch – White spot disease on skin"
        record.result.contains("ulcer", true) -> "Bacterial ulcer – Open wounds with bacterial infection"
        record.result.contains("fungal", true) -> "Various fungal species – General fungal infection signs"
        record.result.contains("healthy", true) -> "No pathogens detected – Fish appear healthy"
        record.result.contains("no fish", true) -> "No fish detected – No visible specimen"
        else -> "Unknown – No further details"
    }


    var y = 130f
    listOf(
        "Date Issued: $formattedTime",
        "",
        "This certifies that the fish specimen was analyzed using the SafeHito system.",
        "The following diagnostic information was obtained from the scan:",
        "",
        "Diagnosis:",
        " - ${record.result}",
        " - Confidence Level: ${(record.confidence * 100).toInt()}%",
        "",
        "Scientific Name & Description:",
        " - $diagnosisDetails"
    ).forEach {
        canvas.drawText(it, 40f, y, bodyPaint)
        y += 20f
    }

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

            val originalWidth = bitmap.width
            val originalHeight = bitmap.height

            val ratio = minOf(
                maxWidth / originalWidth.toFloat(),
                maxHeight / originalHeight.toFloat()
            )

            val scaledWidth = (originalWidth * ratio).toInt()
            val scaledHeight = (originalHeight * ratio).toInt()

            val resized = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

            // Create a bitmap with reduced rounded corners
            val roundedBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
            val canvasRound = Canvas(roundedBitmap)
            val paint = Paint().apply {
                isAntiAlias = true
                shader = BitmapShader(resized, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            }
            val rect = RectF(0f, 0f, scaledWidth.toFloat(), scaledHeight.toFloat())
            canvasRound.drawRoundRect(rect, 10f, 10f, paint) // << Smaller corner radius

            // Align to the left margin (e.g., 40f)
            val xPos = 40f

            // Draw onto the PDF canvas
            canvas.drawBitmap(roundedBitmap, xPos, y, null)

            y += scaledHeight + 30f
        }

    } catch (e: Exception) {
        canvas.drawText("❌ Failed to load image: ${e.message}", 40f, y, bodyPaint)
        y += 30f
    }

    y += 40f
    canvas.drawLine(60f, y, 260f, y, bodyPaint)
    canvas.drawText("Authorized Signature", 60f, y + 20f, bodyPaint)

    pdfDocument.finishPage(page)

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
