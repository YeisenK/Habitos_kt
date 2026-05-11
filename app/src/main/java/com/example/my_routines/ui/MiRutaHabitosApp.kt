package com.example.my_routines.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.my_routines.data.HabitoDao
import com.example.my_routines.data.HabitoEntity
import kotlinx.coroutines.launch

@Composable
fun MiRutaHabitosApp(habitoDao: HabitoDao) {
    var nuevoHabito by remember { mutableStateOf("") }
    val listaHabitos by habitoDao.obtenerHabitos().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val completados = listaHabitos.count { it.completado }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Mi Ruta de Hábitos",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hábitos completados: $completados de ${listaHabitos.size}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = nuevoHabito,
                onValueChange = { nuevoHabito = it },
                label = { Text("Nuevo hábito") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val texto = nuevoHabito.trim()
                    val esDuplicado = listaHabitos.any { it.nombre.equals(texto, ignoreCase = true) }
                    if (texto.isNotBlank() && !esDuplicado) {
                        scope.launch {
                            habitoDao.insertarHabito(HabitoEntity(nombre = texto))
                        }
                        nuevoHabito = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar hábito")
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn {
                items(listaHabitos, key = { it.id }) { habito ->
                    HabitoCard(
                        habito = habito,
                        onCambioEstado = {
                            scope.launch {
                                habitoDao.actualizarHabito(
                                    habito.copy(completado = !habito.completado)
                                )
                            }
                        },
                        onEliminar = {
                            scope.launch {
                                habitoDao.eliminarHabito(habito)
                            }
                        }
                    )
                }
            }
        }
    }
}
