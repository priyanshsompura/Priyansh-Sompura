package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.HMSViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HMSScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HMSScreen(viewModel: HMSViewModel = viewModel()) {
    val context = LocalContext.current
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val notificationFlow = viewModel.notification
    val coroutineScope = rememberCoroutineScope()

    // Observe custom notifications to show standard Android toasts
    LaunchedEffect(key1 = notificationFlow) {
        notificationFlow.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "CareSuite Cross Logo",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CareSuite HMS",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.5.sp
                        )
                    }
                },
                actions = {
                    RoleSelectorDropdown(
                        currentRole = currentRole,
                        onRoleSelected = { viewModel.setRole(it) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.shadowElevation()
            )
        },
        bottomBar = {
            // Adaptive design: Floating bottom role switcher for easy demo interactions
            RoleStickyFooter(
                currentRole = currentRole,
                onRoleSelected = { viewModel.setRole(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentRole,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "PortalTransition"
            ) { role ->
                when (role) {
                    "Admin" -> AdminPortal(viewModel)
                    "Doctor" -> DoctorPortal(viewModel)
                    "Patient" -> PatientPortal(viewModel)
                    "Receptionist" -> ReceptionistPortal(viewModel)
                    "Pharmacist / Lab" -> PharmacistLabPortal(viewModel)
                    else -> AdminPortal(viewModel)
                }
            }
        }
    }
}

// ==========================================
// PORTAL: ADMIN WORKSPACE
// ==========================================
@Composable
fun AdminPortal(viewModel: HMSViewModel) {
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val beds by viewModel.beds.collectAsStateWithLifecycle()
    val bills by viewModel.bills.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    var showAddDoctorDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            HMSSectionHeader(
                title = "Super Admin Terminal",
                subtitle = "Comprehensive system analytics, clinician directories, and ward bed metrics."
            )
        }

        // Stats Grid
        item {
            val totalRevenue = bills.sumOf { it.totalAmount }
            val occupiedBeds = beds.count { it.status == "Occupied" }
            val activeDoctors = doctors.size

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Doctors",
                    value = "$activeDoctors",
                    icon = Icons.Default.Person,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Revenue",
                    value = "$${"%.2f".format(totalRevenue)}",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            val occupiedBeds = beds.count { it.status == "Occupied" }
            val totalBeds = beds.size
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Active Patients",
                    value = "${patients.size}",
                    icon = Icons.Default.AccountBox,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Beds Occupied",
                    value = "$occupiedBeds / $totalBeds",
                    icon = Icons.Default.Info,
                    color = Color(0xFFE65100),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Custom Canvas Chart: Weekly Revenue Graph
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Financial Performance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Real-time billing invoices and payment revenue tracking.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    HMSRevenueCanvasChart()
                }
            }
        }

        // Ward Bed Allocator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ward Beds Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${beds.count { it.status == "Available" }} Available",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Text(
                        text = "Tap any bed to allocate/de-allocate a patient instantly.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        beds.take(5).forEach { bed ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.background)
                                    .clickable { viewModel.toggleBedStatus(bed) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = bed.bedNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "${bed.type} - ${bed.wardName}", fontSize = 11.sp, color = Color.Gray)
                                }
                                BedStatusBadge(status = bed.status)
                            }
                        }
                    }
                }
            }
        }

        // Doctor Directory List
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Clinician Directory",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(
                            onClick = { showAddDoctorDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("add_doctor_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Doctor", fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "Authorized consulting staff and active departments.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    doctors.forEach { doc ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = doc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = doc.specialty,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(text = "Schedule: ${doc.schedule} | Room: ${doc.room}", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }

        // System Audit Logs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HMS System Audit Console",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Security-ready real-time records logging all portal activities.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(auditLogs) { log ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "[${log.userRole.uppercase()}]",
                                        color = Color(0xFF38BDF8),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(90.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = log.actionDescription,
                                        color = Color(0xFFF1F5F9),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }

    if (showAddDoctorDialog) {
        AddDoctorDialog(
            onDismiss = { showAddDoctorDialog = false },
            onConfirm = { name, spec, phone, sched, room ->
                viewModel.registerDoctor(name, spec, phone, sched, room)
                showAddDoctorDialog = false
            }
        )
    }
}

// ==========================================
// PORTAL: DOCTOR WORKSPACE
// ==========================================
@Composable
fun DoctorPortal(viewModel: HMSViewModel) {
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()

    var selectedDoctorIndex by remember { mutableIntStateOf(0) }
    var expandedDoctorDropdown by remember { mutableStateOf(false) }

    val activeDoctor = doctors.getOrNull(selectedDoctorIndex)

    var diagnosingAppt by remember { mutableStateOf<Appointment?>(null) }
    var diagnosisText by remember { mutableStateOf("") }
    var prescriptionText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HMSSectionHeader(
                title = "Consulting Physician Workspace",
                subtitle = "Manage patient rosters, diagnose active appointments, and update clinical logs."
            )
        }

        // Doctor Selection Header
        item {
            if (doctors.isNotEmpty() && activeDoctor != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Consultant Shift Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedDoctorDropdown = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = activeDoctor.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text(text = "${activeDoctor.specialty} — ${activeDoctor.room}", fontSize = 12.sp, color = Color.DarkGray)
                                }
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown")
                            }

                            DropdownMenu(
                                expanded = expandedDoctorDropdown,
                                onDismissRequest = { expandedDoctorDropdown = false }
                            ) {
                                doctors.forEachIndexed { idx, doc ->
                                    DropdownMenuItem(
                                        text = { Text(doc.name) },
                                        onClick = {
                                            selectedDoctorIndex = idx
                                            expandedDoctorDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text("No doctors registered. Please go to Admin or Receptionist role to add clinicians.")
            }
        }

        // Active Consultations Queue
        if (activeDoctor != null) {
            val doctorAppts = appointments.filter { it.doctorId == activeDoctor.id }

            item {
                Text(
                    text = "Scheduled Consultations (${doctorAppts.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (doctorAppts.isEmpty()) {
                item {
                    EmptyListCard(message = "No patient appointments scheduled for this doctor.")
                }
            } else {
                items(doctorAppts) { appt ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = appt.patientName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                StatusBadge(status = appt.status)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Date: ${appt.date} | Time: ${appt.time}", fontSize = 12.sp, color = Color.Gray)
                            Text(text = "Reason: ${appt.reason}", fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))

                            if (appt.status == "Scheduled") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            diagnosingAppt = appt
                                            diagnosisText = ""
                                            prescriptionText = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.weight(1.5f).height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Diagnose", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Begin Diagnosis", fontSize = 11.sp)
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.updateAppointmentStatus(appt, "Cancelled") },
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Cancel", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Diagnostic Entry Card (Conditional)
        if (diagnosingAppt != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Consultation Clinical File: ${diagnosingAppt?.patientName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            IconButton(
                                onClick = { diagnosingAppt = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                            }
                        }
                        Text(
                            text = "Fill diagnostic evaluation details. This updates patient record & registers the prescription.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Client side input fields
                        OutlinedTextField(
                            value = diagnosisText,
                            onValueChange = { diagnosisText = it },
                            label = { Text("Clinical Assessment / Diagnosis Findings") },
                            placeholder = { Text("e.g. Hypertension stage 1, needs medication adjustment") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            minLines = 2
                        )

                        OutlinedTextField(
                            value = prescriptionText,
                            onValueChange = { prescriptionText = it },
                            label = { Text("Prescribed Treatments & Medicines") },
                            placeholder = { Text("e.g. Paracetamol 500mg BID x5 Days") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            minLines = 2
                        )

                        Button(
                            onClick = {
                                val currentAppt = diagnosingAppt
                                if (currentAppt != null) {
                                    if (diagnosisText.isBlank() || prescriptionText.isBlank()) {
                                        viewModel.showToast("Validation Error: Diagnosis and Prescription fields are required.")
                                    } else {
                                        viewModel.updateAppointmentStatus(currentAppt, "Completed")
                                        // Request dynamic prescription log
                                        viewModel.createInvoice(
                                            patientId = currentAppt.patientId,
                                            patientName = currentAppt.patientName,
                                            total = 150.0, // standard OPD consulting fee
                                            discount = 0.0,
                                            tax = 7.50,
                                            method = "Pending",
                                            isPaid = false
                                        )
                                        // Request Lab test suggestion if diagnostic triggers
                                        viewModel.requestLabTest(
                                            patientId = currentAppt.patientId,
                                            patientName = currentAppt.patientName,
                                            testName = "Standard Blood Panel (Ref: ${diagnosisText.take(15)}...)",
                                            category = "Laboratories"
                                        )
                                        diagnosingAppt = null
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Submit")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Submit Clinical Record & Complete Consult")
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// ==========================================
// PORTAL: PATIENT PORTAL
// ==========================================
@Composable
fun PatientPortal(viewModel: HMSViewModel) {
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val labTests by viewModel.labTests.collectAsStateWithLifecycle()
    val bills by viewModel.bills.collectAsStateWithLifecycle()
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()

    var selectedPatientIndex by remember { mutableIntStateOf(0) }
    var expandedPatientDropdown by remember { mutableStateOf(false) }

    val activePatient = patients.getOrNull(selectedPatientIndex)

    // Booking New Appointment State
    var showBookingCard by remember { mutableStateOf(false) }
    var selectedDoctorIndex by remember { mutableIntStateOf(0) }
    var expandedDoctorSelect by remember { mutableStateOf(false) }
    var apptDate by remember { mutableStateOf("") }
    var apptTime by remember { mutableStateOf("") }
    var apptReason by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HMSSectionHeader(
                title = "Patient Medical Portal",
                subtitle = "Access active clinic invoices, diagnostic lab reports, clinical prescriptions, and schedule consults."
            )
        }

        // Active Patient Selector
        item {
            if (patients.isNotEmpty() && activePatient != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(text = "Selected Patient Account Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedPatientDropdown = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = activePatient.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Text(text = "Blood Group: ${activePatient.bloodType} | Contact: ${activePatient.contact}", fontSize = 12.sp, color = Color.DarkGray)
                                }
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown")
                            }

                            DropdownMenu(
                                expanded = expandedPatientDropdown,
                                onDismissRequest = { expandedPatientDropdown = false }
                            ) {
                                patients.forEachIndexed { idx, pat ->
                                    DropdownMenuItem(
                                        text = { Text(pat.name) },
                                        onClick = {
                                            selectedPatientIndex = idx
                                            expandedPatientDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text("No patient registered. Please go to Receptionist role to register patient first.")
            }
        }

        if (activePatient != null) {
            // Patient details & History Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Medical History & Timeline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Age/Gender", fontSize = 11.sp, color = Color.Gray)
                                Text("${activePatient.age} Yrs / ${activePatient.gender}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allergies Alert", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    text = activePatient.allergies,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (activePatient.allergies.lowercase() != "none") MaterialTheme.colorScheme.secondary else Color.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Active Diagnosis", fontSize = 11.sp, color = Color.Gray)
                        Text(activePatient.medicalHistory, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // Book Appointment Trigger / Active Appointments
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Consultations History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { showBookingCard = !showBookingCard },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("book_appt_btn")
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Book", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showBookingCard) "Hide Scheduler" else "Schedule Visit", fontSize = 11.sp)
                    }
                }
            }

            // Quick Booking Form
            if (showBookingCard) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("New Consultation Booking", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Select an available physician and reserve a slot.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                            if (doctors.isNotEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                                    OutlinedButton(
                                        onClick = { expandedDoctorSelect = true },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Physician: ${doctors.getOrNull(selectedDoctorIndex)?.name ?: "Select clinician"}")
                                    }
                                    DropdownMenu(
                                        expanded = expandedDoctorSelect,
                                        onDismissRequest = { expandedDoctorSelect = false }
                                    ) {
                                        doctors.forEachIndexed { index, doctor ->
                                            DropdownMenuItem(
                                                text = { Text("${doctor.name} (${doctor.specialty})") },
                                                onClick = {
                                                    selectedDoctorIndex = index
                                                    expandedDoctorSelect = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = apptDate,
                                onValueChange = { apptDate = it },
                                label = { Text("Consult Date") },
                                placeholder = { Text("YYYY-MM-DD") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = apptTime,
                                onValueChange = { apptTime = it },
                                label = { Text("Slot Time") },
                                placeholder = { Text("HH:MM AM/PM") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )

                            OutlinedTextField(
                                value = apptReason,
                                onValueChange = { apptReason = it },
                                label = { Text("Reason for visit") },
                                placeholder = { Text("Short symptom description") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )

                            Button(
                                onClick = {
                                    val doc = doctors.getOrNull(selectedDoctorIndex)
                                    if (doc == null || apptDate.isBlank() || apptTime.isBlank() || apptReason.isBlank()) {
                                        viewModel.showToast("Validation Error: Please select a doctor and fill out all booking fields.")
                                    } else {
                                        viewModel.bookAppointment(
                                            patientId = activePatient.id,
                                            patientName = activePatient.name,
                                            doctorId = doc.id,
                                            doctorName = doc.name,
                                            date = apptDate,
                                            time = apptTime,
                                            reason = apptReason
                                        )
                                        showBookingCard = false
                                        apptDate = ""
                                        apptTime = ""
                                        apptReason = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Confirm Scheduled Booking")
                            }
                        }
                    }
                }
            }

            // Consult list
            val patientAppts = appointments.filter { it.patientId == activePatient.id }
            if (patientAppts.isEmpty()) {
                item {
                    EmptyListCard(message = "No visits scheduled for this patient.")
                }
            } else {
                items(patientAppts) { appt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = appt.doctorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Date: ${appt.date} | ${appt.time}", fontSize = 12.sp, color = Color.Gray)
                            Text(text = appt.reason, fontSize = 11.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        StatusBadge(status = appt.status)
                    }
                }
            }

            // Lab reports
            item {
                Text(
                    text = "Laboratory & Radiology Reports",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            val patientTests = labTests.filter { it.patientId == activePatient.id }
            if (patientTests.isEmpty()) {
                item {
                    EmptyListCard(message = "No laboratory tests issued.")
                }
            } else {
                items(patientTests) { test ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = test.testName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Category: ${test.category} | Req: ${test.requestedDate}", fontSize = 11.sp, color = Color.Gray)
                                }
                                BedStatusBadge(status = if (test.status == "Pending") "Available" else "Occupied", availableLabel = "Pending", occupiedLabel = "Completed")
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Findings: ${test.result}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (test.status == "Pending") Color.Gray else MaterialTheme.colorScheme.primary
                            )
                            if (test.status == "Completed") {
                                Text(text = "Logged by: ${test.technicianName}", fontSize = 10.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }

            // Billings / Invoice lists
            item {
                Text(
                    text = "Billing & Receipt Invoices",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            val patientBills = bills.filter { it.patientId == activePatient.id }
            if (patientBills.isEmpty()) {
                item {
                    EmptyListCard(message = "No billing ledger or invoices generated.")
                }
            } else {
                items(patientBills) { bill ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Invoice ID: #HMS-00${bill.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Date: ${bill.invoiceDate} | Method: ${bill.paymentMethod}", fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text(text = "Total Amount: ", fontSize = 12.sp, color = Color.Gray)
                                    Text(text = "$${"%.2f".format(bill.totalAmount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                BedStatusBadge(status = if (bill.status == "Paid") "Available" else "Occupied", availableLabel = "PAID", occupiedLabel = "UNPAID")
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = { viewModel.showToast("Receipt exported: #HMS-00${bill.id} PDF document saved in Downloads folder.") },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("PDF Receipt", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// ==========================================
// PORTAL: RECEPTIONIST WORKSPACE
// ==========================================
@Composable
fun ReceptionistPortal(viewModel: HMSViewModel) {
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }

    // Registration State
    var pName by remember { mutableStateOf("") }
    var pAge by remember { mutableStateOf("") }
    var pGender by remember { mutableStateOf("Male") }
    var pBlood by remember { mutableStateOf("O+") }
    var pPhone by remember { mutableStateOf("") }
    var pAllergies by remember { mutableStateOf("") }
    var pHistory by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HMSSectionHeader(
                title = "Clinic Reception Desk",
                subtitle = "Register incoming patients, queue schedules, and query consult records."
            )
        }

        // Search Patients
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Global Patient Registry Search") },
                placeholder = { Text("Search by name, blood group, allergies...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Register Patient Form
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "New Patient Registration Form",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Register demographic data, critical drug allergies, and basic symptoms.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("John Doe") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = pAge,
                            onValueChange = { pAge = it },
                            label = { Text("Age") },
                            placeholder = { Text("30") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pBlood,
                            onValueChange = { pBlood = it },
                            label = { Text("Blood Group") },
                            placeholder = { Text("O+") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = pPhone,
                        onValueChange = { pPhone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+1 (555) 123-4567") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = pAllergies,
                        onValueChange = { pAllergies = it },
                        label = { Text("Known Allergies") },
                        placeholder = { Text("Penicillin, Peanuts (If none, leave blank)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = pHistory,
                        onValueChange = { pHistory = it },
                        label = { Text("Vitals / Presenting Complaints") },
                        placeholder = { Text("Hypertension, fever 101F, general checkup") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        minLines = 2
                    )

                    Button(
                        onClick = {
                            val ageInt = pAge.toIntOrNull()
                            if (pName.isBlank() || ageInt == null || pBlood.isBlank() || pPhone.isBlank()) {
                                viewModel.showToast("Validation Error: Please fill in all required demographic details.")
                            } else {
                                viewModel.registerPatient(
                                    name = pName,
                                    age = ageInt,
                                    gender = pGender,
                                    bloodType = pBlood,
                                    contact = pPhone,
                                    allergies = pAllergies,
                                    history = pHistory
                                )
                                // Clear Form
                                pName = ""
                                pAge = ""
                                pPhone = ""
                                pAllergies = ""
                                pHistory = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Patient File & Queue Record")
                    }
                }
            }
        }

        // Active Patient List
        item {
            Text(
                text = "Patient Registry Directory",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val filteredPatients = patients.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.bloodType.contains(searchQuery, ignoreCase = true) ||
                    it.allergies.contains(searchQuery, ignoreCase = true)
        }

        if (filteredPatients.isEmpty()) {
            item {
                EmptyListCard(message = "No patient records matched the search query.")
            }
        } else {
            items(filteredPatients) { patient ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = patient.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "Blood Type: ${patient.bloodType}",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(text = "Age: ${patient.age} | Phone: ${patient.contact}", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "Allergies: ${patient.allergies}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "History: ${patient.medicalHistory}",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// ==========================================
// PORTAL: PHARMACIST & LAB TECH
// ==========================================
@Composable
fun PharmacistLabPortal(viewModel: HMSViewModel) {
    val medicines by viewModel.medicines.collectAsStateWithLifecycle()
    val labTests by viewModel.labTests.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("Pharmacy") }

    // Lab result submit state
    var selectedTestToResult by remember { mutableStateOf<LabTest?>(null) }
    var labResultFindings by remember { mutableStateOf("") }
    var labTechnicianName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HMSSectionHeader(
                title = "Clinical Support Desk",
                subtitle = "Manage pharmaceutical drug inventories and process diagnostic biological laboratories."
            )
        }

        // Custom Pharmacy vs Lab Tab Switcher
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { activeTab = "Pharmacy" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "Pharmacy") MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (activeTab == "Pharmacy") Color.White else Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Pharm")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pharmacy Stock")
                }
                Button(
                    onClick = { activeTab = "Laboratory" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "Laboratory") MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (activeTab == "Laboratory") Color.White else Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.List, contentDescription = "Lab")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sample Lab Desk")
                }
            }
        }

        if (activeTab == "Pharmacy") {
            // Pharmacy inventory list
            item {
                Text(
                    text = "Apothecary Drug Inventory",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Check Stock Alert
            val lowStockCount = medicines.count { it.stock < it.minStockLevel }
            if (lowStockCount > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warn",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Critical Stock Alert",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "There are $lowStockCount medicines falling below minimum safe stock levels. Order refills immediately.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }

            items(medicines) { med ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = med.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "Brand: ${med.brand} | Generic: ${med.genericName}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text(
                                text = "$${"%.2f".format(med.price)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Stock: ", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    text = "${med.stock} units",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (med.stock < med.minStockLevel) MaterialTheme.colorScheme.secondary else Color.Black
                                )
                                if (med.stock < med.minStockLevel) {
                                    Text(text = " (Low!)", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                            Text(text = "Exp: ${med.expiryDate}", fontSize = 11.sp, color = Color.Gray)
                        }

                        // Simulation buttons to sell or refill stock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.updateMedicineStock(med, -10) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Text("Dispense 10", fontSize = 10.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.updateMedicineStock(med, 50) },
                                modifier = Modifier.weight(1f).height(32.dp)
                            ) {
                                Text("Refill 50", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Lab sample processing Desk
            item {
                Text(
                    text = "Pending Biological Lab Assays",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (labTests.isEmpty()) {
                item {
                    EmptyListCard(message = "No laboratory tests requested by consulting doctors.")
                }
            } else {
                items(labTests) { test ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = test.testName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Patient: ${test.patientName} | Date: ${test.requestedDate}", fontSize = 11.sp, color = Color.Gray)
                                }
                                BedStatusBadge(status = if (test.status == "Pending") "Available" else "Occupied", availableLabel = "PENDING", occupiedLabel = "COMPLETED")
                            }

                            if (test.status == "Pending") {
                                Button(
                                    onClick = {
                                        selectedTestToResult = test
                                        labResultFindings = ""
                                        labTechnicianName = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .height(32.dp)
                                ) {
                                    Text("Enter Diagnostic Results", fontSize = 11.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Result Findings: ${test.result}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                                Text(text = "Signed off by: ${test.technicianName}", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }

            // Input Result modal overlay (drawn in line)
            if (selectedTestToResult != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lab Result entry: ${selectedTestToResult?.testName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                IconButton(
                                    onClick = { selectedTestToResult = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                                }
                            }

                            OutlinedTextField(
                                value = labResultFindings,
                                onValueChange = { labResultFindings = it },
                                label = { Text("Diagnostic Findings") },
                                placeholder = { Text("e.g. Hemoglobin 14.2 g/dL (Normal)") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )

                            OutlinedTextField(
                                value = labTechnicianName,
                                onValueChange = { labTechnicianName = it },
                                label = { Text("Signing Technician / Board Pathologist Name") },
                                placeholder = { Text("Lab Tech Marcus") },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            )

                            Button(
                                onClick = {
                                    val test = selectedTestToResult
                                    if (test != null) {
                                        if (labResultFindings.isBlank() || labTechnicianName.isBlank()) {
                                            viewModel.showToast("Validation Error: findings and lab technician fields must be filled.")
                                        } else {
                                            viewModel.submitLabResult(test, labResultFindings, labTechnicianName)
                                            selectedTestToResult = null
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Complete Lab Assay Analysis")
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

// ==========================================
// ADAPTIVE & HELPER UI SUB-COMPONENTS
// ==========================================

@Composable
fun HMSRevenueCanvasChart() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
    ) {
        val width = size.width
        val height = size.height

        // Draw background grid lines
        val gridLinesCount = 4
        for (i in 0..gridLinesCount) {
            val y = (height / gridLinesCount) * i
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw dynamic billing revenue trend points (Last 7 days mock indicators)
        // Values: $1400, $1500, $1350, $1600, $1420, $1580, $1700
        val dataPoints = listOf(0.2f, 0.4f, 0.3f, 0.65f, 0.45f, 0.75f, 0.9f)
        val stepX = width / (dataPoints.size - 1)
        val path = Path()

        dataPoints.forEachIndexed { idx, value ->
            val cx = stepX * idx
            val cy = height - (value * (height * 0.8f))
            if (idx == 0) {
                path.moveTo(cx, cy)
            } else {
                path.lineTo(cx, cy)
            }
        }

        // Draw line graph
        drawPath(
            path = path,
            color = Color(0xFF0D47A1),
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw glowing gradient fill under the curve
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0D47A1).copy(alpha = 0.3f), Color.Transparent)
            )
        )

        // Draw point dots
        dataPoints.forEachIndexed { idx, value ->
            val cx = stepX * idx
            val cy = height - (value * (height * 0.8f))
            drawCircle(
                color = Color(0xFFD32F2F),
                radius = 4.dp.toPx(),
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
fun RoleSelectorDropdown(currentRole: String, onRoleSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Admin", "Doctor", "Patient", "Receptionist", "Pharmacist / Lab")

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp).testTag("role_dropdown_btn")
        ) {
            Icon(Icons.Default.Lock, contentDescription = "Lock", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(currentRole, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", modifier = Modifier.size(12.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    text = { Text(role, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                    onClick = {
                        onRoleSelected(role)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun RoleStickyFooter(currentRole: String, onRoleSelected: (String) -> Unit) {
    val roles = listOf(
        "Admin" to Icons.Default.Settings,
        "Doctor" to Icons.Default.Person,
        "Patient" to Icons.Default.AccountBox,
        "Receptionist" to Icons.Default.Home,
        "Pharmacist / Lab" to Icons.Default.ShoppingCart
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF072451)), // deep rich sidebar blue
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            roles.forEach { (label, icon) ->
                val active = currentRole == label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onRoleSelected(label) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (active) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label.split(" ")[0],
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
fun HMSSectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color.DarkGray,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun BedStatusBadge(status: String, availableLabel: String = "AVAILABLE", occupiedLabel: String = "OCCUPIED") {
    val green = Color(0xFF1B5E20)
    val red = Color(0xFFB71C1C)
    val bg = if (status.lowercase() == "available") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val color = if (status.lowercase() == "available") green else red

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (status.lowercase() == "available") availableLabel else occupiedLabel,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = color
        )
    }
}

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "Completed" -> Color(0xFF1B5E20)
        "Scheduled" -> Color(0xFF0D47A1)
        "Cancelled" -> Color(0xFFB71C1C)
        else -> Color.Gray
    }
    val bg = when (status) {
        "Completed" -> Color(0xFFE8F5E9)
        "Scheduled" -> Color(0xFFE3F2FD)
        "Cancelled" -> Color(0xFFFFEBEE)
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = color
        )
    }
}

@Composable
fun EmptyListCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                fontSize = 13.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AddDoctorDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clinician Registration", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("Dr. John Watson") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Department / Specialty") },
                    placeholder = { Text("Neurology") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Number") },
                    placeholder = { Text("+1 (555) 012-3456") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    label = { Text("Weekly Shift Schedule") },
                    placeholder = { Text("Mon-Wed 09:00 - 15:00") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Consulting Room Number") },
                    placeholder = { Text("Room 205") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && specialty.isNotBlank() && phone.isNotBlank() && schedule.isNotBlank() && room.isNotBlank()) {
                        onConfirm(name, specialty, phone, schedule, room)
                    }
                }
            ) {
                Text("Register")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun Modifier.shadowElevation(): Modifier = this.shadow(1.dp, RoundedCornerShape(0.dp))
