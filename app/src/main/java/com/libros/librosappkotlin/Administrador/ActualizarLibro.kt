package com.libros.librosappkotlin.Administrador

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.libros.librosappkotlin.R
import com.libros.librosappkotlin.databinding.ActivityActualizarLibroBinding

class ActualizarLibro : AppCompatActivity() {

    private var pdfUrl = ""

    private lateinit var binding : ActivityActualizarLibroBinding

    private var idLibro = ""

    private lateinit var progressDialog: ProgressDialog

    //Titulos
    private lateinit var catTituloArrayList : ArrayList<String>
    //Id
    private lateinit var catIdArrayList : ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityActualizarLibroBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        idLibro = intent.getStringExtra("idLibro")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.TvCategoriaLibro.setOnClickListener {
            dialogCategoria()
        }

        binding.BtnActualizarLibro.setOnClickListener {
            validarinformacion()
        }

        cargarCategorias()

        cargarInformacion()

    }

    private fun cargarInformacion() {
        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.child(idLibro)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val url = snapshot.child("url").value.toString()

                    //Obtener la información en tiempo real del libro seleccionado
                    val titulo = snapshot.child("titulo").value.toString()
                    val descripcion = snapshot.child("descripcion").value.toString()
                    id_seleccionado = snapshot.child("categoria").value.toString()

                    pdfUrl = url

                    //Seteamos en las vistas
                    binding.EtTituloLibro.setText(titulo)
                    binding.EtDescripcionLibro.setText(descripcion)

                    cargarPreviewDesdeUrl(pdfUrl)

                    val refCategoria = FirebaseDatabase.getInstance().getReference("Categorias")
                    refCategoria.child(id_seleccionado)
                        .addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //Obtener la categoria
                                val categoria = snapshot.child("categoria").value
                                //Seteamos en el TextView
                                binding.TvCategoriaLibro.text = categoria.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


    }
    private fun cargarPreviewDesdeUrl(pdfUrl: String) {
        try {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)

            val localFile = java.io.File.createTempFile("temp", ".pdf")

            storageRef.getFile(localFile)
                .addOnSuccessListener {
                    val fileDescriptor = android.os.ParcelFileDescriptor.open(localFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY)
                    val pdfRenderer = android.graphics.pdf.PdfRenderer(fileDescriptor)

                    val page = pdfRenderer.openPage(0)
                    val bitmap = android.graphics.Bitmap.createBitmap(page.width, page.height, android.graphics.Bitmap.Config.ARGB_8888)

                    page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    binding.previewPdfActualizar.setImageBitmap(bitmap)
                    binding.previewPdfActualizar.visibility = android.view.View.VISIBLE

                    page.close()
                    pdfRenderer.close()
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var titulo = ""
    private var descripcion = ""

    private fun validarinformacion() {
        //obtener los datos ingresados
        titulo = binding.EtTituloLibro.text.toString().trim()
        descripcion = binding.EtDescripcionLibro.text.toString().trim()

        if (titulo.isEmpty()){
            Toast.makeText(this, "Ingrese titulo", Toast.LENGTH_SHORT).show()
        }
        else if (descripcion.isEmpty()){
            Toast.makeText(this, "Ingrese descripción", Toast.LENGTH_SHORT).show()
        }else if (id_seleccionado.isEmpty()){
            Toast.makeText(this, "Seleccione una categoria", Toast.LENGTH_SHORT).show()
        }else{
            actualizarInformacion()
        }
    }

    private fun actualizarInformacion() {
        progressDialog.setMessage("Actualizando información")
        progressDialog.show()
        val hashMap = HashMap<String, Any>()
        hashMap["titulo"] = "$titulo"
        hashMap["descripcion"] = "$descripcion"
        hashMap["categoria"] = "$id_seleccionado"

        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.child(idLibro)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "La actualización fue exitosa", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "La actualizacion falló debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var id_seleccionado = ""
    private var titulo_seleccionado = ""

    private fun dialogCategoria() {
        val categoriaArray = arrayOfNulls<String>(catTituloArrayList.size)
        for (i in catTituloArrayList.indices){
            categoriaArray[i] = catTituloArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Seleccione una categoria")
            .setItems(categoriaArray){dialog, posicion->
                id_seleccionado = catIdArrayList[posicion]
                titulo_seleccionado = catTituloArrayList[posicion]

                binding.TvCategoriaLibro.text = titulo_seleccionado
            }
            .show()
    }

    private fun cargarCategorias() {
        catTituloArrayList = ArrayList()
        catIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categorias")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                catTituloArrayList.clear()
                catIdArrayList.clear()
                for (ds in snapshot.children){
                    val id = ""+ds.child("id").value
                    val categoria = ""+ds.child("categoria").value

                    catTituloArrayList.add(categoria)
                    catIdArrayList.add(id)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}




































