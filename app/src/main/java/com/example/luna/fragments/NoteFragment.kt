package com.example.luna.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luna.classes.Notes
import com.example.luna.adapters.NotesAdapter
import com.example.luna.R
import com.example.luna.classes.User
import com.example.luna.database.NotesDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.android.synthetic.main.note_popup_dialog.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class NoteFragment : Fragment() {

    var notesList = mutableListOf<Notes>()
    lateinit var notesAdapter : NotesAdapter
    lateinit var notePop: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        notePop = Dialog(requireContext())
        notePop.setContentView(R.layout.note_popup_dialog)
        return inflater.inflate(R.layout.fragment_note, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

        addNote.setOnClickListener {
            notePop.save.setOnClickListener {
                if(notePop.inputText.text.toString().isNullOrEmpty()){
                    Toast.makeText(requireContext(),"Please enter a note to add",Toast.LENGTH_SHORT).show()
                }
                else{
                    var currentDate = LocalDate.now()
                    var formatter = DateTimeFormatter.ofPattern("dd-MM")
                    var newNote = Notes(
                        notePop.inputText.text.toString(),
                        currentDate.format(formatter)
                    )
                    Thread{
                        NotesDatabase.getInstance(
                            requireContext()
                        ).notesDao().saveNote(newNote)
                    }.start()


                    val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            var nList = snapshot.getValue(User::class.java)
                            //Log.d("list",nList!!.notes!!.size.toString())
                            if(nList?.notes != null){
                                nList.notes!!.add(newNote)
                                ref.child("notes").setValue(nList.notes)
                            }
                            else{
                                ref.child("notes").setValue(mutableListOf(newNote))
                            }
                        }

                    })
                    notePop.inputText.setText("")
                    notePop.cancel()
                }
            }
            notePop.show()
        }


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