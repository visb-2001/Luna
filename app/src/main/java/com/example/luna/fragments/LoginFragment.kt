package com.example.luna.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.luna.MainActivity
import com.example.luna.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_signup.*
import java.time.LocalDate


class LoginFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SignupPage.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().setCustomAnimations(R.anim.right_to_left_enter,R.anim.right_to_left_exit)
                .replace(R.id.LoginFragmentContainer, SignupFragment.newInstance())
                .commit()
        }
        SignupPage.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    SignupPage.paintFlags = Paint.FAKE_BOLD_TEXT_FLAG

                }
                MotionEvent.ACTION_UP -> {
                    SignupPage.paintFlags = Paint.ANTI_ALIAS_FLAG

                }
            }
            false
        }

        login.setOnClickListener {
            val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            var email = loginEmail.text.toString()
            var password = loginPassword.text.toString()
            if(email.isEmpty() || password.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(context, "Please enter valid details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            login.visibility = View.INVISIBLE

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener

                    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    preferences.edit().putBoolean("First_Time",false).apply()
                    preferences.edit().putString("Current_Date", LocalDate.now().toString()).apply()
                    val intent = Intent (activity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity?.startActivity(intent)
                    activity?.overridePendingTransition(R.anim.fade_in,R.anim.fade_out)


                    Log.d("user","user signin")
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to login user\nCheck credentials",Toast.LENGTH_SHORT).show()
                    login.visibility = View.VISIBLE
                }
        }

    }

    companion object {
        fun newInstance() = LoginFragment()
    }
}