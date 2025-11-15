package com.example.lab_week_10.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// Create an Entity with a table name of "total"
// You can add ForeignKeys as a second parameter to set the foreign keys
// You can also add PrimaryKeys as a second parameter to set the primary
// keys
@Entity(tableName = "total")
// Every variable declared inside this class becomes a column in the table
data class Total(
// @PrimaryKey sets a column into a primary key
    @PrimaryKey(autoGenerate = true)
// @ColumnInfo sets the name of the column
    @ColumnInfo(name = "id")
// The variable name can be different from the column name
    val id: Long = 0,
// Here we set another column to store the total value
    @ColumnInfo(name = "total")
    val total: Int = 0
)