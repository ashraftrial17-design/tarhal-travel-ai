package com.tirhal.ai.ui.screens

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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tirhal.ai.data.local.DatabaseProvider

@Composable
fun OperationsCenterScreen() {
    val context = LocalContext.current
    val database = remember { DatabaseProvider.getDatabase(context) }

    val clientsList by database.clientDao().getAllClients().collectAsState(initial = emptyList())
    val tripsList by database.tripDao().getAllTrips().collectAsState(initial = emptyList())
    val bookingsList by database.bookingDao().getAllBookings().collectAsState(initial = emptyList())

    val totalBookingsValue = bookingsList.sumOf { it.totalAmount }
    val totalRevenueCollected = bookingsList.sumOf { it.paidAmount }
    val totalPendingRevenue = totalBookingsValue - totalRevenueCollected

    val completedBookingsCount = bookingsList.count { it.status == "مكتمل" }
    val confirmedUpcomingCount = bookingsList.count { it.status == "مؤكد" }
    val pendingBookingsCount = bookingsList.count { it.status == "قيد الانتظار" }
    val cancelledBookingsCount = bookingsList.count { it.status == "ملغى" || it.status == "ملغاة" }

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
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("اللوحة المالية ومركز العمليات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("مراقبة دقيقة وشاملة للأداء المالي وإحصائيات الحجوزات لمكتب السفريات", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Main Financial Overview Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("💰 الملخص المالي العام للمكتب", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي قيمة الحجوزات:", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalBookingsValue ريال", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي المدفوعات المحصلة:", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalRevenueCollected ريال", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي المبالغ المتبقية (قيد التحصيل):", style = MaterialTheme.typography.bodyMedium)
                        Text("$totalPendingRevenue ريال", fontWeight = FontWeight.Bold, color = if (totalPendingRevenue > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FinancialMetricCard(
                    title = "إجمالي العملاء",
                    value = "${clientsList.size}",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    title = "الرحلات المسجلة",
                    value = "${tripsList.size}",
                    icon = Icons.Default.DirectionsBus,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FinancialMetricCard(
                    title = "إجمالي الحجوزات",
                    value = "${bookingsList.size}",
                    icon = Icons.Default.ConfirmationNumber,
                    modifier = Modifier.weight(1f)
                )
                FinancialMetricCard(
                    title = "المبالغ المحصلة",
                    value = "$totalRevenueCollected ريال",
                    icon = Icons.Default.AttachMoney,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Detailed Bookings Status Breakdowns
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📌 إحصائيات حالات الحجوزات", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الحجوزات المكتملة:", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("$completedBookingsCount حجز", fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الحجوزات القادمة والمؤكدة:", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("$confirmedUpcomingCount حجز", fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassTop, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الحجوزات قيد الانتظار:", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("$pendingBookingsCount حجز", fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MoneyOff, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.height(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("الحجوزات الملغاة:", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("$cancelledBookingsCount حجز", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
