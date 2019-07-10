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
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.tambah_data.*
import java.io.IOException
import java.util.*

class TambahData : AppCompatActivity() {

    lateinit var dbRef: DatabaseReference
    lateinit var helperPref: PrefsHelper
    lateinit var fstorage: FirebaseStorage
    lateinit var fstorageRef: StorageReference
    var datax: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tambah_data)

        datax = intent.getStringExtra("kode")
        helperPref = PrefsHelper(this)

        if (datax != null) {
            showdataFromDB()
//            Toast.makeText(this, "datanya :\n $datax",
//                Toast.LENGTH_SHORT).show()
        }

        helperPref = PrefsHelper(this)
        btn_simpan.setOnClickListener {
            val nama = et_namaPenulis.text.toString()
            val judul = et_judulBuku.text.toString()
            val tgl = et_tanggal.text.toString()
            val desc = et_description.text.toString()

            if (nama.isNotEmpty() || judul.isNotEmpty() || tgl.isNotEmpty() ||
                desc.isNotEmpty()
            ) {
                uploadDatas()
            } else {
                Toast.makeText(
                    this, "inputan tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        helperPrefs = PrefsHelper(this)
        fstorage = FirebaseStorage.getInstance()
        fstorageRef = fstorage.reference

        img_placeholder.setOnClickListener {
            when {
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) -> {
                    if (ContextCompat.checkSelfPermission(
                            this@TambahData,
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
        btn_simpan.setOnClickListener {
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

    fun simpanToFireBase(nama: String, judul: String, tgl: String, desc: String, url: String) {
        val uidUser = helperPref.getUID()
        val CounterID = helperPref.getCounterId()
        dbRef = FirebaseDatabase.getInstance().getReference("dataBuku/$uidUser")
        dbRef.child("$CounterID/id").setValue(uidUser)
        dbRef.child("$CounterID/nama").setValue(nama)
        dbRef.child("$CounterID/judulBuku").setValue(judul)
        dbRef.child("$CounterID/tanggal").setValue(tgl)
        dbRef.child("$CounterID/description").setValue(desc)
        dbRef.child("$CounterID/image").setValue(url)
        Toast.makeText(
            this, "Data Berhasil ditambahkan",
            Toast.LENGTH_SHORT
        ).show()
        if (datax == null) {
            helperPref.saveCounterId(CounterID + 1)
        }
        onBackPressed()
    }

    fun showdataFromDB() {
        dbRef = FirebaseDatabase.getInstance()
            .getReference("dataBuku/${helperPref.getUID()}/${datax}")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val buku = p0.getValue(BukuModel::class.java)
                et_namaPenulis.setText(buku!!.getNama())
                et_judulBuku.setText(buku.getJudulBuku())
                et_tanggal.setText(buku.getTanggal())
                et_description.setText(buku.getDescription())
            }
        })

    }

    lateinit var helperPrefs: PrefsHelper
    val REQUEST_IMAGE = 10002
    val PERMISSION_REQUEST_CODE = 10003
    lateinit var filePathImage: Uri
    var value = 0.0


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0]
                    == PackageManager.PERMISSION_DENIED
                ) {
                    Toast.makeText(
                        this@TambahData,
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

    fun GetFileExtension(uri: Uri): String? {
        val contentResolverz = this.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()

        return mimeTypeMap.getExtensionFromMimeType(contentResolverz.getType(uri))
    }

    fun uploadDatas() {
        val nameX = UUID.randomUUID().toString()
        val uid = helperPrefs.getUID()
        val ref: StorageReference = fstorageRef
            .child("images/$uid/${nameX}.${GetFileExtension(filePathImage)}")
        ref.putFile(filePathImage)
            .addOnSuccessListener {
                Toast.makeText(
                    this@TambahData, "berhasil upload",
                    Toast.LENGTH_SHORT
                ).show()
                progressDownload.visibility = View.GONE
                val nama = et_namaPenulis.text.toString()
                val judul = et_judulBuku.text.toString()
                val tgl = et_tanggal.text.toString()
                val desc = et_description.text.toString()
                ref.downloadUrl.addOnSuccessListener {
                    simpanToFireBase(nama, judul, tgl, desc, it.toString())
                }
            }
            .addOnFailureListener {
                Log.e("TAGERROR", it.message)
            }
            .addOnProgressListener { taskSnapshot ->
                value = (100.0 * taskSnapshot
                    .bytesTransferred / taskSnapshot.totalByteCount)
                progressDownload.visibility = View.VISIBLE
            }
    }
}