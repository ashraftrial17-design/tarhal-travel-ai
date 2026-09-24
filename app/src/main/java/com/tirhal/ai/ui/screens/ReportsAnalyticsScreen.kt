package com.tirhal.ai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tirhal.ai.data.local.DatabaseProvider
import com.tirhal.ai.data.local.entity.BookingEntity
import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.data.local.entity.TripEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportsAnalyticsScreen(
    onOpenClientProfile: ((ClientEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { DatabaseProvider.getDatabase(context) }

    val clientsList by database.clientDao().getAllClients().collectAsState(initial = emptyList())
    val tripsList by database.tripDao().getAllTrips().collectAsState(initial = emptyList())
    val bookingsList by database.bookingDao().getAllBookings().collectAsState(initial = emptyList())

    var refreshKey by remember { mutableIntStateOf(0) }
    var selectedTimeFilter by remember { mutableStateOf("كل الفترة") }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "لوحة الملخص",
        "تحليل الحجوزات",
        "التحليل المالي",
        "تحليل العملاء",
        "الأعلى قيمة",
        "فرص المتابعة",
        "التقرير الشهري والمقارنة"
    )

    // Filter Bookings by Selected Time Filter
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val now = Calendar.getInstance()

    val filteredBookings = remember(bookingsList, selectedTimeFilter, refreshKey) {
        bookingsList.filter { booking ->
            if (selectedTimeFilter == "كل الفترة") return@filter true
            val bookingDate = try { sdf.parse(booking.bookingDate) } catch (e: Exception) { null } ?: return@filter true
            val bookingCal = Calendar.getInstance().apply { time = bookingDate }

            when (selectedTimeFilter) {
                "اليوم" -> {
                    bookingCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            bookingCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                }
                "هذا الأسبوع" -> {
                    bookingCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            bookingCal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
                }
                "هذا الشهر" -> {
                    bookingCal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                            bookingCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                }
                "آخر 3 أشهر" -> {
                    val threeMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -3) }
                    bookingCal.after(threeMonthsAgo)
                }
                "هذه السنة" -> {
                    bookingCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                }
                else -> true
            }
        }
    }

    // Report Export Launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    val csvText = buildString {
                        append("Booking Reference,Client Name,Trip Code,Date,Total Amount,Paid Amount,Status\n")
                        filteredBookings.forEach { b ->
                            val clientName = clientsList.find { c -> c.id == b.clientId }?.fullName ?: "Unknown"
                            val tripCode = tripsList.find { t -> t.id == b.tripId }?.tripCode ?: "Unknown"
                            append("${b.bookingReference},\"$clientName\",$tripCode,${b.bookingDate},${b.totalAmount},${b.paidAmount},${b.status}\n")
                        }
                    }
                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(csvText.toByteArray())
                    }
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "تم تصدير التقرير بنجاح!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "خطأ في التصدير: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Card with Refresh and Export Buttons
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("مركز التقارير والتحليلات المتقدم", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("إحصائيات فورية، تحليلات مالية، ومقارنات الفترات محلياً", style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { refreshKey++ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "تحديث البيانات", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { exportLauncher.launch("tirhal_report_${System.currentTimeMillis()}.csv") }) {
                    Icon(Icons.Default.Download, contentDescription = "تصدير التقرير", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Time Filters Row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filters = listOf("كل الفترة", "اليوم", "هذا الأسبوع", "هذا الشهر", "آخر 3 أشهر", "هذه السنة")
            items(filters) { filter ->
                FilterChip(
                    selected = selectedTimeFilter == filter,
                    onClick = { selectedTimeFilter = filter },
                    label = { Text(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> SummaryDashboardTab(clientsList, filteredBookings)
                1 -> BookingAnalysisTab(filteredBookings, tripsList)
                2 -> FinancialAnalysisTab(filteredBookings)
                3 -> ClientAnalysisTab(clientsList, bookingsList)
                4 -> TopValueClientsTab(clientsList, bookingsList, onOpenClientProfile)
                5 -> FollowUpOpportunitiesTab(clientsList, bookingsList, context)
                6 -> MonthlyAndComparisonTab(bookingsList)
            }
        }
    }
}

@Composable
fun SummaryDashboardTab(
    clientsList: List<ClientEntity>,
    bookingsList: List<BookingEntity>
) {
    val totalValue = bookingsList.sumOf { it.totalAmount }
    val totalPaid = bookingsList.sumOf { it.paidAmount }
    val totalPending = totalValue - totalPaid

    val confirmedCount = bookingsList.count { it.status == "مؤكد" }
    val pendingCount = bookingsList.count { it.status == "قيد الانتظار" }
    val completedCount = bookingsList.count { it.status == "مكتمل" }
    val cancelledCount = bookingsList.count { it.status == "ملغى" || it.status == "ملغاة" }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnalyticsMetricCard("إجمالي العملاء", "${clientsList.size}", MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                AnalyticsMetricCard("إجمالي الحجوزات", "${bookingsList.size}", MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnalyticsMetricCard("إجمالي القيمة", "$totalValue ريال", MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                AnalyticsMetricCard("إجمالي المحصل", "$totalPaid ريال", Color(0xFF2E7D32), modifier = Modifier.weight(1f))
                AnalyticsMetricCard("المتبقي للتحصيل", "$totalPending ريال", Color(0xFFC62828), modifier = Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊 الحجوزات حسب الحالة", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• الحجوزات المؤكدة:")
                        Text("$confirmedCount", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• الحجوزات قيد الانتظار:")
                        Text("$pendingCount", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• الحجوزات المكتملة:")
                        Text("$completedCount", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• الحجوزات الملغاة:")
                        Text("$cancelledCount", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingAnalysisTab(
    bookingsList: List<BookingEntity>,
    tripsList: List<TripEntity>
) {
    val total = bookingsList.size.coerceAtLeast(1)

    val statusCounts = mapOf(
        "مؤكد" to bookingsList.count { it.status == "مؤكد" },
        "قيد الانتظار" to bookingsList.count { it.status == "قيد الانتظار" },
        "مكتمل" to bookingsList.count { it.status == "مكتمل" },
        "ملغى" to bookingsList.count { it.status == "ملغى" || it.status == "ملغاة" }
    )

    val destinationMap = bookingsList.mapNotNull { b -> tripsList.find { t -> t.id == b.tripId }?.destination }
        .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📈 التوزيع النسبي للحجوزات", fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    statusCounts.forEach { (status, count) ->
                        val pct = (count.toFloat() / total * 100).toInt()
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$status: $count حجز")
                                Text("$pct%", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(count.toFloat() / total)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🌴 أكثر الوجهات طلباً", fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    if (destinationMap.isEmpty()) {
                        Text("لا توجد رحلات كافية لحساب أكثر الوجهات طلبًا.")
                    } else {
                        destinationMap.forEach { (dest, count) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("• الوجهة: $dest")
                                Text("$count حجوزات", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialAnalysisTab(bookingsList: List<BookingEntity>) {
    val totalValue = bookingsList.sumOf { it.totalAmount }
    val totalPaid = bookingsList.sumOf { it.paidAmount }
    val collectionRate = if (totalValue > 0) ((totalPaid / totalValue) * 100).toInt() else 0
    val avgValue = if (bookingsList.isNotEmpty()) (totalValue / bookingsList.size).toInt() else 0
    val pendingBookingsCount = bookingsList.count { it.totalAmount > it.paidAmount }

    val topBookings = bookingsList.sortedByDescending { it.totalAmount }.take(5)

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💵 مؤشرات الأداء المالي", fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("نسبة التحصيل:")
                        Text("$collectionRate%", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("متوسط قيمة الحجز:")
                        Text("$avgValue ريال", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("حجوزات تتطلب استكمال السداد:")
                        Text("$pendingBookingsCount حجز", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💎 أعلى الحجوزات قيمة", fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    if (topBookings.isEmpty()) {
                        Text("لا توجد حجوزات مسجلة.")
                    } else {
                        topBookings.forEach { b ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("حجز (${b.bookingReference}) - ${b.bookingDate}")
                                Text("${b.totalAmount} ريال", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientAnalysisTab(
    clientsList: List<ClientEntity>,
    bookingsList: List<BookingEntity>
) {
    val totalClients = clientsList.size
    val returningClients = clientsList.count { c -> bookingsList.count { b -> b.clientId == c.id } > 1 }
    val newClients = totalClients - returningClients

    val avgSatisfaction = if (clientsList.isNotEmpty()) {
        String.format(Locale.US, "%.1f", clientsList.map { it.satisfactionRating }.average())
    } else "0.0"

    val highSatisfaction = clientsList.count { it.satisfactionRating >= 4 }
    val lowSatisfaction = clientsList.count { it.satisfactionRating <= 2 }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👥 تحليلات العملاء والولاء", fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي العملاء:")
                        Text("$totalClients عميل", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("العملاء العائدون (أكثر من حجز):")
                        Text("$returningClients عملاء", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("العملاء الجدد:")
                        Text("$newClients عملاء", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("متوسط رضا العملاء العام:")
                        Text("⭐ $avgSatisfaction / 5", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("العملاء ذوو الرضا المرتفع (>= 4):")
                        Text("$highSatisfaction عميل", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("العملاء ذوو الرضا المنخفض (<= 2):")
                        Text("$lowSatisfaction عميل", fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                    }
                }
            }
        }
    }
}

@Composable
fun TopValueClientsTab(
    clientsList: List<ClientEntity>,
    bookingsList: List<BookingEntity>,
    onOpenClientProfile: ((ClientEntity) -> Unit)?
) {
    val topClients = clientsList.map { c ->
        val cBookings = bookingsList.filter { b -> b.clientId == c.id }
        val totalVal = cBookings.sumOf { it.totalAmount }
        val paidVal = cBookings.sumOf { it.paidAmount }
        val pendingVal = totalVal - paidVal
        Triple(c, cBookings, Triple(totalVal, paidVal, pendingVal))
    }.sortedByDescending { it.third.first }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(topClients) { (client, cBookings, vals) ->
            val (totalVal, paidVal, pendingVal) = vals
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenClientProfile?.invoke(client) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(10.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(client.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("عدد الحجوزات: ${cBookings.size} | آخر سفر: ${client.lastTripDate ?: "غير محدد"}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("إجمالي القيمة: $totalVal ريال | المدفوع: $paidVal | المتبقي: $pendingVal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }

                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun FollowUpOpportunitiesTab(
    clientsList: List<ClientEntity>,
    bookingsList: List<BookingEntity>,
    context: Context
) {
    val opportunities = mutableListOf<Triple<String, String, String>>()

    clientsList.forEach { client ->
        val phone = client.whatsappNumber ?: client.phoneNumber
        if (!client.expectedNextTravelDate.isNullOrBlank()) {
            opportunities.add(Triple("موعد سفر متوقع قريب للعميل ${client.fullName}", "أهلاً أستاذ ${client.fullName}، نود تذكيرك بموعد سفرك المتوقع بتاريخ (${client.expectedNextTravelDate}). يسعدنا الحجز لك مسبقاً مع ترحال AI.", phone))
        }
        if (client.satisfactionRating <= 2) {
            opportunities.add(Triple("عميل بحديث رضا منخفض (${client.fullName})", "أهلاً بك أستاذ ${client.fullName}، يسعدنا الاستماع لملاحظاتك وتحسين تجربتك القادمة مع ترحال AI.", phone))
        }
    }

    bookingsList.filter { it.totalAmount > it.paidAmount }.forEach { b ->
        val client = clientsList.find { c -> c.id == b.clientId }
        val cName = client?.fullName ?: "العميل"
        val phone = client?.whatsappNumber ?: client?.phoneNumber ?: ""
        val remaining = b.totalAmount - b.paidAmount
        opportunities.add(Triple("مبلغ متبقٍ على حجز (${b.bookingReference}) - $cName", "أهلاً أستاذ $cName، يرجى التكرم باستكمال المبلغ المتبقي قدره ($remaining ريال) للحجز رقم (${b.bookingReference}).", phone))
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(opportunities) { (title, msg, phone) ->
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(msg, style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("فرصة متابعة", msg)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ الرسالة", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخ")
                        }
                        Button(
                            onClick = {
                                val cleanPhone = phone.replace(Regex("[^0-9]"), "")
                                val uri = if (cleanPhone.isNotBlank()) {
                                    Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(msg)}")
                                } else {
                                    Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(msg)}")
                                }
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "تعذر فتح WhatsApp", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.height(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyAndComparisonTab(
    bookingsList: List<BookingEntity>
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val now = Calendar.getInstance()

    val currentMonthBookings = bookingsList.filter { b ->
        try {
            val d = sdf.parse(b.bookingDate) ?: return@filter false
            val c = Calendar.getInstance().apply { time = d }
            c.get(Calendar.YEAR) == now.get(Calendar.YEAR) && c.get(Calendar.MONTH) == now.get(Calendar.MONTH)
        } catch (e: Exception) { false }
    }

    val prevMonth = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
    val prevMonthBookings = bookingsList.filter { b ->
        try {
            val d = sdf.parse(b.bookingDate) ?: return@filter false
            val c = Calendar.getInstance().apply { time = d }
            c.get(Calendar.YEAR) == prevMonth.get(Calendar.YEAR) && c.get(Calendar.MONTH) == prevMonth.get(Calendar.MONTH)
        } catch (e: Exception) { false }
    }

    val currVal = currentMonthBookings.sumOf { it.totalAmount }
    val prevVal = prevMonthBookings.sumOf { it.totalAmount }
    val diffVal = currVal - prevVal

    val currCollected = currentMonthBookings.sumOf { it.paidAmount }
    val prevCollected = prevMonthBookings.sumOf { it.paidAmount }
    val diffCollected = currCollected - prevCollected

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🗓️ التقرير الشهري الحالي", fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    Text("• عدد الحجوزات للشهر الحالي: ${currentMonthBookings.size} حجز")
                    Text("• قيمة الحجوزات: $currVal ريال")
                    Text("• المبلغ المحصل: $currCollected ريال")
                    Text("• المبلغ المتبقي: ${currVal - currCollected} ريال")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊 مقارنة الفترة الحالية بالشهر السابق", fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    Text("• الفرق في عدد الحجوزات: ${currentMonthBookings.size - prevMonthBookings.size} حجز")
                    Text("• الفرق في إجمالي القيمة: $diffVal ريال")
                    Text("• الفرق في المحصل الفعلي: $diffCollected ريال")
                }
            }
        }
    }
}

@Composable
fun AnalyticsMetricCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accentColor)
        }
    }
}
