package com.example.authenticationlearn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.auth.auth
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    var contactBox : EditText? = null

    var otpBox : EditText? = null

    var loginBtn : Button? = null

    var forgotPass : TextView? = null

    var sendOtpBtn : TextView? = null

    var googleBtn : Button? = null

    var registerBtn : TextView? = null

    private var storedVerificationId: String? = ""

    private lateinit var auth : FirebaseAuth

    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken

    private lateinit var callbacks: OnVerificationStateChangedCallbacks



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //initialising all values
        contactBox = findViewById(R.id.emailorphn)
        otpBox = findViewById(R.id.otpbox)
        loginBtn = findViewById(R.id.lgbutton)
        forgotPass = findViewById(R.id.forgotpassbtn)
        sendOtpBtn = findViewById(R.id.sendotpbtn)
        googleBtn = findViewById(R.id.lggooglebtn)
        registerBtn = findViewById(R.id.registerbtn)

        //declaring register button functionality
        registerBtn?.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        //Initialising Firebase
        auth = Firebase.auth

        //Initialize phone auth callback
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                Log.w(TAG,"onVerificationFailed",e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this@LoginActivity, "Invalid Request", Toast.LENGTH_SHORT).show()
                } else if (e is FirebaseTooManyRequestsException) {
                    Toast.makeText(this@LoginActivity, "The Sms Quota has been exceeded", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                Toast.makeText(this@LoginActivity, "Enter otp Sent", Toast.LENGTH_SHORT).show()
                Log.d(TAG,"onCodeSent:$verificationId")

                //Save Verification id and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }

            //End of callbacks
        }


    }

    //checking user on start
    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }


    private fun startPhoneNumberVerification(phoneNumber: String){
        //start phone auth
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
        //end phone auth
    }

    private fun verifyPhoneNumberWithCode(verificationId : String?, code : String) {
        //start code verification
        val credential = PhoneAuthProvider.getCredential(verificationId!!,code)
    }

    // [START resend_verification]
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?,
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // (optional) Activity for callback binding
            // If no activity is passed, reCAPTCHA verification can not be used.
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }
    // [END resend_verification]




    //start sign in with phone
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //sign in successfull, update ui with the signed in user's information
                    Log.d(TAG,"SigninWithCredential:success")

                    val user = task.result?.user
                } else {
                    //sign in failed , display a message and updatee the ui
                    Log.w(TAG,"signInWithCredential:failure",task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException){
                        //The verification code entered was incorrect
                    }
                    //update ui
                }
            }
    }

    private fun updateUI(user: FirebaseUser? = auth.currentUser){
    }

    companion object {
        private const val TAG = "PhoneAuthActivity"
    }

}