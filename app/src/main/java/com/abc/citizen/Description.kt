package com.abc.citizen

import android.app.Activity
import android.app.ProgressDialog
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.*
import kotlinx.android.synthetic.main.activity_description.*
import com.google.android.gms.maps.model.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE
import com.google.android.gms.maps.model.MarkerOptions
import android.widget.LinearLayout
import android.widget.TextView
import com.abc.citizen.R.layout.activity_description
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.nav_header.*
import java.io.File
import java.io.IOException
import java.util.*


lateinit var loc: Location

class Description : AppCompatActivity(), OnMapReadyCallback {
    private var CAMERA_REQUEST_CODE = 0
    private lateinit var nMap: GoogleMap

    private val category = "transport"

    // Location
    private var mMarker: Marker? = null
    private lateinit var mLastLocation: Location
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var client: GoogleApiClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // firebase
    private var mDatabase: FirebaseDatabase? = null
    private var mDatabaseReference: DatabaseReference? = null
    private var mAuth: FirebaseAuth? = null

    // scroll menu
    private var textUser: TextView? = null
    private var textUserEmail: TextView? = null
    private var imageProfile: CircleImageView? = null


    // progressbar
    private var mProgressBar: ProgressDialog? = null

    lateinit var photoPath: String


    companion object {
        private const val MY_PERMISSION_CODE: Int = 1000
    }

    private fun initialise() {
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("users")
        mAuth = FirebaseAuth.getInstance()

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headView: View = navigationView.getHeaderView(0)
        imageProfile = headView.findViewById(R.id.profilePic)
        textUser = headView.findViewById(R.id.userNameText)
        textUserEmail = headView.findViewById(R.id.userEmailText)

    }


    override fun onStart() {
        super.onStart()

        userCheck()
    }


    private fun userCheck() {

        val mUser = mAuth!!.currentUser
        Log.d("USer ID", mUser!!.uid)
        val mUserReference = mDatabaseReference!!.child(mUser.uid)

        textUserEmail!!.text = mUser.email

        mUserReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI

                Log.d("check database", snapshot.child("profilePictureUri").value as String)

                textUser!!.text = snapshot.child("username").value as String
                val picUrl: String = snapshot.child("profilePictureUri").value as String
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_description)


        //////////////// Progress Bar ///////////////
        mProgressBar = ProgressDialog(this)
        //////////////// Progress Bar ///////////////

        println("//////////////////onCreate/////////////////////")
        initialise()
        expandMap()

