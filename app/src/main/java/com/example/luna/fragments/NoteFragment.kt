package com.example.luna.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luna.classes.Notes
import com.example.luna.adapters.NotesAdapter
import com.example.luna.R
import com.example.luna.database.NotesDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_note.*


class NoteFragment : Fragment() {

    var notesList = mutableListOf<Notes>()
    lateinit var notesAdapter : NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        notesListView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        notesAdapter =
            NotesAdapter(notesList, requireContext())
        notesListView.adapter = notesAdapter


        context?.let {
            NotesDatabase.getInstance(it).notesDao().readNotes().observe(viewLifecycleOwner,
                Observer<List<Notes>> { notes -> // update the UI here
                    notesList = notes as MutableList<Notes>
                    /*val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid").child("notes")
                    ref.setValue(notesList)*/
                    notesAdapter.updateData(notesList)
                })
        }
    }

}