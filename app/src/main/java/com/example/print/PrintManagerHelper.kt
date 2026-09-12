package com.example.print

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.data.CardWithDistribution
import com.example.data.ShopProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class PrintOption(val label: String) {
    ALL_SERVED("All Served"),
    ALL_UNSERVED("All Unserved"),
    ALL_SERVED_AND_UNSERVED("All Served and Unserved Details")
}

object PrintManagerHelper {

    /**
     * Generates HTML formatted strictly for an A4 sheet layout.
     * Requirement: "only 1st page bottom side Show shop details"
     */
    fun generateA4Html(
        profile: ShopProfile,
        monthYear: String,
        option: PrintOption,
        cards: List<CardWithDistribution>
    ): String {
        val filteredCards = when (option) {
            PrintOption.ALL_SERVED -> cards.filter { it.isServed }
            PrintOption.ALL_UNSERVED -> cards.filter { !it.isServed }
            PrintOption.ALL_SERVED_AND_UNSERVED -> cards
        }

        val currentDeviceSyncTime = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date())

        val totalFamilies = filteredCards.size
        val totalMembers = filteredCards.sumOf { it.numberOfMembers }
        val servedCount = filteredCards.count { it.isServed }
        val unservedCount = filteredCards.count { !it.isServed }

        val rowsHtml = StringBuilder()
        if (filteredCards.isEmpty()) {
            rowsHtml.append("""
                <tr>
                    <td colspan="7" style="text-align: center; padding: 24px; color: #64748b; font-style: italic;">
                        No records found for the selected criteria (${option.label})
                    </td>
                </tr>
            """.trimIndent())
        } else {
            filteredCards.forEachIndexed { index, card ->
                val statusBadge = if (card.isServed) {
                    """<span style="background:#dcfce7;color:#15803d;padding:2px 8px;border-radius:4px;font-weight:600;font-size:11px;">SERVED</span>"""
                } else {
                    """<span style="background:#fee2e2;color:#b91c1c;padding:2px 8px;border-radius:4px;font-weight:600;font-size:11px;">UNSERVED (Nondrawal)</span>"""
                }

                val servedTimeDisplay = if (card.isServed && card.servedDateTime.isNotBlank()) {
                    card.servedDateTime
                } else if (card.isServed) {
                    currentDeviceSyncTime
                } else {
                    "— (Not Drawn)"
                }

                rowsHtml.append("""
                    <tr style="border-bottom: 1px solid #e2e8f0;">
                        <td style="text-align: center; padding: 6px 4px; font-size: 11px;">${index + 1}</td>
                        <td style="padding: 6px 6px; font-size: 12px; font-weight: 600; font-family: monospace;">${card.cardNo}</td>
                        <td style="padding: 6px 6px; font-size: 12px; font-weight: 600;">${card.headOfFamilyName}</td>
                        <td style="text-align: center; padding: 6px 4px; font-size: 12px;">${card.numberOfMembers}</td>
                        <td style="padding: 6px 6px; font-size: 11px; font-family: monospace;">${card.mobileNo.ifBlank { "—" }}</td>
                        <td style="text-align: center; padding: 6px 4px;">$statusBadge</td>
                        <td style="padding: 6px 6px; font-size: 11px; color: #334155;">$servedTimeDisplay</td>
                    </tr>
                """.trimIndent())
            }
        }

        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<title>PDS Smart Register - ${option.label}</title>
<style>
  @page {
    size: A4 portrait;
    margin: 10mm 10mm 12mm 10mm;
  }
  @page:first {
    margin-bottom: 5mm;
  }
  body {
    font-family: 'Segoe UI', -apple-system, Roboto, Helvetica, Arial, sans-serif;
    color: #0f172a;
    background: #ffffff;
    margin: 0;
    padding: 0;
    font-size: 12px;
    line-height: 1.3;
  }
  .page-container {
    width: 100%;
    box-sizing: border-box;
    display: flex;
    flex-direction: column;
    min-height: 100%;
  }
  .header-banner {
    border-bottom: 2px solid #1e3a8a;
    padding-bottom: 8px;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }
  .main-title {
    font-size: 18px;
    font-weight: 800;
    color: #1e3a8a;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    margin: 0 0 4px 0;
  }
  .sub-title {
    font-size: 12px;
    font-weight: 600;
    color: #475569;
    margin: 0;
  }
  .meta-tag {
    text-align: right;
    font-size: 11px;
    color: #64748b;
  }
  .meta-tag strong {
    color: #0f172a;
  }
  .summary-cards-row {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }
  .summary-card {
    flex: 1;
    border: 1px solid #cbd5e1;
    background: #f8fafc;
    border-radius: 4px;
    padding: 6px 10px;
    text-align: center;
  }
  .summary-card .val {
    font-size: 16px;
    font-weight: 700;
    color: #1e3a8a;
  }
  .summary-card .lbl {
    font-size: 10px;
    font-weight: 600;
    color: #64748b;
    text-transform: uppercase;
  }
  table.data-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 16px;
  }
  table.data-table th {
    background-color: #1e3a8a;
    color: #ffffff;
    font-weight: 700;
    font-size: 11px;
    text-transform: uppercase;
    padding: 8px 6px;
    border: 1px solid #1e3a8a;
  }
  table.data-table td {
    border: 1px solid #e2e8f0;
  }
  table.data-table tr:nth-child(even) {
    background-color: #f8fafc;
  }

  /* CRITICAL REQUIREMENT: Only 1st page bottom side Show shop details */
  .first-page-bottom-shop-box {
    margin-top: auto;
    border: 1.5px solid #1e3a8a;
    border-radius: 6px;
    background: #f0fdf4;
    padding: 10px 14px;
    margin-bottom: 12px;
    page-break-inside: avoid;
  }
  .shop-box-title {
    font-size: 12px;
    font-weight: 800;
    color: #166534;
    text-transform: uppercase;
    border-bottom: 1px solid #86efac;
    padding-bottom: 4px;
    margin-bottom: 6px;
  }
  .shop-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 4px 16px;
    font-size: 11px;
  }
  .shop-grid-item strong {
    color: #1f2937;
  }
  .signature-row {
    display: flex;
    justify-content: space-between;
    margin-top: 14px;
    padding-top: 8px;
    border-top: 1px dashed #94a3b8;
    font-size: 11px;
    font-weight: 600;
  }
  .signature-col {
    text-align: center;
    width: 180px;
    padding-top: 28px;
    border-top: 1px solid #0f172a;
  }
