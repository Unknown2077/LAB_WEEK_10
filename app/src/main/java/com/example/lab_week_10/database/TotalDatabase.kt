package com.example.lab_week_10.database

import androidx.room.Database
import androidx.room.RoomDatabase

// Create a database with the @Database annotation
// It has 2 parameters:
// entities: You can define which entities the database relies on.
// It can rely on multiple entities
// version: Used to define schema version when there's a change to the
// schema.
// Update the version when you try to change the schema
@Database(entities = [Total::class], version = 3)
abstract class TotalDatabase : RoomDatabase() {
    // Declare the Dao
    abstract fun totalDao(): TotalDao
    // You can declare another Dao here for other Entities
}