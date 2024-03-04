package com.example.myapplicationwodandrun

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso

class Exercice(context: Context, attrs: AttributeSet?, exercice: wodData) :
    FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.exercice, this)

        findViewById<TextView>(R.id.titleExercice).text = exercice.name

        findViewById<TextView>(R.id.titleExercice).setOnClickListener {
            Toast.makeText(context, exercice.name, Toast.LENGTH_SHORT).show()
        }

        val imageExercice = findViewById<ImageView>(R.id.imageExercice)
        Picasso.get()
            .load(exercice.image)
            .into(imageExercice)

        imageExercice.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(exercice.video))
            context.startActivity(intent)
        }
        val sharedPreferences = context.getSharedPreferences("PREF", Context.MODE_PRIVATE)

        // Utilisez l'ID unique de l'exercice comme clé dans les préférences partagées
        val exerciceKey = "exercice_${exercice.id}"

        if(sharedPreferences.contains(exerciceKey)){
            val valeurExercice = sharedPreferences.getString(exerciceKey, "")
            findViewById<EditText>(R.id.inputExercice).setText(valeurExercice)
        }

        findViewById<Button>(R.id.ButtonValidate).setOnClickListener {
            val editor = sharedPreferences.edit()
            val contenuEditText = findViewById<EditText>(R.id.inputExercice).text.toString()

            // Utilisez l'ID unique de l'exercice comme clé dans les préférences partagées
            editor.putString(exerciceKey, contenuEditText)
            editor.apply()
            Toast.makeText(context, "Modification effectué !", Toast.LENGTH_SHORT).show()
        }
    }
}
