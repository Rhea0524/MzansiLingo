package com.fake.mzansilingo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.fake.mzansilingo.databinding.DialogWelcomeBackBinding

class WelcomeBackDialogFragment : DialogFragment() {

    private var _binding: DialogWelcomeBackBinding? = null
    private val binding get() = _binding!!

    private var streakData: GamificationManager.StreakData? = null
    private var onDismissCallback: (() -> Unit)? = null

    companion object {
        fun newInstance(
            streakData: GamificationManager.StreakData,
            onDismiss: () -> Unit = {}
        ): WelcomeBackDialogFragment {
            return WelcomeBackDialogFragment().apply {
                this.streakData = streakData
                this.onDismissCallback = onDismiss
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogWelcomeBackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        streakData?.let { data ->
            setupDialog(data)
        }

        binding.btnContinue.setOnClickListener {
            // Mark welcome as shown
            context?.let { ctx ->
                GamificationManager(ctx).markWelcomeShown()
            }

            onDismissCallback?.invoke()
            dismiss()
        }
    }

    private fun setupDialog(data: GamificationManager.StreakData) {
        // Set motivational message
        val gamificationManager = context?.let { GamificationManager(it) }
        val message = gamificationManager?.getMotivationalMessage(data.currentStreak)
            ?: "Welcome back to your learning journey!"

        val formattedMessage = formatMessage(message, data.currentStreak)
        binding.tvMotivationMessage.text = formattedMessage

        // Set up streak stars
        setupStreakStars(data.currentStreak)

        // Add some entrance animation
        animateEntrance()
    }

    private fun formatMessage(baseMessage: String, streak: Int): String {
        return when {
            streak == 1 -> "GREAT START!\nYOU'VE BEGUN YOUR JOURNEY!"
            streak in 2..6 -> "WELL DONE!\nYOU NOW HAVE A $streak DAY STREAK!"
            streak in 7..13 -> "FANTASTIC!\nYOU'RE BUILDING A GREAT HABIT!"
            streak in 14..29 -> "AMAZING!\n$streak DAYS OF DEDICATION!"
            streak >= 30 -> "INCREDIBLE!\nYOU'RE A CHAMPION - $streak DAYS!"
            else -> "WELCOME BACK!\nREADY TO CONTINUE LEARNING?"
        }
    }

    private fun setupStreakStars(streak: Int) {
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )

        val filledStars = minOf(5, maxOf(1, (streak + 2) / 3))

        stars.forEachIndexed { index, star ->
            if (index < filledStars) {
                star.setImageResource(R.drawable.ic_star_filled)
                star.setColorFilter(ContextCompat.getColor(requireContext(), R.color.mz_accent_yellow))
                star.alpha = 1.0f
            } else {
                star.setImageResource(R.drawable.ic_star_outline)
                star.setColorFilter(ContextCompat.getColor(requireContext(), R.color.mz_white))
                star.alpha = 0.6f
            }
        }
    }

    private fun animateEntrance() {
        // Animate top star
        binding.imgTopStar.apply {
            scaleX = 0f
            scaleY = 0f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .start()
        }

        // Animate title
        binding.tvWelcomeTitle.apply {
            alpha = 0f
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .start()
        }

        // Animate message
        binding.tvMotivationMessage.apply {
            alpha = 0f
            animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(400)
                .start()
        }

        // Animate stars one by one
        val stars = listOf(
            binding.star1,
            binding.star2,
            binding.star3,
            binding.star4,
            binding.star5
        )

        stars.forEachIndexed { index, star ->
            star.apply {
                scaleX = 0f
                scaleY = 0f
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setStartDelay(600L + index * 100L)
                    .start()
            }
        }

        // Animate button
        binding.btnContinue.apply {
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f
            animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(1200)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}