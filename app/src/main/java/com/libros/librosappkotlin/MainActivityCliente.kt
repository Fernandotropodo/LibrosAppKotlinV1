package com.libros.librosappkotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.libros.librosappkotlin.Fragmentos_Cliente.Fragment_cliente_cuenta
import com.libros.librosappkotlin.Fragmentos_Cliente.Fragment_cliente_dashboard
import com.libros.librosappkotlin.Fragmentos_Cliente.Fragment_clientes_favoritos
import com.libros.librosappkotlin.databinding.ActivityMainClienteBinding

class MainActivityCliente : AppCompatActivity() {

    private lateinit var binding : ActivityMainClienteBinding
    private lateinit var  firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        comprobarSesion()

        verFragmentoDashboard()

        binding.BottomNavCliente.setOnItemSelectedListener { item->
            when(item.itemId){
                R.id.Menu_dashboard_cl->{
                    verFragmentoDashboard()
                    true
                }
                R.id.Menu_favoritos_cl->{
                    verFragmentoFavoritos()
                    true
                }
                R.id.Menu_cuenta_cl->{
                    verFragmentoCuenta()
                    true
                }
                else->{
                    false
                }
            }

        }
    }

    private fun comprobarSesion(){
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            startActivity(Intent(this, Elegir_rol::class.java))
            finishAffinity()
        }
        else {
            Toast.makeText(applicationContext, "Bienvenido(a) ${firebaseUser.email}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verFragmentoDashboard(){
        val nombre_titulo = "Dashboard"
        binding.TituloRLCliente.text = nombre_titulo
        val fragment = Fragment_cliente_dashboard()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsCliente.id, fragment, "Fragment dashboard")
        fragmentTransaction.commit()
    }

    private fun verFragmentoFavoritos(){
        val nombre_titulo = "Favoritos"
        binding.TituloRLCliente.text = nombre_titulo
        val fragment = Fragment_clientes_favoritos()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsCliente.id, fragment, "Fragment favoritos")
        fragmentTransaction.commit()
    }

    private fun verFragmentoCuenta(){
        val nombre_titulo = "Cuenta"
        binding.TituloRLCliente.text = nombre_titulo
        val fragment = Fragment_cliente_cuenta()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsCliente.id, fragment, "Fragment cuenta")
        fragmentTransaction.commit()
    }
}












































