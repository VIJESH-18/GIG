package com.gigverse.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.gigverse.app.data.model.BidEntity
import com.gigverse.app.data.model.DisputeEntity
import com.gigverse.app.data.model.GigPackageEntity
import com.gigverse.app.data.model.MessageEntity
import com.gigverse.app.data.model.MilestoneSubmissionEntity
import com.gigverse.app.data.model.ProjectEntity
import com.gigverse.app.data.model.ProposalEntity
import com.gigverse.app.data.model.ReviewEntity
import com.gigverse.app.data.model.TaskEntity
import com.gigverse.app.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        ProjectEntity::class,
        ProposalEntity::class,
        BidEntity::class,
        MessageEntity::class,
        ReviewEntity::class,
        GigPackageEntity::class,
        TaskEntity::class,
        DisputeEntity::class,
        MilestoneSubmissionEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private fun applyMigrationToV4(db: SupportSQLiteDatabase) {
            // Safely resolve any pre-existing duplicates by suffixing the secondary account's email with user ID
            db.execSQL(
                """
                UPDATE users 
                SET email = email || '_dup_' || id 
                WHERE id NOT IN (
                    SELECT MIN(id) FROM users GROUP BY LOWER(TRIM(email))
                )
                """.trimIndent()
            )
            // Create unique index
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                applyMigrationToV4(db)
            }
        }

        val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                applyMigrationToV4(db)
            }
        }

        val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                applyMigrationToV4(db)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gigverse_v2_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_1_4, MIGRATION_2_4)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
