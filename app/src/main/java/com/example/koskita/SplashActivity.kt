package com.example.koskita

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import com.example.koskita.data.FirebaseUserRepository
import com.example.koskita.databinding.ActivitySplashBinding

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bounceAnim = AnimationUtils.loadAnimation(this, R.anim.bounce_in)
        binding.tvSplashIcon.startAnimation(bounceAnim)

        val fadeAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        fadeAnim.startOffset = 300
        binding.tvSplashTitle.startAnimation(fadeAnim)
        binding.tvSplashSubtitle.startAnimation(fadeAnim)

        Handler(Looper.getMainLooper()).postDelayed({
            if (FirebaseUserRepository.isLoggedIn()) {
                FirebaseUserRepository.getCurrentUser(
                    onSuccess = { user ->
                        val intent = Intent(this, DashboardActivity::class.java)
                        intent.putExtra("nama",  user.nama)
                        intent.putExtra("email", user.email)
                        intent.putExtra("role",  user.role)
                        startActivity(intent)
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    },
                    onError = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                    }
                )
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }, 2500)
    }
}