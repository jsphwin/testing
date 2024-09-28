package com.example.shoppingsapps
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : Activity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth





        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            Toast.makeText(baseContext, "welcome back   ",
                Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)


        }






        val register = findViewById<Button>(R.id.rgbtn)

        register.setOnClickListener {

            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }




        val adminLoginButton = findViewById<Button>(R.id.login_as_admin)

        adminLoginButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Enter Password")

            val input = EditText(this)
            // Set the input type to show the password
            input.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD

            builder.setView(input)

            builder.setPositiveButton("OK") { _, _ ->
                val enteredPassword = input.text.toString()
                if (enteredPassword == "appmelodies.com") {
                    // Password is correct, start Admin_Panel activity
                    val intent = Intent(this, Admin_Panel::class.java)
                    startActivity(intent)
                } else {
                    // Password is incorrect, you can show an error message or handle it accordingly
                    // For simplicity, we'll just show a toast message
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            builder.show()
        }




        val login = findViewById<Button>(R.id.loginbtn)

        login.setOnClickListener {

            val email = findViewById<EditText>(R.id.username)
            val pass = findViewById<EditText>(R.id.passwordd)

            val email1 = email.text.toString()
            val password = pass.text.toString()

            if (email.text.isEmpty() || pass.text.isEmpty()){
                Toast.makeText(baseContext, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener


            }



            auth.signInWithEmailAndPassword(email1, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(baseContext, "success",
                            Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(baseContext, "Authentication failed. ${it.localizedMessage}",
                        Toast.LENGTH_SHORT).show()
                }

        }


    }
}