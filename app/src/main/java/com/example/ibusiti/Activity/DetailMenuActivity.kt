package com.example.ibusiti.Activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.ibusiti.Model.MenuModel
import com.example.ibusiti.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class DetailMenuActivity : AppCompatActivity() {

    private lateinit var tvDjudul : TextView
    private lateinit var tvDharga : TextView
    private lateinit var tvDdes : TextView
    private lateinit var tvDimg : ImageView
    private lateinit var imgEdit : ImageView
    private lateinit var imgHapus : ImageView

    private lateinit var DRef : DatabaseReference

    private lateinit var itemList: ArrayList<MenuModel>


    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_menu)

        initView()
        setValueDetail()
        itemList = arrayListOf<MenuModel>()

        imgEdit.setOnClickListener {
                val itemId = intent.getStringExtra("id").toString()
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("id", itemId)
                startActivityForResult(intent, EDIT_ACTIVITY_REQUEST_CODE)
        }


        imgHapus.setOnClickListener {
            hapusItem()
        }

    }

    private fun hapusItem() {
        val itemId = intent.getStringExtra("id").toString()
        val imageUrl = intent.getStringExtra("imageUrl").toString()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Konfirmasi Hapus")
        builder.setMessage("Apakah Anda yakin ingin menghapus item ini?")
        builder.setPositiveButton("Ya") { dialog, which ->
            // Hapus gambar dari Firebase Storage
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    // Gambar berhasil dihapus dari Firebase Storage
                    Toast.makeText(this, "Gambar berhasil dihapus", Toast.LENGTH_SHORT).show()
                    // Hapus item dari Firebase Realtime Database
                    val DRef = FirebaseDatabase.getInstance().getReference("Menu").child(itemId)
                    val mTask = DRef.removeValue()
                    mTask.addOnSuccessListener {
                        // Item berhasil dihapus
                        Toast.makeText(this, "Item berhasil dihapus", Toast.LENGTH_SHORT).show()
                        // Kembali ke halaman HomeActivity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                        .addOnFailureListener { e ->
                            // Tampilkan dialog untuk merefresh halaman
                            val refreshDialog = AlertDialog.Builder(this)
                            refreshDialog.setTitle("Gagal Menghapus Data")
                            refreshDialog.setMessage("Gagal menghapus item. Coba lagi?")
                            refreshDialog.setPositiveButton("Ya") { dialog, which ->
                                // Merefresh halaman
                                val intent = Intent(this,HomeActivity::class.java)
                                startActivity(intent)
                            }
                            refreshDialog.setNegativeButton("Tidak") { dialog, which ->
                                // Batalkan penghapusan
                            }
                            refreshDialog.show()
                        }
                }
                .addOnFailureListener { e ->
                    // Tampilkan dialog untuk merefresh halaman
                    val refreshDialog = AlertDialog.Builder(this)
                    refreshDialog.setTitle("Gagal menghapus Gambar")
                    refreshDialog.setMessage("Gagal Gambar. Coba lagi?")
                    refreshDialog.setPositiveButton("Ya") { dialog, which ->
                        // Merefresh halaman
                       val intent = Intent(this,HomeActivity::class.java)
                        startActivity(intent)
                    }
                    refreshDialog.setNegativeButton("Tidak") { dialog, which ->
                        // Batalkan penghapusan
                    }
                    refreshDialog.show()
                }
        }
        builder.setNegativeButton("Tidak") { dialog, which ->
            // Batalkan penghapusan
        }
        builder.show()
    }




    private fun initView() {
        tvDjudul = findViewById(R.id.tvDjudul)
        tvDdes = findViewById(R.id.tvDdes)
        tvDharga = findViewById(R.id.tvDharga)
        tvDimg = findViewById(R.id.tvDimg)
        imgEdit = findViewById(R.id.tvbtnEdit)
        imgHapus = findViewById(R.id.tvbtnHapus)
    }

    private fun setValueDetail() {
        tvDjudul.text = intent.getStringExtra("judul")
        tvDdes.text = intent.getStringExtra("deskripsi")
        tvDharga.text = "Rp. ${intent.getStringExtra("harga")}"
        Glide.with(this)
            .load(intent.getStringExtra("imageUrl"))
            .placeholder(R.drawable.random) // Placeholder jika gambar belum dimuat
            .into(tvDimg)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EDIT_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Panggil fungsi untuk mengambil data yang telah diperbarui dari intent
            val updatedJudul = data?.getStringExtra("judul")
            val updatedDeskripsi = data?.getStringExtra("deskripsi")
            val updatedHarga = data?.getStringExtra("harga")
            val updatedImageUrl = data?.getStringExtra("imageUrl")

            // Terapkan data yang telah diperbarui ke tampilan
            tvDjudul.text = updatedJudul
            tvDdes.text = updatedDeskripsi
            tvDharga.text = "Rp. $updatedHarga"
            Glide.with(this)
                .load(updatedImageUrl)
                .placeholder(R.drawable.random) // Placeholder jika gambar belum dimuat
                .into(tvDimg)
        }
    }

    companion object {
        const val EDIT_ACTIVITY_REQUEST_CODE = 1
    }

}