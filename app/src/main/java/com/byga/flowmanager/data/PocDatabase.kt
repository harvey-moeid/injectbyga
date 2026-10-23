package com.byga.flowmanager.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "slots")
data class SlotEntity(@PrimaryKey val id: String, val emailCipher: String, val method: String, val provider: String)

@Entity(tableName = "trials")
data class TrialEntity(
    @PrimaryKey val id: String, val slot: String, val expectedCipher: String,
    val method: String, val provider: String, val providerVersion: String,
    val profile: String, val scenario: String, val run: String,
    val startedAt: Long, val outcome: String, val reviewedAt: Long? = null,
    val observedCipher: String? = null, val errorCode: String? = null
)

@Dao
interface PocDao {
    @Query("SELECT * FROM slots ORDER BY id") fun observeSlots(): Flow<List<SlotEntity>>
    @Query("SELECT * FROM slots ORDER BY id") suspend fun slots(): List<SlotEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveSlot(slot: SlotEntity)
    @Query("SELECT * FROM trials ORDER BY rowid DESC") fun observeTrials(): Flow<List<TrialEntity>>
    @Query("SELECT * FROM trials ORDER BY rowid DESC") suspend fun trials(): List<TrialEntity>
    @Query("SELECT * FROM trials ORDER BY rowid DESC LIMIT 1") suspend fun latest(): TrialEntity?
    @Insert suspend fun insertTrial(trial: TrialEntity)
    @Query("UPDATE trials SET outcome = :outcome, errorCode = :code WHERE id = :id AND outcome = 'STARTING'")
    suspend fun finishLaunch(id: String, outcome: String, code: String?)
    @Query("UPDATE trials SET outcome = 'ABANDONED' WHERE outcome IN ('UNVERIFIED', 'STARTING')") suspend fun abandonPending()
    @Query("UPDATE trials SET outcome = :outcome, observedCipher = :observed, reviewedAt = :time WHERE id = :id AND outcome = 'UNVERIFIED'")
    suspend fun review(id: String, outcome: String, observed: String?, time: Long): Int
    @Query("DELETE FROM trials") suspend fun deleteTrials()
    @Query("DELETE FROM slots") suspend fun deleteSlots()
}

@Database(entities = [SlotEntity::class, TrialEntity::class], version = 1, exportSchema = true)
abstract class PocDatabase : RoomDatabase() { abstract fun dao(): PocDao }
