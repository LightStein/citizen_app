package com.abc.citizen

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.*
import kotlinx.android.synthetic.main.activity_description.*
import com.google.android.gms.maps.model.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.media.Image
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
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
import com.abc.citizen.R.layout.activity_description
import java.io.*
import java.net.Socket

var handler = Handler()
lateinit var loc: Location

class Description : AppCompatActivity(), OnMapReadyCallback {
    private val CAMERA_REQUEST_CODE = 0
    private lateinit var nMap: GoogleMap
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private lateinit var  mLastLocation: Location
    private var mMarker: Marker?=null

    // Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    ////////////// სურთის გასაგზავნად///////////////
    var imageView: ImageView? = null

    ///////////////////////////////////////////////


    companion object {
        private const val MY_PERMISSION_CODE: Int = 1000
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
                if(resultCode == Activity.RESULT_OK && data != null){
                    photoImageView.setImageBitmap(data.extras.get("data") as Bitmap)
                }
            }
            else -> {

                Toast.makeText(this,"Unrecognized request code", Toast.LENGTH_SHORT).show()
            }
        }
        ////////////// სურათის გასაგზავნად /////////////////
        if(requestCode== 1)
        {
            if (requestCode == Activity.RESULT_OK)
            {
                try{
                    var imageUri: Uri = data?.data as Uri
                    var imageStream: InputStream  = getContentResolver().openInputStream(imageUri) as InputStream
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    photoImageView.setImageBitmap(selectedImage)

                    val bos = ByteArrayOutputStream()
                    selectedImage.compress(Bitmap.CompressFormat.PNG, 1, bos)
                    val array = bos.toByteArray()

                    val sic = SendImageClient()
                    sic.execute(array)

                }catch (e: FileNotFoundException){
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_LONG).show()
                }
            } else { Toast.makeText(applicationContext, "No Image Selected", Toast.LENGTH_LONG).show() }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        println("//////////////////onCreate/////////////////////")

        super.onCreate(savedInstanceState)
        setContentView(activity_description)
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
            if (checkLocationPermission()){
            buildLocationRequest()
            buildLocationCallBack()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(LocationRequest(), LocationCallback(), Looper.myLooper())
            }
        }
        else{
            buildLocationRequest()
            buildLocationCallBack()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(LocationRequest(), LocationCallback(), Looper.myLooper())
        }
        expandMap()
        camera()
        cameraButton.setOnClickListener{
            val callCameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if(callCameraIntent.resolveActivity(packageManager) != null){
                startActivityForResult(callCameraIntent, CAMERA_REQUEST_CODE)
            }

        }   //  კამერის გამშვები

        /////////////////////სურათის გასაგზავნად////////////////////////

    }

    fun send(v: View){
        println("////////////////// send /////////////////////")

    val messageSender = MessageSender()
    messageSender.execute(commentText.text.toString())
//    println("//////////////////"+loc.toString()+"/////////////////////")
    }   // ჯერჯერობით გზავნის კომენტარს

    fun sendImage(v: View){
        println("////////////////// send image /////////////////////")

        /////////   სურათის გაგზავნა    //////////
        var i = Intent(Intent.ACTION_OPEN_DOCUMENT)
        i.run {
            addCategory(Intent.CATEGORY_OPENABLE)
            setType("image/*")
        }
        startActivityForResult(i,1)
    }

    private fun camera(){
        println("////////////////// camera /////////////////////")

        cameraButton.setOnClickListener{

            val intent = Intent(applicationContext, Camera::class.java)
            startActivity(intent)
        }
    }   // კამერის გამშვები

    private fun expandMap(){
        println("////////////////// Expand Map /////////////////////")

        var isExpanded = false

        button_max.setOnClickListener{
            if(isExpanded == false){
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


            } else{
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

    private fun buildLocationCallBack() {
        println("////////////////// Build CallBack /////////////////////")

        locationCallback = object : LocationCallback(){

            override fun onLocationResult(p0: LocationResult?) {
                println("//////////////////  onLocation Result /////////////////////")

                mLastLocation = p0!!.locations.get(p0.locations.size-1) // get last location
                loc = mLastLocation
                println(loc)
                if (mMarker != null)
                    mMarker!!.remove()
                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude
                val latLng = LatLng(latitude,longitude)
                println("///////////////    Lat Lng    ////////////////////")
                val markerOptions = MarkerOptions()
                    .position(latLng)
                    .title("your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mMarker = nMap.addMarker(markerOptions)

                // move camera
                println("/////////////////  Move Camera  //////////////////")
                nMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                nMap.animateCamera(CameraUpdateFactory.zoomBy(11f))
            }
        }
    }   //  მოძებნილი კოორდინატები რუკაზე გადააქვს

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }   //  ადგილმდებარეობის მოთხოვნის პარამეტრები

    private fun checkLocationPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            else
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), MY_PERMISSION_CODE)
            return false
        }
        else
            return true
    }   // ამოწმებს GPS ნავიგაციის ნებართვას და გამოაქვს ან True ან False
    //  თუ ნავიგაციაზე ნება დართულია კოორდინატებს ვეძებთ და ვიძახებთ buildLocationCallback-ს და Request-ს
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            MY_PERMISSION_CODE->(
                    if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
                            if (checkLocationPermission()){
                                buildLocationRequest()
                                buildLocationCallBack()

                                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                                fusedLocationProviderClient.requestLocationUpdates(LocationRequest(), LocationCallback(), Looper.myLooper())
                                nMap.isMyLocationEnabled=true
                            }
                    }
                    else
                        Toast.makeText(this, "Permssion Denied", Toast.LENGTH_LONG).show())
        }
    }

    override fun onStop() {
        println("///////////////////// Stopped //////////////////////")
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }   // აპის ჩაკეცვისას აჩერებს ადგილმდებარეობის განახლებებს

    override fun onMapReady(googleMap: GoogleMap) {
         nMap = googleMap
         nMap.mapType =  MAP_TYPE_SATELLITE

        // init Google Play Services

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            nMap.isMyLocationEnabled=true
            }
        }
        else
            nMap.isMyLocationEnabled = false


        // Enable Zoom Control

        nMap.uiSettings.isZoomControlsEnabled = true
        nMap.uiSettings.isMapToolbarEnabled = false
    }   // რუკის პარამეტრები + ხელმეორედ მოწმდება ნებართვა
}

 class SendImageClient : AsyncTask<ByteArray, Void, Void>() {
     override fun doInBackground(vararg params: ByteArray?): Void {
         TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
     }

     private fun doInBackground(vararg voids: Byte) {

        try {
            val socket = Socket("31.192.57.86", 6800)

            val out = socket.getOutputStream()
            val dos = DataOutputStream(out)
            dos.writeInt(voids.size)
            dos.write(voids, 0, voids.size)
            //handler.post{ Toast.makeText(this, "image sent", Toast.LENGTH_LONG).show() }
            dos.close()
            out.close()
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
            //handler.post{ Toast.makeText(Description , "i/o exception", Toast.LENGTH_SHORT).show() }
        }

    }

}
