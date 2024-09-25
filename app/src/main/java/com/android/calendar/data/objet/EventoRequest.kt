package com.android.calendar.data.objet

data class EventoRequest(
    val idUsuario: String,
    val evento: String,
    val fechaPeticionEvento: String,
    val nombreDia: String
)
