package com.example.my_routines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.my_routines.data.HabitoDatabase
import com.example.my_routines.ui.MiRutaHabitosApp
import com.example.my_routines.ui.theme.My_routinesTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: HabitoDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = HabitoDatabase.obtenerBaseDatos(this)

        setContent {
            My_routinesTheme {
                MiRutaHabitosApp(database.habitoDao())
            }
        }
    }
}
