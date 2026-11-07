package com.example.mdeps_application.gro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RiskDao {

    @Query("SELECT * FROM risks ORDER BY id ASC")
    fun observeAllRisks(): Flow<List<RiskEntity>>

    @Query("SELECT * FROM risks WHERE project_id = :projectId ORDER BY id ASC")
    fun observeRisksByProjectId(projectId: Long): Flow<List<RiskEntity>>

    @Query("SELECT * FROM risks WHERE id = :riskId LIMIT 1")
    fun observeRiskById(riskId: Long): Flow<RiskEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRisk(risk: RiskEntity): Long

    @Update
    suspend fun updateRisk(risk: RiskEntity)

    @Delete
    suspend fun deleteRisk(risk: RiskEntity)

    @Query("DELETE FROM risks WHERE id = :riskId")
    suspend fun deleteRiskById(riskId: Long)
}