        //firebase initialization
        mAuth = FirebaseAuth.getInstance()


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Request Runtime Permission
        println("===================================================")
        println(Build.VERSION.SDK_INT)
        println(Build.VERSION_CODES.M)
        println("===================================================")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallBack()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient.requestLocationUpdates(
                    LocationRequest(),
                    LocationCallback(),
                    Looper.myLooper()
                )
            }
        } else {
            buildLocationRequest()
            buildLocationCallBack()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(LocationRequest(), LocationCallback(), Looper.myLooper())
        }

        cameraButton.setOnClickListener {
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (callCameraIntent.resolveActivity(packageManager) != null) {


                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (e: IOException) {
                }
                if (photoFile != null) {

                    selectedPhotoUri = FileProvider.getUriForFile(
                        this,
                        "com.abc.citizen.fileprovider",
                        photoFile
                    )
                }
                    callCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,selectedPhotoUri)
                    startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)

            }

        }   //  კამერის გამშვები


    }


    fun uploadPost(view: View) {
        ///////////////// Photo upload ////////////////
        uploadPhotoToStorage(selectedPhotoUri)

    }


    private fun uploadPostToDatabase(photoUrl: String) {

        val mUser = mAuth!!.currentUser

        if (commentText.text.isEmpty())
            Toast.makeText(
                this, "გთხოვთ დაწეროთ კომენტარი",
                Toast.LENGTH_LONG
            ).show()
        else {

            val commentId = UUID.randomUUID().toString()
            Log.d("Photo URL", photoUrl)
            writeNewPost(commentId, photoUrl, mUser!!.uid, commentText.text.toString(), category)
            commentText.text.clear()
            photoImageView.setImageResource(android.R.color.transparent)
            mProgressBar!!.hide()
            Toast.makeText(this, "განცხადება წარმატებით აიტვირთა", Toast.LENGTH_SHORT).show()
        }

    }


    fun uploadPhotoToStorage(selectedPhotoUri: Uri?) {

        if (selectedPhotoUri != null) {
            mProgressBar!!.setMessage("გთხოვთ დაიცადოთ...")
            mProgressBar!!.show()
            val filename = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/post_pictures/$filename")

            ref.putFile(selectedPhotoUri)
                .addOnSuccessListener {
                    Log.d("Register Activity", "წარმატებით აიტვირთა სურათი: ${it.metadata?.path}")
                    ref.downloadUrl.addOnSuccessListener {
                        Log.d("RegisterActivity", "File Location: $it")
//                    uploadUserToFirebaseDatabase(it.toString())
                        uploadPostToDatabase(it.toString())
                    }
                }
        } else {
            Toast.makeText(this, "გთხოვთ აირჩიოთ სურათი", Toast.LENGTH_SHORT).show()
        }
    }


    fun choosePhotoFromGallery(view: View) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 8)
    }


    private fun createImageFile(): File? {
        val fileName = "temp"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            fileName,
            ".jpg",
            storageDir
        )

        photoPath = image.absolutePath

        return image
    }

    var selectedPhotoUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {

                    photoImageView.setImageURI(Uri.parse(photoPath))
                }
            }
            8 -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // შევამოწმებთ მიღებულ სურათს
                    selectedPhotoUri = data.data
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
                    photoImageView.setImageBitmap(bitmap)
                }
            }
            else -> {
                if (data == null)
                    Toast.makeText(this, "Unrecognized request code (data = 0)", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(
                        this,
                        "Unrecognized request code (resultCode != Activity.RESULT_OK)",
                        Toast.LENGTH_SHORT
                    ).show()
            }
        }


    }


    private fun expandMap() {
        println("////////////////// Expand Map /////////////////////")

        var isExpanded = false

        button_max.setOnClickListener {
            if (isExpanded == false) {
                isExpanded = true
                val param = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f
                )
                mapLayout.layoutParams = param // 6
                descriptionBody.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    15.0f
                ) // 4
                descriptionBottomPannel.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    13.0f
                ) // 8
                descriptionTopPanel.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    13.0f
                ) // 8


            } else {
                isExpanded = false
                val param = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    6.0f
                )
                mapLayout.layoutParams = param // 6
                descriptionBody.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    4.0f
                ) // 4
                descriptionBottomPannel.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    8.0f
                ) // 8
                descriptionTopPanel.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    8.0f
                ) // 8
            }
        }
    }      //   რუკის ფანჯრის სრულ ეკრანზე გადიდება/დაპატარავება

    //  მოძებნილი კოორდინატები რუკაზე გადააქვს
    private fun buildLocationCallBack() {
        println("////////////////// Build Location CallBack /////////////////////")

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                println("//////////////////  onLocation Result /////////////////////")

                mLastLocation = p0!!.locations.get(p0.locations.size - 1) // get last location
                loc = mLastLocation
                println(loc)
                if (mMarker != null)
                    mMarker!!.remove()
                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude
                val latLng = LatLng(latitude, longitude)
                println("///////////////    Lat Lng    ////////////////////")
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = nMap.addMarker(markerOptions)

                // move camera
                println("/////////////////  Move Camera  //////////////////")
                nMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                println("**********************" + latLng.toString() + "*************************")
                nMap.animateCamera(CameraUpdateFactory.zoomBy(11f))
            }
        }
    }


    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }   //  ადგილმდებარეობის მოთხოვნის პარამეტრები


    private fun checkLocationPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ), MY_PERMISSION_CODE
                )
            else
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ), MY_PERMISSION_CODE
                )
            return false
        } else
            return true
    }   // ამოწმებს GPS ნავიგაციის ნებართვას და გამოაქვს ან True ან False

    //  თუ ნავიგაციაზე ნება დართულია კოორდინატებს ვეძებთ და ვიძახებთ buildLocationCallback-ს და Request-ს
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_CODE -> (
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                            if (checkLocationPermission()) {
                                buildLocationRequest()
                                buildLocationCallBack()

                                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                                fusedLocationProviderClient.requestLocationUpdates(
                                    LocationRequest(),
                                    LocationCallback(),
                                    Looper.myLooper()
                                )

                                nMap.isMyLocationEnabled = true
                            }
                    } else
                        Toast.makeText(this, "Permssion Denied", Toast.LENGTH_LONG).show())
        }
    }


    override fun onStop() {
        println("///////////////////// Stopped //////////////////////")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }   // აპის ჩაკეცვისას აჩერებს ადგილმდებარეობის განახლებებს


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("onMapReady", "On Map Ready Started")
        nMap = googleMap
        nMap.mapType = MAP_TYPE_SATELLITE

        // init Google Play Services

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                nMap.isMyLocationEnabled = true
            }
        } else
            nMap.isMyLocationEnabled = false


        // Enable Zoom Control

        nMap.uiSettings.isZoomControlsEnabled = true
        nMap.uiSettings.isMapToolbarEnabled = false
    }   // რუკის პარამეტრები + ხელმეორედ მოწმდება ნებართვა


    private fun writeNewPost(
        commentId: String,
        pictureUri: String,
        authorId: String,
        comment: String,
        category: String
    ) {
        mDatabaseReference = mDatabase!!.reference
        val post = Post(pictureUri, authorId, comment, category)
        mDatabaseReference!!.child("posts").child(commentId).setValue(post)
    }

}

data class Post(
    val pictureUri: String? = "",
    var authorId: String? = "",
    var comment: String? = "",
    var category: String? = ""
)

