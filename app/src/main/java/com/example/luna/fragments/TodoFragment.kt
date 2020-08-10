package com.example.luna.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.luna.R
import com.example.luna.adapters.NotesAdapter
import com.example.luna.adapters.TodoAdapter
import com.example.luna.classes.Notes
import com.example.luna.classes.Todo
import com.example.luna.database.NotesDatabase
import com.example.luna.database.TodoDatabase
import kotlinx.android.synthetic.main.fragment_note.*
import kotlinx.android.synthetic.main.fragment_todo.*


class   TodoFragment : Fragment() {

    var todoList = mutableListOf<Todo>()
    lateinit var todoAdapter : TodoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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


        context?.let {
            TodoDatabase.getInstance(it).todoDao().readTodo().observe(viewLifecycleOwner,
                Observer<List<Todo>> { todo -> // update the UI here
                    todoList = todo as MutableList<Todo>
                    todoAdapter.updateData(todoList)
                })
        }
    }

}