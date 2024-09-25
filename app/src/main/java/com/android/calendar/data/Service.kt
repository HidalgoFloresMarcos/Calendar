package com.android.calendar.data

import com.android.calendar.data.objet.Evento
import com.android.calendar.data.objet.EventoRequest
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// Interfaz del servicio Retrofit
interface ServiceEvent {

    // Solicitud GET para obtener los eventos de un día específico
    @GET("eventos")
    suspend fun listevent(
        @Query("idUsuario") idUsuario: String,
        @Query("nombreDia") nombreDia: String
    ): List<Evento>

    // Solicitud POST para enviar el evento capturado por voz
    @POST("eventos")
    suspend fun enviarEvento(
        @Body request: EventoRequest
    ): Response<Void>

    // Solicitud DELETE para eliminar un evento por su _id
    @DELETE("eventos")
    suspend fun eliminarEvento(
        @Query("_id") _id: String
    ): Response<Void>
}

object ServiceFactory {
    private const val BASE_URL = "https://0ctdas3z1d.execute-api.us-east-1.amazonaws.com/prod/"

    fun makeService(): ServiceEvent {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceEvent::class.java)
    }
}
