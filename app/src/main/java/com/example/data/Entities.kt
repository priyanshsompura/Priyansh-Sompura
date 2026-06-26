package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val specialty: String,
    val contact: String,
    val schedule: String,
    val room: String,
    val availability: Boolean = true
)

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val age: Int,
    val gender: String,
    val bloodType: String,
    val contact: String,
    val allergies: String = "None",
    val medicalHistory: String = "No previous chronic issues"
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val doctorId: Long,
    val doctorName: String,
    val date: String,
    val time: String,
    val reason: String,
    val status: String = "Scheduled" // Scheduled, Completed, Cancelled
)

@Entity(tableName = "beds")
data class Bed(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bedNumber: String,
    val type: String, // General, ICU, Semi-Private, Emergency
    val status: String, // Available, Occupied
    val wardName: String
)

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String,
    val genericName: String,
    val stock: Int,
    val minStockLevel: Int,
    val price: Double,
    val expiryDate: String
)

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val totalAmount: Double,
    val discountAmount: Double,
    val taxAmount: Double,
    val status: String, // Paid, Unpaid, PendingInsurance
    val invoiceDate: String,
    val paymentMethod: String // Cash, Card, Insurance, UPI
)

@Entity(tableName = "lab_tests")
data class LabTest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: Long,
    val patientName: String,
    val testName: String,
    val category: String, // Blood Bank, Laboratories, Radiology
    val requestedDate: String,
    val result: String = "Pending Result",
    val status: String = "Pending", // Pending, Completed
    val technicianName: String = "Pending Assignment"
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val userRole: String,
    val actionDescription: String
)
