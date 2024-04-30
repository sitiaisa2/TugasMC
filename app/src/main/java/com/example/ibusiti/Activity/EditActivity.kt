package com.example.ibusiti.Activity
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.ibusiti.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditActivity : AppCompatActivity() {

    private lateinit var itemId: String
    private lateinit var btnEkembali: Button
    private lateinit var btnEUpdate: Button
    private lateinit var tvEditJudul: EditText
    private lateinit var tvEditDes: EditText
    private lateinit var tvEditHarga: EditText
    private lateinit var btnEditImg: Button
    private lateinit var imgVitem: ImageView

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    private var selectedImageUri: Uri? = null
    private var oldImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        initView()

        itemId = intent.getStringExtra("id").toString()
        if (itemId.isNotEmpty()) {
            fetchItem(itemId)
        } else {
            showDataNotFoundDialog()
        }

        btnEkembali.setOnClickListener {
            finish()
        }

        btnEUpdate.setOnClickListener {
            updateItem()
        }

        btnEditImg.setOnClickListener {
            openImageChooser()
        }
    }

    private fun initView() {
        btnEkembali = findViewById(R.id.btnEback)
        btnEUpdate = findViewById(R.id.btnEdit)
        btnEditImg = findViewById(R.id.btnEditImg)
        imgVitem = findViewById(R.id.imageViewEdit)
        tvEditJudul = findViewById(R.id.inputJudulEdit)
        tvEditDes = findViewById(R.id.inputDeskripsiEdit)
        tvEditHarga = findViewById(R.id.inputHargaEdit)

        storageReference = FirebaseStorage.getInstance().reference
    }

    private fun fetchItem(itemId: String) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Menu").child(itemId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val judul = snapshot.child("judul").getValue(String::class.java)
                    val deskripsi = snapshot.child("deskripsi").getValue(String::class.java)
                    val harga = snapshot.child("harga").getValue(String::class.java)
                    val imageUrl = snapshot.child("imageUrl").getValue(String::class.java)

                    // Set nilai pada EditText dan ImageView
                    tvEditJudul.setText(judul)
                    tvEditDes.setText(deskripsi)
                    tvEditHarga.setText(harga)
                    oldImageUrl = imageUrl
                    imageUrl?.let { Glide.with(this@EditActivity).load(it).into(imgVitem) }
                } else {
                    showDataNotFoundDialog()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateItem() {
        val newJudul = tvEditJudul.text.toString().trim()
        val newDes = tvEditDes.text.toString().trim()
        val newHarga = tvEditHarga.text.toString().trim()

        if (newJudul.isEmpty() || newDes.isEmpty() || newHarga.isEmpty()) {
            Toast.makeText(this, "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedImageUri != null) {
            uploadNewImageAndData(newJudul, newDes, newHarga)
        } else {
            updateDataOnly(newJudul, newDes, newHarga)
        }
    }

    private fun uploadNewImageAndData(judul: String, des: String, harga: String) {
        val newImageName = "${judul}_${System.currentTimeMillis()}.jpg"
        val imageRef = storageReference.child("images/$newImageName")
        val uploadTask = imageRef.putFile(selectedImageUri!!)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val imageUrl = downloadUri.toString()
                // Delete old image
                deleteOldImage(oldImageUrl)
                // Update data with new image URL
                updateData(judul, des, harga, imageUrl)
            } else {
                // Handle failures
                Toast.makeText(this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateDataOnly(judul: String, des: String, harga: String) {
        updateData(judul, des, harga, oldImageUrl)
    }

    private fun updateData(judul: String, des: String, harga: String, imageUrl: String?) {
        val updatedData = HashMap<String, Any>()
        updatedData["judul"] = judul
        updatedData["deskripsi"] = des
        updatedData["harga"] = harga
        imageUrl?.let { updatedData["imageUrl"] = it }

        databaseReference.updateChildren(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil mengupdate data", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengupdate data", Toast.LENGTH_SHORT).show()
            }

        val intent = Intent()
        intent.putExtra("judul", judul)
        intent.putExtra("deskripsi", des)
        intent.putExtra("harga", harga)
        intent.putExtra("imageUrl", imageUrl)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun deleteOldImage(imageUrl: String?) {
        if (imageUrl != null) {
            val oldImageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
            oldImageRef.delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Gambar berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menghapus gambar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun showDataNotFoundDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Data Tidak Ditemukan")
        builder.setMessage("Maaf, data tidak ditemukan.")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            onBackPressed()
        }
        builder.setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            Glide.with(this).load(selectedImageUri).into(imgVitem)
        }
    }
}
