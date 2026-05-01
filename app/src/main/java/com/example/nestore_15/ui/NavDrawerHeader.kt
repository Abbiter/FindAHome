package com.example.nestore_15.ui

import android.widget.TextView
import com.example.nestore_15.R
import com.example.nestore_15.data.session.SessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun NavigationView.bindPersonalizedDrawerHeader(
    scope: CoroutineScope,
    sessionManager: SessionManager
) {
    scope.launch {
        sessionManager.getCurrentUser().collectLatest { user ->
            val header = getHeaderView(0)
            val nameTv = header.findViewById<TextView>(R.id.tvUserName)
            val emailTv = header.findViewById<TextView>(R.id.tvEmail)
            val ctx = header.context
            if (user != null) {
                nameTv.text = ctx.getString(R.string.nav_welcome_named, user.greetingName())
                emailTv.text = user.email
            } else {
                nameTv.text = ctx.getString(R.string.nav_welcome_generic)
                emailTv.text = ""
            }
        }
    }
}
