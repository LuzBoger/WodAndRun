package com.example.myapplicationwodandrun

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class RunActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run)

        /* Changement de nom vers run */
        val navBar = findViewById<TextView>(R.id.titleNavBar)
        navBar.text ="RUN"
    }
}