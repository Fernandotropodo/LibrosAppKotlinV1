package com.libros.librosappkotlin.Cliente

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.libros.librosappkotlin.Administrador.MisFunciones
import com.libros.librosappkotlin.Modelos.Modelopdf
import com.libros.librosappkotlin.databinding.ItemLibroFavoritoBinding

class AdaptadorPdfFavorito : RecyclerView.Adapter<AdaptadorPdfFavorito.HolderPdfFavorito>{

    private val context : Context
    private var arraylistLibros : ArrayList<Modelopdf>
    private lateinit var binding: ItemLibroFavoritoBinding

    constructor(context: Context, arraylistLibros: ArrayList<Modelopdf>) {
        this.context = context
        this.arraylistLibros = arraylistLibros
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorito {
        binding = ItemLibroFavoritoBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfFavorito(binding.root)
    }

    override fun getItemCount(): Int {
        return arraylistLibros.size
    }

    override fun onBindViewHolder(holder: HolderPdfFavorito, position: Int) {
        val modelo = arraylistLibros[position]
        cargarDetalleLibro(modelo, holder)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetalleLibro_Cliente::class.java)
            intent.putExtra("idLibro", modelo.id)
            context.startActivity(intent)
        }

        holder.ib_eliminar_fav.setOnClickListener {
            MisFunciones.eliminarFavoritos(context, modelo.id)
        }
    }

    private fun cargarDetalleLibro(modelo: Modelopdf, holder: AdaptadorPdfFavorito.HolderPdfFavorito) {
        val idLibro = modelo.id

        val ref = FirebaseDatabase.getInstance().getReference("Libros")
            ref.child(idLibro)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoria = "${snapshot.child("categoria").value}"
                    val descripcion = "${snapshot.child("descripcion").value}"
                    val conDescargas = "${snapshot.child("contadorDescargas").value}"
                    val tiempo = "${snapshot.child("tiempo").value}"
                    val titulo = "${snapshot.child("titulo").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val conVistas = "${snapshot.child("contadorVistas").value}"

                    modelo.esFavorito = true
                    modelo.titulo = titulo
                    modelo.descripcion = descripcion
                    modelo.categoria = categoria
                    modelo.titulo = tiempo
                    modelo.uid = uid
                    modelo.url = url
                    modelo.contadorVistas = conVistas.toLong()
                    modelo.contadorDescargas = conDescargas.toLong()

                    val fecha = MisFunciones.formatoTiempo(tiempo.toLong())

                    MisFunciones.CargarCategoria("$categoria", holder.categoria)
                    MisFunciones.CargarPdfUrl("$url", "$titulo", holder.visualizadorPdf, holder.progressBar, null)
                    MisFunciones.CargarTamanioPdf("$url", "$titulo", holder.tamanio)

                    holder.titulo.text = titulo
                    holder.descripcion.text = descripcion
                    holder.fecha.text = fecha
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    inner class HolderPdfFavorito(itemView : View) : RecyclerView.ViewHolder(itemView){
        var visualizadorPdf = binding.VisualizadorPDF
        var progressBar = binding.progressBar
        var titulo = binding.TxtTituloLibroItem
        var descripcion = binding.TxtDescripcionLibroItem
        var categoria = binding.TxtCategoriaLibroAdmi
        var tamanio = binding.TxtTamanioLibroAdmin
        var fecha = binding.TxtFechaLibroAdmin
        var ib_eliminar_fav = binding.IbEliminarFav
    }

}




































