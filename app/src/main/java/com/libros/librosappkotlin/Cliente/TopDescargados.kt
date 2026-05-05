package com.libros.librosappkotlin.Cliente

import android.os.Bundle
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
import com.libros.librosappkotlin.databinding.ActivityTopDescargadosBinding

class TopDescargados : AppCompatActivity() {

    private lateinit var binding : ActivityTopDescargadosBinding
    private lateinit var pdfArrayList: ArrayList<Modelopdf>
    private lateinit var adaptadorPdfCliente: AdaptadorPdfCliente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTopDescargadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        topDescargados()

    }
    private fun topDescargados() {
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Libros")
        ref.orderByChild("contadorDescargados").limitToLast(3)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children){
                        val modelo = ds.getValue(Modelopdf::class.java)
                        pdfArrayList.add(modelo!!)
                    }
                    pdfArrayList.reverse()
                    adaptadorPdfCliente = AdaptadorPdfCliente(this@TopDescargados, pdfArrayList)
                    binding.RvTopDescargados.adapter = adaptadorPdfCliente
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}