package com.tirhal.ai.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tirhal.ai.data.local.DatabaseProvider
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String
)

@Composable
fun AICenterScreen() {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }
    val scope = rememberCoroutineScope()

    val clientsList by database.clientDao().getAllClients().collectAsState(initial = emptyList())
    val bookingsList by database.bookingDao().getAllBookings().collectAsState(initial = emptyList())
    val followUpsList by database.followUpActionDao().getAllFollowUpActions().collectAsState(initial = emptyList())
    val ratingsList by database.travelerRatingDao().getAllRatings().collectAsState(initial = emptyList())

    val messages = remember {
        mutableStateListOf(
            ChatMessage("ai", "أهلاً بك في قسم 'اسأل ترحال'! أنا مساعدك الذكي لتحليل بيانات المكتب المحلي. اختر سؤالاً سريعاً أو اكتب استفسارك مباشرة.")
        )
    }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickPrompts = listOf(
        "من يحتاج متابعة؟",
        "من العملاء المنقطعين؟",
        "من لديه رضا منخفض؟",
        "من المتوقع أن يسافر قريبًا؟",
        "ما الإجراءات المطلوبة اليوم؟"
    )

    fun handleQuery(query: String) {
        messages.add(ChatMessage("user", query))

        val response = when {
            query.contains("متابعة") -> {
                val pendingBookings = bookingsList.filter { it.paymentStatus != "مدفوع بالكامل" }
                val pendingActions = followUpsList.filter { it.status == "معلقة" }
                "📊 تحليل المتابعة:\n" +
                        "• يوجد ${pendingBookings.size} حجز يتطلب متابعة التحصيل المالي.\n" +
                        "• يوجد ${pendingActions.size} إجراء متابعة معلق يتطلب التنسيق مع العميل."
            }
            query.contains("منقطعين") -> {
                val inactive = clientsList.filter { client ->
                    bookingsList.none { it.clientId == client.id && it.status != "مكتمل" }
                }
                "👥 العملاء المنقطعين حالياً: ${inactive.size} عملاء.\n" +
                        "أبرزهم: ${inactive.joinToString(", ") { it.fullName }}.\n" +
                        "💡 ينصح بتقديم عروض تسويقية لهم في قسم التسويق الإلكتروني."
            }
            query.contains("رضا منخفض") -> {
                val lowRatings = ratingsList.filter { it.ratingStars <= 3 }
                if (lowRatings.isEmpty()) {
                    "🌟 ممتاز! جميع تقييمات العملاء المتاحة عالية ولا يوجد تقييمات منخفضة."
                } else {
                    val clientsWithLow = lowRatings.mapNotNull { r -> clientsList.find { it.id == r.travelerId }?.fullName }
                    "⚠️ العملاء ذوو التقييم المنخفض (<= 3 نجوم):\n" +
                            clientsWithLow.joinToString("\n• ") +
                            "\n💡 يُوصى بالتواصل معهم عبر قسم المتابعة لتقديم اعتذار وتوضيح الخدمات."
                }
            }
            query.contains("يسافر قريبًا") || query.contains("متوقع") -> {
                val upcoming = bookingsList.filter { it.status == "مؤكد" || it.status == "قيد الانتظار" }
                "✈️ العملاء المتوقع سفرهم قريبًا:\n" +
                        if (upcoming.isEmpty()) "لا توجد رحلات مجدولة سارية حالياً."
                        else upcoming.joinToString("\n") { b ->
                            val cName = clientsList.find { it.id == b.clientId }?.fullName ?: "عميل"
                            "• $cName - حجز رقم (${b.bookingReference})"
                        }
            }
            query.contains("اليوم") || query.contains("الإجراءات") -> {
                "📝 الإجراءات المطلوبة اليوم:\n" +
                        "• إجمالي الحجوزات القائمة: ${bookingsList.size}\n" +
                        "• المهام المعلقة: ${followUpsList.filter { it.status == "معلقة" }.size}\n" +
                        "💡 يمكنك الانتقال لصفحة 'متابعة المسافر' لاستعراض الأزرار والتواصل المباشر عبر واتساب."
            }
            else -> {
                "بناءً على التحليل المحلي لبيانات المكتب الحالية:\n" +
                        "• عدد العملاء المسجلين: ${clientsList.size}\n" +
                        "• عدد الحجوزات: ${bookingsList.size}\n" +
                        "يسعدني مساعدتك بإجابات مخصصة عبر الأسئلة السريعة أعلاه."
            }
        }

        messages.add(ChatMessage("ai", response))
    }

    LaunchedEffect(messages.size) {
        listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // AI Title Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("اسأل ترحال AI - المساعد الذكي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("تحليل دقيق ومباشر لبيانات وقواعد بيانات تطبيقك محلياً", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Prompts Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickPrompts) { prompt ->
                FilterChip(
                    selected = false,
                    onClick = { handleQuery(prompt) },
                    label = { Text(prompt) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Messages List
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "user"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 12.dp
                                )
                            )
                            .background(
                                if (isUser) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = msg.text,
                            color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input Field and Send Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("اسأل ترحال أي سؤال...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText
                        inputText = ""
                        handleQuery(text)
                    }
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "إرسال",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
