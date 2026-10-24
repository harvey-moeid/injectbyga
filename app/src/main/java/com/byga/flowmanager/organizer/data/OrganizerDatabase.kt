package com.byga.flowmanager.organizer.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "managed_groups")
data class GroupRow(@PrimaryKey val id: String, val nameCipher: String)

@Entity(tableName = "managed_accounts", foreignKeys = [ForeignKey(
    entity = GroupRow::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.SET_NULL
)], indices = [Index("groupId")])
data class AccountRow(
    @PrimaryKey val id: String, val metadataCipher: String, val groupId: String?, val favorite: Boolean,
    val color: Int, val createdAt: Long, val updatedAt: Long, val lastOpenedAt: Long? = null
)

@Entity(tableName = "managed_launches", foreignKeys = [ForeignKey(
    entity = AccountRow::class, parentColumns = ["id"], childColumns = ["accountId"], onDelete = ForeignKey.CASCADE
)], indices = [Index("accountId")])
data class LaunchRow(
    @PrimaryKey val id: String, val accountId: String, val targetCipher: String,
    val provider: String, val providerVersion: String, val startedAt: Long,
    val outcome: String, val observedCipher: String? = null, val reviewedAt: Long? = null, val errorCode: String? = null
)

@Dao
interface OrganizerDao {
    @Query("SELECT * FROM managed_accounts ORDER BY rowid") fun observeAccounts(): Flow<List<AccountRow>>
    @Query("SELECT * FROM managed_groups ORDER BY rowid") fun observeGroups(): Flow<List<GroupRow>>
    @Query("SELECT * FROM managed_launches ORDER BY rowid DESC") fun observeLaunches(): Flow<List<LaunchRow>>
    @Query("SELECT * FROM managed_accounts ORDER BY rowid") suspend fun accounts(): List<AccountRow>
    @Query("SELECT * FROM managed_groups ORDER BY rowid") suspend fun groups(): List<GroupRow>
    @Query("SELECT * FROM managed_launches ORDER BY rowid DESC LIMIT 1") suspend fun latest(): LaunchRow?
    @Upsert suspend fun saveAccount(row: AccountRow)
    @Upsert suspend fun saveGroup(row: GroupRow)
    @Insert suspend fun insertLaunch(row: LaunchRow)
    @Query("DELETE FROM managed_accounts WHERE id IN (:ids)") suspend fun deleteAccounts(ids: List<String>)
    @Query("UPDATE managed_accounts SET favorite = :favorite, updatedAt = :time WHERE id = :id") suspend fun favorite(id: String, favorite: Boolean, time: Long)
    @Query("UPDATE managed_accounts SET groupId = :groupId, updatedAt = :time WHERE id IN (:ids)") suspend fun move(ids: List<String>, groupId: String?, time: Long)
    @Query("DELETE FROM managed_groups WHERE id = :id") suspend fun deleteGroup(id: String)
    @Query("UPDATE managed_launches SET outcome = 'ABANDONED' WHERE outcome IN ('STARTING', 'UNVERIFIED')") suspend fun abandonPending()
    @Query("UPDATE managed_launches SET outcome = :outcome, errorCode = :code WHERE id = :id AND outcome = 'STARTING'") suspend fun finishLaunch(id: String, outcome: String, code: String?)
    @Query("UPDATE managed_accounts SET lastOpenedAt = :time WHERE id = :id") suspend fun opened(id: String, time: Long)
    @Query("UPDATE managed_launches SET outcome = :outcome, observedCipher = :observed, reviewedAt = :time WHERE id = :id AND outcome = 'UNVERIFIED'")
    suspend fun review(id: String, outcome: String, observed: String?, time: Long): Int
}

@Database(entities = [AccountRow::class, GroupRow::class, LaunchRow::class], version = 1, exportSchema = true)
abstract class OrganizerDatabase : RoomDatabase() { abstract fun dao(): OrganizerDao }
