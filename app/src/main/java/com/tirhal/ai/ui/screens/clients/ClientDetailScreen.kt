package com.tirhal.ai.ui.screens.clients

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tirhal.ai.data.local.entity.ClientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: Long,
    viewModel: ClientViewModel,
    onBackClick: () -> Unit,
    onEditClick: (Long) -> Unit,
    onDeleted: () -> Unit
) {
    var client by remember { mutableStateOf<ClientEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(clientId) {
        client = viewModel.getClientById(clientId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "تأكيد الحذف") },
            text = { Text(text = "هل أنت تأكد من رغبتك في حذف العميل \"${client?.fullName}\"؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        client?.let { c ->
                            viewModel.deleteClient(c) {
                                showDeleteDialog = false
                                onDeleted()
                            }
                        }
                    }
                ) {
                    Text(
                        text = "حذف",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "إلغاء")
                }
            }
        )
    }

    val currentClient = client

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = currentClient?.fullName ?: "ملف العميل") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    if (currentClient != null) {
                        IconButton(onClick = { onEditClick(currentClient.id) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        if (currentClient == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "جاري تحميل بيانات العميل...")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Client Header Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = currentClient.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "نوع العميل: ${currentClient.clientType}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // 1. البيانات الأساسية
                DetailSectionCard(
                    title = "البيانات الأساسية",
                    icon = Icons.Default.Person
                ) {
                    DetailRow(label = "الاسم الكامل", value = currentClient.fullName)
                    DetailRow(label = "رقم الهاتف", value = currentClient.phoneNumber)
                    DetailRow(label = "رقم WhatsApp", value = currentClient.whatsappNumber)
                    DetailRow(label = "نوع العميل", value = currentClient.clientType)
                    DetailRow(
                        label = "عدد المرافقين / أفراد العائلة",
                        value = if (currentClient.companionCount > 0) "${currentClient.companionCount}" else "لا يوجد"
                    )
                    DetailRow(label = "الوجهة المفضلة", value = currentClient.preferredDestination)
                    DetailRow(label = "ملاحظات عامة", value = currentClient.notes)
                }

                // 2. بيانات السفر والخدمات
                DetailSectionCard(
                    title = "بيانات السفر والخدمات",
                    icon = Icons.Default.CardTravel
                ) {
                    DetailRow(label = "الرحلات السابقة", value = currentClient.previousTrips)
                    DetailRow(label = "الخدمات المستخدمة", value = currentClient.usedServices)
                    DetailRow(label = "تفضيلات السفر", value = currentClient.travelPreferences)
                    DetailRow(
                        label = "تقييم العميل",
                        value = if (currentClient.rating > 0) "⭐ ${currentClient.rating} / 5" else null
                    )
                    DetailRow(label = "سجل التواصل", value = currentClient.contactLog)
                }

                // 3. بيانات الرحلة الأساسية
                DetailSectionCard(
                    title = "بيانات الرحلة الأساسية داخل الملف",
                    icon = Icons.Default.Flight
                ) {
                    DetailRow(label = "رقم التذكرة", value = currentClient.ticketNumber)
                    DetailRow(label = "تاريخ ووقت المغادرة", value = currentClient.departureDateTime)
                    DetailRow(label = "مسار المغادرة", value = currentClient.departureRoute)
                    DetailRow(label = "تاريخ ووقت العودة", value = currentClient.returnDateTime)
                    DetailRow(label = "مسار العودة", value = currentClient.returnRoute)
                    DetailRow(label = "شركة الطيران / النقل", value = currentClient.airlineCompany)
                    DetailRow(label = "رقم الرحلة", value = currentClient.flightNumber)
                    DetailRow(label = "حالة العودة", value = currentClient.returnStatus)
                }

                // 4. بيانات الضامن / الوسيط
                DetailSectionCard(
                    title = "بيانات الضامن / الوسيط",
                    icon = Icons.Default.Security
                ) {
                    DetailRow(label = "اسم الضامن / الوسيط", value = currentClient.guarantorName)
                    DetailRow(label = "رقم الهاتف", value = currentClient.guarantorPhone)
                    DetailRow(label = "رقم WhatsApp", value = currentClient.guarantorWhatsapp)
                    DetailRow(label = "ملاحظات الضامن", value = currentClient.guarantorNotes)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { onEditClick(currentClient.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "تعديل البيانات")
                    }

                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "حذف العميل")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String?
) {
    val displayValue = if (value.isNullOrBlank()) "غير محدد" else value

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyMedium,
            color = if (value.isNullOrBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.55f)
        )
    }
}
