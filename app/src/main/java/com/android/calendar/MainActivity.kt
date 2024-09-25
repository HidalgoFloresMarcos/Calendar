package com.android.calendar

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.calendar.data.ServiceFactory
import com.android.calendar.data.objet.Evento
import com.android.calendar.data.objet.EventoRequest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity(), View.OnClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventoAdapter
    private lateinit var btn_lunes: Button
    private lateinit var btn_martes: Button
    private lateinit var btn_miercoles: Button
    private lateinit var btn_jueves: Button
    private lateinit var btn_viernes: Button
    private lateinit var btn_sabado: Button
    private lateinit var btn_domingo: Button
    private lateinit var microphoneButton: Button
    private lateinit var tvTituloEventos: TextView

    // Guardamos una referencia al último botón presionado
    private var botonSeleccionado: Button? = null

    // Inicializar SpeechRecognizer para el botón de micrófono
    private val speechToTextLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val spokenText =
                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                spokenText?.let {
                    // Enviar el texto capturado a la API
                    enviarEvento(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Inicializar el TextView para el título de los eventos
        tvTituloEventos = findViewById(R.id.tvTituloEventos)

        // Inicializar los botones de los días
        btn_lunes = findViewById(R.id.lunes)
        btn_martes = findViewById(R.id.martes)
        btn_miercoles = findViewById(R.id.miercoles)
        btn_jueves = findViewById(R.id.jueves)
        btn_viernes = findViewById(R.id.viernes)
        btn_sabado = findViewById(R.id.sabado)
        btn_domingo = findViewById(R.id.domingo)

        // Inicializar el RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventoAdapter(listOf()) { evento ->
            mostrarConfirmacionEliminar(evento)
        }
        recyclerView.adapter = adapter
        // Inicializar el botón de micrófono
        microphoneButton = findViewById(R.id.microphone_button)

        // Asignar listeners a los botones de los días
        btn_lunes.setOnClickListener(this)
        btn_martes.setOnClickListener(this)
        btn_miercoles.setOnClickListener(this)
        btn_jueves.setOnClickListener(this)
        btn_viernes.setOnClickListener(this)
        btn_sabado.setOnClickListener(this)
        btn_domingo.setOnClickListener(this)

        // Acción al presionar el botón de micrófono
        microphoneButton.setOnClickListener {
            iniciarReconocimientoDeVoz()
        }
    }

    override fun onClick(v: View?) {
        val nombreDia: String = when (v?.id) {
            R.id.lunes -> "lunes"
            R.id.martes -> "martes"
            R.id.miercoles -> "miércoles"
            R.id.jueves -> "jueves"
            R.id.viernes -> "viernes"
            R.id.sabado -> "sábado"
            R.id.domingo -> "domingo"
            else -> ""
        }

        // Actualizar el título con el nombre del día seleccionado
        tvTituloEventos.text = "Eventos del Día $nombreDia"

        // Cambiar el color del botón seleccionado
        cambiarColorBotonSeleccionado(v as Button)

        // Llamar al método para obtener los eventos del día seleccionado
        obtenerEventos("12345", nombreDia)
    }

    // Método para obtener eventos del día seleccionado
    private fun obtenerEventos(idUsuario: String, nombreDia: String) {
        // Aquí tu lógica para obtener los eventos del día seleccionado
        lifecycleScope.launch {
            try {
                val service = ServiceFactory.makeService()
                val eventos = service.listevent(idUsuario, nombreDia)

                // Verificar si la lista de eventos es nula o está vacía
                if (eventos != null && eventos.isNotEmpty()) {
                    // Si hay eventos, actualizamos el adaptador con los datos
                    adapter.actualizarEventos(eventos)
                } else {
                    // Si la lista está vacía o es nula, mostramos un mensaje al usuario
                    Toast.makeText(this@MainActivity, "No hay eventos para $nombreDia", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                // En caso de error, lo mostramos
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error al cargar eventos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para cambiar el color del botón seleccionado
    private fun cambiarColorBotonSeleccionado(boton: Button) {
        botonSeleccionado?.let {
            // Restauramos el color original del último botón seleccionado
            it.backgroundTintList = ContextCompat.getColorStateList(this, R.color.primary_color)
        }

        // Cambiamos el color del botón actual
        boton.backgroundTintList = ContextCompat.getColorStateList(this, R.color.colorAccent)

        // Guardamos una referencia al botón seleccionado
        botonSeleccionado = boton
    }

    // Método para iniciar el reconocimiento de voz
    private fun iniciarReconocimientoDeVoz() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        // Configurar el idioma a español
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-ES")  // Español de España
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "es-ES")
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "es-ES")

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")  // Mensaje al usuario
        speechToTextLauncher.launch(intent)
    }

    // Método para enviar el evento capturado por voz
    private fun enviarEvento(eventoTexto: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val fechaActual = sdf.format(Date())

        val nombreDia = SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date())

        lifecycleScope.launch {
            try {
                val service = ServiceFactory.makeService()

                val eventoRequest = EventoRequest(
                    idUsuario = "12345",
                    evento = eventoTexto,
                    fechaPeticionEvento = fechaActual,
                    nombreDia = nombreDia.lowercase(Locale.getDefault())
                )

                val response = service.enviarEvento(eventoRequest)

                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Evento enviado con éxito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Error al enviar evento", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Mostrar un diálogo para confirmar la eliminación del evento
    private fun mostrarConfirmacionEliminar(evento: Evento) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Evento")
            .setMessage("¿Estás seguro de que deseas eliminar este evento?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarEvento(evento)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Método para eliminar el evento llamando a la API
    private fun eliminarEvento(evento: Evento) {
        // Validar que el evento tenga un ID
        if (evento._id.isNullOrEmpty()) {
            Toast.makeText(this, "ID del evento no válido", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val service = ServiceFactory.makeService()
                val response = service.eliminarEvento(evento._id)

                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Evento eliminado con éxito", Toast.LENGTH_SHORT).show()
                    // Actualizar la lista de eventos después de la eliminación
                    obtenerEventos("12345", SimpleDateFormat("EEEE", Locale("es", "ES")).format(Date()))
                } else {
                    Toast.makeText(this@MainActivity, "Error al eliminar evento", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
