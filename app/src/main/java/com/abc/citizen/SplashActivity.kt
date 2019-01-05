package com.abc.citizen

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_splash.*
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

        val fileName = "intro"
        val filePlace="android.resource://"+packageName + "/raw/" + R.raw.intro
        val videoview = findViewById<View>(R.id.videoView) as VideoView

        videoview.setVideoURI(Uri.parse(filePlace))

        videoview.start()

    }

    override fun onStart() {
        super.onStart()
        handler.postDelayed(runnable,3000)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }
}