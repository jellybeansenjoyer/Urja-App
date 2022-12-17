package com.example.urja.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.example.urja.R
import com.example.urja.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
   lateinit private var loginProgressLayout: LinearLayout
   lateinit private var emailTIL: TextInputLayout
   lateinit private var emailET: TextInputEditText
   lateinit private var mBinding: ActivityLoginBinding
   lateinit private var passwordTIL: TextInputLayout
   lateinit private var passwordET:  TextInputEditText
   private val firebaseAuth = FirebaseAuth.getInstance()

   private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
      val user = firebaseAuth.currentUser?.uid
      user?.let {
         startActivity(HomeActivity.newIntent(this))
      }
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_login)
      emailET = mBinding.emailET
      emailTIL = mBinding.emailTIL
      passwordET = mBinding.passwordET
      passwordTIL = mBinding.passwordTIL
      loginProgressLayout =mBinding.loginProgressLayout
      setTextChangeListener(emailET, emailTIL)
      setTextChangeListener(passwordET, passwordTIL)

      /* Progress layout must intercept all touches in the layout
      *  so user will only be able to click on the login button only
      *  ONCE while login() is processing authorisation.
      * */
      loginProgressLayout.setOnTouchListener { v, event -> true }
   }


   // Text-Watcher
   fun setTextChangeListener(et: TextInputEditText, til: TextInputLayout) {
      et.addTextChangedListener(object : TextWatcher {
         override fun afterTextChanged(s: Editable?) {
         }

         override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
         }

         /* when user starts typing, error message disappears. */
         override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            til.isErrorEnabled = false
         }

      })
   }

   // On-Login
   fun onLogin(view: View) {
      var proceed = true

      // Email Check
      if (emailET.text.isNullOrEmpty()) {
         emailTIL.error = "Email is required"
         emailTIL.isErrorEnabled = true
         proceed = false
      }

      // Password Check
      if (passwordET.text.isNullOrEmpty()) {
         passwordTIL.error = "Password is required"
         passwordTIL.isErrorEnabled = true
         proceed = false
      }

      // if Login proceeds
      if (proceed) {
         // show progressbar
         loginProgressLayout.visibility = View.VISIBLE

         // login with Firebase Auth
         firebaseAuth.signInWithEmailAndPassword(
            emailET.text.toString(),
            passwordET.text.toString()
         )
            .addOnCompleteListener {
               if (!it.isSuccessful) {
                  loginProgressLayout.visibility = View.GONE
                  Toast.makeText(
                     this@LoginActivity,
                     "Login Error: ${it.exception?.localizedMessage}",
                     Toast.LENGTH_LONG
                  ).show()
               }
            }
            .addOnFailureListener {
               it.printStackTrace()
               loginProgressLayout.visibility = View.GONE
            }
      }
   }


   // Go-to-Signup
   fun goToSignup(view: View) {
      startActivity(SignupActivity.newIntent(this))
      finish()
   }


   //--- Override METHODS:

   /* when LoginActivity starts up, we have to attach this to FirebaseAuth instance
   *  to check if user is already logged-in.
   * */
   override fun onStart() {
      super.onStart()

      // pass the firebaseAuthListener to see if user is still logged-in
      firebaseAuth.addAuthStateListener(firebaseAuthListener)
   }

   /* when screen starts up, we have to detach/remove  this from FirebaseAuth */
   override fun onStop() {
      super.onStop()
      firebaseAuth.removeAuthStateListener(firebaseAuthListener)
   }


   //--- Intent
   companion object {
      fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
   }
}
