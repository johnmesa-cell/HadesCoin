package com.example.hadescoin.presentation.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.hadescoin.domain.model.AppNotification
import com.example.hadescoin.presentation.components.ShowLoadingAlertDialog
import com.example.hadescoin.presentation.components.HadesBackground
import com.example.hadescoin.presentation.utils.formatTimestamp
import com.example.hadescoin.ui.theme.HadesCyan
import com.example.hadescoin.ui.theme.HadesNavyDark
import com.example.hadescoin.ui.theme.HadesOnDark
import com.example.hadescoin.ui.theme.HadesOrange
import com.example.hadescoin.ui.theme.HadesPurple

@Composable
fun NotificationsView(
    phoneNumber: String,
    navController: NavController,
    viewModel: NotificationsViewModel = viewModel()
) {
    val cargando by viewModel.cargando.observeAsState(false)
    val notificaciones by viewModel.notificaciones.observeAsState(emptyList())
    val error by viewModel.error.observeAsState()
    val noLeidas by viewModel.noLeidas.observeAsState(0)

    var errorMostrado by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(phoneNumber) {
        viewModel.cargarNotificaciones(phoneNumber)
    }

    HadesBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = HadesCyan
                        )
                    }
                    Text(
                        text = "NOTIFICACIONES",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = HadesPurple,
                        letterSpacing = 2.sp
                    )
                }

                BadgedBox(
                    badge = {
                        if (noLeidas > 0) {
                            Badge { Text(noLeidas.toString()) }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = HadesCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (notificaciones.isEmpty() && !cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes notificaciones aún",
                        color = HadesOnDark.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(notificaciones) { notificacion ->
                        NotificationRow(
                            notification = notificacion,
                            onClick = {
                                if (!notificacion.read) {
                                    viewModel.marcarComoLeida(phoneNumber, notificacion.id)
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (cargando) ShowLoadingAlertDialog()
}

@Composable
private fun NotificationRow(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val statusColor = if (notification.read) HadesOnDark.copy(alpha = 0.35f) else HadesOrange

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HadesNavyDark)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column {
                Text(
                    text = notification.title,
                    color = HadesOnDark,
                    fontSize = 14.sp,
                    fontWeight = if (notification.read) FontWeight.SemiBold else FontWeight.Bold
                )
                Text(
                    text = notification.message,
                    color = HadesOnDark.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTimestamp(notification.createdAt),
                    color = HadesOnDark.copy(alpha = 0.45f),
                    fontSize = 11.sp
                )
            }
        }

        if (!notification.read) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(HadesOrange)
            )
        }
    }
}

