package com.tirhal.ai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tirhal.ai.data.local.DatabaseProvider
import com.tirhal.ai.data.local.entity.ClientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Constants for Client Types and Statuses
val CLIENT_TYPES = listOf("الكل", "فرد", "عائلة", "شركة", "مؤسسة", "مجموعة")
val CLIENT_TYPES_FORM = listOf("فرد", "عائلة", "شركة", "مؤسسة", "مجموعة")
val CLIENT_STATUSES = listOf("الكل", "نشط", "محتمل", "منقطع", "يحتاج متابعة")
val CLIENT_STATUSES_FORM = listOf("نشط", "محتمل", "منقطع", "يحتاج متابعة")

enum class ClientSortOption(val title: String) {
    LAST_TRIP_DESC("آخر رحلة (الأحدث)"),
    LAST_TRIP_ASC("آخر رحلة (الأقدم)"),
    NEXT_TRAVEL_ASC("أقرب موعد متابعة/سفر"),
    NAME("الاسم الكامل"),
    SATISFACTION("درجة الرضا")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClientsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { DatabaseProvider.getDatabase(context) }

    val clientsList by database.clientDao().getAllClients().collectAsState(initial = emptyList())
    val bookingsList by database.bookingDao().getAllBookings().collectAsState(initial = emptyList())
    val followUpsList by database.followUpActionDao().getAllFollowUpActions().collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: القائمة الرئيسية, 1: المتابعة الذكية, 2: الإحصائيات

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") }
    var selectedStatusFilter by remember { mutableStateOf("الكل") }
    var selectedDestFilter by remember { mutableStateOf("الكل") }
    var currentSortOption by remember { mutableStateOf(ClientSortOption.LAST_TRIP_DESC) }
    var showSortMenu by remember { mutableStateOf(false) }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }
    var selectedClientDetail by remember { mutableStateOf<ClientEntity?>(null) }
    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }

    // Collect all available preferred destinations
    val availableDestinations = remember(clientsList) {
        val destSet = mutableSetOf("الكل")
        clientsList.forEach { client ->
            client.preferredDestinations?.split("،", ",")?.forEach {
                val trimmed = it.trim()
                if (trimmed.isNotEmpty()) destSet.add(trimmed)
            }
        }
        destSet.toList()
    }

    // Filter logic
    val filteredClients = clientsList.filter { client ->
        val matchesSearch = client.fullName.contains(searchQuery, ignoreCase = true) ||
                client.phoneNumber.contains(searchQuery) ||
                (client.whatsappNumber?.contains(searchQuery) == true) ||
                (client.email?.contains(searchQuery, ignoreCase = true) == true)

        val matchesType = if (selectedTypeFilter == "الكل") true else client.clientType == selectedTypeFilter
        val matchesStatus = if (selectedStatusFilter == "الكل") true else client.status == selectedStatusFilter
        val matchesDest = if (selectedDestFilter == "الكل") true else {
            client.preferredDestinations?.contains(selectedDestFilter, ignoreCase = true) == true
        }

        matchesSearch && matchesType && matchesStatus && matchesDest
    }.let { list ->
        when (currentSortOption) {
            ClientSortOption.LAST_TRIP_DESC -> list.sortedByDescending { it.lastTripDate ?: "" }
            ClientSortOption.LAST_TRIP_ASC -> list.sortedBy { it.lastTripDate ?: "9999-99-99" }
            ClientSortOption.NEXT_TRAVEL_ASC -> list.sortedBy { calculateNextFollowUpDate(it.lastTripDate, it.expectedNextTravelDate, it.travelCycleMonths) ?: "9999-99-99" }
            ClientSortOption.NAME -> list.sortedBy { it.fullName }
            ClientSortOption.SATISFACTION -> list.sortedByDescending { it.satisfactionRating }
        }
    }

    // Clients due for smart follow-up
    val dueFollowUpClients = clientsList.filter { isClientDueForFollowUp(it) || it.status == "يحتاج متابعة" }

    Scaffold(
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = {
                        clientToEdit = null
                        showAddEditDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = "إضافة عميل")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة عميل", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Top Tabs
            @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("قائمة العملاء (${filteredClients.size})") },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("المتابعة الذكية (${dueFollowUpClients.size})") },
                    icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("الإحصائيات") },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) }
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    // TAB 0: Clients List Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Search bar & Sort button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("بحث باسم أو هاتف...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box {
                                OutlinedButton(
                                    onClick = { showSortMenu = true },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Icon(Icons.Default.Sort, contentDescription = "ترتيب")
                                }
                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    ClientSortOption.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.title) },
                                            onClick = {
                                                currentSortOption = option
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Horizontal Filters Row: Client Type, Status, Destination
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("النوع:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            CLIENT_TYPES.forEach { type ->
                                FilterChip(
                                    selected = selectedTypeFilter == type,
                                    onClick = { selectedTypeFilter = type },
                                    label = { Text(type, fontSize = 12.sp) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الحالة:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            CLIENT_STATUSES.forEach { status ->
                                FilterChip(
                                    selected = selectedStatusFilter == status,
                                    onClick = { selectedStatusFilter = status },
                                    label = { Text(status, fontSize = 12.sp) }
                                )
                            }
                        }

                        if (availableDestinations.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("الوجهة:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                availableDestinations.forEach { dest ->
                                    FilterChip(
                                        selected = selectedDestFilter == dest,
                                        onClick = { selectedDestFilter = dest },
                                        label = { Text(dest, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (filteredClients.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isEmpty()) "لا يوجد عملاء مطابقين للفلاتر. اضغط أضف عميل للبدء!" else "لا توجد نتائج للبحث",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(filteredClients, key = { it.id }) { client ->
                                    val clientBookings = bookingsList.filter { it.clientId == client.id }
                                    ClientItemCard(
                                        client = client,
                                        bookingsCount = clientBookings.size,
                                        onCardClick = { selectedClientDetail = client },
                                        onEditClick = {
                                            clientToEdit = client
                                            showAddEditDialog = true
                                        },
                                        onDeleteClick = { clientToDelete = client }
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: Smart Follow-Up & WhatsApp Screen
                    SmartFollowUpScreen(
                        dueClients = dueFollowUpClients,
                        onClientClick = { selectedClientDetail = it }
                    )
                }

                2 -> {
                    // TAB 2: Basic Analytics Overview Screen
                    ClientAnalyticsScreen(
                        clientsList = clientsList,
                        bookingsList = bookingsList
                    )
                }
            }
        }
    }

    if (showAddEditDialog) {
        ClientAddEditDialog(
            client = clientToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { fullName, phone, whatsapp, email, address, type, dests, lastTrip, nextTravel, cycle, satisfaction, status, notes ->
                scope.launch(Dispatchers.IO) {
                    if (clientToEdit == null) {
                        database.clientDao().insertClient(
                            ClientEntity(
                                fullName = fullName,
                                phoneNumber = phone,
                                whatsappNumber = whatsapp.ifBlank { phone },
                                email = email.ifBlank { null },
                                address = address.ifBlank { null },
                                clientType = type,
                                preferredDestinations = dests.ifBlank { null },
                                lastTripDate = lastTrip.ifBlank { null },
                                expectedNextTravelDate = nextTravel.ifBlank { null },
                                travelCycleMonths = cycle,
                                satisfactionRating = satisfaction,
                                status = status,
                                notes = notes.ifBlank { null }
                            )
                        )
                    } else {
                        database.clientDao().updateClient(
                            clientToEdit!!.copy(
                                fullName = fullName,
                                phoneNumber = phone,
                                whatsappNumber = whatsapp.ifBlank { phone },
                                email = email.ifBlank { null },
                                address = address.ifBlank { null },
                                clientType = type,
                                preferredDestinations = dests.ifBlank { null },
                                lastTripDate = lastTrip.ifBlank { null },
                                expectedNextTravelDate = nextTravel.ifBlank { null },
                                travelCycleMonths = cycle,
                                satisfactionRating = satisfaction,
                                status = status,
                                notes = notes.ifBlank { null }
                            )
                        )
                    }
                    withContext(Dispatchers.Main) {
                        showAddEditDialog = false
                        Toast.makeText(context, "تم حفظ بيانات العميل بنجاح", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    selectedClientDetail?.let { client ->
        val clientBookings = bookingsList.filter { it.clientId == client.id }
        val clientFollowUps = followUpsList.filter { it.travelerId == client.id }

        ClientProfileDialog(
            client = client,
            bookings = clientBookings,
            followUps = clientFollowUps,
            onDismiss = { selectedClientDetail = null },
            onOpenWhatsApp = { openWhatsAppMessage(context, client.whatsappNumber ?: client.phoneNumber, generateWhatsAppTemplate(client)) }
        )
    }

    clientToDelete?.let { client ->
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت تأكد من حذف العميل '${client.fullName}'؟ سيؤدي ذلك لحذف كافة الحجوزات المرتبطة به.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            database.clientDao().deleteClient(client)
                            withContext(Dispatchers.Main) {
                                clientToDelete = null
                                Toast.makeText(context, "تم حذف العميل بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { clientToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ClientItemCard(
    client: ClientEntity,
    bookingsCount: Int,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val statusColor = when (client.status) {
        "نشط" -> Color(0xFF2E7D32)
        "محتمل" -> Color(0xFF0288D1)
        "منقطع" -> Color(0xFFD32F2F)
        "يحتاج متابعة" -> Color(0xFFED6C02)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = client.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = client.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.height(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = client.phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• نوع العميل: ${client.clientType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$bookingsCount حجوزات",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "⭐ ${client.satisfactionRating}/5",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    client.preferredDestinations?.let {
                        Text(
                            text = "🌴 $it",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ClientProfileDialog(
    client: ClientEntity,
    bookings: List<com.tirhal.ai.data.local.entity.BookingEntity>,
    followUps: List<com.tirhal.ai.data.local.entity.FollowUpActionEntity>,
    onDismiss: () -> Unit,
    onOpenWhatsApp: () -> Unit
) {
    val totalBookingsValue = bookings.sumOf { it.totalAmount }
    val totalPaid = bookings.sumOf { it.paidAmount }
    val totalPending = totalBookingsValue - totalPaid
    val nextFollowUpDate = calculateNextFollowUpDate(client.lastTripDate, client.expectedNextTravelDate, client.travelCycleMonths)
    val suggestedAction = getSuggestedNextAction(client)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ملف العميل: ${client.fullName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(modifier = Modifier.height(420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("👤 نوع العميل: ${client.clientType} | الحالة: ${client.status}", fontWeight = FontWeight.Bold)
                            Text("📱 رقم الاتصال: ${client.phoneNumber}")
                            Text("💬 واتساب: ${client.whatsappNumber ?: client.phoneNumber}")
                            client.email?.let { Text("📧 البريد: $it") }
                            client.address?.let { Text("📍 العنوان: $it") }
                            Text("⭐ رضا العميل: ${client.satisfactionRating} / 5")
                            Text("🔄 دورة السفر المتوقعة: كل ${client.travelCycleMonths} أشهر")
                            client.preferredDestinations?.let { Text("🌴 الوجهات المفضلة: $it") }
                            client.lastTripDate?.let { Text("🛫 تاريخ آخر رحلة: $it") }
                            nextFollowUpDate?.let { Text("📅 موعد المتابعة/السفر المتوقع القادم: $it", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                            client.notes?.let { Text("📝 ملاحظات: $it") }
                        }
                    }
                }

                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("🤖 الإجراء التالي المقترح (الذكاء الاصطناعي):", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            Text(suggestedAction, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onOpenWhatsApp,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إرسال رسالة واتساب مقترحة", color = Color.White)
                            }
                        }
                    }
                }

                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("📊 الملخص المالي للحجوزات:", fontWeight = FontWeight.Bold)
                            Text("• إجمالي الحجوزات: $totalBookingsValue ريال (${bookings.size} حجز)")
                            Text("• إجمالي المدفوع: $totalPaid ريال")
                            Text("• المتبقي: $totalPending ريال", color = if (totalPending > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Text("سجل الحجوزات والرحلات (${bookings.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                if (bookings.isEmpty()) {
                    item { Text("لا توجد حجوزات مسجلة لهذا العميل.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(bookings) { booking ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("رقم الحجز: ${booking.bookingReference}", fontWeight = FontWeight.Bold)
                                Text("التاريخ: ${booking.bookingDate} | الحالة: ${booking.status}")
                                Text("المبلغ: ${booking.totalAmount} ريال | المدفوع: ${booking.paidAmount} ريال")
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("سجل المتابعات والتواصل (${followUps.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                if (followUps.isEmpty()) {
                    item { Text("لا توجد إجراءات متابعة سابقة مسجلة.", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(followUps) { action ->
                        Text("• [${action.status}] ${action.actionType}: ${action.description} (${action.dueDate})", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun SmartFollowUpScreen(
    dueClients: List<ClientEntity>,
    onClientClick: (ClientEntity) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "نظام المتابعة الذكية التلقائي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "يتم التنبؤ بموعد التواصل والتذكير بناءً على دورة سفر العميل المحددة وتاريخ آخر رحلة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (dueClients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎉 لا يوجد عملاء بحاجة إلى متابعة حالياً!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = "العملاء الذين حان موعد متابعتهم (${dueClients.size}):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(dueClients) { client ->
                    val templateMsg = generateWhatsAppTemplate(client)
                    val nextDate = calculateNextFollowUpDate(client.lastTripDate, client.expectedNextTravelDate, client.travelCycleMonths)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClientClick(client) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(client.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("حان موعد المتابعة", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("📱 الجوال: ${client.phoneNumber} | 🔄 دورة السفر: كل ${client.travelCycleMonths} أشهر")
                            client.preferredDestinations?.let { Text("🌴 الوجهة المفضلة: $it") }
                            nextDate?.let { Text("📅 الموعد المتوقع: $it", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary) }

                            Spacer(modifier = Modifier.height(8.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("💬 الرسالة المقترحة للواتساب:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    Text(templateMsg, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        openWhatsAppMessage(context, client.whatsappNumber ?: client.phoneNumber, templateMsg)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("فتح واتساب", color = Color.White, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        copyToClipboard(context, templateMsg)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("نسخ الرسالة", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClientAnalyticsScreen(
    clientsList: List<ClientEntity>,
    bookingsList: List<com.tirhal.ai.data.local.entity.BookingEntity>
) {
    val totalClients = clientsList.size
    val activeClients = clientsList.count { it.status == "نشط" }
    val potentialClients = clientsList.count { it.status == "محتمل" }
    val lapsedClients = clientsList.count { it.status == "منقطع" }
    val needsFollowUpClients = clientsList.count { it.status == "يحتاج متابعة" || isClientDueForFollowUp(it) }

    // Calculate top preferred destinations
    val destCounts = mutableMapOf<String, Int>()
    clientsList.forEach { client ->
        client.preferredDestinations?.split("،", ",")?.forEach {
            val dest = it.trim()
            if (dest.isNotEmpty()) {
                destCounts[dest] = (destCounts[dest] ?: 0) + 1
            }
        }
    }
    val topDestinations = destCounts.entries.sortedByDescending { it.value }.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("📊 نظرة عامة على إحصائيات العملاء", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "إجمالي العملاء",
                    value = "$totalClients",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "العملاء النشطون",
                    value = "$activeClients",
                    containerColor = Color(0xFFE8F5E9),
                    contentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    title = "العملاء المحتملون",
                    value = "$potentialClients",
                    containerColor = Color(0xFFE1F5FE),
                    contentColor = Color(0xFF0288D1),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "العملاء المنقطعون",
                    value = "$lapsedClients",
                    containerColor = Color(0xFFFFEBEE),
                    contentColor = Color(0xFFC62828),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            StatCard(
                title = "عملاء بحاجة لمتابعة عاجلة",
                value = "$needsFollowUpClients",
                containerColor = Color(0xFFFFF3E0),
                contentColor = Color(0xFFEF6C00),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🌴 أكثر الوجهات تفضيلاً لدى العملاء", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (topDestinations.isEmpty()) {
                        Text("لا توجد بيانات وجهات مسجلة حالياً.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        topDestinations.forEach { (dest, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("• $dest", style = MaterialTheme.typography.bodyMedium)
                                Text("$count عميل", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = contentColor)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun ClientAddEditDialog(
    client: ClientEntity?,
    onDismiss: () -> Unit,
    onSave: (
        fullName: String,
        phone: String,
        whatsapp: String,
        email: String,
        address: String,
        type: String,
        preferredDests: String,
        lastTrip: String,
        nextTravel: String,
        cycleMonths: Int,
        satisfaction: Int,
        status: String,
        notes: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf(client?.fullName ?: "") }
    var phoneNumber by remember { mutableStateOf(client?.phoneNumber ?: "") }
    var whatsappNumber by remember { mutableStateOf(client?.whatsappNumber ?: client?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(client?.email ?: "") }
    var address by remember { mutableStateOf(client?.address ?: "") }
    var selectedType by remember { mutableStateOf(client?.clientType ?: "فرد") }
    var preferredDests by remember { mutableStateOf(client?.preferredDestinations ?: "") }
    var lastTripDate by remember { mutableStateOf(client?.lastTripDate ?: "") }
    var nextTravelDate by remember { mutableStateOf(client?.expectedNextTravelDate ?: "") }
    var cycleMonthsText by remember { mutableStateOf((client?.travelCycleMonths ?: 6).toString()) }
    var satisfactionRating by remember { mutableIntStateOf(client?.satisfactionRating ?: 5) }
    var selectedStatus by remember { mutableStateOf(client?.status ?: "نشط") }
    var notes by remember { mutableStateOf(client?.notes ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (client == null) "إضافة ملف عميل جديد" else "تعديل ملف العميل") },
        text = {
            LazyColumn(modifier = Modifier.height(420.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("الاسم الكامل *") },
                        singleLine = true,
                        isError = showError && fullName.isBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("رقم الهاتف *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            isError = showError && phoneNumber.isBlank(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = whatsappNumber,
                            onValueChange = { whatsappNumber = it },
                            label = { Text("رقم الواتساب") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Text("نوع العميل:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CLIENT_TYPES_FORM.forEach { type ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(type) }
                            )
                        }
                    }
                }
                item {
                    Text("حالة العميل:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CLIENT_STATUSES_FORM.forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = preferredDests,
                        onValueChange = { preferredDests = it },
                        label = { Text("الوجهات المفضلة (مثل: دبي، مكة، تركيا)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = lastTripDate,
                            onValueChange = { lastTripDate = it },
                            label = { Text("آخر رحلة (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = nextTravelDate,
                            onValueChange = { nextTravelDate = it },
                            label = { Text("الموعد القادم المتوقع") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cycleMonthsText,
                            onValueChange = { cycleMonthsText = it },
                            label = { Text("دورة السفر (أشهر)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = satisfactionRating.toString(),
                            onValueChange = { satisfactionRating = it.toIntOrNull()?.coerceIn(1, 5) ?: 5 },
                            label = { Text("رضا العميل (1-5)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("العنوان") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات خاصة بالعميل") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullName.isBlank() || phoneNumber.isBlank()) {
                        showError = true
                    } else {
                        val cycle = cycleMonthsText.toIntOrNull() ?: 6
                        onSave(
                            fullName,
                            phoneNumber,
                            whatsappNumber,
                            email,
                            address,
                            selectedType,
                            preferredDests,
                            lastTripDate,
                            nextTravelDate,
                            cycle,
                            satisfactionRating,
                            selectedStatus,
                            notes
                        )
                    }
                }
            ) {
                Text("حفظ الملف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

// Utility Helper Functions
fun calculateNextFollowUpDate(lastTripDate: String?, expectedNextDate: String?, cycleMonths: Int): String? {
    if (!expectedNextDate.isNull_or_Blank()) return expectedNextDate
    if (lastTripDate.isNull_or_Blank()) return null

    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = sdf.parse(lastTripDate) ?: return null
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, cycleMonths)
        sdf.format(cal.time)
    } catch (e: Exception) {
        null
    }
}

fun isClientDueForFollowUp(client: ClientEntity): Boolean {
    val nextDateStr = calculateNextFollowUpDate(client.lastTripDate, client.expectedNextTravelDate, client.travelCycleMonths) ?: return false
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val nextDate = sdf.parse(nextDateStr) ?: return false
        val today = Date()
        nextDate.before(today) || (nextDate.time - today.time) <= (7 * 24 * 60 * 60 * 1000L) // Due or within 7 days
    } catch (e: Exception) {
        false
    }
}

fun getSuggestedNextAction(client: ClientEntity): String {
    return when (client.status) {
        "منقطع" -> "إرسال عرض خصم خاص للعودة واستكشاف الوجهة المفضلة (${client.preferredDestinations ?: "المفضلة"})."
        "محتمل" -> "إرسال برنامج رحلات الموسم الجديد ومتابعة تأكيد الحجز الأول."
        "يحتاج متابعة" -> "التواصل فوراً هاتفيًا أو عبر واتساب لتأكيد تفاصيل السفر القادم."
        else -> "إرسال بطاقة تهنئة والتذكير بموعد الرحلة القادمة حسب دورة سفر العميل."
    }
}

fun generateWhatsAppTemplate(client: ClientEntity): String {
    val dest = client.preferredDestinations?.split("،", ",")?.firstOrNull()?.trim() ?: "وجهتك المفضلة"
    return "مرحباً ${client.fullName}، يسعدنا في agency Tarhal AI تقديم أحدث عروض الرحلات الحصرية إلى $dest. نسعد بتواصلك لتنظيم رحلتك القادمة بكل سهولة!"
}

fun openWhatsAppMessage(context: Context, phone: String, message: String) {
    try {
        val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "لم يتم العثور على تطبيق واتساب", Toast.LENGTH_SHORT).show()
    }
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("WhatsApp Message", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "تم نسخ الرسالة إلى الحافظة", Toast.LENGTH_SHORT).show()
}

private fun String?.isNull_or_Blank(): Boolean {
    return this == null || this.trim().isEmpty()
}
