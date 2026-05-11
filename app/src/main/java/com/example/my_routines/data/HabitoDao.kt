package com.example.my_routines.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitoDao {

    @Query("SELECT * FROM habitos ORDER BY id")
    fun obtenerHabitos(): Flow<List<HabitoEntity>>

    @Insert
    suspend fun insertarHabito(habito: HabitoEntity)

    @Update
    suspend fun actualizarHabito(habito: HabitoEntity)

    @Delete
    suspend fun eliminarHabito(habito: HabitoEntity)
}
