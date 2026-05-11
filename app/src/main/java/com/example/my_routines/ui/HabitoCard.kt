package com.example.my_routines.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.my_routines.data.HabitoEntity

@Composable
fun HabitoCard(
    habito: HabitoEntity,
    onCambioEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habito.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (habito.completado) "Completado" else "Pendiente",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Checkbox(
                checked = habito.completado,
                onCheckedChange = { onCambioEstado() }
            )

            TextButton(onClick = onEliminar) {
                Text("Eliminar")
            }
        }
    }
}
