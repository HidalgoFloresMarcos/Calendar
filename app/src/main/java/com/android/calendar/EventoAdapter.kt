package com.android.calendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.calendar.data.objet.Evento
import com.android.calendar.R

class EventoAdapter(
    private var eventos: List<Evento>,
    private val onEliminarClick: (Evento) -> Unit  // Callback para manejar el clic en eliminar
) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    // Método para actualizar la lista de eventos
    fun actualizarEventos(nuevosEventos: List<Evento>) {
        this.eventos = nuevosEventos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_evento, parent, false)
        return EventoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]
        holder.nombreEvento.text = evento.nombreEvento
        holder.descripcionEvento.text = evento.descripcionEvento
        holder.fechaEvento.text = "${evento.fechaInicioEvento} - ${evento.fechaFinEvento}"

        // Configuración del ícono de eliminar
        holder.btnEliminar.setOnClickListener {
            onEliminarClick(evento)  // Se llama al callback cuando el usuario hace clic en el ícono
        }
    }

    override fun getItemCount(): Int = eventos.size

    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreEvento: TextView = view.findViewById(R.id.nombreEvento)
        val descripcionEvento: TextView = view.findViewById(R.id.descripcionEvento)
        val fechaEvento: TextView = view.findViewById(R.id.fechaEvento)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminarEvento)  // Botón de eliminar
    }
}
