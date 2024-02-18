package com.smart.server

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText

class CustomDialog(private val activity: Activity) {
    private val dialog = Dialog(activity)

    fun myDig(){
        dialog.setContentView(R.layout.activity_dialog)

        val btnDone = dialog.findViewById<Button>(R.id.btnDone)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
//        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        btnDone.setOnClickListener{
            (activity as Map).cancelGet()
            (activity as Map).getCode()

        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
            (activity as Map).cancelHandler()
            val intent = Intent(activity, Room::class.java)
            activity.startActivity(intent)
        }

        dialog.show()
    }

}