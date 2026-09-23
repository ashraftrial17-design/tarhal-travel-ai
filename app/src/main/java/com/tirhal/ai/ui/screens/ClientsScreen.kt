package com.tirhal.ai.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import com.tirhal.ai.data.local.DatabaseProvider
import com.tirhal.ai.data.local.entity.ClientEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ClientsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { DatabaseProvider.getDatabase(context) }

    val clientsList by database.clientDao().getAllClients().collectAsState(initial = emptyList())
    val bookingsList by database.bookingDao().getAllBookings().collectAsState(initial = emptyList())
    val followUpsList by database.followUpActionDao().getAllFollowUpActions().collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<ClientEntity?>(null) }
    var selectedClientDetail by remember { mutableStateOf<ClientEntity?>(null) }
    var clientToDelete by remember { mutableStateOf<ClientEntity?>(null) }

    val filteredClients = clientsList.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery) ||
                (it.whatsappNumber?.contains(searchQuery) == true) ||
                (it.email?.contains(searchQuery, ignoreCase = true) == true)
    }

    Scaffold(
        floatingActionButton = {
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
                placeholder = { Text("بحث عن عميل باسم، هاتف، واتساب، أو بريد...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إجمالي العملاء المسجلين", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("${filteredClients.size} عميل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredClients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "لا يوجد عملاء حالياً. اضغط أضف عميل للبدء!" else "لا توجد نتائج للبحث",
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

    if (showAddEditDialog) {
        ClientAddEditDialog(
            client = clientToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { fullName, phone, whatsapp, email, address, dests, lastTrip, nextTravel, cycle, satisfaction, notes ->
                scope.launch(Dispatchers.IO) {
                    if (clientToEdit == null) {
                        database.clientDao().insertClient(
                            ClientEntity(
                                fullName = fullName,
                                phoneNumber = phone,
                                whatsappNumber = whatsapp.ifBlank { phone },
                                email = email.ifBlank { null },
                                address = address.ifBlank { null },
                                preferredDestinations = dests.ifBlank { null },
                                lastTripDate = lastTrip.ifBlank { null },
                                expectedNextTravelDate = nextTravel.ifBlank { null },
                                travelCycleMonths = cycle,
                                satisfactionRating = satisfaction,
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
                                preferredDestinations = dests.ifBlank { null },
                                lastTripDate = lastTrip.ifBlank { null },
                                expectedNextTravelDate = nextTravel.ifBlank { null },
                                travelCycleMonths = cycle,
                                satisfactionRating = satisfaction,
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

        val totalBookingsValue = clientBookings.sumOf { it.totalAmount }
        val totalPaid = clientBookings.sumOf { it.paidAmount }
        val totalPending = totalBookingsValue - totalPaid

        AlertDialog(
            onDismissRequest = { selectedClientDetail = null },
            title = {
                Text(
                    text = "ملف العميل: ${client.fullName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(modifier = Modifier.height(350.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("📱 رقم الاتصال: ${client.phoneNumber}", fontWeight = FontWeight.SemiBold)
                                Text("💬 واتساب: ${client.whatsappNumber ?: client.phoneNumber}")
                                client.email?.let { Text("📧 البريد: $it") }
                                client.address?.let { Text("📍 العنوان: $it") }
                                Text("⭐ رضا العميل: ${client.satisfactionRating} / 5")
                                Text("🔄 دورة السفر: كل ${client.travelCycleMonths} أشهر")
                                client.preferredDestinations?.let { Text("🌴 الوجهات المفضلة: $it") }
                                client.lastTripDate?.let { Text("🛫 آخر رحلة: $it") }
                                client.expectedNextTravelDate?.let { Text("📅 موعد السفر القادم المتوقع: $it") }
                                client.notes?.let { Text("📝 ملاحظات: $it") }
                            }
                        }
                    }

                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("📊 الملخص المالي للعميل:", fontWeight = FontWeight.Bold)
                                Text("• إجمالي الحجوزات: $totalBookingsValue ريال (${clientBookings.size} حجوزات)")
                                Text("• إجمالي المدفوع: $totalPaid ريال")
                                Text("• المتبقي: $totalPending ريال", color = if (totalPending > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Text("سجل الحجوزات (${clientBookings.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }

                    if (clientBookings.isEmpty()) {
                        item { Text("لا توجد حجوزات مسجلة لهذا العميل.", style = MaterialTheme.typography.bodySmall) }
                    } else {
                        items(clientBookings) { booking ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("رقم الحجز: ${booking.bookingReference}", fontWeight = FontWeight.Bold)
                                    Text("التاريخ: ${booking.bookingDate} | الحالة: ${booking.status}")
                                    Text("المبلغ: ${booking.totalAmount} | المدفوع: ${booking.paidAmount} | المتبقي: ${booking.totalAmount - booking.paidAmount}")
                                }
                            }
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("سجل المتابعات (${clientFollowUps.size}):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }

                    if (clientFollowUps.isEmpty()) {
                        item { Text("لا توجد إجراءات متابعة سابقة مسجلة.", style = MaterialTheme.typography.bodySmall) }
                    } else {
                        items(clientFollowUps) { action ->
                            Text("• [${action.status}] ${action.actionType}: ${action.description} (${action.dueDate})", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedClientDetail = null }) {
                    Text("إغلاق")
                }
            }
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
                Text(
                    text = client.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
fun ClientAddEditDialog(
    client: ClientEntity?,
    onDismiss: () -> Unit,
    onSave: (
        fullName: String,
        phone: String,
        whatsapp: String,
        email: String,
        address: String,
        preferredDests: String,
        lastTrip: String,
        nextTravel: String,
        cycleMonths: Int,
        satisfaction: Int,
        notes: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf(client?.fullName ?: "") }
    var phoneNumber by remember { mutableStateOf(client?.phoneNumber ?: "") }
    var whatsappNumber by remember { mutableStateOf(client?.whatsappNumber ?: client?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(client?.email ?: "") }
    var address by remember { mutableStateOf(client?.address ?: "") }
    var preferredDests by remember { mutableStateOf(client?.preferredDestinations ?: "") }
    var lastTripDate by remember { mutableStateOf(client?.lastTripDate ?: "") }
    var nextTravelDate by remember { mutableStateOf(client?.expectedNextTravelDate ?: "") }
    var cycleMonthsText by remember { mutableStateOf((client?.travelCycleMonths ?: 6).toString()) }
    var satisfactionRating by remember { mutableIntStateOf(client?.satisfactionRating ?: 5) }
    var notes by remember { mutableStateOf(client?.notes ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (client == null) "إضافة ملف عميل جديد" else "تعديل ملف العميل") },
        text = {
            LazyColumn(modifier = Modifier.height(380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("رقم الهاتف *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        isError = showError && phoneNumber.isBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = whatsappNumber,
                        onValueChange = { whatsappNumber = it },
                        label = { Text("رقم الواتساب") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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
                        onSave(fullName, phoneNumber, whatsappNumber, email, address, preferredDests, lastTripDate, nextTravelDate, cycle, satisfactionRating, notes)
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
