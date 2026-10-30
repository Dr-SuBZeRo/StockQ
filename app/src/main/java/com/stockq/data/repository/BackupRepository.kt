package com.stockq.data.repository

import android.content.Context
import com.stockq.core.database.StockQDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class BackupRepository(
    private val context: Context,
    private val database: StockQDatabase
) {
    companion object {
        private const val SQLITE_HEADER = "SQLite format 3"
    }

    /**
     * Flushes WAL pages and exports the database file to the provided OutputStream (SAF URI).
     */
    suspend fun exportDatabase(outputStream: OutputStream): Result<Long> = withContext(Dispatchers.IO) {
        try {
            // Checkpoint WAL into main database file
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { cursor ->
                cursor.moveToFirst()
            }

            val dbFile = context.getDatabasePath(StockQDatabase.DATABASE_NAME)
            if (!dbFile.exists()) {
                return@withContext Result.failure(IllegalStateException("Database file does not exist"))
            }

            var bytesCopied = 0L
            FileInputStream(dbFile).use { input ->
                outputStream.use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesCopied += read
                    }
                    output.flush()
                }
            }

            Result.success(bytesCopied)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validates and restores the database from an InputStream (SAF URI).
     */
    suspend fun restoreDatabase(inputStream: InputStream): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(StockQDatabase.DATABASE_NAME)
            val tempRestoreFile = File(context.cacheDir, "restore_temp.db")

            // Copy to temp file while validating SQLite header
            var isValidSQLite = false
            FileOutputStream(tempRestoreFile).use { output ->
                inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    var isFirstBlock = true
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        if (isFirstBlock) {
                            val header = String(buffer.take(15).toByteArray(), Charsets.US_ASCII)
                            if (header == SQLITE_HEADER) {
                                isValidSQLite = true
                            }
                            isFirstBlock = false
                        }
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }

            if (!isValidSQLite) {
                tempRestoreFile.delete()
                return@withContext Result.failure(IllegalArgumentException("Selected file is not a valid StockQ SQLite backup"))
            }

            // Close database connection
            database.close()

            // Delete wal and shm files
            val walFile = File(dbFile.parentFile, "${StockQDatabase.DATABASE_NAME}-wal")
            val shmFile = File(dbFile.parentFile, "${StockQDatabase.DATABASE_NAME}-shm")
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            // Replace main database file
            if (dbFile.exists()) {
                dbFile.delete()
            }
            tempRestoreFile.copyTo(dbFile, overwrite = true)
            tempRestoreFile.delete()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

