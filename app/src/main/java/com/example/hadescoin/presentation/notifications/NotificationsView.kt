package com.example.hadescoin.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.R
import com.example.hadescoin.domain.model.AppNotification
import com.example.hadescoin.presentation.components.HadesScreen
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.utils.formatTimestamp
import com.example.hadescoin.ui.theme.*

@Composable
fun NotificationsView(
    phoneNumber: String,
    navController: NavController,
    viewModel: NotificationsViewModel = viewModel()
) {
    val cargando       by viewModel.cargando.observeAsState(false)
    val notificaciones by viewModel.notificaciones.observeAsState(emptyList())
    val noLeidas       by viewModel.noLeidas.observeAsState(0)

    LaunchedEffect(phoneNumber) { viewModel.cargarNotificaciones(phoneNumber) }

    // Limpiar estado al salir de la pantalla para evitar diálogos "flotando"
    DisposableEffect(Unit) {
        onDispose {
            viewModel.limpiarEstado()
        }
    }

    HadesScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Fila superior: atrás + título + badge mejorado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                            tint = HadesCyan
                        )
                    }
                    Text(
                        text = stringResource(R.string.notifications_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = HadesPurple,
                        letterSpacing = 2.sp
                    )
                }
                
                // Badge de no leídas más prominente
                if (noLeidas > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(HadesOrange.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.notifications_unread_count, noLeidas),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = HadesOrange
                        )
                    }
                }
            }

            // Botón "Marcar todas como leídas"
            if (noLeidas > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { viewModel.marcarTodasComoLeidas(phoneNumber) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = stringResource(R.string.notifications_mark_all_read),
                        color = HadesCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (notificaciones.isEmpty() && !cargando) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.notifications_empty), color = HadesOnDark.copy(alpha = 0.6f), fontSize = 14.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(notificaciones) { notificacion ->
                        NotificationRow(
                            notification = notificacion,
                            onClick = { if (!notificacion.read) viewModel.marcarComoLeida(phoneNumber, notificacion.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }

        // Corrección: Diálogo de carga DENTRO de HadesScreen para evitar leaks de contexto
        if (cargando) ShowLoadingAlertDialog()
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, onClick: () -> Unit) {
    val statusColor = if (notification.read) HadesOnDark.copy(alpha = 0.35f) else HadesOrange
    
    val bgColor = if (notification.read)
        HadesNavyDark
    else
        Color(0xFF0E1A5A) // HadesNavyDark + tinte cyan sutil

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .then(
                if (!notification.read)
                    Modifier.border(
                        width = 1.dp,
                        color = HadesCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp)
                    )
                else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(statusColor.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(imageVector = Icons.Filled.Notifications, contentDescription = null, tint = statusColor, modifier = Modifier.size(17.dp))
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(text = notification.title,   color = HadesOnDark, fontSize = 14.sp, fontWeight = if (notification.read) FontWeight.SemiBold else FontWeight.Bold)
                Text(text = notification.message, color = HadesOnDark.copy(alpha = 0.7f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = formatTimestamp(notification.createdAt), color = HadesOnDark.copy(alpha = 0.45f), fontSize = 11.sp)
            }
        }
        if (!notification.read) Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(HadesOrange))
    }
}
