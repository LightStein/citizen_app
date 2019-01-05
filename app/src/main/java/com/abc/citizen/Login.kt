package com.abc.citizen

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.res.ResourcesCompat
import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*

class Login : AppCompatActivity() {
    private lateinit var database: DatabaseReference

    private var _typeFace: Typeface? = null

    private val spannableStringRegWhite = SpannableString("გაწევრიანება")
    private val spannableStringLogWhite = SpannableString("შესვლა")

    private val spannableStringRegGray = SpannableString("გაწევრიანება")
    private val spannableStringLogGray = SpannableString("შესვლა")

    private val mWhite = ForegroundColorSpan(Color.WHITE)
    private val mGray = ForegroundColorSpan(Color.GRAY)

    private lateinit var auth: FirebaseAuth

    private var mProgressBar: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        titleFontChange()

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // Initialize Firebase Data
        database = FirebaseDatabase.getInstance().reference

        loginButton.setOnClickListener {
            //////////////// Progress Bar ///////////////
            mProgressBar = ProgressDialog(this)
            //////////////// Progress Bar ///////////////

            if (LogEmailText.text.toString().isEmpty()) {
                LogEmailText.error = "გთხოვთ შეიყვანოთ ელფოსტა"
                Toast.makeText(applicationContext, "შეიყვანეთ ელფოსტა", Toast.LENGTH_SHORT).show()
            } else if (LogPasswordText.text.toString().isEmpty()) {
                LogPasswordText.error = "გთხოვთ შეიყვანოთ პაროლი"
                Toast.makeText(applicationContext, "შეიყვანეთ პაროლი", Toast.LENGTH_SHORT).show()
            } else {
                ///////////////////////
                mProgressBar!!.setMessage("გთხოვთ დაიცადოთ...")
                mProgressBar!!.show()
                ////////////////////////
                auth.signInWithEmailAndPassword(LogEmailText.text.toString(), LogPasswordText.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("success", "signInWithEmail:success")
                            val user = auth.currentUser
                            mProgressBar!!.hide()
                            Toast.makeText(this, "თქვენ წარმატებით შეხვედით", Toast.LENGTH_SHORT).show()
                            val intent = Intent(baseContext, MainActivity::class.java)
                            startActivity(intent)

                        } else {
                            mProgressBar!!.hide()
                            // If sign in fails, display a message to the user.
                            Log.w("fail", "ავტორიზაცია ჩაიშალა", task.exception)
                            Toast.makeText(
                                this, "გთხოვთ გადაამოწმოთ ინფრმაციის სისწორე",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        regButton.setOnClickListener {
            //////////////// Progress Bar ///////////////
            mProgressBar = ProgressDialog(this)
            //////////////// Progress Bar ///////////////
            if (regNameText.text.isEmpty() || regEmailText.text.isEmpty() ||
                regPasswordText.text.isEmpty() || regConfirmPasswordText.text.isEmpty()
            )
                Toast.makeText(
                    this, "გთხოვთ შეავსოთ ყველა გრაფა",
                    Toast.LENGTH_LONG
                ).show()
            else if (regPasswordText.text.toString() != regConfirmPasswordText.text.toString()) {
                regConfirmPasswordText.error = "გთხოვთ გადაამოწმოთ შეყვანამდე"
                Toast.makeText(
                    this, "პაროლები ერთმანეთს არ ემთხვევა",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                mProgressBar!!.setMessage("გთხოვთ დაიცადოთ...")
                mProgressBar!!.show()
                uploadImageToFirebaseStorage()

            }
        }

        // add profile picture
        regProfilePic.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        // fonts
        this._typeFace = ResourcesCompat.getFont(this.applicationContext, R.font.achveull)
        _initializeGUI()
    }

    private fun _initializeGUI() {
        LogEmailText.setHint(
            Html.fromHtml(
                "<small>" +
                        "ელფოსტა" + "</small>"
            )
        )

        LogPasswordText.setHint(
            Html.fromHtml(
                "<small>" +
                        "პაროლი" + "</small>"
            )
        )
        regEmailText.setHint(
            Html.fromHtml(
                "<small>" +
                        "ელფოსტა" + "</small>"
            )
        )

        regPasswordText.setHint(
            Html.fromHtml(
                "<small>" +
                        "პაროლი" + "</small>"
            )
        )

        regConfirmPasswordText.setHint(
            Html.fromHtml(
                "<small>" +
                        "გაიმეორეთ პაროლი" + "</small>"
            )
        )

        regNameText.setHint(
            Html.fromHtml(
                "<small>" +
                        "სახელი" + "</small>"
            )
        )

    } // ფონტების მისანიჭებლად

    private fun titleFontChange() {
        spannableStringLogWhite.setSpan(mWhite, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStringLogGray.setSpan(mGray, 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStringRegGray.setSpan(mGray, 0, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableStringRegWhite.setSpan(mWhite, 0, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

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
        circle_profile_picture.visibility = View.INVISIBLE

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
            circle_profile_picture.visibility = View.INVISIBLE
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
            circle_profile_picture.visibility = View.VISIBLE
        }

    }

    private fun uploadUserToFirebaseDatabase(profilePicUrl: String) {

        // Firebase-ზე იგზავნება სახელი მეილი და პაროლი
        auth.createUserWithEmailAndPassword(regEmailText.text.toString(), regPasswordText.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("Success", "მომხმარებლის აკაუნტის შექმნა : წარმატებით დასრულდა")
                    val user = auth.currentUser
                    writeNewUser(profilePicUrl, user!!.uid, regNameText.text.toString(), regEmailText.text.toString())
                    mProgressBar!!.hide()
                    Toast.makeText(
                        this, "თქვენ წარმატებით გაწევრიანდით",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(baseContext, MainActivity::class.java)
                    startActivity(intent)

                    regEmailText.text.clear()
                    regPasswordText.text.clear()
                    regNameText.text.clear()
                    regConfirmPasswordText.text.clear()

                } else {
                    // If sign in fails, display a message to the user.
                    mProgressBar!!.hide()
                    Log.w("Fail", "მომხმარებლის აკაუნტის შექმნა : ჩაიშალა", task.exception)
                    Toast.makeText(
                        this, "რეგისტრაცია ჩაიშალა",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null)
            return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/profile_pictures/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("Register Activity", "წარმატებით აიტვირთა სურათი: ${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File Location: $it")
                    uploadUserToFirebaseDatabase(it.toString())

                }
            }
    }

    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // შევამოწმებთ მიღებულ სურათს
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            circle_profile_picture.setImageBitmap(bitmap)
        }
    }

    private fun writeNewUser(profilePictureUri: String, userId: String, name: String, email: String?) {
        val user = User(profilePictureUri, userId, name, email)
        database.child("users").child(userId).setValue(user)
    }

}

data class User(
    val profilePictureUri: String? = "",
    var userId: String? = "",
    var username: String? = "",
    var email: String? = ""
)
