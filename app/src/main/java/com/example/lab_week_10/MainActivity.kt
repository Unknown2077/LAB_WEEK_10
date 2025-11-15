package com.example.lab_week_10

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.lab_week_10.viewmodels.TotalViewModel
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase
import com.example.lab_week_10.database.TotalObject
import java.util.Date

class MainActivity : AppCompatActivity() {
    private val viewModel by lazy {
        ViewModelProvider(this)[TotalViewModel::class.java]
    }

    // Create an instance of the TotalDatabase when first accessed
    private val db by lazy { prepareDatabase() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize the value of the total from the database
        initializeValueFromDatabase()
        prepareViewModel()
    }

    override fun onStart() {
        super.onStart()
        // When the activity starts, show the last saved date if present
        val totalList = db.totalDao().getTotal(ID)
        if (totalList.isNotEmpty()) {
            val date = totalList.first().total.date
            Toast.makeText(this, "Last updated: $date", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateText(total: Int) {
        findViewById<TextView>(R.id.text_total).text =
            getString(R.string.text_total, total)
    }

    private fun prepareViewModel(){
        // Observe the LiveData object
        viewModel.total.observe(this) {
            // Whenever the value of the LiveData object changes
            // the updateText() is called, with the new value as the parameter
            updateText(it)
        }
        findViewById<Button>(R.id.button_increment).setOnClickListener {
            viewModel.incrementTotal()
        }
    }

    // Create and build the TotalDatabase with the name 'total-database'
    // allowMainThreadQueries() is used to allow queries to be run on the main
    // thread. This is not recommended for production, but used here for
    // simplicity in the lab.
    private fun prepareDatabase(): TotalDatabase {
        // Migration to move schema from version 1 -> 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // We need to create a new table that matches the Room entity exactly
                // (date must be NOT NULL). Then copy data from the old table into
                // the new one, drop the old table, and rename the new table.
                // This approach avoids problems where ALTER TABLE ... ADD COLUMN
                // cannot add NOT NULL constraints and Room's schema validation
                // would fail.
                val now = Date().toString()

                // 1) Create the new table with the correct schema (date NOT NULL)
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `total_new` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `value` INTEGER NOT NULL DEFAULT 0,
                      `date` TEXT NOT NULL
                    )
                """.trimIndent())

                // 2) Copy existing data from the old table into the new table.
                // The old schema had columns (id, total). Map `total` -> `value`
                // and set `date` to the current date for existing rows.
                try {
                    database.execSQL("INSERT INTO `total_new` (id, value, date) SELECT id, COALESCE(`total`, 0), '$now' FROM `total`")
                } catch (e: Exception) {
                    // If copying fails (table may already be migrated or schema
                    // is different), ignore and proceed with an empty/new table.
                }

                // 3) Drop the old table
                try {
                    database.execSQL("DROP TABLE IF EXISTS `total`")
                } catch (e: Exception) {
                    // ignore
                }

                // 4) Rename the new table to the original name
                database.execSQL("ALTER TABLE `total_new` RENAME TO `total`")
            }
        }

        // Migration to move schema from version 2 -> 3 (defensive):
        // Some devices may already have a table with columns (id, value, date)
        // but with `date` nullable or with an extra `total` column. This
        // migration ensures the final table matches Room's expectations.
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val now = Date().toString()
                // If the table already has `date` column and it's NOT NULL, nothing to do.
                // We'll try creating a new normalized table and copy data defensively.
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `total_new` (
                      `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                      `value` INTEGER NOT NULL DEFAULT 0,
                      `date` TEXT NOT NULL
                    )
                """.trimIndent())

                // Copy data: prefer existing `value` column if present, else map `total`.
                try {
                    // Try copy assuming `value` exists
                    database.execSQL("INSERT INTO `total_new` (id, value, date) SELECT id, COALESCE(`value`, 0), COALESCE(`date`, '$now') FROM `total`")
                } catch (e: Exception) {
                    try {
                        // Fallback: map old `total` column -> value
                        database.execSQL("INSERT INTO `total_new` (id, value, date) SELECT id, COALESCE(`total`, 0), '$now' FROM `total`")
                    } catch (e2: Exception) {
                        // ignore — leave table empty
                    }
                }

                try { database.execSQL("DROP TABLE IF EXISTS `total`") } catch (_: Exception) {}
                database.execSQL("ALTER TABLE `total_new` RENAME TO `total`")
            }
        }

        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java, "total-database"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .allowMainThreadQueries().build()
    }

    // Initialize the value of the total from the database
    // If the database is empty, insert a new Total object with the value of 0
    // If the database is not empty, get the value of the total from the database
    private fun initializeValueFromDatabase() {
        val totalList = db.totalDao().getTotal(ID)
        if (totalList.isEmpty()) {
            val now = Date().toString()
            db.totalDao().insert(Total(id = ID, total = TotalObject(value = 0, date = now)))
            viewModel.setTotal(0)
        } else {
            viewModel.setTotal(totalList.first().total.value)
        }
    }

    // Update the value of the total in the database whenever the activity is paused
    override fun onPause() {
        super.onPause()
        val current = viewModel.total.value ?: 0
        val now = Date().toString()
        db.totalDao().update(Total(ID, TotalObject(value = current, date = now)))
    }

    companion object {
        const val ID: Long = 1
    }
}