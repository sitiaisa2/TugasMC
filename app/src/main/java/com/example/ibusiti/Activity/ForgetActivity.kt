package com.example.ibusiti.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.ibusiti.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ForgetActivity : AppCompatActivity() {

    private lateinit var btnReset: Button
    private lateinit var inputEmail: EditText
    private lateinit var btnback : LinearLayout
    private lateinit var Fauth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget)

        Fauth = FirebaseAuth.getInstance()

        initView()

        btnReset.setOnClickListener {
            resetpass()
        }

        btnback.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun initView() {
        inputEmail = findViewById(R.id.inputFemail)
        btnReset = findViewById(R.id.btnReset)
        btnback = findViewById(R.id.tvback)
    }

    private fun resetpass() {
            val email: String = inputEmail.text.toString().trim()

            if (email.isEmpty()) {
                showErrorSnackbar("Email Tidak Boleh Kosong", android.R.drawable.ic_dialog_alert)
                inputEmail.requestFocus()
                return
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showErrorSnackbar("Email Tidak Valid", android.R.drawable.ic_dialog_alert)
                inputEmail.requestFocus()
                return
            }else{
                Fauth.sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(applicationContext, "Cek Email Anda !!!", Toast.LENGTH_SHORT).show()
                        Intent(this, LoginActivity::class.java).also {
                            it.flags = Intent.FLAG_ACTIVITY_TASK_ON_HOME or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(it)
                        }
                    } else {
//               Pesan apabila kondisi diatas tidak terpenuhi
                        Toast.makeText(applicationContext, "Terjadi Kesalahan Saat Mereset Password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }


    private fun showErrorSnackbar(message: String, iconResId: Int) {
        val rootView = findViewById<View>(android.R.id.content)
        val snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT)
        val snackbarView = snackbar.view
        val drawable = ContextCompat.getDrawable(this, iconResId)
        drawable?.setBounds(0, 0, drawable?.intrinsicWidth ?: 0, drawable?.intrinsicHeight ?: 0)
        snackbarView.setPadding(0, 0, 0, 0)

        val textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
        textView.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen.snackbar_spasing)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        textView.gravity = Gravity.CENTER_VERTICAL
        snackbar.show()
    }
}