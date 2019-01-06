package com.abc.citizen

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserInfo
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.nav_header.*


class MainActivity : AppCompatActivity() {
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    private var textUser: TextView? = null
    private var textUserEmail: TextView? = null
    private var imageProfile: CircleImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialise()

        button_transport.setOnClickListener {
            val intent = Intent(applicationContext, Description::class.java)
            startActivity(intent)
        }
        buttonSearch.setOnClickListener {
            val intent = Intent(applicationContext, Login::class.java)
            startActivity(intent)
        }

    }

    private fun initialise() {
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("users")
        mAuth = FirebaseAuth.getInstance()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headView: View = navigationView.getHeaderView(0)
        imageProfile = headView.findViewById(R.id.profilePic)
        textUser = headView.findViewById(R.id.userNameText)
        textUserEmail = headView.findViewById(R.id.userEmailText)

    }

    override fun onStart() {
        super.onStart()

        val mUser = mAuth!!.currentUser

        if (mUser != null) {
            val mUserReference = mDatabaseReference!!.child(mUser!!.uid)

            textUserEmail!!.text = mUser.email

            mUserReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI

                    textUser!!.text = snapshot.child("username").value as String
                    var picUrl: String = snapshot.child("profilePictureUri").value as String
                    Log.d("DataListener", "მონაცემთა ბაზაში ცვლილებებია")

                    Picasso.get().load(picUrl).resize(80, 80).centerCrop().into(imageProfile)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w("ListenData", "loadUser:onCancelled", databaseError.toException())
                    // ...
                }
            })
        }
        else{
            Toast.makeText(this,"No User Loaded",Toast.LENGTH_SHORT).show()
            imageProfile!!.setImageResource(android.R.color.transparent)
        }
    }
}

