package com.example.my_routines.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habitos")
data class HabitoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val completado: Boolean = false
)
