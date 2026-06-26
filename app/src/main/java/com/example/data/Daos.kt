package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HMSDao {
    // Doctors
    @Query("SELECT * FROM doctors ORDER BY id ASC")
    fun getAllDoctors(): Flow<List<Doctor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: Doctor)

    @Update
    suspend fun updateDoctor(doctor: Doctor)

    @Delete
    suspend fun deleteDoctor(doctor: Doctor)

    // Patients
    @Query("SELECT * FROM patients ORDER BY id DESC")
    fun getAllPatients(): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE id = :id LIMIT 1")
    suspend fun getPatientById(id: Long): Patient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient): Long

    @Update
    suspend fun updatePatient(patient: Patient)

    @Delete
    suspend fun deletePatient(patient: Patient)

    // Appointments
    @Query("SELECT * FROM appointments ORDER BY date DESC, time DESC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    // Beds
    @Query("SELECT * FROM beds ORDER BY id ASC")
    fun getAllBeds(): Flow<List<Bed>>

    @Update
    suspend fun updateBed(bed: Bed)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBed(bed: Bed)

    // Medicines
    @Query("SELECT * FROM medicines ORDER BY stock ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine)

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    // Bills
    @Query("SELECT * FROM bills ORDER BY id DESC")
    fun getAllBills(): Flow<List<Bill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    // Lab Tests
    @Query("SELECT * FROM lab_tests ORDER BY id DESC")
    fun getAllLabTests(): Flow<List<LabTest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabTest(labTest: LabTest)

    @Update
    suspend fun updateLabTest(labTest: LabTest)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)
}
