package com.example.androidlatihan15_firebasedb

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.et_email
import kotlinx.android.synthetic.main.activity_main.et_password
import kotlinx.android.synthetic.main.register.*
import kotlinx.android.synthetic.main.tambah_data.*

class Register : AppCompatActivity() {

    lateinit var fAuth: FirebaseAuth
    lateinit var dbRef: DatabaseReference
    lateinit var helperPref: PrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        fAuth = FirebaseAuth.getInstance()
        btn_submit.setOnClickListener {
            var email = et_email.text.toString()
            var password = et_password.text.toString()
            var nama = et_nama.text.toString()

            val helperPrefs = PrefsHelper(this)
            if (email.isNotEmpty() || password.isNotEmpty() || !email.equals("") || !password.equals("")
                ||  et_password.length() >= 6) {
                fAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            simpanToFirebase(nama)
                            Toast.makeText(this, "Register Berhasil!", Toast.LENGTH_SHORT).show()
                            onBackPressed()
                        } else {
                            Toast.makeText(this, "Value must be 6 or more digit!", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Email dan password harus diisi!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun simpanToFirebase(nama: String) {
        val uidUser = helperPref.getUID()
        val counterId = helperPref.getCounterId()
        dbRef = FirebaseDatabase.getInstance().getReference("dataUser/$uidUser/$counterId")
        dbRef.child("/id").setValue(uidUser)
        dbRef.child("/nama").setValue(nama)
        Toast.makeText(this, "Welcome ${nama}", Toast.LENGTH_SHORT).show()
        helperPref.saveCounterId(counterId + 1)
        startActivity(Intent(this, HalamanDepan::class.java))
    }
}