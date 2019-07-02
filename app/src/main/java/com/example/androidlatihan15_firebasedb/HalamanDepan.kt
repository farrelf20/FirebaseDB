package com.example.androidlatihan15_firebasedb

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log.e
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.halaman_depan.*

class HalamanDepan : AppCompatActivity() {

    private var bukaAdapter: BukaAdapter? = null
    private var rcView: RecyclerView? = null
    private var list: MutableList<BukuModel> = ArrayList<BukuModel>()
    lateinit var dbref: DatabaseReference
    lateinit var helperPrefs: PrefsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.halaman_depan)
        rcView = findViewById(R.id.rc_view)
        rcView!!.layoutManager = LinearLayoutManager(this)
        rcView!!.setHasFixedSize(true)
        helperPrefs = PrefsHelper(this)

        dbref = FirebaseDatabase.getInstance()
            .getReference("dataBuku/${helperPrefs.getUID()}")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children) {
                    val addDataAll = dataSnapshot.getValue(BukuModel::class.java)
                    list.add(addDataAll!!)
                }
                bukaAdapter = BukaAdapter(applicationContext, list)
                rcView!!.adapter = bukaAdapter
            }

            override fun onCancelled(p0: DatabaseError) {
                e("TAGERROR", p0.message)
            }

        })
        floatingActionButton.setOnClickListener {
            //lets do something
            startActivity(Intent(this, TambahData::class.java))
        }
    }
}