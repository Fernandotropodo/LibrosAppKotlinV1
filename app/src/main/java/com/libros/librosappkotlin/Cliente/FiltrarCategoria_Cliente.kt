package com.libros.librosappkotlin.Cliente

import android.widget.Filter
import com.libros.librosappkotlin.Modelos.ModeloCategoria

class FiltrarCategoria_Cliente : Filter{

    private var filtroLista : ArrayList<ModeloCategoria>
    public var adaptadorCategoriaCliente : AdaptadorCategoria_Cliente

    constructor(filtroLista: ArrayList<ModeloCategoria>, adaptadorCategoriaCliente: AdaptadorCategoria_Cliente) {
        this.filtroLista = filtroLista
        this.adaptadorCategoriaCliente = adaptadorCategoriaCliente
    }

    override fun performFiltering(categoria: CharSequence?): FilterResults {
        var categoria = categoria
        var resultados = FilterResults()

        if (categoria != null && categoria.isNotEmpty()){
            categoria = categoria.toString().uppercase()
            val modeloFiltrado : ArrayList<ModeloCategoria> = ArrayList()
            for (i in 0 until filtroLista.size){
                if (filtroLista[i].categoria.uppercase().startsWith(categoria)){
                    modeloFiltrado.add(filtroLista[i])
                }
                resultados.count = modeloFiltrado.size
                resultados.values = modeloFiltrado
            }
        }

        else {
            resultados.count = filtroLista.size
            resultados.values = filtroLista
        }
        return resultados
    }

    override fun publishResults(p0: CharSequence?, resultados: FilterResults) {
        adaptadorCategoriaCliente.categoriaArrayList = resultados.values as ArrayList<ModeloCategoria>
        adaptadorCategoriaCliente.notifyDataSetChanged()
    }

}