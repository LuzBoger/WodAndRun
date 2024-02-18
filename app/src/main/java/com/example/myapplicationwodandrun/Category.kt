package com.example.myapplicationwodandrun

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class Category(
    context: Context,
    attrs: AttributeSet?,
    title: String,
    exercices: List<wodData>
) :
    FrameLayout(context, attrs) {

    init {
        inflate(context, R.layout.category, this)
        findViewById<TextView>(R.id.titleCategory).text = title

        val linear = findViewById<LinearLayout>(R.id.child)

        exercices.forEach {
            val ex = Exercice(context, null, it)
            linear.addView(ex)
        }
    }
}