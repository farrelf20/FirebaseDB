package com.example.androidlatihan15_firebasedb

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.tambah_data.*

class TambahData : AppCompatActivity() {

    lateinit var dbRef: DatabaseReference
    lateinit var helperPref: PrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tambah_data)
        helperPref = PrefsHelper(this)
        btn_simpan.setOnClickListener {
            val nama = et_namaPenulis.text.toString()
            val judul = et_judulBuku.text.toString()
            val tgl = et_tanggal.text.toString()
            val desc = et_description.text.toString()

            if (nama.isNotEmpty() || judul.isNotEmpty() || tgl.isNotEmpty() ||
                desc.isNotEmpty()
            ) {
                simpanToFireBase(nama, judul, tgl, desc)
            } else {
                Toast.makeText(
                    this, "inputan tidak boleh kosong",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun simpanToFireBase(nama: String, judul: String, tgl: String, desc: String) {
        val uidUser = helperPref.getUID()
        val CounterID = helperPref.getCounterId()
        dbRef = FirebaseDatabase.getInstance().getReference("dataBuku/$uidUser")
        dbRef.child("$CounterID/id").setValue(uidUser)
        dbRef.child("$CounterID/nama").setValue(nama)
        dbRef.child("$CounterID/judulBuku").setValue(judul)
        dbRef.child("$CounterID/tanggal").setValue(tgl)
        dbRef.child("$CounterID/description").setValue(desc)
        Toast.makeText(
            this, "Data Berhasil ditambahkan",
            Toast.LENGTH_SHORT
        ).show()

        helperPref.saveCounterId(CounterID + 1)
        onBackPressed()
    }
}