package com.example.androidlatihan15_firebasedb

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log.e
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.halaman_depan.*

class HalamanDepan : AppCompatActivity(), BukaAdapter.FirebaseDataListener {


    private var bukaAdapter: BukaAdapter? = null
    private var rcView: RecyclerView? = null
    private var list: MutableList<BukuModel> = ArrayList()
    lateinit var dbref: DatabaseReference
    lateinit var helperPrefs: PrefsHelper

    lateinit var fAuth: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.halaman_depan)

        btn_logout.setOnClickListener {
            val fAuth = FirebaseAuth.getInstance()
            fAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        rcView = findViewById(R.id.rc_view)
        rcView!!.layoutManager = LinearLayoutManager(this)
        rcView!!.setHasFixedSize(true)
        helperPrefs = PrefsHelper(this)

        dbref = FirebaseDatabase.getInstance()
            .getReference("dataBuku/${helperPrefs.getUID()}")
        dbref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                list = ArrayList()
                for (dataSnapshot in p0.children) {
                    val addDataAll = dataSnapshot.getValue(BukuModel::class.java)
                    addDataAll!!.setKey(dataSnapshot.key!!)
                    list.add(addDataAll!!)
                    bukaAdapter = BukaAdapter(
                        this@HalamanDepan,
                        list!!
                    )
                    rcView!!.adapter = bukaAdapter
                }
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

    override fun onDeleteData(buku: BukuModel, position: Int) {
        dbref = FirebaseDatabase.getInstance()
            .getReference("dataBuku/${helperPrefs.getUID()}")
        if (dbref != null) {
            dbref.child(buku.getKey()).removeValue().addOnSuccessListener {
                Toast.makeText(
                    this, "data berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
                bukaAdapter!!.notifyDataSetChanged()
            }
        }
    }

    override fun onUpdated(buku: BukuModel, position: Int) {
        dbref = FirebaseDatabase.getInstance()
            .getReference("dataBuku/${helperPrefs.getUID()}")
        if(dbref != null){
            val datax = dbref.child(buku.getKey()).key
            val intent = Intent(this, TambahData::class.java)
            intent.putExtra("kode", datax)
            startActivity(intent)
        }
    }


}