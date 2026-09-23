package com.tirhal.ai.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tirhal.ai.data.local.DatabaseProvider
import com.tirhal.ai.data.local.entity.ClientEntity

@Composable
fun FollowUpTrackingScreen() {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }

    val clientsList by database.clientDao().getAllClients().collectAsState(initial = emptyList())
    val bookingsList by database.bookingDao().getAllBookings().collectAsState(initial = emptyList())
    val followUpsList by database.followUpActionDao().getAllFollowUpActions().collectAsState(initial = emptyList())
    val ratingsList by database.travelerRatingDao().getAllRatings().collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("ماذا أفعل اليوم؟", "عملاء يتطلبون متابعة", "المتوقع عودتهم للسفر")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTabIndex) {
            0 -> {
                // Today's Action Items
                TodayActionsSection(
                    clients = clientsList,
                    followUps = followUpsList,
                    context = context
                )
            }
            1 -> {
                // Clients requiring follow up (due payments, upcoming departure, or low rating)
                ClientsRequiringFollowUpSection(
                    clients = clientsList,
                    bookings = bookingsList,
                    ratings = ratingsList,
                    context = context
                )
            }
            2 -> {
                // Return cycle travelers / inactive clients
                ExpectedReturnSection(
                    clients = clientsList,
                    bookings = bookingsList,
                    context = context
                )
            }
        }
    }
}

@Composable
fun TodayActionsSection(
    clients: List<ClientEntity>,
    followUps: List<com.tirhal.ai.data.local.entity.FollowUpActionEntity>,
    context: Context
) {
    if (followUps.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("لا توجد مهام معلقة لهذا اليوم. جميع المتابعات مكتملة!")
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(followUps) { action ->
                val client = clients.find { it.id == action.travelerId }
                val clientName = client?.fullName ?: "عميل"
                val phone = client?.phoneNumber ?: ""

                val message = "مرحبًا ${clientName}، نود تذكيرك بخصوص: ${action.description} - مكتب ترحال AI للخدمات والسفريات."

                FollowUpCardItem(
                    title = "مهمة: ${action.actionType}",
                    subtitle = "العميل: $clientName | التاريخ: ${action.dueDate}",
                    description = action.description,
                    message = message,
                    phoneNumber = phone,
                    context = context
                )
            }
        }
    }
}

@Composable
fun ClientsRequiringFollowUpSection(
    clients: List<ClientEntity>,
    bookings: List<com.tirhal.ai.data.local.entity.BookingEntity>,
    ratings: List<com.tirhal.ai.data.local.entity.TravelerRatingEntity>,
    context: Context
) {
    val pendingBookings = bookings.filter { it.paymentStatus != "مدفوع بالكامل" || it.status == "قيد الانتظار" }
    val lowRatings = ratings.filter { it.ratingStars <= 3 }

    if (pendingBookings.isEmpty() && lowRatings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("جميع العملاء بحالة ممتازة ولا يحتاجون لمتابعة خاصة حالياً.")
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(pendingBookings) { booking ->
                val client = clients.find { it.id == booking.clientId }
                val clientName = client?.fullName ?: "عميل"
                val phone = client?.phoneNumber ?: ""

                val message = "مرحبًا ${clientName} العزيز، يُرجى العلم بأن لديك حجز برقم (${booking.bookingReference}) وحالة الدفع الحالية (${booking.paymentStatus}). يسعدنا استكمال الإجراءات معك - ترحال AI."

                FollowUpCardItem(
                    title = "متابعة حجز: ${booking.bookingReference}",
                    subtitle = "العميل: $clientName | حالة الدفع: ${booking.paymentStatus}",
                    description = "المبلغ المتبقي للدفعة: ${booking.totalAmount - booking.paidAmount} ريال",
                    message = message,
                    phoneNumber = phone,
                    context = context
                )
            }

            items(lowRatings) { rating ->
                val client = clients.find { it.id == rating.travelerId }
                val clientName = client?.fullName ?: "عميل"
                val phone = client?.phoneNumber ?: ""

                val message = "أهلاً بك أستاذ ${clientName}، يهُمنا جداً رضاك عن خدمات ترحال AI. لاحظنا تقييمك للرحلة السابقة ونود الاستماع لملاحظاتك وتحسين تجربتك القادمة."

                FollowUpCardItem(
                    title = "متابعة رضا عميل (تقييم ${rating.ratingStars}/5)",
                    subtitle = "العميل: $clientName",
                    description = "الملاحظة: ${rating.feedback ?: "لا توجد ملاحظات تفصيلية"}",
                    message = message,
                    phoneNumber = phone,
                    context = context
                )
            }
        }
    }
}

@Composable
fun ExpectedReturnSection(
    clients: List<ClientEntity>,
    bookings: List<com.tirhal.ai.data.local.entity.BookingEntity>,
    context: Context
) {
    // Clients with older or completed bookings (e.g., inactive for over 3 months)
    val inactiveClients = clients.filter { client ->
        val clientBookings = bookings.filter { it.clientId == client.id }
        clientBookings.all { it.status == "مكتمل" } || clientBookings.isEmpty()
    }

    if (inactiveClients.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا يوجد عملاء منقطعين في الوقت الحالي.")
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(inactiveClients) { client ->
                val message = "أهلاً أستاذ ${client.fullName}، اشتقنا لرحلاتك مع ترحال AI! لدينا عروض جديدة ومميزة متوفرة هذا الشهر للرحلات القادمة. هل تخطط لسفر قريب؟"

                FollowUpCardItem(
                    title = "عميل متوقع عودته للسفر",
                    subtitle = "العميل: ${client.fullName} | ${client.phoneNumber}",
                    description = "مرت فترة على آخر رحلة، فرصة ممتازة لعرض الرحلات والعروض الجديدة.",
                    message = message,
                    phoneNumber = client.phoneNumber,
                    context = context
                )
            }
        }
    }
}

@Composable
fun FollowUpCardItem(
    title: String,
    subtitle: String,
    description: String,
    message: String,
    phoneNumber: String,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))

            // Box showing pre-generated Arabic message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp)
            ) {
                Text(
                    text = "💬 الرسالة الجاهزة:\n\"$message\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Copy Message Button
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("رسالة ترحال", message)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ الرسالة إلى الحافظة", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.height(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نسخ الرسالة")
                }

                // Open WhatsApp Button
                Button(
                    onClick = {
                        val cleanPhone = phoneNumber.replace("+", "").replace(" ", "")
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "تعذر فتح WhatsApp، تأكد من تثبيت التطبيق", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.height(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", color = Color.White)
                }
            }
        }
    }
}
