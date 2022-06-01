package com.example.tagtabazaradmin

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.tagtabazaradmin.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*
import android.R.attr.bitmap
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity(), View.OnClickListener{

    private lateinit var requestPermission: ActivityResultLauncher<Array<String>>
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: ActivityMainBinding
    lateinit var outputFileUri: Uri
    lateinit var image: ImageView
    lateinit var db: FirebaseFirestore
    lateinit var storage: FirebaseStorage
    var categoryList = mutableListOf<String>()
    var imageList = mutableListOf<String>("","","","","","")
    var imageIndex = 0
    var gender=""
    var image1=""
    var image2=""
    var image3=""
    var image4=""
    var image5=""
    var image6=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        getCategories()

        requestPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            permissions.forEach { actionMap ->
                when(actionMap.key){
                    Manifest.permission.CAMERA ->{
                        if(actionMap.value){
                            startCameraIntent()
                        }else{
                            Toast.makeText(this, "Kamera rugsady gerek.", Toast.LENGTH_LONG).show()
                        }
                    }
                    Manifest.permission.READ_EXTERNAL_STORAGE -> {
                        if(actionMap.value){
                            startGalleryIntent()
                        }else{
                            Toast.makeText(this, "Gallery rugsady gerek.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                handleCameraImage(result.data)
            }
        }

        setListeners()
    }

    private fun setListeners() {
        binding.iImage1.setOnClickListener(this)
        binding.iImage2.setOnClickListener(this)
        binding.iImage3.setOnClickListener(this)
        binding.iImage4.setOnClickListener(this)
        binding.iImage5.setOnClickListener(this)
        binding.iImage6.setOnClickListener(this)
        binding.bSave.setOnClickListener(this)
    }

    private fun handleCameraImage(data: Intent?) {
//        val bitmap = data?.extras?.get("data") as Uri
//        image.setImageURI(data?.data)
        imageList[imageIndex] = data?.data.toString()
    }



    private fun prepRequestsCamera() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ){
            startCameraIntent()
        }else{
            val permissionRequest = arrayOf(Manifest.permission.CAMERA)
            requestPermission.launch(permissionRequest)
        }
    }

    private fun prepRequestsGallery() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ){
            startGalleryIntent()
        }else{
            val externalRequest = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermission.launch(externalRequest)
        }
    }

    private fun startGalleryIntent() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        resultLauncher.launch(galleryIntent)
    }

    private fun startCameraIntent() {
//        val calendar = Calendar.getInstance()
//        val file = File(Environment.getExternalStorageDirectory(), "${calendar.get(Calendar.MILLISECOND)}.jpg")
//        outputFileUri = Uri.fromFile(file)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        resultLauncher.launch(cameraIntent)
    }

    override fun onClick(p0: View?) {
//        p0?.let { image = it as ImageView }
        when(p0?.id){
            R.id.i_image1 -> {
                imageIndex=0
                prepRequestsGallery()
            }
            R.id.i_image2 -> {
                imageIndex=1
                prepRequestsGallery()
            }
            R.id.i_image3 -> {
                imageIndex=2
                prepRequestsGallery()
            }
            R.id.i_image4 -> {
                imageIndex=3
                prepRequestsGallery()
            }
            R.id.i_image5 -> {
                imageIndex=4
                prepRequestsGallery()
            }
            R.id.i_image6 -> {
                imageIndex=5
                prepRequestsGallery()
            }
            R.id.b_save -> {
                uploadData()
            }
            else -> {
//                prepRequestsCamera()
//                prepRequestsGallery()
            }
        }
    }

    private fun uploadData() {
        val filePath = storage.getReference(Calendar.getInstance().get(Calendar.MILLISECOND).toString())
        filePath.putFile(Uri.parse(imageList[0]))
            .addOnSuccessListener {
                filePath.downloadUrl.addOnSuccessListener {
                    Log.d("URL: ", it.toString())
                }
            }
    }

    private fun getCategories(){
        categoryList.clear()
        db.collection("Categories")
            .get()
            .addOnSuccessListener { d ->
                d.toObjects(Category::class.java).forEach {
                    it.Name?.let { it1 -> categoryList.add(it1) }
                }
                val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryList)
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.sCategory.adapter = aa
            }
    }

}