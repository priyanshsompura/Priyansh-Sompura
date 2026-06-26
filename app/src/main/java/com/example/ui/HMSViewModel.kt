package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HMSViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HMSRepository

    // Flow lists from Database
    val doctors: StateFlow<List<Doctor>>
    val patients: StateFlow<List<Patient>>
    val appointments: StateFlow<List<Appointment>>
    val beds: StateFlow<List<Bed>>
    val medicines: StateFlow<List<Medicine>>
    val bills: StateFlow<List<Bill>>
    val labTests: StateFlow<List<LabTest>>
    val auditLogs: StateFlow<List<AuditLog>>

    // Selected user role perspective
    private val _currentRole = MutableStateFlow("Admin")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // Filters and searches
    private val _patientQuery = MutableStateFlow("")
    val patientQuery: StateFlow<String> = _patientQuery.asStateFlow()

    private val _doctorQuery = MutableStateFlow("")
    val doctorQuery: StateFlow<String> = _doctorQuery.asStateFlow()

    private val _medicineQuery = MutableStateFlow("")
    val medicineQuery: StateFlow<String> = _medicineQuery.asStateFlow()

    // Toast/Alert notifications
    private val _notification = MutableSharedFlow<String>()
    val notification: SharedFlow<String> = _notification.asSharedFlow()

    // Selected items for detail modals or portal views
    private val _selectedPatientId = MutableStateFlow<Long?>(null)
    val selectedPatientId: StateFlow<Long?> = _selectedPatientId.asStateFlow()

    private val _selectedDoctorId = MutableStateFlow<Long?>(null)
    val selectedDoctorId: StateFlow<Long?> = _selectedDoctorId.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        val dao = db.hmsDao()
        repository = HMSRepository(dao)

        doctors = repository.allDoctors.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        patients = repository.allPatients.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        appointments = repository.allAppointments.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        beds = repository.allBeds.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        medicines = repository.allMedicines.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        bills = repository.allBills.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        labTests = repository.allLabTests.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        auditLogs = repository.allAuditLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        // Check and seed database if empty
        viewModelScope.launch {
            doctors.first { true } // wait for first load
            if (doctors.value.isEmpty()) {
                seedDemoData()
            }
        }
    }

    fun setRole(role: String) {
        _currentRole.value = role
        logActivity("Role Changed", "Switched view perspective to role: $role")
    }

    fun setPatientQuery(q: String) {
        _patientQuery.value = q
    }

    fun setDoctorQuery(q: String) {
        _doctorQuery.value = q
    }

    fun setMedicineQuery(q: String) {
        _medicineQuery.value = q
    }

    fun selectPatient(id: Long?) {
        _selectedPatientId.value = id
    }

    fun selectDoctor(id: Long?) {
        _selectedDoctorId.value = id
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _notification.emit(message)
        }
    }

    // ACTIONS: Patient Portal
    fun registerPatient(
        name: String, age: Int, gender: String, bloodType: String,
        contact: String, allergies: String, history: String
    ) {
        viewModelScope.launch {
            val p = Patient(
                name = name, age = age, gender = gender,
                bloodType = bloodType, contact = contact,
                allergies = allergies.ifBlank { "None" },
                medicalHistory = history.ifBlank { "No previous chronic conditions" }
            )
            val newId = repository.insertPatient(p)
            logActivity("Patient Registration", "Registered new patient: $name (ID: $newId)")
            showToast("Success: Registered patient $name!")
        }
    }

    // ACTIONS: Admin / Receptionist Doctor Management
    fun registerDoctor(name: String, specialty: String, contact: String, schedule: String, room: String) {
        viewModelScope.launch {
            val d = Doctor(name = name, specialty = specialty, contact = contact, schedule = schedule, room = room)
            repository.insertDoctor(d)
            logActivity("Doctor Management", "Added doctor: $name ($specialty)")
            showToast("Success: Doctor $name registered.")
        }
    }

    // ACTIONS: Appointment Booking
    fun bookAppointment(patientId: Long, patientName: String, doctorId: Long, doctorName: String, date: String, time: String, reason: String) {
        viewModelScope.launch {
            val appt = Appointment(
                patientId = patientId, patientName = patientName,
                doctorId = doctorId, doctorName = doctorName,
                date = date, time = time, reason = reason, status = "Scheduled"
            )
            repository.insertAppointment(appt)
            logActivity("Appointments", "Booked appointment for $patientName with $doctorName")
            showToast("Success: Appointment booked for $date at $time!")
        }
    }

    fun updateAppointmentStatus(appt: Appointment, newStatus: String) {
        viewModelScope.launch {
            val updated = appt.copy(status = newStatus)
            repository.updateAppointment(updated)
            logActivity("Appointments", "Updated appointment #${appt.id} status to $newStatus")
            showToast("Appointment status changed to $newStatus.")
        }
    }

    // ACTIONS: Lab Tests Management
    fun requestLabTest(patientId: Long, patientName: String, testName: String, category: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val test = LabTest(
                patientId = patientId, patientName = patientName,
                testName = testName, category = category,
                requestedDate = sdf.format(Date()), status = "Pending"
            )
            repository.insertLabTest(test)
            logActivity("Laboratories", "Requested $testName for patient $patientName")
            showToast("Success: Lab test $testName requested.")
        }
    }

    fun submitLabResult(test: LabTest, resultText: String, technician: String) {
        viewModelScope.launch {
            val updated = test.copy(
                result = resultText,
                status = "Completed",
                technicianName = technician
            )
            repository.updateLabTest(labTest = updated)
            logActivity("Laboratories", "Submitted lab results for test #${test.id}: $resultText")
            showToast("Lab test completed and results filed.")
        }
    }

    // ACTIONS: Pharmacy Stock Management
    fun addMedicine(name: String, brand: String, genericName: String, stock: Int, minStock: Int, price: Double, expiry: String) {
        viewModelScope.launch {
            val m = Medicine(
                name = name, brand = brand, genericName = genericName,
                stock = stock, minStockLevel = minStock, price = price, expiryDate = expiry
            )
            repository.insertMedicine(m)
            logActivity("Pharmacy", "Added medicine stock: $name ($stock units)")
            showToast("Medicine $name stock saved.")
        }
    }

    fun updateMedicineStock(med: Medicine, quantityChange: Int) {
        viewModelScope.launch {
            val newStock = (med.stock + quantityChange).coerceAtLeast(0)
            val updated = med.copy(stock = newStock)
            repository.updateMedicine(updated)
            logActivity("Pharmacy", "Updated stock for ${med.name}: ${med.stock} -> $newStock")
            showToast("Stock updated for ${med.name}.")
        }
    }

    // ACTIONS: Bed allocation
    fun toggleBedStatus(bed: Bed) {
        viewModelScope.launch {
            val newStatus = if (bed.status == "Available") "Occupied" else "Available"
            val updated = bed.copy(status = newStatus)
            repository.updateBed(updated)
            logActivity("Wards & Beds", "Bed ${bed.bedNumber} changed to $newStatus")
            showToast("Bed ${bed.bedNumber} is now $newStatus.")
        }
    }

    // ACTIONS: Billing and Invoice
    fun createInvoice(patientId: Long, patientName: String, total: Double, discount: Double, tax: Double, method: String, isPaid: Boolean) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val bill = Bill(
                patientId = patientId, patientName = patientName,
                totalAmount = total, discountAmount = discount, taxAmount = tax,
                status = if (isPaid) "Paid" else "Unpaid",
                invoiceDate = sdf.format(Date()), paymentMethod = method
            )
            repository.insertBill(bill)
            logActivity("Billing", "Generated Invoice for $patientName: Total $${"%.2f".format(total)}")
            showToast("Success: Invoice created for $patientName.")
        }
    }

    // Logging helper
    private fun logActivity(role: String, action: String) {
        viewModelScope.launch {
            repository.insertAuditLog(AuditLog(userRole = role, actionDescription = action))
        }
    }

    // Extensive Demo data seeder
    private suspend fun seedDemoData() {
        logActivity("System", "Initializing database seeding with premium HMS demo records...")

        // 1. Seed Doctors
        val d1 = Doctor(name = "Dr. Alexander Mercer", specialty = "Cardiology", contact = "+1 (555) 019-2831", schedule = "Mon-Fri 09:00 - 17:00", room = "Room 301")
        val d2 = Doctor(name = "Dr. Beatrice Vance", specialty = "Pediatrics", contact = "+1 (555) 019-4820", schedule = "Mon-Thu 08:00 - 15:00", room = "Room 102")
        val d3 = Doctor(name = "Dr. Charles Xavier", specialty = "Neurology", contact = "+1 (555) 019-9031", schedule = "Wed-Fri 10:00 - 18:00", room = "Room 405")
        val d4 = Doctor(name = "Dr. Diana Prince", specialty = "Orthopedics", contact = "+1 (555) 019-7462", schedule = "Tue-Fri 09:00 - 16:00", room = "Room 204")
        val d5 = Doctor(name = "Dr. Edward Elric", specialty = "General Surgery", contact = "+1 (555) 019-3829", schedule = "Mon-Wed 11:00 - 19:00", room = "Room 501 (OT)")

        repository.insertDoctor(d1)
        repository.insertDoctor(d2)
        repository.insertDoctor(d3)
        repository.insertDoctor(d4)
        repository.insertDoctor(d5)

        // 2. Seed Patients
        val p1 = Patient(name = "John Smith", age = 45, gender = "Male", bloodType = "A+", contact = "+1 (555) 123-4567", allergies = "Peanut Allergy", medicalHistory = "Hypertension since 2021. Managed on Atorvastatin.")
        val p2 = Patient(name = "Jane Doe", age = 32, gender = "Female", bloodType = "O-", contact = "+1 (555) 987-6543", allergies = "Penicillin", medicalHistory = "Mild childhood asthma. No active treatment.")
        val p3 = Patient(name = "Bruce Wayne", age = 40, gender = "Male", bloodType = "AB+", contact = "+1 (555) 444-1939", allergies = "None", medicalHistory = "Multiple severe trauma injuries healed, fractured ribs recovery.")
        val p4 = Patient(name = "Peter Parker", age = 21, gender = "Male", bloodType = "B+", contact = "+1 (555) 321-7654", allergies = "None", medicalHistory = "Anemia diagnostics, general health screen.")

        val p1Id = repository.insertPatient(p1)
        val p2Id = repository.insertPatient(p2)
        val p3Id = repository.insertPatient(p3)
        val p4Id = repository.insertPatient(p4)

        // 3. Seed Beds
        repository.insertBed(Bed(bedNumber = "Bed A-101", type = "General", status = "Available", wardName = "General Male Ward"))
        repository.insertBed(Bed(bedNumber = "Bed A-102", type = "General", status = "Occupied", wardName = "General Male Ward"))
        repository.insertBed(Bed(bedNumber = "Bed B-201", type = "Semi-Private", status = "Available", wardName = "Semi-Private Ward B"))
        repository.insertBed(Bed(bedNumber = "Bed B-202", type = "Semi-Private", status = "Occupied", wardName = "Semi-Private Ward B"))
        repository.insertBed(Bed(bedNumber = "ICU-301", type = "ICU", status = "Occupied", wardName = "Intensive Care Unit 1"))
        repository.insertBed(Bed(bedNumber = "ICU-302", type = "ICU", status = "Available", wardName = "Intensive Care Unit 1"))
        repository.insertBed(Bed(bedNumber = "ER-01", type = "Emergency", status = "Available", wardName = "Emergency Room"))

        // 4. Seed Medicines
        repository.insertMedicine(Medicine(name = "Paracetamol 500mg", brand = "GlaxoSmithKline", genericName = "Acetaminophen", stock = 650, minStockLevel = 100, price = 4.25, expiryDate = "2028-11-30"))
        repository.insertMedicine(Medicine(name = "Amoxicillin 250mg", brand = "Sandoz Biotech", genericName = "Amoxicillin (Antibiotic)", stock = 42, minStockLevel = 75, price = 14.50, expiryDate = "2027-04-15"))
        repository.insertMedicine(Medicine(name = "Insulin Humalog 100U", brand = "Eli Lilly", genericName = "Insulin Lispro Injection", stock = 85, minStockLevel = 25, price = 48.00, expiryDate = "2026-12-10"))
        repository.insertMedicine(Medicine(name = "Atorvastatin 20mg", brand = "Pfizer Pharmaceuticals", genericName = "Atorvastatin (Lipitor)", stock = 320, minStockLevel = 50, price = 9.99, expiryDate = "2028-02-28"))
        repository.insertMedicine(Medicine(name = "Ibuprofen 400mg", brand = "Advil Professional", genericName = "Ibuprofen (NSAID)", stock = 500, minStockLevel = 100, price = 6.00, expiryDate = "2027-09-18"))

        // 5. Seed Appointments
        repository.insertAppointment(Appointment(patientId = p1Id, patientName = "John Smith", doctorId = 1, doctorName = "Dr. Alexander Mercer", date = "2026-06-27", time = "10:30 AM", reason = "Routine cardiology follow-up on hypertension medication.", status = "Scheduled"))
        repository.insertAppointment(Appointment(patientId = p2Id, patientName = "Jane Doe", doctorId = 2, doctorName = "Dr. Beatrice Vance", date = "2026-06-28", time = "09:00 AM", reason = "Pediatric health query and development screen.", status = "Scheduled"))
        repository.insertAppointment(Appointment(patientId = p3Id, patientName = "Bruce Wayne", doctorId = 4, doctorName = "Dr. Diana Prince", date = "2026-06-26", time = "02:00 PM", reason = "Orthopedic physical check on bone healing progress.", status = "Completed"))
        repository.insertAppointment(Appointment(patientId = p4Id, patientName = "Peter Parker", doctorId = 3, doctorName = "Dr. Charles Xavier", date = "2026-06-29", time = "11:15 AM", reason = "Headaches, migraine testing.", status = "Scheduled"))

        // 6. Seed Bills/Invoices
        repository.insertBill(Bill(patientId = p3Id, patientName = "Bruce Wayne", totalAmount = 1420.00, discountAmount = 100.00, taxAmount = 70.00, status = "Paid", invoiceDate = "2026-06-26", paymentMethod = "Credit Card"))
        repository.insertBill(Bill(patientId = p1Id, patientName = "John Smith", totalAmount = 185.00, discountAmount = 0.00, taxAmount = 9.25, status = "Unpaid", invoiceDate = "2026-06-25", paymentMethod = "Insurance"))
        repository.insertBill(Bill(patientId = p2Id, patientName = "Jane Doe", totalAmount = 75.00, discountAmount = 10.00, taxAmount = 3.25, status = "Paid", invoiceDate = "2026-06-24", paymentMethod = "Cash"))

        // 7. Seed Laboratory Tests
        repository.insertLabTest(LabTest(patientId = p1Id, patientName = "John Smith", testName = "Lipid Profile Panel", category = "Laboratories", requestedDate = "2026-06-25", result = "Total Cholesterols: 198 mg/dL (Borderline High), Triglycerides: 155 mg/dL.", status = "Completed", technicianName = "Lab Tech Marcus"))
        repository.insertLabTest(LabTest(patientId = p3Id, patientName = "Bruce Wayne", testName = "Chest & Ribs X-Ray Scan", category = "Radiology", requestedDate = "2026-06-26", result = "Rib fractures 4 and 5 are showing excellent calcification and bridging. No fluid in pleural cavities.", status = "Completed", technicianName = "Dr. Diana Prince"))
        repository.insertLabTest(LabTest(patientId = p4Id, patientName = "Peter Parker", testName = "Full CBC Blood Count", category = "Blood Bank", requestedDate = "2026-06-26", status = "Pending"))

        logActivity("System", "Database seeded successfully. 5 Doctors, 4 Patients, 7 Beds, 5 Medicines, 4 Appointments, 3 Invoices, and 3 Lab Tests initialized.")
    }
}
