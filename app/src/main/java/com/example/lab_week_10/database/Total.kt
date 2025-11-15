package com.example.lab_week_10.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

// Create an Entity with a table name of "total"
@Entity(tableName = "total")
// Every variables declared inside this class counts
// as declaring a new column inside the table
data class Total(
// @PrimaryKey sets a column into a primary key
    @PrimaryKey(autoGenerate = true)
// @ColumnInfo sets the name of the column
    @ColumnInfo(name = "id")
// The variable name can be different from the column name
    val id: Long = 0,
// Here we set a second column to store the total object
// containing the value and the date
// @Embedded is used to set column as an object
    @Embedded val total: TotalObject
)

// Here, we declare the class that's embedded into the column
data class TotalObject(
    @ColumnInfo(name = "value") val value: Int,
    @ColumnInfo(name = "date") val date: String
)