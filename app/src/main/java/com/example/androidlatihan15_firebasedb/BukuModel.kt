package com.example.androidlatihan15_firebasedb

class BukuModel {
    private var nama: String? = null
    private var tanggal: String? = null
    private var judulBuku: String? = null
    private var id: String? = null
    private var description: String? = null
    private var key : String? = null
    private var image: String? = null

    constructor()
    constructor(nama: String, tanggal: String, judul: String) {
        this.nama = nama
        this.tanggal = tanggal
        this.judulBuku = judul
    }

    fun getNama(): String {
        return nama!!
    }

    fun getTanggal(): String {
        return tanggal!!
    }

    fun getJudulBuku(): String {
        return judulBuku!!
    }

    fun getId(): String {
        return id!!
    }

    fun getId(id: String) {
        this.id = id
    }

    fun getDescription(): String {
        return description!!
    }

    fun getImage(): String {
        return image!!
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun setNama(nama: String) {
        this.nama = nama
    }

    fun setTanggal(tanggal: String) {
        this.tanggal = tanggal
    }

    fun setJudul(judul: String) {
        this.judulBuku = judul
    }

    fun setImage(image: String) {
        this.image = image
    }
    fun getKey() : String{return key!!}
    fun setKey(key : String){this.key = key}
}