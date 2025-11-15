package com.example.lab_week_10

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.lab_week_10.viewmodels.TotalViewModel
import com.example.lab_week_10.database.Total
import com.example.lab_week_10.database.TotalDatabase

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
        return Room.databaseBuilder(
            applicationContext,
            TotalDatabase::class.java, "total-database"
        ).allowMainThreadQueries().build()
    }

    // Initialize the value of the total from the database
    // If the database is empty, insert a new Total object with the value of 0
    // If the database is not empty, get the value of the total from the database
    private fun initializeValueFromDatabase() {
        val totalList = db.totalDao().getTotal(ID)
        if (totalList.isEmpty()) {
            db.totalDao().insert(Total(id = ID, total = 0))
            viewModel.setTotal(0)
        } else {
            viewModel.setTotal(totalList.first().total)
        }
    }

    // Update the value of the total in the database whenever the activity is paused
    override fun onPause() {
        super.onPause()
        val current = viewModel.total.value ?: 0
        db.totalDao().update(Total(ID, current))
    }

    companion object {
        const val ID: Long = 1
    }
}