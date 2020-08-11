package com.example.luna.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.luna.MainActivity
import com.example.luna.R
import com.example.luna.classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_signup.*
import java.time.LocalDate

class SignupFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoginPage.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.left_to_right_enter,R.anim.left_to_right_exit)
                .replace(R.id.LoginFragmentContainer, LoginFragment.newInstance())
                .commit()
        }
        LoginPage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    LoginPage.paintFlags = Paint.FAKE_BOLD_TEXT_FLAG

                }
                MotionEvent.ACTION_UP -> {
                    LoginPage.paintFlags = Paint.ANTI_ALIAS_FLAG

                }
            }
            false
        }


        signup.setOnClickListener {
            val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            var email = signupEmail.text.toString()
            var password = signupPassword.text.toString()
            var username = signupUsername.text.toString()
            if(username.isEmpty() || email.isEmpty() || password.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(context, "Please enter valid details",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signup.visibility = View.INVISIBLE

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener
                    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    preferences.edit().putBoolean("First_Time",true).apply()
                    preferences.edit().putString("Current_Date",LocalDate.now().toString()).commit()
                    Log.d("user","usercreated")
                    val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                    if (uid != null) {
                        val user = User(uid, username)
                        ref.setValue(user)
                            .addOnSuccessListener {
                                val intent = Intent (activity, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                activity?.startActivity(intent)
                                activity?.overridePendingTransition(R.anim.fade_in,R.anim.fade_out)
                            }
                    }

                }
                .addOnFailureListener {
                    Log.d("error", it.message)
                    Toast.makeText(context, "Failed to create user",Toast.LENGTH_SHORT).show()
                    signup.visibility = View.VISIBLE
                }



        }
    }

    companion object {
        fun newInstance() = SignupFragment()
    }
}