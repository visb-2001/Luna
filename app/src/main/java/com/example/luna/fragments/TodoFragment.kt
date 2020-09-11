package com.example.luna.fragments

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luna.R
import com.example.luna.adapters.NotesAdapter
import com.example.luna.adapters.TodoAdapter
import com.example.luna.classes.Notes
import com.example.luna.classes.Todo
import com.example.luna.classes.User
import com.example.luna.database.NotesDatabase
import com.example.luna.database.TodoDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.android.synthetic.main.fragment_todo.*
import kotlinx.android.synthetic.main.note_popup_dialog.*
import kotlinx.android.synthetic.main.note_popup_dialog.inputText
import kotlinx.android.synthetic.main.note_popup_dialog.save
import kotlinx.android.synthetic.main.todo_popup_dialog.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class   TodoFragment : Fragment() {

    var todoList = mutableListOf<Todo>()
    lateinit var todoAdapter : TodoAdapter
    lateinit var todoPop: Dialog
    var firstTime = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        todoPop = Dialog(requireContext())
        todoPop.setContentView(R.layout.todo_popup_dialog)
        return inflater.inflate(R.layout.fragment_todo, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        todoListView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        todoAdapter =
            TodoAdapter(todoList, requireContext())
        todoListView.adapter = todoAdapter

        addTodo.setOnClickListener {
            todoPop.save.setOnClickListener {
                if(todoPop.inputText.text.toString().isNullOrEmpty()){
                    Toast.makeText(requireContext(),"Please enter a todo to add", Toast.LENGTH_SHORT).show()
                }
                else{
                    firstTime = true
                    var newTodo = Todo(
                        todoPop.inputText.text.toString(),
                        false,
                        0
                    )
                    Thread{
                        TodoDatabase.getInstance(
                            requireContext()
                        ).todoDao().saveTodo(newTodo)
                    }.start()

                    val uid = FirebaseAuth.getInstance().uid
                    val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            var tList = snapshot.getValue(User::class.java)
                            //Log.d("list",nList!!.notes!!.size.toString())
                            if(tList?.todo != null){
                                tList.todo!!.add(newTodo)
                                ref.child("todo").setValue(tList.todo)
                            }
                            else{
                                ref.child("todo").setValue(mutableListOf(newTodo))
                            }
                        }

                    })
                    todoPop.inputText.setText("")
                    todoPop.cancel()
                }
            }
            todoPop.show()
        }


        context?.let {
            TodoDatabase.getInstance(it).todoDao().readTodo().observe(viewLifecycleOwner,
                Observer<List<Todo>> { todo -> // update the UI here
                    todoList = todo as MutableList<Todo>
                    todoAdapter.updateData(todoList)
                    if(firstTime){
                        todoAdapter.notifyDataSetChanged()
                        firstTime = false
                    }
                })
        }
    }

}