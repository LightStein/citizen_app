package com.abc.citizen

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.nav_header.*



class MainActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//            val user = FirebaseAuth.getInstance().currentUser
//            user?.let {
//                // Name, email address, and profile photo Url
//                // Name, email address, and profile photo Url
//                val name = user.displayName
//                val email = user.email
//                val photoUrl = user.photoUrl
//                Log.d("username", "0000000000000000000000000000000")
//                Log.d("username", name)
//                Log.d("username", "0000000000000000000000000000000")
//            }

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headView: View = navigationView.getHeaderView(0)
        val imageProfile: ImageView = headView.findViewById(R.id.profilePic)
        val textUser: TextView = headView.findViewById(R.id.userNameText)
        val textUserEmail: TextView = headView.findViewById(R.id.userEmailText)



        button_transport.setOnClickListener {
            val intent = Intent(applicationContext, Description::class.java)
            startActivity(intent)
        }
        buttonSearch.setOnClickListener {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
        }

    }



}
