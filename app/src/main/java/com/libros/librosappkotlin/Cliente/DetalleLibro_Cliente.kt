package com.libros.librosappkotlin.Cliente

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.libros.librosappkotlin.Administrador.Constantes
import com.libros.librosappkotlin.Administrador.MisFunciones
import com.libros.librosappkotlin.LeerLibro
import com.libros.librosappkotlin.Modelos.ModeloComentario
import com.libros.librosappkotlin.R
import com.libros.librosappkotlin.databinding.ActivityDetalleLibroBinding
import com.libros.librosappkotlin.databinding.ActivityDetalleLibroClienteBinding
import com.libros.librosappkotlin.databinding.DialogAgregarComentarioBinding
import java.io.FileOutputStream

class DetalleLibro_Cliente : AppCompatActivity() {

    private lateinit var binding : ActivityDetalleLibroClienteBinding
    private var idLibro = ""

    private var tituloLibro = ""
    private var urlLibro = ""

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var esFavorito = false

    private lateinit var comentarioArrayList : ArrayList<ModeloComentario>
    private lateinit var adaptadorComentario: AdaptadorComentario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetalleLibroClienteBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idLibro = intent.getStringExtra("idLibro")!!

        MisFunciones.incrementarVistas(idLibro)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.BtnLeerLibroC.setOnClickListener{
            val intent = Intent(this@DetalleLibro_Cliente, LeerLibro::class.java)
            intent.putExtra("idLibro", idLibro)
            startActivity(intent)
        }

        binding.BtnDescargarLibroC.setOnClickListener {

            if (android.os.Build.VERSION.SDK_INT <= 28) {

                if (
                    ContextCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    desacargarLibro()

                } else {

                    permisoAlmacenamiento.launch(
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )

                }

            } else {

                desacargarLibro()

            }
        }

        binding.BtnFavoritosLibroC.setOnClickListener {
            if (esFavorito){
                MisFunciones.eliminarFavoritos(this@DetalleLibro_Cliente, idLibro)
            }else{
                agregarFavoritos()
            }
        }

        binding.IbAgregarComentario.setOnClickListener {
            dialogComentar()
        }

