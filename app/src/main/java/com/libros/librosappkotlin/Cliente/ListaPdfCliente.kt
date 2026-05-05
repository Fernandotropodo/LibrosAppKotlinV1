package com.libros.librosappkotlin.Cliente

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.libros.librosappkotlin.Modelos.Modelopdf
import com.libros.librosappkotlin.R
import com.libros.librosappkotlin.databinding.ActivityListaPdfClienteBinding

class ListaPdfCliente : AppCompatActivity() {

    private lateinit var binding : ActivityListaPdfClienteBinding

    private var idCategoria = ""
    private var tituloCategoria = ""

    private lateinit var pdfArrayList: ArrayList<Modelopdf>
    private lateinit var adapatadorPdfCliente: AdaptadorPdfCliente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityListaPdfClienteBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Obtener los datos del adaptador
        val intent = intent
        idCategoria = intent.getStringExtra("idCategoria")!!
        tituloCategoria = intent.getStringExtra("tituloCategoria")!!

        binding.TxtCategoriaLibro.text = tituloCategoria

        binding.IbRegresar.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        cargarLibros()

        binding.EtBuscarLibroCliente.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(libro: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    adapatadorPdfCliente.filter.filter(libro)
                }catch (e: Exception){

                }
            }
        })

    }

    private fun cargarLibros() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.orderByChild("categoria").equalTo(idCategoria)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        val modelo = ds.getValue(Modelopdf::class.java)
                        if (modelo != null){
                            pdfArrayList.add(modelo)
                        }
                    }
                    adapatadorPdfCliente = AdaptadorPdfCliente(this@ListaPdfCliente, pdfArrayList)
                    binding.RvLibrosCliente.adapter = adapatadorPdfCliente
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

    }
}

//GCUT 420





































