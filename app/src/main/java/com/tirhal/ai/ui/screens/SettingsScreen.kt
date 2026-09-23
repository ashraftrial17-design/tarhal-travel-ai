package com.tirhal.ai.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { DatabaseProvider.getDatabase(context) }

    val clientsList by database.clientDao().getAllClients().collectAsState(initial = emptyList())
    val tripsList by database.tripDao().getAllTrips().collectAsState(initial = emptyList())
    val bookingsList by database.bookingDao().getAllBookings().collectAsState(initial = emptyList())

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var pendingJsonData by remember { mutableStateOf<String?>(null) }

    // Launcher for exporting JSON
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    val rootJson = JSONObject()

                    // Export Clients with all expanded fields
                    val clientsArray = JSONArray()
                    clientsList.forEach { client ->
                        val obj = JSONObject().apply {
                            put("id", client.id)
                            put("fullName", client.fullName)
                            put("phoneNumber", client.phoneNumber)
                            put("whatsappNumber", client.whatsappNumber ?: "")
                            put("email", client.email ?: "")
                            put("address", client.address ?: "")
                            put("preferredDestinations", client.preferredDestinations ?: "")
                            put("lastTripDate", client.lastTripDate ?: "")
                            put("expectedNextTravelDate", client.expectedNextTravelDate ?: "")
                            put("travelCycleMonths", client.travelCycleMonths)
                            put("satisfactionRating", client.satisfactionRating)
                            put("notes", client.notes ?: "")
                        }
                        clientsArray.put(obj)
                    }
                    rootJson.put("clients", clientsArray)

                    // Export Trips
                    val tripsArray = JSONArray()
                    tripsList.forEach { trip ->
                        val obj = JSONObject().apply {
                            put("id", trip.id)
                            put("tripCode", trip.tripCode)
                            put("origin", trip.origin)
                            put("destination", trip.destination)
                            put("departureDate", trip.departureDate)
                            put("departureTime", trip.departureTime ?: "")
                            put("transportationType", trip.transportationType)
                            put("carrierCompany", trip.carrierCompany ?: "")
                            put("price", trip.price)
                            put("status", trip.status)
                            put("notes", trip.notes ?: "")
                        }
                        tripsArray.put(obj)
                    }
                    rootJson.put("trips", tripsArray)

                    // Export Bookings
                    val bookingsArray = JSONArray()
                    bookingsList.forEach { booking ->
                        val obj = JSONObject().apply {
                            put("id", booking.id)
                            put("bookingReference", booking.bookingReference)
                            put("clientId", booking.clientId)
                            put("tripId", booking.tripId)
                            put("bookingDate", booking.bookingDate)
                            put("status", booking.status)
                            put("totalAmount", booking.totalAmount)
                            put("paidAmount", booking.paidAmount)
                            put("paymentStatus", booking.paymentStatus)
                            put("notes", booking.notes ?: "")
                        }
                        bookingsArray.put(obj)
                    }
                    rootJson.put("bookings", bookingsArray)

                    context.contentResolver.openOutputStream(it)?.use { stream ->
                        stream.write(rootJson.toString(2).toByteArray())
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "تم تصدير النسخة الاحتياطية بنجاح!", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "حدث خطأ أثناء التصدير: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    // Launcher for importing JSON
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    val content = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() }
                    if (!content.isNullExhaustive()) {
                        pendingJsonData = content
                        withContext(Dispatchers.Main) {
                            showRestoreConfirmDialog = true
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "فشل قراءة ملف النسخة الاحتياطية", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("إعدادات النظام والنسخ الاحتياطي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("إدارة البيانات، التصدير والاستعادة دون فقدان سجلاتك", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("النسخ الاحتياطي والاستعادة", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }

                    Text(
                        "يمكنك حفظ جميع بيانات العملاء والوجهات والرحلات والحجوزات في ملف نسخي خارجي بصيغة JSON واستعادتها في أي وقت.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("tirhal_backup_${System.currentTimeMillis()}.json") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تصدير النسخة")
                        }

                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استعادة النسخة")
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("معلومات التطبيق وقاعدة البيانات", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }

                    Text("اسم التطبيق: ترحال AI (Tirhal AI)", style = MaterialTheme.typography.bodyMedium)
                    Text("الإصدار: 1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Text("حالة قاعدة البيانات: متصلة (نسخة v2 مع هجرة سلسة بدون كسر البيانات)", style = MaterialTheme.typography.bodySmall)
                    Text("إجمالي السجلات: ${clientsList.size} عملاء / ${bookingsList.size} حجوزات", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    if (showRestoreConfirmDialog && pendingJsonData != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("تأكيد استعادة البيانات") },
            text = { Text("هل تريد استعادة البيانات من الملف المحدد؟ سيتم إضافة وتأمين سجلات العملاء والحجوزات مع الاحتفاظ بالعلاقات بينها.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                val root = JSONObject(pendingJsonData!!)
                                val clientIdMap = mutableMapOf<Long, Long>()
                                val tripIdMap = mutableMapOf<Long, Long>()

                                if (root.has("clients")) {
                                    val clientsArr = root.getJSONArray("clients")
                                    for (i in 0 until clientsArr.length()) {
                                        val c = clientsArr.getJSONObject(i)
                                        val oldId = c.optLong("id", -1L)
                                        val newId = database.clientDao().insertClient(
                                            ClientEntity(
                                                fullName = c.getString("fullName"),
                                                phoneNumber = c.getString("phoneNumber"),
                                                whatsappNumber = c.optString("whatsappNumber").ifBlank { c.getString("phoneNumber") },
                                                email = c.optString("email").ifBlank { null },
                                                address = c.optString("address").ifBlank { null },
                                                preferredDestinations = c.optString("preferredDestinations").ifBlank { null },
                                                lastTripDate = c.optString("lastTripDate").ifBlank { null },
                                                expectedNextTravelDate = c.optString("expectedNextTravelDate").ifBlank { null },
                                                travelCycleMonths = c.optInt("travelCycleMonths", 6),
                                                satisfactionRating = c.optInt("satisfactionRating", 5),
                                                notes = c.optString("notes").ifBlank { null }
                                            )
                                        )
                                        if (oldId != -1L) {
                                            clientIdMap[oldId] = newId
                                        }
                                    }
                                }

                                if (root.has("trips")) {
                                    val tripsArr = root.getJSONArray("trips")
                                    for (i in 0 until tripsArr.length()) {
                                        val t = tripsArr.getJSONObject(i)
                                        val oldId = t.optLong("id", -1L)
                                        val newId = database.tripDao().insertTrip(
                                            TripEntity(
                                                tripCode = t.getString("tripCode"),
                                                origin = t.getString("origin"),
                                                destination = t.getString("destination"),
                                                departureDate = t.getString("departureDate"),
                                                departureTime = t.optString("departureTime").ifBlank { null },
                                                transportationType = t.getString("transportationType"),
                                                carrierCompany = t.optString("carrierCompany").ifBlank { null },
                                                price = t.getDouble("price"),
                                                status = t.getString("status"),
                                                notes = t.optString("notes").ifBlank { null }
                                            )
                                        )
                                        if (oldId != -1L) {
                                            tripIdMap[oldId] = newId
                                        }
                                    }
                                }

                                if (root.has("bookings")) {
                                    val bookingsArr = root.getJSONArray("bookings")
                                    for (i in 0 until bookingsArr.length()) {
                                        val b = bookingsArr.getJSONObject(i)
                                        val rawClientId = b.getLong("clientId")
                                        val rawTripId = b.getLong("tripId")

                                        val targetClientId = clientIdMap[rawClientId] ?: rawClientId
                                        val targetTripId = tripIdMap[rawTripId] ?: rawTripId

                                        database.bookingDao().insertBooking(
                                            BookingEntity(
                                                bookingReference = b.getString("bookingReference"),
                                                clientId = targetClientId,
                                                tripId = targetTripId,
                                                bookingDate = b.getString("bookingDate"),
                                                status = b.getString("status"),
                                                totalAmount = b.getDouble("totalAmount"),
                                                paidAmount = b.getDouble("paidAmount"),
                                                paymentStatus = b.getString("paymentStatus"),
                                                notes = b.optString("notes").ifBlank { null }
                                            )
                                        )
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    showRestoreConfirmDialog = false
                                    pendingJsonData = null
                                    Toast.makeText(context, "تمت استعادة البيانات ونماذج العملاء بنجاح!", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    showRestoreConfirmDialog = false
                                    Toast.makeText(context, "خطأ أثناء استعادة البيانات: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                ) {
                    Text("استعادة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

private fun String?.isNullExhaustive(): Boolean = this == null || this.isBlank()
