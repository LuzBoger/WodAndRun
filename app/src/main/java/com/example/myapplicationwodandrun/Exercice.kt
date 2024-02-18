package com.example.myapplicationwodandrun

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
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
        val imageExercice = findViewById<ImageView>(R.id.imageExercice)
        Picasso.get()
            .load(exercice.image)
            .into(imageExercice)
        imageExercice.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(exercice.video))
            context.startActivity(intent)
        }

        findViewById<TextView>(R.id.titleExercice).setOnClickListener {
            Toast.makeText(context, exercice.name, Toast.LENGTH_SHORT).show()
        }
    }
}
