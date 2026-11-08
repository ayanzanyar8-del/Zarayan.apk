package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.AppUser
import com.example.data.model.DhikrProgress
import com.example.data.model.FavoriteName
import com.example.data.model.JanazaAnnouncement
import com.example.data.model.QuranBookmark
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM app_users ORDER BY timestamp DESC")
    fun getAllUsers(): Flow<List<AppUser>>

    @Query("SELECT COUNT(*) FROM app_users")
    fun getUserCount(): Flow<Int>

    @Query("SELECT * FROM app_users WHERE id = :id")
    suspend fun getUserById(id: Long): AppUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUser): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<AppUser>)

    @Update
    suspend fun updateUser(user: AppUser)

    @Query("DELETE FROM app_users WHERE id = :id")
    suspend fun deleteUserById(id: Long)

    @Query("DELETE FROM app_users")
    suspend fun clearAllUsers()
}

@Dao
interface JanazaDao {
    @Query("SELECT * FROM janaza_announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<JanazaAnnouncement>>

    @Query("SELECT * FROM janaza_announcements ORDER BY timestamp DESC")
    suspend fun getAllAnnouncementsList(): List<JanazaAnnouncement>

    @Query("SELECT * FROM janaza_announcements ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAnnouncement(): Flow<JanazaAnnouncement?>

    @Query("SELECT * FROM janaza_announcements WHERE id = :id")
    suspend fun getAnnouncementById(id: Long): JanazaAnnouncement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: JanazaAnnouncement): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(announcements: List<JanazaAnnouncement>)

    @Update
    suspend fun updateAnnouncement(announcement: JanazaAnnouncement)

    @Query("UPDATE janaza_announcements SET likesCount = likesCount + 1, isLiked = 1 WHERE id = :id")
    suspend fun likeAnnouncement(id: Long)

    @Query("UPDATE janaza_announcements SET likesCount = MAX(0, likesCount - 1), isLiked = 0 WHERE id = :id")
    suspend fun unlikeAnnouncement(id: Long)

    @Query("DELETE FROM janaza_announcements WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface DhikrDao {
    @Query("SELECT * FROM dhikr_progress")
    fun getAllProgress(): Flow<List<DhikrProgress>>

    @Query("SELECT * FROM dhikr_progress WHERE dhikrId = :dhikrId")
    suspend fun getProgress(dhikrId: String): DhikrProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: DhikrProgress)

    @Query("UPDATE dhikr_progress SET currentCount = :count, isCompleted = :isCompleted WHERE dhikrId = :dhikrId")
    suspend fun updateCount(dhikrId: String, count: Int, isCompleted: Boolean)

    @Query("UPDATE dhikr_progress SET currentCount = 0, isCompleted = 0 WHERE dhikrId = :dhikrId")
    suspend fun resetProgress(dhikrId: String)
}

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorite_names")
    fun getFavoriteNames(): Flow<List<FavoriteName>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavoriteName(favorite: FavoriteName)

    @Query("DELETE FROM favorite_names WHERE nameNumber = :number")
    suspend fun removeFavoriteName(number: Int)

    @Query("SELECT * FROM quran_bookmarks ORDER BY timestamp DESC LIMIT 1")
    fun getLatestQuranBookmark(): Flow<QuranBookmark?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuranBookmark(bookmark: QuranBookmark)
}

@Database(
    entities = [
        AppUser::class,
        JanazaAnnouncement::class,
        DhikrProgress::class,
        FavoriteName::class,
        QuranBookmark::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun janazaDao(): JanazaDao
    abstract fun dhikrDao(): DhikrDao
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zarayen_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
