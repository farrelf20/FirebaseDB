package com.example.androidlatihan15_firebasedb

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log.e
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.upload_image.*
import java.io.IOException
import java.util.*

class UploadFireStorage : AppCompatActivity() {

    lateinit var helperPrefs: PrefsHelper
    val REQUEST_IMAGE = 10002
    val PERMISSION_REQUEST_CODE = 10003
    lateinit var filePathImage: Uri
    var value = 0.0

    lateinit var dbRef: DatabaseReference

    lateinit var fstorage: FirebaseStorage
    lateinit var fstorageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_image)
        helperPrefs = PrefsHelper(this)
        fstorage = FirebaseStorage.getInstance()
        fstorageRef = fstorage.reference

        img_placeholder.setOnClickListener {
            when {
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) -> {
                        if (ContextCompat.checkSelfPermission(
                                this@UploadFireStorage,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissions(
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                ),
                                PERMISSION_REQUEST_CODE
                            )
                        } else {
                            imageChooser()
                        }
            }
                else -> {
                    imageChooser()
                }
            }
        }
        btn_kirim.setOnClickListener {
            uploadDatas()
        }
    }

    private fun imageChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(
            Intent.createChooser(intent, "select image"),
            REQUEST_IMAGE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0]
                    == PackageManager.PERMISSION_DENIED
                ) {
                    Toast.makeText(
                        this@UploadFireStorage,
                        "izin ditolak !!", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    imageChooser()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_IMAGE -> {
                filePathImage = data?.data!!
                try {
                    val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(
                        this.contentResolver, filePathImage
                    )
                    Glide.with(this)
                        .load(bitmap)
                        .override(250, 250)
                        .centerCrop()
                        .into(img_placeholder)
                } catch (x: IOException) {
                    x.printStackTrace()
                }
            }
        }
    }
    fun GetFileExtension(uri : Uri) : String?{
        val contentResolverz = this.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()

        return mimeTypeMap.getExtensionFromMimeType(contentResolverz.getType(uri))
    }
    fun uploadDatas(){
        val nameX = UUID.randomUUID().toString()
        val uid = helperPrefs.getUID()
        val ref : StorageReference = fstorageRef
            .child("images/$uid/${nameX}.${GetFileExtension(filePathImage)}")
        ref.putFile(filePathImage)
            .addOnSuccessListener {
                Toast.makeText(this@UploadFireStorage, "berhasil upload",
                        Toast.LENGTH_SHORT).show()
                progressDownload.visibility = View.GONE
                ref.downloadUrl.addOnSuccessListener {
                    dbRef = FirebaseDatabase.getInstance().getReference("image/")
                    dbRef.child("urlImage/${helperPrefs.getCounterId()}").setValue(it.toString())
                }
            }
            .addOnFailureListener {
                e("TAGERROR" ,it.message)
            }
            .addOnProgressListener {
                taskSnapshot ->
                    value = (100.0*taskSnapshot
                        .bytesTransferred/taskSnapshot.totalByteCount)
                progressDownload.visibility = View.VISIBLE
            }
    }
}