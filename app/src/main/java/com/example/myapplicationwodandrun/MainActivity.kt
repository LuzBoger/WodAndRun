package com.example.myapplicationwodandrun

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.myapplicationwodandrun.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        setContentView(view)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonWod = findViewById<Button>(R.id.buttonWod)
        val buttonRun = findViewById<Button>(R.id.buttonRun)

        buttonWod.setOnClickListener {
            intent = Intent(this, WodActivity::class.java)
            startActivity(intent);
        }
        buttonRun.setOnClickListener {
            intent = Intent(this, RunActivity::class.java)
            startActivity(intent);
        }


    }
}