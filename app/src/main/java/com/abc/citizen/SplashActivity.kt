package com.abc.citizen

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import java.lang.Exception

class SplashActivity : AppCompatActivity() {

    private val handler = Handler()

    private val runnable = Runnable {

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // User is signed in
            val intent = Intent(baseContext, MainActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(baseContext, Login::class.java)
            startActivity(intent)
        }



    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()
        handler.postDelayed(runnable,2000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}