        comprobarFavoritos()
        cargarDetalleLibro()
        listarComentarios()

    }

    private fun listarComentarios() {
        comentarioArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.child(idLibro).child("Comentarios")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    comentarioArrayList.clear()
                    for (ds in snapshot.children){
                        val modelo = ds.getValue(ModeloComentario::class.java)
                        comentarioArrayList.add(modelo!!)
                    }
                    adaptadorComentario = AdaptadorComentario(this@DetalleLibro_Cliente, comentarioArrayList)
                    binding.RvComentarios.adapter = adaptadorComentario
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private var comentario = ""
    private fun dialogComentar() {
        val agregar_com_binding = DialogAgregarComentarioBinding.inflate(LayoutInflater.from(this))

        val builder = AlertDialog.Builder(this)
        builder.setView(agregar_com_binding.root)

        val alertDialog = builder.create()
        alertDialog.show()
        alertDialog.setCanceledOnTouchOutside(false)

        agregar_com_binding.IbCerrar.setOnClickListener {
            alertDialog.dismiss()
        }

        agregar_com_binding.BtnComentar.setOnClickListener {
            comentario = agregar_com_binding.EtAgregarComentario.text.toString().trim()
            if (comentario.isEmpty()){
                Toast.makeText(applicationContext, "Agrege un comentario", Toast.LENGTH_SHORT).show()
            }else{
                alertDialog.dismiss()
                agregarComentario()
            }
        }
    }

    private fun agregarComentario() {
        progressDialog.setMessage("Agregando Comentario")
        progressDialog.show()

        val tiempo = "${System.currentTimeMillis()}"

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$tiempo"
        hashMap["idLibro"] = "${idLibro}"
        hashMap["tiempo"] = "$tiempo"
        hashMap["comentario"] = "${comentario}"
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.child(idLibro).child("Comentarios").child(tiempo)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Su comentario se ha publicado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun comprobarFavoritos() {
        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idLibro)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    esFavorito = snapshot.exists()
                    if (esFavorito){
                        binding.BtnFavoritosLibroC.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_agregar_favorito,
                            0,
                            0
                        )
                        binding.BtnFavoritosLibroC.text = "Eliminar de favoritos"
                    }else{
                        binding.BtnFavoritosLibroC.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            0,
                            R.drawable.ic_no_favorito,
                            0,
                            0
                        )
                        binding.BtnFavoritosLibroC.text = "Agregar a favoritos"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun agregarFavoritos(){
        val tiempo = System.currentTimeMillis()

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = idLibro
        hashMap["tiempo"] = tiempo

        val ref = FirebaseDatabase.getInstance().getReference("Usuarios")
        ref.child(firebaseAuth.uid!!).child("Favoritos").child(idLibro)
            .setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Libro añadido a favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(applicationContext, "No se agregó a favoritos debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun desacargarLibro() {
        progressDialog.setMessage("Descargando libro")
        progressDialog.show()

        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(urlLibro)
        storageReference.getBytes(Constantes.Maximo_bytes_pdf)
            .addOnSuccessListener {bytes->

                guardarLibroDisp(bytes)

            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun guardarLibroDisp(bytes: ByteArray) {

        val nombreLibro_extension = "$tituloLibro"+System.currentTimeMillis()+".pdf"
        try {
            val carpeta = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            carpeta.mkdirs()

            val archivo_ruta = carpeta.path+"/"+nombreLibro_extension
            val out = FileOutputStream(archivo_ruta)
            out.write(bytes)
            out.close()

            Toast.makeText(applicationContext, "Libro guardado con éxito", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()

            incrementarNumDes()

        }catch (e:Exception){
            Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }

    }

    private fun cargarDetalleLibro() {
        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.child(idLibro)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //Obtener la información del libro a través del id
                    val categoria = "${snapshot.child("categoria").value}"
                    val contadorDescargas = "${snapshot.child("contadorDescargas").value}"
                    val contadorVistas = "${snapshot.child("contadorVistas").value}"
                    val descripcion = "${snapshot.child("descripcion").value}"
                    val tiempo = "${snapshot.child("tiempo").value}"
                    tituloLibro = "${snapshot.child("titulo").value}"
                    urlLibro = "${snapshot.child("url").value}"

                    //Cambiar el formato del tiempo
                    val fecha = MisFunciones.formatoTiempo(tiempo.toLong())
                    //Cargar categoría del libro
                    MisFunciones.CargarCategoria(categoria, binding.categoriaD)
                    //Cargar la miniatura del libro, contador de paginas
                    MisFunciones.CargarPdfUrl(
                        "$urlLibro",
                        "$tituloLibro",
                        binding.VisualizadorPDF,
                        binding.progressBar,
                        binding.paginasD
                    )

                    //Cargar tamaño
                    MisFunciones.CargarTamanioPdf("$urlLibro", "$tituloLibro", binding.tamanioD)

                    //Seteamos informacíon restante
                    binding.tituloLibroD.text = tituloLibro
                    binding.descripcionD.text = descripcion
                    binding.vistasD.text = contadorVistas
                    binding.descargasD.text = contadorDescargas
                    binding.fechaD.text = fecha
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun incrementarNumDes(){
        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.child(idLibro)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var contDescarActual = "${snapshot.child("contadorDescargas").value}"

                    if (contDescarActual == "" || contDescarActual == "null"){
                        contDescarActual = "0"
                    }

                    val nuevaDes = contDescarActual.toLong() + 1

                    val hashMap = HashMap<String, Any>()
                    hashMap["contadorDescargas"] = nuevaDes

                    val BDRef = FirebaseDatabase.getInstance().getReference("Libros")
                    BDRef.child(idLibro)
                        .updateChildren(hashMap)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private val permisoAlmacenamiento = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){permiso_concedido->
        if (permiso_concedido){
            desacargarLibro()
        }else{
            Toast.makeText(applicationContext, "El permiso de almacenamiento a sido denegado", Toast.LENGTH_SHORT).show()
        }
    }

}





































