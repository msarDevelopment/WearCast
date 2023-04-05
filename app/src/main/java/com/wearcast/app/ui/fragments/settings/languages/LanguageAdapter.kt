package com.wearcast.app.ui.fragments.settings.languages

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.wearcast.app.Language
import com.wearcast.app.MainActivity
import com.wearcast.app.R

class LanguageAdapter : RecyclerView.Adapter<LanguageAdapter.LanguageItemViewHolder>(){

    var languages = emptyList<Language>()
    private lateinit var typeface: Typeface
    private lateinit var context: Context

    inner class LanguageItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLanguageName: TextView = itemView.findViewById(R.id.tvItemName)
        val ivSelectedLanguageCheckmark: ImageView = itemView.findViewById(R.id.ivItemIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageItemViewHolder {
        context = parent.context
        typeface = ResourcesCompat.getFont(context, R.font.poppins_medium)!!
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_name_and_icon, parent, false)
        return LanguageItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageItemViewHolder, position: Int) {

        val currentItem = languages[position]

        holder.apply {
            tvLanguageName.text = currentItem.name
            tvLanguageName.typeface = typeface
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val language = sp.getString("app_language", "en")
            val edit = sp.edit()

            ivSelectedLanguageCheckmark.setImageResource(R.drawable.ic_checkmark)

            if(language == currentItem.short) {
                ivSelectedLanguageCheckmark.visibility = View.VISIBLE
            }
            else {
                ivSelectedLanguageCheckmark.visibility = View.INVISIBLE
            }


            itemView.setOnClickListener {

                if(language == currentItem.short) {
                    return@setOnClickListener
                }
                else {
                    edit.putString("app_language", currentItem.short)
                    edit.commit()

                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)

                }

            }
        }

    }

    override fun getItemCount(): Int {
        return languages.size
    }

    fun setData(languages: List<Language>) {
        this.languages = languages
        notifyDataSetChanged()
    }
}