package com.example.ibusiti.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.ibusiti.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {

    private lateinit var btnDaftar      : Button
    private lateinit var tvmasuk        : TextView
    private lateinit var inputEmail     : EditText
    private lateinit var inputPass      : EditText
    private lateinit var inputConfirm   : EditText

    private lateinit var  fAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fAuth = FirebaseAuth.getInstance()

        initView()

       tvmasuk.setOnClickListener {
           val intent = Intent(this, LoginActivity::class.java)
           startActivity(intent)
       }

        btnDaftar.setOnClickListener{
            registerUser()
        }

    }

    private fun registerUser() {
        val email: String = inputEmail.text.toString().trim()
        val pass: String =inputPass.text.toString().trim()
        val confirm: String = inputConfirm.text.toString().trim()

        if (email.isEmpty()) {
            showErrorSnackbar("Silahkan mengisi alamat email", android.R.drawable.ic_dialog_alert)
            inputEmail.requestFocus()
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorSnackbar("Masukkan Alamat Email yang valid", android.R.drawable.ic_dialog_alert)
            inputEmail.requestFocus()
            return
        }

        if (pass.isEmpty() || pass.length < 8) {
            showErrorSnackbar("Maksimal 8 karakter dan Tidak boleh kosong", android.R.drawable.ic_dialog_alert)
           inputPass.requestFocus()
        } else if (pass != confirm) {
            showErrorSnackbar("Password tidak sama !!!", android.R.drawable.ic_dialog_alert)
           inputConfirm.requestFocus()
            return
        }
        registerUser(email, pass)
    }

    private fun initView() {
        btnDaftar = findViewById(R.id.btnDaftar)
        tvmasuk = findViewById(R.id.tvmasuk)
        inputEmail = findViewById(R.id.inputRemail)
        inputPass = findViewById(R.id.inputRpass)
        inputConfirm = findViewById(R.id.inputRconfirm)
    }

    private fun registerUser(email: String, pass: String) {
        fAuth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    if (result != null && result.signInMethods != null && result.signInMethods!!.isNotEmpty()) {
                        showErrorSnackbar("Alamat email sudah terdaftar.", android.R.drawable.ic_dialog_alert)
                    } else {
                        fAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { registrationTask ->
                            if (registrationTask.isSuccessful) {
                                Toast.makeText(applicationContext, "Berhasil Mendaftar, Silahkan Login !!!", Toast.LENGTH_SHORT).show()
                                Intent(this, LoginActivity::class.java).also{
                                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(it)
                                }
                                finish()
                            } else {
                                val errorMessage = registrationTask.exception?.message ?: "Terjadi kesalahan saat mendaftar"
                                showErrorSnackbar(errorMessage, android.R.drawable.ic_dialog_alert)
                            }
                        }
                    }
                } else {
                    // Terjadi kesalahan saat memeriksa email
                    showErrorSnackbar("Terjadi kesalahan saat memeriksa email.", android.R.drawable.ic_dialog_alert)
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