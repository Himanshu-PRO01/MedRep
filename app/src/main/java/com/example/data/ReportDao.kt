package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY date DESC")
    fun getAllReports(): Flow<List<SavedReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SavedReport)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReport(id: String)
    
    @Query("DELETE FROM reports")
    suspend fun deleteAll()
}
