package com.tirhal.ai.ui.screens.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tirhal.ai.data.local.entity.ClientEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClientScreen(
    clientId: Long?,
    viewModel: ClientViewModel,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    var existingClient by remember { mutableStateOf<ClientEntity?>(null) }

    // Form fields
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var whatsappNumber by remember { mutableStateOf("") }
    var clientType by remember { mutableStateOf("فرد") }
    var companionCountText by remember { mutableStateOf("0") }
    var preferredDestination by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Travel & Services fields
    var previousTrips by remember { mutableStateOf("") }
    var usedServices by remember { mutableStateOf("") }
    var travelPreferences by remember { mutableStateOf("") }
    var ratingText by remember { mutableStateOf("0") }
    var contactLog by remember { mutableStateOf("") }

    // Trip details
    var ticketNumber by remember { mutableStateOf("") }
    var departureDateTime by remember { mutableStateOf("") }
    var departureRoute by remember { mutableStateOf("") }
    var returnDateTime by remember { mutableStateOf("") }
    var returnRoute by remember { mutableStateOf("") }
    var airlineCompany by remember { mutableStateOf("") }
    var flightNumber by remember { mutableStateOf("") }
    var returnStatus by remember { mutableStateOf("") }

    // Guarantor fields
    var guarantorName by remember { mutableStateOf("") }
    var guarantorPhone by remember { mutableStateOf("") }
    var guarantorWhatsapp by remember { mutableStateOf("") }
    var guarantorNotes by remember { mutableStateOf("") }

    var fullNameError by remember { mutableStateOf(false) }
    var phoneNumberError by remember { mutableStateOf(false) }

    LaunchedEffect(clientId) {
        if (clientId != null && clientId != 0L) {
            val c = viewModel.getClientById(clientId)
            if (c != null) {
                existingClient = c
                fullName = c.fullName
                phoneNumber = c.phoneNumber
                whatsappNumber = c.whatsappNumber ?: ""
                clientType = c.clientType
                companionCountText = c.companionCount.toString()
                preferredDestination = c.preferredDestination ?: ""
                email = c.email ?: ""
                address = c.address ?: ""
                notes = c.notes ?: ""

                previousTrips = c.previousTrips ?: ""
                usedServices = c.usedServices ?: ""
                travelPreferences = c.travelPreferences ?: ""
                ratingText = if (c.rating > 0) c.rating.toString() else "0"
                contactLog = c.contactLog ?: ""

                ticketNumber = c.ticketNumber ?: ""
                departureDateTime = c.departureDateTime ?: ""
                departureRoute = c.departureRoute ?: ""
                returnDateTime = c.returnDateTime ?: ""
                returnRoute = c.returnRoute ?: ""
                airlineCompany = c.airlineCompany ?: ""
                flightNumber = c.flightNumber ?: ""
                returnStatus = c.returnStatus ?: ""

                guarantorName = c.guarantorName ?: ""
                guarantorPhone = c.guarantorPhone ?: ""
                guarantorWhatsapp = c.guarantorWhatsapp ?: ""
                guarantorNotes = c.guarantorNotes ?: ""
            }
        }
    }

    val isEditing = clientId != null && clientId != 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditing) "تعديل بيانات العميل" else "إضافة مسافر/عميل جديد") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. البيانات الأساسية
            FormSectionCard(title = "البيانات الأساسية", icon = Icons.Default.Person) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        if (it.isNotBlank()) fullNameError = false
                    },
                    label = { Text(text = "الاسم الكامل *") },
                    isError = fullNameError,
                    supportingText = if (fullNameError) { { Text(text = "يرجى إدخال الاسم الكامل") } } else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        phoneNumber = it
                        if (it.isNotBlank()) phoneNumberError = false
                    },
                    label = { Text(text = "رقم الهاتف *") },
                    isError = phoneNumberError,
                    supportingText = if (phoneNumberError) { { Text(text = "يرجى إدخال رقم الهاتف") } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = whatsappNumber,
                    onValueChange = { whatsappNumber = it },
                    label = { Text(text = "رقم WhatsApp") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "نوع العميل:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val clientTypes = listOf("فرد", "عائلة", "شركة", "مؤسسة", "مجموعة")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    clientTypes.forEach { type ->
                        FilterChip(
                            selected = clientType == type,
                            onClick = { clientType = type },
                            label = { Text(text = type) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = companionCountText,
                    onValueChange = { companionCountText = it },
                    label = { Text(text = "عدد المرافقين / أفراد العائلة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = preferredDestination,
                    onValueChange = { preferredDestination = it },
                    label = { Text(text = "الوجهة المفضلة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(text = "ملاحظات عامة") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }

            // 2. بيانات السفر والخدمات
            FormSectionCard(title = "بيانات السفر والخدمات", icon = Icons.Default.CardTravel) {
                OutlinedTextField(
                    value = previousTrips,
                    onValueChange = { previousTrips = it },
                    label = { Text(text = "الرحلات السابقة") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = usedServices,
                    onValueChange = { usedServices = it },
                    label = { Text(text = "الخدمات التي استخدمها العميل") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = travelPreferences,
                    onValueChange = { travelPreferences = it },
                    label = { Text(text = "تفضيلات السفر") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = ratingText,
                    onValueChange = { ratingText = it },
                    label = { Text(text = "تقييم العميل (من 1 إلى 5)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contactLog,
                    onValueChange = { contactLog = it },
                    label = { Text(text = "سجل التواصل") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // 3. بيانات الرحلة الأساسية
            FormSectionCard(title = "بيانات الرحلة الأساسية داخل الملف", icon = Icons.Default.Flight) {
                OutlinedTextField(
                    value = ticketNumber,
                    onValueChange = { ticketNumber = it },
                    label = { Text(text = "رقم التذكرة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = departureDateTime,
                    onValueChange = { departureDateTime = it },
                    label = { Text(text = "تاريخ ووقت المغادرة (مثال: 2025-05-10 14:00)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = departureRoute,
                    onValueChange = { departureRoute = it },
                    label = { Text(text = "مسار المغادرة (مثال: الخرطوم -> القاهرة)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = returnDateTime,
                    onValueChange = { returnDateTime = it },
                    label = { Text(text = "تاريخ ووقت العودة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = returnRoute,
                    onValueChange = { returnRoute = it },
                    label = { Text(text = "مسار العودة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = airlineCompany,
                    onValueChange = { airlineCompany = it },
                    label = { Text(text = "شركة الطيران / النقل") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = flightNumber,
                    onValueChange = { flightNumber = it },
                    label = { Text(text = "رقم الرحلة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = returnStatus,
                    onValueChange = { returnStatus = it },
                    label = { Text(text = "حالة العودة (مثال: مؤكدة / معلقة)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 4. بيانات الضامن / الوسيط
            FormSectionCard(title = "بيانات الضامن / الوسيط", icon = Icons.Default.Security) {
                OutlinedTextField(
                    value = guarantorName,
                    onValueChange = { guarantorName = it },
                    label = { Text(text = "اسم الضامن أو الوسيط") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = guarantorPhone,
                    onValueChange = { guarantorPhone = it },
                    label = { Text(text = "رقم هاتف الضامن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = guarantorWhatsapp,
                    onValueChange = { guarantorWhatsapp = it },
                    label = { Text(text = "WhatsApp الضامن") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = guarantorNotes,
                    onValueChange = { guarantorNotes = it },
                    label = { Text(text = "ملاحظات الضامن") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // Save Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val isNameValid = fullName.isNotBlank()
                        val isPhoneValid = phoneNumber.isNotBlank()

                        fullNameError = !isNameValid
                        phoneNumberError = !isPhoneValid

                        if (isNameValid && isPhoneValid) {
                            val clientToSave = ClientEntity(
                                id = existingClient?.id ?: 0L,
                                fullName = fullName.trim(),
                                phoneNumber = phoneNumber.trim(),
                                whatsappNumber = whatsappNumber.trim().ifBlank { null },
                                clientType = clientType,
                                companionCount = companionCountText.toIntOrNull() ?: 0,
                                preferredDestination = preferredDestination.trim().ifBlank { null },
                                email = email.trim().ifBlank { null },
                                address = address.trim().ifBlank { null },
                                notes = notes.trim().ifBlank { null },

                                previousTrips = previousTrips.trim().ifBlank { null },
                                usedServices = usedServices.trim().ifBlank { null },
                                travelPreferences = travelPreferences.trim().ifBlank { null },
                                rating = ratingText.toFloatOrNull() ?: 0.0f,
                                contactLog = contactLog.trim().ifBlank { null },

                                ticketNumber = ticketNumber.trim().ifBlank { null },
                                departureDateTime = departureDateTime.trim().ifBlank { null },
                                departureRoute = departureRoute.trim().ifBlank { null },
                                returnDateTime = returnDateTime.trim().ifBlank { null },
                                returnRoute = returnRoute.trim().ifBlank { null },
                                airlineCompany = airlineCompany.trim().ifBlank { null },
                                flightNumber = flightNumber.trim().ifBlank { null },
                                returnStatus = returnStatus.trim().ifBlank { null },

                                guarantorName = guarantorName.trim().ifBlank { null },
                                guarantorPhone = guarantorPhone.trim().ifBlank { null },
                                guarantorWhatsapp = guarantorWhatsapp.trim().ifBlank { null },
                                guarantorNotes = guarantorNotes.trim().ifBlank { null },

                                createdAt = existingClient?.createdAt ?: System.currentTimeMillis()
                            )

                            viewModel.saveClient(clientToSave) {
                                onSaved()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isEditing) "حفظ التعديلات" else "إضافة العميل")
                }

                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "إلغاء")
                }
            }
        }
    }
}

@Composable
fun FormSectionCard(
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
                    tint = MaterialTheme.colorScheme.primary
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

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}
