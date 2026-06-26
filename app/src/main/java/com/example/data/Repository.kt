package com.example.data

import kotlinx.coroutines.flow.Flow

class HMSRepository(private val dao: HMSDao) {
    val allDoctors: Flow<List<Doctor>> = dao.getAllDoctors()
    val allPatients: Flow<List<Patient>> = dao.getAllPatients()
    val allAppointments: Flow<List<Appointment>> = dao.getAllAppointments()
    val allBeds: Flow<List<Bed>> = dao.getAllBeds()
    val allMedicines: Flow<List<Medicine>> = dao.getAllMedicines()
    val allBills: Flow<List<Bill>> = dao.getAllBills()
    val allLabTests: Flow<List<LabTest>> = dao.getAllLabTests()
    val allAuditLogs: Flow<List<AuditLog>> = dao.getAllAuditLogs()

    suspend fun getPatientById(id: Long) = dao.getPatientById(id)

    suspend fun insertDoctor(doctor: Doctor) = dao.insertDoctor(doctor)
    suspend fun updateDoctor(doctor: Doctor) = dao.updateDoctor(doctor)
    suspend fun deleteDoctor(doctor: Doctor) = dao.deleteDoctor(doctor)

    suspend fun insertPatient(patient: Patient): Long = dao.insertPatient(patient)
    suspend fun updatePatient(patient: Patient) = dao.updatePatient(patient)
    suspend fun deletePatient(patient: Patient) = dao.deletePatient(patient)

    suspend fun insertAppointment(appointment: Appointment) = dao.insertAppointment(appointment)
    suspend fun updateAppointment(appointment: Appointment) = dao.updateAppointment(appointment)
    suspend fun deleteAppointment(appointment: Appointment) = dao.deleteAppointment(appointment)

    suspend fun insertBed(bed: Bed) = dao.insertBed(bed)
    suspend fun updateBed(bed: Bed) = dao.updateBed(bed)

    suspend fun insertMedicine(medicine: Medicine) = dao.insertMedicine(medicine)
    suspend fun updateMedicine(medicine: Medicine) = dao.updateMedicine(medicine)

    suspend fun insertBill(bill: Bill) = dao.insertBill(bill)

    suspend fun insertLabTest(labTest: LabTest) = dao.insertLabTest(labTest)
    suspend fun updateLabTest(labTest: LabTest) = dao.updateLabTest(labTest)

    suspend fun insertAuditLog(log: AuditLog) = dao.insertAuditLog(log)
}
