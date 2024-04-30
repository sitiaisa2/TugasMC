package com.example.ibusiti.Activity

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ibusiti.Adapter.ItemAdapter
import com.example.ibusiti.R
import com.example.ibusiti.Model.MenuModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.view.View
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {

    private lateinit var Fdatabase : DatabaseReference
    private lateinit var Out : CardView
    private lateinit var Add : CardView
    private lateinit var RVitem : RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemList: ArrayList<MenuModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initView()

        Add.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

        Out.setOnClickListener {
            singout()
        }

        val layoutManager = GridLayoutManager(this, 2)
        RVitem.layoutManager = layoutManager
        RVitem.setHasFixedSize(true)

        itemList = arrayListOf<MenuModel>()
        getMenuItem()
    }

    private fun initView() {
        Out = findViewById(R.id.btnOut)
        Add = findViewById(R.id.btnAdd)
        RVitem = findViewById(R.id.RVItem)
    }
    private fun singout() {
        // Membuat AlertDialog
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.apply {
            setTitle("Logout")
            setMessage("Apakah Anda yakin ingin logout?")
            setPositiveButton("Ya", DialogInterface.OnClickListener { dialog, id ->
                // Melakukan sign out dari Firebase
                FirebaseAuth.getInstance().signOut()
                navigateToLoginPage()
                // Tambahkan tindakan lain sesuai kebutuhan setelah logout
            })
            setNegativeButton("Tidak", DialogInterface.OnClickListener { dialog, id ->
                // Menutup dialog tanpa melakukan tindakan apa pun
                dialog.dismiss()
            })
        }

        // Menampilkan AlertDialog
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private fun getMenuItem() {
        RVitem.visibility = View.GONE

        Fdatabase = FirebaseDatabase.getInstance().getReference("Menu")

        Fdatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                if (snapshot.exists()) {
                    for (menuSnap in snapshot.children) {
                        val menuData = menuSnap.getValue(MenuModel::class.java)
                       itemList.add(menuData!!)
                    }
                    val itemAdapter = ItemAdapter(itemList)
                    RVitem.adapter = itemAdapter

                    itemAdapter.setOnItemClickListener(object : ItemAdapter.onItemClickListener{
                        override fun onItemClick(position: Int) {
                            val intent = Intent(this@HomeActivity, DetailMenuActivity::class.java)
                            // Kirim data menu ke DetailMenuActivity
                            intent.putExtra("id", itemList[position].id)
                            intent.putExtra("judul", itemList[position].judul)
                            intent.putExtra("harga", itemList[position].harga)
                            intent.putExtra("deskripsi", itemList[position].deskripsi)
                            intent.putExtra("imageUrl", itemList[position].imageUrl)
                            startActivity(intent)
                        }
                    })

                    RVitem.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error jika baca data dari database gagal
            }

        })
    }

    private fun navigateToLoginPage() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}