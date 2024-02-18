package com.example.myapplicationwodandrun

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.myapplicationwodandrun.service.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

class WodActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wod)

        /* Changement de nom vers cross fit */
        val navBar = findViewById<TextView>(R.id.titleNavBar)
        navBar.text ="CROSS-FIT"

        /* Modification du titre pour juste Ecrire Cross-fit */
        executeCall()
    }

    private fun executeCall() {
        val linear = findViewById<LinearLayout>(R.id.linear)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val response = ApiClient.apiService.getWod()
                if (response.isSuccessful && response.body() != null) {
                    val content = response.body()
                    Toast.makeText(this@WodActivity, "J'ai réussi Whaou", Toast.LENGTH_SHORT).show()
                    creationInterface(content)
                } else {
                    Toast.makeText(
                        this@WodActivity,
                        "J'ai pas récupérer Wha non fdp",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@WodActivity, "Error Occured : ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun creationInterface(contents: MutableList<wodData>?) {
        val linear = findViewById<LinearLayout>(R.id.linear)

        val mapOfExercice : MutableMap<String, MutableList<wodData>> = mutableMapOf()
        contents?.forEach {
            val list : MutableList<wodData> = mapOfExercice[it.type!!] ?: mutableListOf()
            list.add(it)
            mapOfExercice[it.type] = list
        }

        mapOfExercice.entries.forEach { content ->
            val category = Category(this, null, content.key, content.value)
            linear.addView(category)
        }


    }
    fun changeTitle(){

    }
}