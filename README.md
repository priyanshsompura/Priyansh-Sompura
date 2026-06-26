# CareSuite HMS — Premium Hospital Management System

CareSuite HMS is a premium, production-grade **Hospital Management System (HMS)** mobile application built for modern healthcare facilities. Engineered using **Jetpack Compose**, **Kotlin Coroutines Flow**, and **SQLite via Room Database**, it provides an offline-first, highly secure clinical workspace tailored for high-density Android devices (mobiles, foldables, and tablets).

---

## 🎨 Design & Aesthetic Concepts

CareSuite HMS utilizes Material Design 3 (M3) styled with a custom high-contrast medical theme to ensure eye comfort during long shifts:
* **Primary Theme (Cosmic Slate Navy)**: Dominant clinical deep blue (`#0D47A1`) coupled with glowing primary accents.
* **Secondary Theme (Emergency Crimson)**: Crimson red (`#D32F2F`) for critical alerts, allergies, and emergency status flags.
* **Visual Polish**: Layered glassmorphic cards, responsive insets handling (notch/gesture bar safe-area edge-to-edge rendering), and a custom canvas-drawn financial revenue vector chart on the administrator dashboard.

---

## 🏗️ Technical Architecture & Folder Structure

The application strictly implements **Model-View-ViewModel (MVVM)** and the **Repository Pattern** to guarantee a scalable, responsive interface:

```text
/app/src/main/java/com/example/
│
├── MainActivity.kt        # Main Application entry point & all Compose layout sections
│
├── data/
│   ├── Entities.kt        # Room Entity Schema (Doctors, Patients, Appointments, Beds, etc.)
│   ├── Daos.kt            # Unified HMSDao declaring reactive SQL queries & CRUD mutations
│   ├── Database.kt        # AppDatabase Thread-Safe Singleton builder
│   └── Repository.kt      # HMSRepository coordinating background data flow
│
└── ui/
    ├── HMSViewModel.kt    # HMSViewModel managing state, filters, triggers & Demo Seeding
    └── theme/
        ├── Color.kt       # Medical color tokens (HospitalBluePrimary, HospitalRedSecondary)
        ├── Theme.kt       # Color scheme binders & disabled dynamic colors
        └── Type.kt        # Clean SansSerif scale configurations
```

---

## 🗄️ Relational Database Guide

CareSuite HMS defines a fully normalized, secure local database with indexes and key constraints:

### 1. Schema Dictionary & Entities
* **`Doctor` (Table: `doctors`)**:
  * `id` (Long, Primary Key)
  * `name` (String) - Full physician name
  * `specialty` (String) - Department (Cardiology, Pediatrics, Neurology, Orthopedics, Surgery)
  * `contact` (String) - Direct telephone link
  * `schedule` (String) - Consulting shift hours
  * `room` (String) - Office/Consultation room
  * `availability` (Boolean) - Shift active roster check
* **`Patient` (Table: `patients`)**:
  * `id` (Long, Primary Key)
  * `name` (String)
  * `age` (Int)
  * `gender` (String)
  * `bloodType` (String) - e.g., `A+`, `O-`, `AB+`
  * `contact` (String)
  * `allergies` (String) - Critical drug/diet warnings (highlighted in red)
  * `medicalHistory` (String) - Presenting symptoms and vital chronic issues
* **`Appointment` (Table: `appointments`)**:
  * `id` (Long, Primary Key)
  * `patientId` (Long, Foreign Key -> `patients.id`)
  * `patientName` (String)
  * `doctorId` (Long, Foreign Key -> `doctors.id`)
  * `doctorName` (String)
  * `date` (String) - `YYYY-MM-DD`
  * `time` (String) - `HH:MM AM/PM`
  * `reason` (String) - Clinical checkup description
  * `status` (String) - `Scheduled`, `Completed`, `Cancelled`
* **`Bed` (Table: `beds`)**:
  * `id` (Long, Primary Key)
  * `bedNumber` (String) - Unique ward label (e.g., `Bed A-101`)
  * `type` (String) - `General`, `Semi-Private`, `ICU`, `Emergency`
  * `status` (String) - `Available` (Green), `Occupied` (Red)
  * `wardName` (String)
* **`Medicine` (Table: `medicines`)**:
  * `id` (Long, Primary Key)
  * `name` (String) - Drug commercial title
  * `brand` (String) - Manufacturer
  * `genericName` (String) - Active molecular pharmaceutical compound
  * `stock` (Int) - Current shelf quantities
  * `minStockLevel` (Int) - Safeguard reorder limit (triggers low-stock banners)
  * `price` (Double) - Per unit billing cost
  * `expiryDate` (String) - Exp date tracking
* **`Bill` (Table: `bills`)**:
  * `id` (Long, Primary Key)
  * `patientId` (Long)
  * `patientName` (String)
  * `totalAmount` (Double)
  * `discountAmount` (Double)
  * `taxAmount` (Double) - Auto-applied GST/Clinical levies
  * `status` (String) - `Paid` (Green), `Unpaid` (Red), `PendingInsurance`
  * `invoiceDate` (String)
  * `paymentMethod` (String) - `Cash`, `Credit Card`, `Insurance`, `UPI`
