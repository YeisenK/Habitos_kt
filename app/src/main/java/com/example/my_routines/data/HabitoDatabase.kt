package com.example.my_routines.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HabitoEntity::class],
    version = 1
)
abstract class HabitoDatabase : RoomDatabase() {

    abstract fun habitoDao(): HabitoDao

    companion object {
        @Volatile
        private var INSTANCE: HabitoDatabase? = null

        fun obtenerBaseDatos(context: Context): HabitoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    HabitoDatabase::class.java,
                    "habitos_db"
                ).build()
                INSTANCE = instancia
                instancia
            }
        }
    }
}
