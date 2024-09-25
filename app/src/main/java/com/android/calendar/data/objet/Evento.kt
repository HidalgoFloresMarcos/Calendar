package com.android.calendar.data.objet

data class Evento(
    val _id: String,
    val idUsuario: String,
    val nombreEvento: String,
    val descripcionEvento: String,
    val fechaInicioEvento: String,
    val fechaFinEvento: String,
    val nombreDia: String,
    val esRecurrente: Boolean
)
