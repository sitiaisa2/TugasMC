package com.example.ibusiti.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.example.ibusiti.Model.MenuModel
import com.example.ibusiti.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

@Suppress("DEPRECATION")
class AddActivity : AppCompatActivity() {

    private lateinit var simpan: Button
    private lateinit var kembali: Button
    private lateinit var addImg: Button
    private lateinit var addjudul: EditText
    private lateinit var addDeskripsi: EditText
    private lateinit var addHarga: EditText
    private lateinit var viewImg: ImageView
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tambah)

        // Inisialisasi Firebase Database
        database = FirebaseDatabase.getInstance().reference.child("Menu")

        // Inisialisasi Firebase Storage
        storageReference = FirebaseStorage.getInstance().reference.child("images")

        initView()

//        viewImg.visibility = View.GONE

        addImg.setOnClickListener {
            openImageChooser()
        }

        simpan.setOnClickListener {
            simpanData()
        }

        kembali.setOnClickListener {
            val intent = Intent(this,HomeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun simpanData() {
        val judul = addjudul.text.toString().trim()
        val deskripsi = addDeskripsi.text.toString().trim()
        val harga = addHarga.text.toString().trim()

        if (judul.isEmpty()) {
            showError("Judul harus diisi")
            addjudul.requestFocus()
            return
        }

        if (deskripsi.isEmpty()) {
            showError("Deskripsi harus diisi")
            addDeskripsi.requestFocus()
            return
        }

        if (harga.isEmpty()) {
            showError("Harga harus diisi")
            addHarga.requestFocus()
            return
        }

        if (selectedImageUri == null) {
            showError("Gambar harus dipilih")
            return
        }

        // Jika semua input terisi, lanjutkan proses simpan data
        val imageRef = storageReference.child(judul + "_" + System.currentTimeMillis() + ".jpg")
        val uploadTask = imageRef.putFile(selectedImageUri!!)

        uploadTask.addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                val menuId = database.push().key
                val menu = MenuModel(menuId, judul, deskripsi, harga, uri.toString())
                menuId?.let { database.child(it).setValue(menu) }
                Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                finish()
            }.addOnFailureListener {
                showError("Gagal mengunggah gambar")
            }
        }.addOnFailureListener {
            showError("Gagal mengunggah gambar")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            viewImg.setImageURI(selectedImageUri)
//            viewImg.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun initView() {
        simpan = findViewById(R.id.btnSave)
        kembali = findViewById(R.id.btnback)
        addImg = findViewById(R.id.btnAddImg)
        viewImg = findViewById(R.id.imageViewMenu)
        addjudul = findViewById(R.id.inputJudulAdd)
        addDeskripsi = findViewById(R.id.inputDeskripsiAdd)
        addHarga = findViewById(R.id.inputHargaAdd)
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
