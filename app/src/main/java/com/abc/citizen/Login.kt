package com.abc.citizen

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.nav_header.*

class Login : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    private val spannableStringRegWhite = SpannableString("REGISTER")
    private val spannableStringLogWhite = SpannableString("LOGIN")

    private val spannableStringRegGray = SpannableString("REGISTER")
    private val spannableStringLogGray = SpannableString("LOGIN")

    private val mWhite = ForegroundColorSpan(Color.WHITE)
    private val mGray = ForegroundColorSpan(Color.GRAY)

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        titleFontChange()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase Data
        database = FirebaseDatabase.getInstance().reference

        loginButton.setOnClickListener {

            if (LogEmailText.text.toString().isEmpty()) {
                LogEmailText.error = "Please Enter your Email address"
                Toast.makeText(applicationContext, "Enter your Email address", Toast.LENGTH_SHORT).show()
            } else if (LogPasswordText.text.toString().isEmpty()) {
                LogPasswordText.error = "Please Enter your Password"
                Toast.makeText(applicationContext, "Enter the Password", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(LogEmailText.text.toString(), LogPasswordText.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("success", "signInWithEmail:success")
                            val user = auth.currentUser

                            Toast.makeText(this, "You've signed in successfully", Toast.LENGTH_SHORT).show()
                            val intent = Intent(baseContext, MainActivity::class.java)
                            startActivity(intent)

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("fail", "signInWithEmail:failure", task.exception)
                            Toast.makeText(
                                this, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        regButton.setOnClickListener {
            if (regNameText.text.isEmpty() || regEmailText.text.isEmpty() ||
                regPasswordText.text.isEmpty() || regConfirmPasswordText.text.isEmpty()
            )
                Toast.makeText(
                    this, "Please fill all the fields",
                    Toast.LENGTH_LONG
                ).show()
            else if (regPasswordText.text.toString() != regConfirmPasswordText.text.toString()) {
                regConfirmPasswordText.error = "please Double-check your password"
                Toast.makeText(
                    this, "Passwords Doesn't match",
                    Toast.LENGTH_LONG
                ).show()
            } else {

                auth.createUserWithEmailAndPassword(regEmailText.text.toString(), regPasswordText.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Success", "createUserWithEmail:success")
                            val user = auth.currentUser
                            writeNewUser(user!!.uid, regNameText.text.toString(), regEmailText.text.toString())
                            Toast.makeText(
                                this, "you have signed up successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            regEmailText.text.clear()
                            regPasswordText.text.clear()
                            regNameText.text.clear()
                            regConfirmPasswordText.text.clear()

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Fail", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(
                                this, "Authentication failed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        // add admin account

    }

    private fun titleFontChange() {
        spannableStringLogWhite.setSpan(mWhite, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStringLogGray.setSpan(mGray, 0, 5, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStringRegGray.setSpan(mGray, 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStringRegWhite.setSpan(mWhite, 0, 8, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        loginSectorButton.text = spannableStringLogWhite
        registerSectorButton.text = spannableStringRegGray

        LogEmailText.visibility = View.VISIBLE
        LogPasswordText.visibility = View.VISIBLE
        loginButton.visibility = View.VISIBLE
        regNameText.visibility = View.INVISIBLE
        regEmailText.visibility = View.INVISIBLE
        regPasswordText.visibility = View.INVISIBLE
        regConfirmPasswordText.visibility = View.INVISIBLE
        regButton.visibility = View.INVISIBLE
        regProfilePic.visibility = View.INVISIBLE

        loginSectorButton.setOnClickListener {
            registerSectorButton.typeface = Typeface.DEFAULT
            loginSectorButton.typeface = Typeface.DEFAULT_BOLD

            loginSectorButton.text = spannableStringLogWhite
            registerSectorButton.text = spannableStringRegGray

            LogEmailText.visibility = View.VISIBLE
            LogPasswordText.visibility = View.VISIBLE
            loginButton.visibility = View.VISIBLE
            regNameText.visibility = View.INVISIBLE
            regEmailText.visibility = View.INVISIBLE
            regPasswordText.visibility = View.INVISIBLE
            regConfirmPasswordText.visibility = View.INVISIBLE
            regButton.visibility = View.INVISIBLE
            regProfilePic.visibility = View.INVISIBLE
        }

        registerSectorButton.setOnClickListener {
            registerSectorButton.typeface = Typeface.DEFAULT_BOLD
            loginSectorButton.typeface = Typeface.DEFAULT

            loginSectorButton.text = spannableStringLogGray
            registerSectorButton.text = spannableStringRegWhite

            LogEmailText.visibility = View.INVISIBLE
            LogPasswordText.visibility = View.INVISIBLE
            loginButton.visibility = View.INVISIBLE
            regNameText.visibility = View.VISIBLE
            regEmailText.visibility = View.VISIBLE
            regPasswordText.visibility = View.VISIBLE
            regConfirmPasswordText.visibility = View.VISIBLE
            regButton.visibility = View.VISIBLE
            regProfilePic.visibility = View.VISIBLE
        }

    }

    private fun writeNewUser(userId: String, name: String, email: String?) {
        val user = User(name, email)
        database.child("users").child(userId).setValue(user)
    }

}

data class User(
    var username: String? = "",
    var email: String? = ""
)
