package com.fake.mzansilingo

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(
    private val users: List<LeaderboardUser>
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.card_user)
        private val tvRank: TextView = itemView.findViewById(R.id.tv_rank)
        private val tvUsername: TextView = itemView.findViewById(R.id.tv_username)
        private val tvScore: TextView = itemView.findViewById(R.id.tv_score)
        private val tvWordsCorrect: TextView = itemView.findViewById(R.id.tv_words_correct)
        private val tvPhrasesCorrect: TextView = itemView.findViewById(R.id.tv_phrases_correct)
        private val tvAccuracy: TextView = itemView.findViewById(R.id.tv_accuracy)
        private val ivTrophy: ImageView = itemView.findViewById(R.id.iv_trophy)
        private val ivMedal: ImageView = itemView.findViewById(R.id.iv_medal)

        fun bind(user: LeaderboardUser) {
            // Set rank
            tvRank.text = "#${user.rank}"

            // Set username (truncate if too long)
            val displayName = if (user.username.length > 15) {
                "${user.username.take(12)}..."
            } else {
                user.username
            }
            tvUsername.text = displayName

            // Set total score
            tvScore.text = "${user.totalScore} pts"

            // Set individual scores
            tvWordsCorrect.text = "Words: ${user.wordsCorrect}"
            tvPhrasesCorrect.text = "Phrases: ${user.phrasesCorrect}"

            // Set accuracy
            val accuracy = user.getAccuracy()
            tvAccuracy.text = "${String.format("%.1f", accuracy)}%"

            // Handle top 3 rankings with special styling
            when (user.rank) {
                1 -> {
                    // Gold - First place
                    cardView.setCardBackgroundColor(Color.parseColor("#FFD700"))
                    tvRank.setTextColor(Color.parseColor("#B8860B"))
                    tvUsername.setTextColor(Color.parseColor("#B8860B"))
                    tvScore.setTextColor(Color.parseColor("#B8860B"))
                    ivTrophy.visibility = View.VISIBLE
                    ivMedal.visibility = View.GONE
                    ivTrophy.setImageResource(R.drawable.ic_trophy)
                    ivTrophy.setColorFilter(Color.parseColor("#B8860B"))
                }
                2 -> {
                    // Silver - Second place
                    cardView.setCardBackgroundColor(Color.parseColor("#C0C0C0"))
                    tvRank.setTextColor(Color.parseColor("#708090"))
                    tvUsername.setTextColor(Color.parseColor("#708090"))
                    tvScore.setTextColor(Color.parseColor("#708090"))
                    ivTrophy.visibility = View.GONE
                    ivMedal.visibility = View.VISIBLE
                    ivMedal.setImageResource(R.drawable.ic_medal)
                    ivMedal.setColorFilter(Color.parseColor("#708090"))
                }
                3 -> {
                    // Bronze - Third place
                    cardView.setCardBackgroundColor(Color.parseColor("#CD7F32"))
                    tvRank.setTextColor(Color.parseColor("#8B4513"))
                    tvUsername.setTextColor(Color.parseColor("#8B4513"))
                    tvScore.setTextColor(Color.parseColor("#8B4513"))
                    ivTrophy.visibility = View.GONE
                    ivMedal.visibility = View.VISIBLE
                    ivMedal.setImageResource(R.drawable.ic_medal)
                    ivMedal.setColorFilter(Color.parseColor("#8B4513"))
                }
                else -> {
                    // Regular styling for other ranks
                    cardView.setCardBackgroundColor(Color.WHITE)
                    val textColor = ContextCompat.getColor(itemView.context, R.color.mz_navy_dark)
                    tvRank.setTextColor(textColor)
                    tvUsername.setTextColor(textColor)
                    tvScore.setTextColor(textColor)
                    ivTrophy.visibility = View.GONE
                    ivMedal.visibility = View.GONE
                }
            }

            // Set other text colors for details
            val detailTextColor = when (user.rank) {
                1 -> Color.parseColor("#B8860B")
                2 -> Color.parseColor("#708090")
                3 -> Color.parseColor("#8B4513")
                else -> ContextCompat.getColor(itemView.context, R.color.mz_navy_dark)
            }

            tvWordsCorrect.setTextColor(detailTextColor)
            tvPhrasesCorrect.setTextColor(detailTextColor)
            tvAccuracy.setTextColor(detailTextColor)

            // Add elevation for top 3
            if (user.rank <= 3) {
                cardView.cardElevation = 8f
            } else {
                cardView.cardElevation = 2f
            }
        }
    }
}