</style>
</head>
<body>

<div class="page-container">
  <!-- Header Banner -->
  <div class="header-banner">
    <div>
      <div class="main-title">PDS Distribution & Nondrawal Register</div>
      <div class="sub-title">Monthly Ration Card Distribution Entry • ${option.label}</div>
    </div>
    <div class="meta-tag">
      <div>Register Month: <strong>$monthYear</strong></div>
      <div>Device Sync Time: <strong>$currentDeviceSyncTime</strong></div>
      <div>FPS Code: <strong>${profile.fpsCode}</strong></div>
    </div>
  </div>

  <!-- Summary Statistics -->
  <div class="summary-cards-row">
    <div class="summary-card">
      <div class="val">$totalFamilies</div>
      <div class="lbl">Total Families (Cards)</div>
    </div>
    <div class="summary-card">
      <div class="val">$totalMembers</div>
      <div class="lbl">Total Beneficiaries (Members)</div>
    </div>
    <div class="summary-card">
      <div class="val" style="color: #16a34a;">$servedCount</div>
      <div class="lbl">Served Families</div>
    </div>
    <div class="summary-card">
      <div class="val" style="color: #dc2626;">$unservedCount</div>
      <div class="lbl">Unserved (Nondrawal)</div>
    </div>
  </div>

  <!-- Table of Cards -->
  <table class="data-table">
    <thead>
      <tr>
        <th style="width: 5%;">Sl</th>
        <th style="width: 17%;">Ration Card No</th>
        <th style="width: 25%;">Head of Family Name</th>
        <th style="width: 9%;">Members</th>
        <th style="width: 15%;">Mobile No</th>
        <th style="width: 14%;">Status</th>
        <th style="width: 15%;">Served Date & Time</th>
      </tr>
    </thead>
    <tbody>
      $rowsHtml
    </tbody>
  </table>

  <!-- CRITICAL REQUIREMENT: Only 1st page bottom side Show shop details -->
  <div class="first-page-bottom-shop-box">
    <div class="shop-box-title">★ FAIR PRICE SHOP (FPS) DETAILS (Page 1 Registered Details)</div>
    <div class="shop-grid">
      <div class="shop-grid-item"><strong>Shop Name:</strong> ${profile.shopName}</div>
      <div class="shop-grid-item"><strong>Proprietor / Secretary:</strong> ${profile.proprietorName}</div>
      <div class="shop-grid-item"><strong>FPS Code:</strong> ${profile.fpsCode}</div>
      <div class="shop-grid-item"><strong>Licence Number:</strong> ${profile.licenceNo}</div>
      <div class="shop-grid-item"><strong>Area / Sector:</strong> ${profile.area}</div>
      <div class="shop-grid-item"><strong>Shop Address:</strong> ${profile.shopAddress}</div>
    </div>

    <!-- Authorized Signatures -->
    <div class="signature-row">
      <div class="signature-col">
        Signature / Seal of FPS Dealer
      </div>
      <div class="signature-col">
        Food Inspector / Sub-Divisional Officer
      </div>
    </div>
  </div>
</div>

</body>
</html>
        """.trimIndent()
    }

    /**
     * Triggers the Android Print Spooler with A4 print attributes.
     */
    fun printDocument(
        context: Context,
        profile: ShopProfile,
        monthYear: String,
        option: PrintOption,
        cards: List<CardWithDistribution>
    ) {
        val htmlContent = generateA4Html(profile, monthYear, option, cards)
        val webView = WebView(context).apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    val jobName = "PDS_Register_${monthYear}_${option.name}"
                    val printAdapter = view.createPrintDocumentAdapter(jobName)
                    val printAttributes = PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                        .build()
                    printManager?.print(jobName, printAdapter, printAttributes)
                }
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "utf-8", null)
    }
}
