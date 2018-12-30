package com.abc.citizen

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.nav_header.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headView: View = navigationView.getHeaderView(0)
        val imageProfile: ImageView = headView.findViewById(R.id.profilePic)
        val textUser: TextView = headView.findViewById(R.id.userNameText)
        val textUserEmail: TextView = headView.findViewById(R.id.userEmailText)

        textUser.text = "name"

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
