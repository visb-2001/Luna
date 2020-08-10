package com.example.luna

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.luna.fragments.LoginFragment
import java.lang.reflect.Array.newInstance

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportFragmentManager.beginTransaction()
            .replace(R.id.LoginFragmentContainer, LoginFragment.newInstance())
            .commit()

    }
}