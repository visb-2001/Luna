package com.example.luna.adapters

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.luna.R
import com.example.luna.classes.Notes
import com.example.luna.classes.Todo
import com.example.luna.database.NotesDatabase
import com.example.luna.database.TodoDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class TodoAdapter(
    todo: List<Todo>,
    context: Context
) :
    RecyclerView.Adapter<TodoAdapter.CustomViewHolder>() {
    var todo:  MutableList<Todo> = todo as MutableList<Todo>
    var context: Context = context


    inner class CustomViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var todoText: TextView = itemView.findViewById(R.id.todoText)
        var box: CheckBox = itemView.findViewById(R.id.checkBox)
        var persist: Switch = itemView.findViewById(R.id.persist)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.todo_layout
    }

    override fun getItemCount(): Int {
        return todo.size
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

        holder.todoText.text = todo[position].todoText
        if(todo[position].isChecked){
            holder.box.isChecked = true
            holder.todoText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
           // var tempTodo = todo[position]
           // todo.removeAt(position)
           // todo.add(tempTodo)
            //notifyDataSetChanged()
        }

        holder.box.setOnClickListener {
            var currentPos = holder.adapterPosition
            if(holder.box.isChecked && !todo[currentPos].isChecked){
                todo[currentPos].isChecked = true
                holder.todoText.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                var tempTodo = todo[currentPos]
                tempTodo.position = todo.last().position+1
                todo.removeAt(currentPos)
                todo.add(tempTodo)
                notifyItemMoved(currentPos,todo.lastIndex)
                Thread {
                    Thread.sleep(500)
                    TodoDatabase.getInstance(context).todoDao().updateTodo(todo.last())
                    Log.d("heylo",todo.last().todoText + " " +todo.last().isChecked.toString()+ " " +todo.last().position.toString())
                }.start()
                val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
                ref.child("todo").setValue(todo)

            }
            else if(!holder.box.isChecked){
                holder.todoText.paintFlags = Paint.ANTI_ALIAS_FLAG
                todo[currentPos].isChecked = false
                var tempTodo = todo[currentPos]
                tempTodo.position = todo[0].position-1
                todo.removeAt(currentPos)
                todo.add(0,tempTodo)
                notifyItemMoved(currentPos,0)
                Thread {
                    Thread.sleep(500)
                    TodoDatabase.getInstance(context).todoDao().updateTodo(todo[0])
                    Log.d("heylo",todo.last().todoText + " " +todo.last().isChecked.toString()+ " " +todo.last().position.toString())
                }.start()
                val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
                ref.child("todo").setValue(todo)
            }

        }

        holder.persist.setOnCheckedChangeListener { buttonView, isChecked ->
            var currentPos = holder.adapterPosition
            todo[currentPos].persist = isChecked
            Thread {
                Thread.sleep(500)
                TodoDatabase.getInstance(context).todoDao().updateTodo(todo[currentPos])
                Log.d("heylo",todo.last().todoText + " " +todo.last().isChecked.toString()+ " " +todo.last().position.toString())
            }.start()
            val ref = FirebaseDatabase.getInstance().getReference("/users/${FirebaseAuth.getInstance().uid}")
            ref.child("todo").setValue(todo)
        }
    }

    fun updateData(todoList: MutableList<Todo>){
        todo = todoList
        notifyDataSetChanged()
    }

}