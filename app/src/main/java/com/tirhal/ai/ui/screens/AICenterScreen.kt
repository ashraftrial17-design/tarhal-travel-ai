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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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

data class ChatMessage(
    val sender: String, // "user" or "ai"
    val text: String
)

@Composable
fun AICenterScreen() {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }

    val clientsList by database.clientDao().getAllClients().collectAsState(initial = emptyList())
    val bookingsList by database.bookingDao().getAllBookings().collectAsState(initial = emptyList())
    val followUpsList by database.followUpActionDao().getAllFollowUpActions().collectAsState(initial = emptyList())

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("اسأل ترحال (المحادثة)", "مقترحات المتابعة", "فرص البيع", "أفكار وحملات تسويقية")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("مركز الذكاء الاصطناعي والمتابعة الذكية", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("تحليل دقيق ومحلي لبيانات وقواعد بيانات مكتبك بذكاء وفكاهة ترحال", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTabIndex) {
            0 -> AIChatTab(clientsList, bookingsList, followUpsList)
            1 -> AIFollowUpSuggestionsTab(clientsList, bookingsList, followUpsList, context)
            2 -> AISalesOpportunitiesTab(clientsList, bookingsList, context)
            3 -> AIMarketingIdeasTab(context)
        }
    }
}

@Composable
fun AIChatTab(
    clientsList: List<com.tirhal.ai.data.local.entity.ClientEntity>,
    bookingsList: List<com.tirhal.ai.data.local.entity.BookingEntity>,
    followUpsList: List<com.tirhal.ai.data.local.entity.FollowUpActionEntity>
) {
    val messages = remember {
        mutableStateListOf(
            ChatMessage("ai", "أهلاً بك في قسم 'اسأل ترحال'! أنا مساعدك الذكي لتحليل بيانات المكتب محلياً. اختر سؤالاً سريعاً أو اكتب استفسارك مباشرة.")
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
                val lowSatisfactionClients = clientsList.filter { it.satisfactionRating <= 3 }
                if (lowSatisfactionClients.isEmpty()) {
                    "🌟 ممتاز! جميع تقييمات رضا العملاء المتاحة عالية (أكثر من 3 نجوم)."
                } else {
                    "⚠️ العملاء ذوو التقييم والرضا المنخفض (<= 3 نجوم):\n• " +
                            lowSatisfactionClients.joinToString("\n• ") { "${it.fullName} (تقييم: ${it.satisfactionRating}/5)" } +
                            "\n💡 يُوصى بالتواصل معهم عبر المتابعة وتحديد سبب عدم الرضا."
                }
            }
            query.contains("يسافر قريبًا") || query.contains("متوقع") -> {
                val upcoming = clientsList.filter { !it.expectedNextTravelDate.isNullOrBlank() }
                "✈️ العملاء المتوقع سفرهم قريبًا حسب ملفاتهم:\n" +
                        if (upcoming.isEmpty()) "لا توجد مواعيد سفر متوقعة قريبة في ملفات العملاء."
                        else upcoming.joinToString("\n") { c ->
                            "• ${c.fullName} - تاريخ السفر المتوقع: (${c.expectedNextTravelDate})"
                        }
            }
            query.contains("اليوم") || query.contains("الإجراءات") -> {
                "📝 الإجراءات المطلوبة اليوم:\n" +
                        "• إجمالي الحجوزات القائمة: ${bookingsList.size}\n" +
                        "• المهام المعلقة: ${followUpsList.filter { it.status == "معلقة" }.size}\n" +
                        "💡 يمكنك الانتقال لتبويب 'مقترحات المتابعة' للتواصل المباشر عبر واتساب."
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

    Column(modifier = Modifier.fillMaxSize()) {
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

@Composable
fun AIFollowUpSuggestionsTab(
    clientsList: List<com.tirhal.ai.data.local.entity.ClientEntity>,
    bookingsList: List<com.tirhal.ai.data.local.entity.BookingEntity>,
    followUpsList: List<com.tirhal.ai.data.local.entity.FollowUpActionEntity>,
    context: Context
) {
    val pendingFollowUps = followUpsList.filter { it.status == "معلقة" }

    if (pendingFollowUps.isEmpty() && bookingsList.all { it.paymentStatus == "مدفوع بالكامل" }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد مقترحات متابعة حالية. جميع أمور العملاء مستقرة ومكتملة!")
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(pendingFollowUps) { action ->
                val client = clientsList.find { it.id == action.travelerId }
                val clientName = client?.fullName ?: "العميل"
                val phone = client?.whatsappNumber ?: client?.phoneNumber ?: ""

                val suggestedMsg = "مرحبًا أستاذ $clientName، يود فريق ترحال AI تذكيرك بخصوص: ${action.description}. يسعدنا خدمتكم في أي وقت."

                AISuggestionCard(
                    title = "مهمة متابعة: ${action.actionType}",
                    subtitle = "العميل: $clientName | موعد الإجراء: ${action.dueDate}",
                    suggestedMessage = suggestedMsg,
                    phone = phone,
                    context = context
                )
            }
        }
    }
}

@Composable
fun AISalesOpportunitiesTab(
    clientsList: List<com.tirhal.ai.data.local.entity.ClientEntity>,
    bookingsList: List<com.tirhal.ai.data.local.entity.BookingEntity>,
    context: Context
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(clientsList) { client ->
            val count = bookingsList.count { it.clientId == client.id }
            val offerText = if (count > 1) "ترقية إلى باقة السفر الذهبية وتوفير حجز فندقي" else "توفير تأمين سفر وحجز طيران عودة بخصم 10%"
            val phone = client.whatsappNumber ?: client.phoneNumber
            val msg = "أهلاً أستاذ ${client.fullName}، يسعدنا في ترحال AI تقديم عرض حصري لك بخصوص $offerText! هل تود استعراض التفاصيل؟"

            AISuggestionCard(
                title = "فرصة بيع إضافية (Cross-selling)",
                subtitle = "العميل المميز: ${client.fullName} (عدد الرحلات: $count)",
                suggestedMessage = msg,
                phone = phone,
                context = context
            )
        }
    }
}

@Composable
fun AIMarketingIdeasTab(context: Context) {
    val campaignIdeas = listOf(
        Pair("حملة العمرة والزيارة الموسمية", "✨ عروض خاصة لرحلات العمرة مع ترحال AI! إقامة ممتازة وتنقلات مريحة بالحافلات والرحلات الجوية. احجز الآن واستفد من الخصم الخاص!"),
        Pair("عروض عطلة نهاية الأسبوع", "🌴 خُطط سفر خاطفة لعطلة نهاية الأسبوع من ترحال AI إلى أفضل الوجهات المحلية. خصومات على تذاكر الطيران للرحلات الفردية والعائلية!"),
        Pair("خدمات تذاكر الطيران الاقتصادي", "✈️ لا تشيل هم التذاكر! بوفر لك ترحال AI أفضل أسعار الطيران مع إمكانية تعديل وتأكيد المقاعد فوراً.")
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(campaignIdeas) { (title, ideaMsg) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(text = ideaMsg, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("فكرة حملة", ideaMsg)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ فكرة الحملة", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخ النص")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AISuggestionCard(
    title: String,
    subtitle: String,
    suggestedMessage: String,
    phone: String,
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
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp)
            ) {
                Text(text = "💬 اقتراح ترحال للرسالة:\n\"$suggestedMessage\"", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("رسالة ترحال", suggestedMessage)
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
                        val cleanPhone = phone.replace("+", "").replace(" ", "")
                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(suggestedMessage)}")
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
