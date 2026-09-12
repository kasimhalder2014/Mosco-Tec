package com.example.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.CardWithDistribution
import com.example.data.RationCard
import com.example.data.ShopProfile
import com.example.print.PrintOption
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelCsvHelper {

    /**
     * Exports cards and distribution state to a CSV file compatible with Excel.
     */
    fun exportCardsToCsv(
        context: Context,
        profile: ShopProfile,
        monthYear: String,
        cards: List<CardWithDistribution>,
        option: PrintOption = PrintOption.ALL_SERVED_AND_UNSERVED
    ): File {
        val filteredCards = when (option) {
            PrintOption.ALL_SERVED -> cards.filter { it.isServed }
            PrintOption.ALL_UNSERVED -> cards.filter { !it.isServed }
            PrintOption.ALL_SERVED_AND_UNSERVED -> cards
        }

        val fileName = "PDS_Register_${monthYear.replace("-", "_")}.csv"
        val exportDir = File(context.cacheDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, fileName)

        val totalFamilies = filteredCards.size
        val totalMembers = filteredCards.sumOf { it.numberOfMembers }
        val servedCount = filteredCards.count { it.isServed }
        val unservedCount = filteredCards.count { !it.isServed }
        val syncDateStr = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())

        FileOutputStream(file).bufferedWriter().use { writer ->
            // Shop Details Header
            writer.write("\"PDS SMART REGISTER - MONTHLY DISTRIBUTION REPORT\"\n")
            writer.write("\"Shop Name:\",\"${escapeCsv(profile.shopName)}\",\"Report Type:\",\"${option.label}\"\n")
            writer.write("\"Proprietor/Secretary:\",\"${escapeCsv(profile.proprietorName)}\",\"Month:\",\"$monthYear\"\n")
            writer.write("\"FPS Code:\",\"${escapeCsv(profile.fpsCode)}\",\"Licence No:\",\"${escapeCsv(profile.licenceNo)}\"\n")
            writer.write("\"Area / Village:\",\"${escapeCsv(profile.area)}\",\"Shop Address:\",\"${escapeCsv(profile.shopAddress)}\"\n")
            writer.write("\"Generated On:\",\"$syncDateStr\"\n\n")

            // Summary row
            writer.write("\"SUMMARY STATISTICS\"\n")
            writer.write("\"Total Registered Cards:\",\"$totalFamilies\",\"Total Beneficiaries:\",\"$totalMembers\",\"Served Families:\",\"$servedCount\",\"Unserved Families:\",\"$unservedCount\"\n\n")

            // Column Headers
            writer.write("\"Sl No\",\"Ration Card No\",\"Category\",\"Head of Family Name\",\"Number of Members\",\"Mobile No\",\"Status\",\"Served Date and Time\"\n")

            // Records
            filteredCards.forEachIndexed { index, item ->
                val status = if (item.isServed) "SERVED" else "UNSERVED (Nondrawal)"
                val servedTime = if (item.isServed && item.servedDateTime.isNotBlank()) item.servedDateTime else if (item.isServed) syncDateStr else "— (Not Drawn)"
                writer.write(
                    "\"${index + 1}\",\"${escapeCsv(item.cardNo)}\",\"${escapeCsv(item.category.ifBlank { "PHH" })}\",\"${escapeCsv(item.headOfFamilyName)}\",\"${item.numberOfMembers}\",\"${escapeCsv(item.mobileNo)}\",\"$status\",\"${escapeCsv(servedTime)}\"\n"
                )
            }
        }

        // Save copy to MediaStore Downloads
        try {
            saveToPublicDownloads(context, file, "text/csv")
        } catch (_: Exception) {}

        return file
    }

    fun saveAndShareCsv(
        context: Context,
        profile: ShopProfile,
        monthYear: String,
        cards: List<CardWithDistribution>,
        option: PrintOption = PrintOption.ALL_SERVED_AND_UNSERVED
    ): File {
        val file = exportCardsToCsv(context, profile, monthYear, cards, option)
        shareExportedFile(context, file)
        return file
    }

    fun shareExportedFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "PDS Register Excel Export - ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Save / Share Excel File (Drive, Files, Sheets)"))
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

    /**
     * Parses an input stream of CSV content and extracts RationCard records.
     * Expected columns:
     * Card No, Head of Family Name, Category, Number of Members, Mobile No
     */
    fun parseCsvCards(inputStream: InputStream): List<RationCard> {
        val result = mutableListOf<RationCard>()
        val lines = inputStream.bufferedReader().readLines()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            val tokens = parseCsvLine(trimmed)
            if (tokens.size < 4) continue

            val col0 = tokens[0].trim()
            // Skip header row if matches common terms
            if (col0.contains("Card", ignoreCase = true) || col0.contains("Sl", ignoreCase = true) || col0.contains("Shop", ignoreCase = true) || col0.contains("PDS", ignoreCase = true) || col0.contains("Summary", ignoreCase = true)) {
                continue
            }

            var cardNo = ""
            var name = ""
            var category = "PHH"
            var members = 1
            var mobile = ""

            if (tokens.size >= 8 && tokens[1].isNotBlank() && (tokens[1].contains("RC") || tokens[1].all { it.isDigit() || it.isLetter() })) {
                // Exported CSV format: Sl No(0), Ration Card No(1), Category(2), Head of Family Name(3), Number of Members(4), Mobile No(5)
                cardNo = tokens[1]
                category = tokens[2].ifBlank { "PHH" }
                name = tokens[3]
                members = tokens[4].toIntOrNull() ?: 1
                mobile = tokens[5]
            } else if (tokens.size >= 5) {
                // Format: Card No(0), Name(1), Category(2), Members(3), Mobile(4)
                // Or Name(0), Card No(1), Category(2), Members(3), Mobile(4)
                val possibleCat = tokens[2].uppercase()
                if (possibleCat in listOf("PHH", "SPHH", "AAY", "RKSY1", "RKSY2") || tokens[3].toIntOrNull() != null) {
                    cardNo = tokens[0]
                    name = tokens[1]
                    category = tokens[2].ifBlank { "PHH" }
                    members = tokens[3].toIntOrNull() ?: 1
                    mobile = tokens[4]
                } else if (tokens[1].uppercase() in listOf("PHH", "SPHH", "AAY", "RKSY1", "RKSY2")) {
                    name = tokens[0]
                    cardNo = tokens[1]
                    category = tokens[2].ifBlank { "PHH" }
                    members = tokens[3].toIntOrNull() ?: 1
                    mobile = tokens[4]
                } else {
                    cardNo = tokens[0]
                    name = tokens[1]
                    category = tokens[2].ifBlank { "PHH" }
                    members = tokens[3].toIntOrNull() ?: 1
                    mobile = tokens[4]
                }
            } else if (tokens.size == 4) {
                // 4 columns fallback: Card No(0), Name(1), Members(2), Mobile(3)
                cardNo = tokens[0]
                name = tokens[1]
                category = "PHH"
                members = tokens[2].toIntOrNull() ?: 1
                mobile = tokens[3]
            }

            if (cardNo.isNotEmpty() && name.isNotEmpty()) {
                result.add(
                    RationCard(
                        cardNo = cardNo,
                        headOfFamilyName = name,
                        category = category,
                        numberOfMembers = members.coerceAtLeast(1),
                        mobileNo = mobile
                    )
                )
            }
        }
        return result
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (i in line.indices) {
            val c = line[i]
            if (c == '\"') {
                inQuotes = !inQuotes
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim().removeSurrounding("\""))
                sb.clear()
            } else {
                sb.append(c)
            }
        }
        result.add(sb.toString().trim().removeSurrounding("\""))
        return result
    }

    /**
     * Sample template text for import demo or manual entry.
     */
    val sampleCsvTemplate = """
Card No,Head of Family Name,Category,Number of Members,Mobile No
RC90031121,Prakash Kumar,PHH,4,9830012345
RC90031122,Sumitra Paul,AAY,5,9830023456
RC90031123,Harun Al Rashid,SPHH,6,9830034567
RC90031124,Manju Devi,RKSY1,3,9830045678
RC90031125,Bikram Roy,RKSY2,4,9830056789
    """.trimIndent()
}
