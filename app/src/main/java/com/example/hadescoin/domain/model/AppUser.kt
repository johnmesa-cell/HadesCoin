package com.example.hadescoin.domain.model

// domain/model/AppUser.kt
data class AppUser(
    val id:                 String  = "",
    val documentNumber:     String  = "",
    val phoneNumber:        String  = "",
    val fullName:           String  = "",
    val pin:                String  = "",
    val balance:            Double  = 0.0,
    val createdAt:          String  = "",
    val cedulaVerificada:   Boolean = false,
    val nickname:           String  = "",
    val email:              String  = "",
    // Campo temporal en Firebase: almacena el codigo de verificacion activo.
    // Se borra automaticamente al validarlo. Reutilizable para PIN y Retiro.
    val verificationCode:   String  = ""
)
