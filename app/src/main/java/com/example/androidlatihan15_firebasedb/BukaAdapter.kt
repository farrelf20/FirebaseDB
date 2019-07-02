package com.example.androidlatihan15_firebasedb

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class BukaAdapter : RecyclerView.Adapter<BukaAdapter.BukuViewHolder> {
    lateinit var mContext: Context
    lateinit var itemBuku: List<BukuModel>

    constructor()
    constructor(mContext: Context, list: List<BukuModel>) {
        this.mContext = mContext
        this.itemBuku = list
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): BukuViewHolder {
        val view: View = LayoutInflater.from(p0.context).inflate(
            R.layout.show_data, p0, false
        )
        val bukuViewHolder = BukuViewHolder(view)
        return bukuViewHolder
    }

    override fun getItemCount(): Int {
        return itemBuku.size
    }

    override fun onBindViewHolder(p0: BukuViewHolder, p1: Int) {
        val bukuModel: BukuModel = itemBuku.get(p1)
        p0.tv_nama.text = bukuModel.getNama()
        p0.tv_tanggal.text = bukuModel.getTanggal()
        p0.tv_judul.text = bukuModel.getJudulBuku()
        p0.ll_content.setOnClickListener {
            Toast.makeText(
                mContext, "contoh touch listener",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    inner class BukuViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        var ll_content: LinearLayout
        var tv_nama: TextView
        var tv_tanggal: TextView
        var tv_judul: TextView

        init {
            ll_content = itemview.findViewById(R.id.ll_content)
            tv_nama = itemview.findViewById(R.id.tv_penulis)
            tv_judul = itemview.findViewById(R.id.tv_title)
            tv_tanggal = itemview.findViewById(R.id.tv_tgl)
        }
    }
}