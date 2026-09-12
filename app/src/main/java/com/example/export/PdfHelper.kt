package com.example.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.CardWithDistribution
import com.example.data.ShopProfile
import com.example.print.PrintOption
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfHelper {

    private const val PAGE_WIDTH = 595 // A4 width in points (72 dpi)
    private const val PAGE_HEIGHT = 842 // A4 height in points (72 dpi)
    private const val MARGIN_LEFT = 36f
    private const val MARGIN_RIGHT = 559f
    private const val MARGIN_TOP = 36f
    private const val MARGIN_BOTTOM = 806f
    private const val CONTENT_WIDTH = MARGIN_RIGHT - MARGIN_LEFT // 523f

    /**
     * Generates a multi-page A4 PDF file from shop profile and card list.
     */
    fun generatePdfReport(
        context: Context,
        profile: ShopProfile,
        monthYear: String,
        option: PrintOption,
        cards: List<CardWithDistribution>
    ): File {
        val filteredCards = when (option) {
            PrintOption.ALL_SERVED -> cards.filter { it.isServed }
            PrintOption.ALL_UNSERVED -> cards.filter { !it.isServed }
            PrintOption.ALL_SERVED_AND_UNSERVED -> cards
        }

        val document = PdfDocument()

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 0.75f
            color = Color.rgb(203, 213, 225)
        }

        val totalFamilies = filteredCards.size
        val totalMembers = filteredCards.sumOf { it.numberOfMembers }
        val servedCount = filteredCards.count { it.isServed }
        val unservedCount = filteredCards.count { !it.isServed }
        val syncDateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

        val rowHeight = 18f
        val tableHeaderHeight = 22f

        // Column widths matching 523f total width:
        // Sl: 24, Card No: 76, Cat: 38, Head Name: 115, Mbrs: 30, Mobile: 72, Status: 64, Served DateTime: 104
        val colWidths = floatArrayOf(24f, 76f, 38f, 115f, 30f, 72f, 64f, 104f)
        val colHeaders = arrayOf("SL", "CARD NO", "CAT", "HEAD OF FAMILY", "MBR", "MOBILE NO", "STATUS", "SERVED DATE & TIME")

        // First page has top header (~120pt) + summary (~45pt) + bottom footer (~35pt)
        // Usable height on Page 1 = 842 - 36 - 36 - 120 - 45 - 35 = ~570 pt -> ~28 rows
        // Usable height on Sub-pages = 842 - 36 - 36 - 40 - 25 = ~705 pt -> ~36 rows
        val firstPageCapacity = 25
        val otherPageCapacity = 35

        var remainingCards = filteredCards
        var pageNumber = 1
        val totalPages = if (filteredCards.isEmpty()) 1 else {
            if (filteredCards.size <= firstPageCapacity) 1
            else 1 + Math.ceil((filteredCards.size - firstPageCapacity).toDouble() / otherPageCapacity).toInt()
        }

        var cardIndex = 0

        while (cardIndex < filteredCards.size || (pageNumber == 1 && filteredCards.isEmpty())) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            var currentY = MARGIN_TOP

            if (pageNumber == 1) {
                // --- Page 1 Header Banner ---
                fillPaint.color = Color.rgb(30, 58, 138) // Deep Blue #1E3A8A
                canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 3f, fillPaint)
                currentY += 14f

                boldPaint.textSize = 14f
                boldPaint.color = Color.rgb(30, 58, 138)
                canvas.drawText("PDS SMART REGISTER - MONTHLY DISTRIBUTION REPORT", MARGIN_LEFT, currentY, boldPaint)

                textPaint.textSize = 9f
                textPaint.color = Color.rgb(100, 116, 139)
                val monthText = "Month: $monthYear | Type: ${option.label}"
                canvas.drawText(monthText, MARGIN_RIGHT - textPaint.measureText(monthText), currentY, textPaint)
                currentY += 14f

                // Shop info box
                fillPaint.color = Color.rgb(248, 250, 252) // #F8FAFC
                canvas.drawRoundRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 38f, 4f, 4f, fillPaint)
                canvas.drawRoundRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 38f, 4f, 4f, strokePaint)

                boldPaint.textSize = 10f
                boldPaint.color = Color.rgb(15, 23, 42)
                canvas.drawText(profile.shopName.ifBlank { "Fair Price Shop" }, MARGIN_LEFT + 8f, currentY + 12f, boldPaint)

                textPaint.textSize = 8.5f
                textPaint.color = Color.rgb(51, 65, 85)
                val line1 = "Proprietor: ${profile.proprietorName.ifBlank { "—" }}  |  FPS Code: ${profile.fpsCode.ifBlank { "—" }}  |  Licence: ${profile.licenceNo.ifBlank { "—" }}"
                canvas.drawText(line1, MARGIN_LEFT + 8f, currentY + 23f, textPaint)

                val line2 = "Area / Village: ${profile.area.ifBlank { "—" }}  |  Address: ${profile.shopAddress.ifBlank { "—" }}"
                canvas.drawText(line2, MARGIN_LEFT + 8f, currentY + 33f, textPaint)

                currentY += 46f

                // Summary Statistics Bar (4 columns)
                val sumBoxWidth = (CONTENT_WIDTH - 18f) / 4f
                val stats = listOf(
                    Triple("TOTAL CARDS", "$totalFamilies", Color.rgb(30, 58, 138)),
                    Triple("ALL MEMBERS", "$totalMembers", Color.rgb(2, 132, 199)),
                    Triple("SERVED FAMILIES", "$servedCount", Color.rgb(21, 128, 61)),
                    Triple("UNSERVED", "$unservedCount", Color.rgb(185, 28, 28))
                )

                for (i in stats.indices) {
                    val boxX = MARGIN_LEFT + i * (sumBoxWidth + 6f)
                    fillPaint.color = Color.rgb(241, 245, 249)
                    canvas.drawRoundRect(boxX, currentY, boxX + sumBoxWidth, currentY + 28f, 4f, 4f, fillPaint)
                    canvas.drawRoundRect(boxX, currentY, boxX + sumBoxWidth, currentY + 28f, 4f, 4f, strokePaint)

                    textPaint.textSize = 7f
                    textPaint.color = Color.rgb(100, 116, 139)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(stats[i].first, boxX + 6f, currentY + 10f, textPaint)

                    boldPaint.textSize = 11f
                    boldPaint.color = stats[i].third
                    canvas.drawText(stats[i].second, boxX + 6f, currentY + 23f, boldPaint)
                }

                currentY += 36f
            } else {
                // Subsequent page header
                boldPaint.textSize = 9f
                boldPaint.color = Color.rgb(30, 58, 138)
                canvas.drawText("PDS Smart Register • $monthYear • ${option.label}", MARGIN_LEFT, currentY + 10f, boldPaint)

                textPaint.textSize = 8f
                textPaint.color = Color.rgb(100, 116, 139)
                val pageIndicator = "Page $pageNumber of $totalPages"
                canvas.drawText(pageIndicator, MARGIN_RIGHT - textPaint.measureText(pageIndicator), currentY + 10f, textPaint)

                currentY += 18f
                fillPaint.color = Color.rgb(203, 213, 225)
                canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + 1f, fillPaint)
                currentY += 8f
            }

            // --- Table Header ---
            fillPaint.color = Color.rgb(30, 58, 138)
            canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + tableHeaderHeight, fillPaint)

            boldPaint.color = Color.WHITE
            boldPaint.textSize = 7.5f
            var colX = MARGIN_LEFT
            for (i in colHeaders.indices) {
                val headerText = colHeaders[i]
                val textX = if (i == 0 || i == 3) {
                    colX + (colWidths[i] - boldPaint.measureText(headerText)) / 2f
                } else {
                    colX + 4f
                }
                canvas.drawText(headerText, textX, currentY + 14f, boldPaint)
                colX += colWidths[i]
            }
            currentY += tableHeaderHeight

            // --- Rows for this page ---
            val pageCapacity = if (pageNumber == 1) firstPageCapacity else otherPageCapacity
            val endIndex = Math.min(cardIndex + pageCapacity, filteredCards.size)

            if (filteredCards.isEmpty()) {
                textPaint.textSize = 9f
                textPaint.color = Color.rgb(100, 116, 139)
                val emptyMsg = "No records found for ${option.label}"
                canvas.drawText(emptyMsg, MARGIN_LEFT + (CONTENT_WIDTH - textPaint.measureText(emptyMsg)) / 2f, currentY + 20f, textPaint)
                currentY += 30f
            } else {
                for (i in cardIndex until endIndex) {
                    val card = filteredCards[i]
                    val isEven = (i % 2 == 0)

                    // Row background
                    fillPaint.color = if (isEven) Color.rgb(248, 250, 252) else Color.WHITE
                    canvas.drawRect(MARGIN_LEFT, currentY, MARGIN_RIGHT, currentY + rowHeight, fillPaint)

                    // Row bottom border
                    strokePaint.color = Color.rgb(226, 232, 240)
                    canvas.drawLine(MARGIN_LEFT, currentY + rowHeight, MARGIN_RIGHT, currentY + rowHeight, strokePaint)

                    // Values
                    var curColX = MARGIN_LEFT

                    // 1. SL No
                    textPaint.textSize = 8f
                    textPaint.color = Color.rgb(51, 65, 85)
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    val slText = "${i + 1}"
                    canvas.drawText(slText, curColX + (colWidths[0] - textPaint.measureText(slText)) / 2f, currentY + 12f, textPaint)
                    curColX += colWidths[0]

                    // 2. Card No
                    boldPaint.textSize = 8f
                    boldPaint.color = Color.rgb(30, 58, 138)
                    boldPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    val cardNoText = card.cardNo.take(16)
                    canvas.drawText(cardNoText, curColX + 3f, currentY + 12f, boldPaint)
                    curColX += colWidths[1]

                    // 3. Category
                    boldPaint.textSize = 8f
                    boldPaint.color = Color.rgb(30, 58, 138)
                    boldPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val catText = card.category.ifBlank { "PHH" }
                    canvas.drawText(catText, curColX + (colWidths[2] - boldPaint.measureText(catText)) / 2f, currentY + 12f, boldPaint)
                    curColX += colWidths[2]

                    // 3. Head of Family Name
                    boldPaint.textSize = 8f
                    boldPaint.color = Color.rgb(15, 23, 42)
                    boldPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    val nameText = if (card.headOfFamilyName.length > 22) card.headOfFamilyName.take(21) + "…" else card.headOfFamilyName
                    canvas.drawText(nameText, curColX + 3f, currentY + 12f, boldPaint)
                    curColX += colWidths[3]

                    // 4. Members
                    textPaint.textSize = 8f
                    textPaint.color = Color.rgb(15, 23, 42)
                    val mbrText = "${card.numberOfMembers}"
                    canvas.drawText(mbrText, curColX + (colWidths[4] - textPaint.measureText(mbrText)) / 2f, currentY + 12f, textPaint)
                    curColX += colWidths[4]

                    // 5. Mobile No
                    textPaint.textSize = 7.5f
                    textPaint.color = Color.rgb(71, 85, 105)
                    val mobText = card.mobileNo.ifBlank { "—" }
                    canvas.drawText(mobText, curColX + 3f, currentY + 12f, textPaint)
                    curColX += colWidths[5]

                    // 6. Status Badge
                    if (card.isServed) {
                        fillPaint.color = Color.rgb(220, 252, 231) // Green bg
                        canvas.drawRoundRect(curColX + 2f, currentY + 3f, curColX + colWidths[6] - 2f, currentY + rowHeight - 3f, 3f, 3f, fillPaint)
                        boldPaint.textSize = 6.5f
                        boldPaint.color = Color.rgb(21, 128, 61) // Green text
                        val sText = "SERVED"
                        canvas.drawText(sText, curColX + (colWidths[6] - boldPaint.measureText(sText)) / 2f, currentY + 11.5f, boldPaint)
                    } else {
                        fillPaint.color = Color.rgb(254, 226, 226) // Red bg
                        canvas.drawRoundRect(curColX + 2f, currentY + 3f, curColX + colWidths[6] - 2f, currentY + rowHeight - 3f, 3f, 3f, fillPaint)
                        boldPaint.textSize = 6.5f
                        boldPaint.color = Color.rgb(185, 28, 28) // Red text
                        val sText = "UNSERVED"
                        canvas.drawText(sText, curColX + (colWidths[6] - boldPaint.measureText(sText)) / 2f, currentY + 11.5f, boldPaint)
                    }
                    curColX += colWidths[6]

                    // 7. Served Date & Time
                    textPaint.textSize = 6.8f
                    textPaint.color = Color.rgb(71, 85, 105)
                    val servedTime = if (card.isServed && card.servedDateTime.isNotBlank()) {
                        card.servedDateTime
                    } else if (card.isServed) {
                        syncDateStr
                    } else {
                        "— (Nondrawal)"
                    }
                    canvas.drawText(servedTime, curColX + 3f, currentY + 11.5f, textPaint)

                    currentY += rowHeight
                }
            }

            // --- Bottom Footer ---
            val footerY = MARGIN_BOTTOM - 6f
            strokePaint.color = Color.rgb(203, 213, 225)
            canvas.drawLine(MARGIN_LEFT, footerY - 14f, MARGIN_RIGHT, footerY - 14f, strokePaint)

            textPaint.textSize = 7.5f
            textPaint.color = Color.rgb(100, 116, 139)
            val footerLeft = "Generated on: $syncDateStr • PDS Smart Register"
            canvas.drawText(footerLeft, MARGIN_LEFT, footerY, textPaint)

            val pageStr = "Page $pageNumber of $totalPages"
            canvas.drawText(pageStr, MARGIN_RIGHT - textPaint.measureText(pageStr), footerY, textPaint)

            if (pageNumber == 1) {
                // Authorized Signature line on Page 1
                val signText = "Dealer / Authorized Signature: __________________"
                canvas.drawText(signText, MARGIN_LEFT + (CONTENT_WIDTH - textPaint.measureText(signText)) / 2f, footerY, textPaint)
            }

            document.finishPage(page)

            cardIndex = endIndex
            pageNumber++
        }

        // Save PDF to cache/exports directory
        val fileName = "PDS_Register_${monthYear.replace("-", "_")}.pdf"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)

        FileOutputStream(file).use { outputStream ->
            document.writeTo(outputStream)
        }
        document.close()

        return file
    }

    /**
     * Saves PDF into user's Public Downloads folder and opens the Share/Save to Drive sheet.
     */
    fun saveAndSharePdf(
        context: Context,
        profile: ShopProfile,
        monthYear: String,
        option: PrintOption,
        cards: List<CardWithDistribution>
    ): File {
        val file = generatePdfReport(context, profile, monthYear, option, cards)

        // Also copy into public MediaStore Downloads so it is directly accessible in Device Files / Drive
        try {
            saveToPublicDownloads(context, file, "application/pdf")
        } catch (_: Exception) {}

        sharePdfFile(context, file)
        return file
    }

    fun sharePdfFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "PDS Register PDF - ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Save / Share PDF (Drive, Files, WhatsApp)"))
    }

    fun saveToPublicDownloads(context: Context, sourceFile: File, mimeType: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.name)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PDSRegister")
            }
        }

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val uri = resolver.insert(collection, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { out ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }
        }
        return uri
    }
}