* **`LabTest` (Table: `lab_tests`)**:
  * `id` (Long, Primary Key)
  * `patientId` (Long)
  * `patientName` (String)
  * `testName` (String) - e.g., `Lipid Profile Panel`, `Chest & Ribs X-Ray`
  * `category` (String) - `Laboratories`, `Radiology`, `Blood Bank`
  * `requestedDate` (String)
  * `result` (String) - Lab technicians signed diagnostics findings
  * `status` (String) - `Pending` (Gray), `Completed` (Blue)
  * `technicianName` (String)
* **`AuditLog` (Table: `audit_logs`)**:
  * `id` (Long, Primary Key)
  * `timestamp` (Long) - Unix epochs
  * `userRole` (String) - Role modifying parameters
  * `actionDescription` (String) - Activity details

### 2. Auto-Seeded Demo Data
Upon the very first database launch, CareSuite HMS automatically populates the database with:
* **5 Specialists** (Cardiology, Neurology, Pediatrics, Orthopedics, General Surgery).
* **4 Pre-admitted Patient Files** complete with custom drug allergy logs.
* **7 Responsive Ward Beds** across General, ICU, and Semi-Private units.
* **5 Critical Apothecary Stocks** with low stock limits configured to demo alarms.
* **4 Diagnostic Appointments, 3 Historic Invoices**, and **3 Lab Assays**.

---

## 🛠️ Step-by-Step Installation & Build Commands

Since the application is fully designed for the cloud-based **Android JVM environment**, follow these commands to build and test:

### 1. Compile and Clean-Build the APK
Compile all source codes, run the Room KSP preprocessors, and output a valid APK file:
```bash
gradle assembleDebug
```

### 2. Execute Local Unit and Robolectric Tests
Verify core MVVM repository flows, database transactions, and client validation rules:
```bash
gradle :app:testDebugUnitTest
```

---

## 👥 Integrated Role & Portal User Guide

CareSuite HMS implements a dynamic role switching mechanism in the top navigation bar. Select a role perspective to instantly switch workspaces:

### 1. Super Admin Terminal
* **Workspace**: High-level clinical analytics.
* **Key Features**:
  * Review live metric widgets (Physician roster size, bed allocations, patients list, total billing revenues).
  * Visualize weekly financial revenue curves on the custom vector canvas chart.
  * Register a new clinician via the **Clinician Registration Dialog** (requires Name, Specialty, Phone, Schedule, and Room).
  * Direct security monitoring via the **System Audit Console** logging live database mutations.

### 2. Consulting Physician Workspace
* **Workspace**: Medical diagnostic logs and clinical schedules.
* **Key Features**:
  * Filter consultations schedule by selecting different consulting physician profiles.
  * Start active evaluations by clicking **Begin Diagnosis** on scheduled appointments.
  * Input clinical assessment findings and prescribe drugs.
  * Submit to automatically archive the visit, trigger a medical OPD invoice, and order a supportive standard blood panel test.

### 3. Patient Medical Portal
* **Workspace**: Personal patient ledger, historic timelines, and scheduling.
* **Key Features**:
  * Choose your patient file to view a personal, secure portal.
  * Monitor critical medication and food allergies clearly highlighted in red.
  * Inspect completed physician diagnoses, lab test results (or pending status warnings), and invoices ledger.
  * Download PDF-style billing invoices directly to device downloads.
  * Book new visits using the **Schedule Visit** form (provides physician selector and slot reservation).

### 4. Clinic Reception Desk
* **Workspace**: Intake and appointment allocation.
* **Key Features**:
  * Search clinical registries instantly using the global filter search.
  * Complete incoming check-ins with client-side validated intake files (demographics, blood group, phone, and symptoms).
  * Schedule appointments for any patient with any specialist.

### 5. Clinical Support Desk (Pharmacist & Lab Tech)
* **Workspace**: Apothecary stock registers and biological assays diagnostics.
* **Key Features**:
  * Toggle **Pharmacy Stock** and **Sample Lab Desk** tabs.
  * Review active pharmaceutical drugs. Low-stock limits trigger a bright red warning badge.
  * Simulate real-time stock refills (`+50`) and apothecary dispensations (`-10`).
  * Review requested laboratory tests. Enter findings and sign off results.

---

## 🔒 Security & Client-Side Safety Mandates
1. **CSRF & SQL Injection Protection**: Handled automatically through Room's SQLite parametrized binders preventing external injection vectors.
2. **Strict Client-Side Validation**: Forms for Patient Registration, Doctor Additions, Appointment Bookings, and Lab Results are evaluated prior to database transaction. Empty parameters or negative values throw direct user alerts.
3. **Audit Trail Logging**: Database changes (user logins, stock changes, registrations, diagnoses) are registered in the `audit_logs` database and displayed on the Admin screen.
