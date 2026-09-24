package com.tirhal.ai.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tirhal.ai.data.local.DatabaseProvider
import com.tirhal.ai.data.local.entity.BookingEntity
import com.tirhal.ai.data.local.entity.ClientEntity
import com.tirhal.ai.data.local.entity.TripEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BookingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { DatabaseProvider.getDatabase(context) }

    val bookingsList by database.bookingDao().getAllBookings().collectAsState(initial = emptyList())
    val clientsList by database.clientDao().getAllClients().collectAsState(initial = emptyList())
    val tripsList by database.tripDao().getAllTrips().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var bookingToEdit by remember { mutableStateOf<BookingEntity?>(null) }
    var selectedBookingDetail by remember { mutableStateOf<BookingEntity?>(null) }
    var bookingToDelete by remember { mutableStateOf<BookingEntity?>(null) }

    val filteredBookings = bookingsList.filter { booking ->
        val clientName = clientsList.find { it.id == booking.clientId }?.fullName ?: ""
        val tripCode = tripsList.find { it.id == booking.tripId }?.tripCode ?: ""
        booking.bookingReference.contains(searchQuery, ignoreCase = true) ||
                clientName.contains(searchQuery, ignoreCase = true) ||
                tripCode.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    bookingToEdit = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة حجز")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("بحث برقم الحجز، اسم العميل، أو كود الرحلة...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "لا توجد حجوزات مسجلة حالياً." else "لا توجد نتائج مطابقة للبحث",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredBookings, key = { it.id }) { booking ->
                        val client = clientsList.find { it.id == booking.clientId }
                        val trip = tripsList.find { it.id == booking.tripId }

                        BookingItemCard(
                            booking = booking,
                            clientName = client?.fullName ?: "عميل غير معروف",
                            tripInfo = trip?.let { "${it.tripCode} (${it.origin} ➔ ${it.destination})" } ?: "رحلة غير معروفة",
                            onCardClick = { selectedBookingDetail = booking },
                            onEditClick = {
                                bookingToEdit = booking
                                showAddEditDialog = true
                            },
                            onDeleteClick = { bookingToDelete = booking }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        BookingAddEditDialog(
            booking = bookingToEdit,
            clients = clientsList,
            trips = tripsList,
            onDismiss = { showAddEditDialog = false },
            onSave = { bookingRef, clientId, rawTripId, newTrip, bookingDate, status, totalAmount, paidAmount, paymentStatus, notes ->
                scope.launch(Dispatchers.IO) {
                    val targetTripId = if (newTrip != null) {
                        database.tripDao().insertTrip(newTrip)
                    } else {
                        rawTripId
                    }

                    if (bookingToEdit == null) {
                        database.bookingDao().insertBooking(
                            BookingEntity(
                                bookingReference = bookingRef,
                                clientId = clientId,
                                tripId = targetTripId,
                                bookingDate = bookingDate,
                                status = status,
                                totalAmount = totalAmount,
                                paidAmount = paidAmount,
                                paymentStatus = paymentStatus,
                                notes = notes.ifBlank { null }
                            )
                        )
                    } else {
                        database.bookingDao().updateBooking(
                            bookingToEdit!!.copy(
                                bookingReference = bookingRef,
                                clientId = clientId,
                                tripId = targetTripId,
                                bookingDate = bookingDate,
                                status = status,
                                totalAmount = totalAmount,
                                paidAmount = paidAmount,
                                paymentStatus = paymentStatus,
                                notes = notes.ifBlank { null }
                            )
                        )
                    }
                    withContext(Dispatchers.Main) {
                        showAddEditDialog = false
                        Toast.makeText(context, "تم حفظ الحجز بنجاح", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    selectedBookingDetail?.let { booking ->
        val client = clientsList.find { it.id == booking.clientId }
        val trip = tripsList.find { it.id == booking.tripId }

        AlertDialog(
            onDismissRequest = { selectedBookingDetail = null },
            title = {
                Text(
                    text = "تفاصيل الحجز: ${booking.bookingReference}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👤 العميل: ${client?.fullName ?: "غير معروف"}")
                    Text("📱 هاتف العميل: ${client?.phoneNumber ?: "غير متوفر"}")
                    Text("🚌 الرحلة: ${trip?.tripCode ?: "غير معروف"} (${trip?.origin} - ${trip?.destination})")
                    Text("📅 تاريخ الحجز: ${booking.bookingDate}")
                    Text("📌 حالة الحجز: ${booking.status}")
                    Text("💰 المبلغ الإجمالي: ${booking.totalAmount} ريال")
                    Text("💵 المبلغ المدفوع: ${booking.paidAmount} ريال")
                    Text("💳 حالة الدفع: ${booking.paymentStatus}")
                    booking.notes?.let { Text("📝 ملاحظات: $it") }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBookingDetail = null }) {
                    Text("إغلاق")
                }
            }
        )
    }

    bookingToDelete?.let { booking ->
        AlertDialog(
            onDismissRequest = { bookingToDelete = null },
            title = { Text("تأكيد حذف الحجز") },
            text = { Text("هل أنت تأكد من حذف الحجز رقم '${booking.bookingReference}'؟") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            database.bookingDao().deleteBooking(booking)
                            withContext(Dispatchers.Main) {
                                bookingToDelete = null
                                Toast.makeText(context, "تم حذف الحجز بنجاح", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToDelete = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun BookingItemCard(
    booking: BookingEntity,
    clientName: String,
    tripInfo: String,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
            ) {
                Icon(
                    imageVector = Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "حجز: ${booking.bookingReference}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "العميل: $clientName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "الرحلة: $tripInfo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• ${booking.paymentStatus}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingAddEditDialog(
    booking: BookingEntity?,
    clients: List<ClientEntity>,
    trips: List<TripEntity>,
    onDismiss: () -> Unit,
    onSave: (
        bookingRef: String,
        clientId: Long,
        tripId: Long,
        newTrip: TripEntity?,
        bookingDate: String,
        status: String,
        totalAmount: Double,
        paidAmount: Double,
        paymentStatus: String,
        notes: String
    ) -> Unit
) {
    var bookingRef by remember { mutableStateOf(booking?.bookingReference ?: "BKG-${System.currentTimeMillis().toString().takeLast(5)}") }
    var selectedClientId by remember { mutableStateOf(booking?.clientId ?: clients.firstOrNull()?.id ?: 0L) }
    var selectedTripId by remember { mutableStateOf(booking?.tripId ?: trips.firstOrNull()?.id ?: 0L) }
    var isNewTripMode by remember { mutableStateOf(trips.isEmpty()) }

    var tripCode by remember { mutableStateOf("TRP-${System.currentTimeMillis().toString().takeLast(4)}") }
    var tripOrigin by remember { mutableStateOf("الرياض") }
    var tripDestination by remember { mutableStateOf("جدة") }
    var tripTransport by remember { mutableStateOf("طيران") }

    var bookingDate by remember { mutableStateOf(booking?.bookingDate ?: "2025-03-05") }
    var status by remember { mutableStateOf(booking?.status ?: "مؤكد") }
    var totalAmountText by remember { mutableStateOf(booking?.totalAmount?.toString() ?: "450.0") }
    var paidAmountText by remember { mutableStateOf(booking?.paidAmount?.toString() ?: "450.0") }
    var notes by remember { mutableStateOf(booking?.notes ?: "") }

    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var tripDropdownExpanded by remember { mutableStateOf(false) }
    var statusDropdownExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf("مؤكد", "قيد الانتظار", "مكتمل", "ملغى")

    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (booking == null) "إضافة حجز جديد" else "تعديل الحجز") },
        text = {
            LazyColumn(modifier = Modifier.height(380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = bookingRef,
                        onValueChange = { bookingRef = it },
                        label = { Text("رقم مرجع الحجز *") },
                        singleLine = true,
                        isError = showError && bookingRef.isBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    if (clients.isEmpty()) {
                        Text(
                            text = "⚠️ لا يوجد عملاء مسجلون. يرجى إضافة عميل من شاشة العملاء أولاً.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        ExposedDropdownMenuBox(
                            expanded = clientDropdownExpanded,
                            onExpandedChange = { clientDropdownExpanded = !clientDropdownExpanded }
                        ) {
                            val clientName = clients.find { it.id == selectedClientId }?.fullName ?: "اختر العميل *"
                            OutlinedTextField(
                                value = clientName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("العميل *") },
                                isError = showError && selectedClientId == 0L,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = clientDropdownExpanded,
                                onDismissRequest = { clientDropdownExpanded = false }
                            ) {
                                clients.forEach { client ->
                                    DropdownMenuItem(
                                        text = { Text(client.fullName) },
                                        onClick = {
                                            selectedClientId = client.id
                                            clientDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    if (!isNewTripMode && trips.isNotEmpty()) {
                        Column {
                            ExposedDropdownMenuBox(
                                expanded = tripDropdownExpanded,
                                onExpandedChange = { tripDropdownExpanded = !tripDropdownExpanded }
                            ) {
                                val tripText = trips.find { it.id == selectedTripId }?.let { "${it.tripCode} (${it.origin} ➔ ${it.destination})" } ?: "اختر الرحلة *"
                                OutlinedTextField(
                                    value = tripText,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("الرحلة *") },
                                    isError = showError && selectedTripId == 0L,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tripDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = tripDropdownExpanded,
                                    onDismissRequest = { tripDropdownExpanded = false }
                                ) {
                                    trips.forEach { trip ->
                                        DropdownMenuItem(
                                            text = { Text("${trip.tripCode} (${trip.origin} ➔ ${trip.destination})") },
                                            onClick = {
                                                selectedTripId = trip.id
                                                totalAmountText = trip.price.toString()
                                                tripDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            TextButton(onClick = { isNewTripMode = true }) {
                                Text("➕ إضافة بيانات رحلة جديدة بدلاً من الرحلات المسجلة")
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("بيانات الرحلة الجديدة:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = tripOrigin,
                                    onValueChange = { tripOrigin = it },
                                    label = { Text("نقطة الانطلاق *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = tripDestination,
                                    onValueChange = { tripDestination = it },
                                    label = { Text("الوجهة *") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = tripCode,
                                    onValueChange = { tripCode = it },
                                    label = { Text("كود الرحلة") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = tripTransport,
                                    onValueChange = { tripTransport = it },
                                    label = { Text("وسيلة النقل") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (trips.isNotEmpty()) {
                                TextButton(onClick = { isNewTripMode = false }) {
                                    Text("↩️ اختيار من القائمة المسجلة")
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = bookingDate,
                        onValueChange = { bookingDate = it },
                        label = { Text("تاريخ الحجز (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    ExposedDropdownMenuBox(
                        expanded = statusDropdownExpanded,
                        onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("حالة الحجز") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            statusOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        status = option
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = totalAmountText,
                            onValueChange = { totalAmountText = it },
                            label = { Text("الإجمالي (ريال)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = paidAmountText,
                            onValueChange = { paidAmountText = it },
                            label = { Text("المدفوع (ريال)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("ملاحظات الحجز") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (bookingRef.isBlank() || selectedClientId == 0L || (!isNewTripMode && selectedTripId == 0L)) {
                        showError = true
                    } else {
                        val total = totalAmountText.toDoubleOrNull() ?: 0.0
                        val paid = paidAmountText.toDoubleOrNull() ?: 0.0
                        val computedPaymentStatus = when {
                            paid >= total && total > 0 -> "مدفوع بالكامل"
                            paid > 0 -> "مدفوع جزئيًا"
                            else -> "غير مدفوع"
                        }

                        val newTripEntity = if (isNewTripMode) {
                            TripEntity(
                                tripCode = tripCode.ifBlank { "TRP-NEW" },
                                origin = tripOrigin.ifBlank { "الرياض" },
                                destination = tripDestination.ifBlank { "جدة" },
                                departureDate = bookingDate,
                                transportationType = tripTransport.ifBlank { "طيران" },
                                price = total,
                                status = "مجدولة"
                            )
                        } else null

                        onSave(bookingRef, selectedClientId, selectedTripId, newTripEntity, bookingDate, status, total, paid, computedPaymentStatus, notes)
                    }
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
