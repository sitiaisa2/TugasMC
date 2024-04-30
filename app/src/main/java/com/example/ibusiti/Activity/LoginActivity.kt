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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private  lateinit var tvDaftar : TextView
    private  lateinit var btnMasuk : Button
    private  lateinit var tvForget : TextView
    private  lateinit var inputEmail : EditText
    private  lateinit var inputPass : EditText

    private lateinit var fAuth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        fAuth = FirebaseAuth.getInstance()

        initView()

        tvDaftar.setOnClickListener {
            val intent = Intent (this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvForget.setOnClickListener {
            val intent = Intent (this, ForgetActivity::class.java)
            startActivity(intent)
        }

        btnMasuk.setOnClickListener {
            Flogin()
        }

    }

    private fun initView() {
        tvDaftar = findViewById(R.id.tvDaftar)
        btnMasuk = findViewById(R.id.btnMasuk)
        tvForget = findViewById(R.id.tvforget)
        inputEmail = findViewById(R.id.inputLemail)
        inputPass = findViewById(R.id.inputLpass)
    }

    private fun Flogin() {
        val email: String = inputEmail.text.toString().trim()
        val pass: String = inputPass.text.toString().trim()

        if (email.isEmpty()) {
            showErrorSnackbar("Email Tidak Boleh Kosong", android.R.drawable.ic_dialog_alert)
            inputEmail.requestFocus()
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorSnackbar("Email Tidak Valid", android.R.drawable.ic_dialog_alert)
            inputEmail.requestFocus()
            return
        } else if (pass.isEmpty() || pass.length < 8) {
            showErrorSnackbar("Maksimal 8 karakter dan Tidak boleh kosong", android.R.drawable.ic_dialog_alert)
            inputPass.requestFocus()
            return
        } else {
            loginUser(email, pass)
        }
    }

    private fun loginUser(email: String, pass: String) {
        fAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(applicationContext, "Selamat Datang", Toast.LENGTH_SHORT).show()
                Intent(this, HomeActivity::class.java).also{
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                }
            } else {
                val exception = task.exception
                if (exception is FirebaseAuthInvalidUserException || exception is FirebaseAuthInvalidCredentialsException) {
                    showErrorSnackbar("Email atau kata sandi salah !!!", android.R.drawable.ic_dialog_alert)
                } else if (exception is FirebaseAuthInvalidCredentialsException) {
                    showErrorSnackbar("Registrasi terlebih dahulu !!!", android.R.drawable.ic_dialog_alert)
                } else {
                    showErrorSnackbar("Terjadi kesalahan saat login !!!", android.R.drawable.ic_dialog_alert)
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