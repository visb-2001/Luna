package com.example.luna.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luna.R
import com.example.luna.classes.Notes
import com.example.luna.database.NotesDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NotesAdapter(
    notes: List<Notes>,
    context: Context
) :
    RecyclerView.Adapter<NotesAdapter.CustomViewHolder>() {
    var notes:  MutableList<Notes> = notes as MutableList<Notes>
    var context: Context = context


    inner class CustomViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var title: TextView = itemView.findViewById(R.id.title)
        var extract: TextView = itemView.findViewById(R.id.description)
        var delete: ImageButton = itemView.findViewById(R.id.delete)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.note_layout
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(viewType, parent, false)
        )
    }

    override fun onBindViewHolder(
        holder: CustomViewHolder,
        position: Int
    ) {
        holder.title.text = notes[position].noteTitle
        holder.extract.text = notes[position].noteText
        holder.delete.setOnClickListener {
            var note = notes[holder.adapterPosition]
            notes.removeAt(holder.adapterPosition)
            notifyItemRemoved(holder.adapterPosition)

            Thread {
                Thread.sleep(100)
                NotesDatabase.getInstance(context).notesDao().deleteNote(note)
            }.start()

            val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
            ref.child("notes").setValue(notes)



        }
    }

    fun updateData(notesList: MutableList<Notes>){
        notes = notesList
        notifyDataSetChanged()
    }

}