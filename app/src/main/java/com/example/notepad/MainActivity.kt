@file:Suppress("DEPRECATION")
package com.example.notepad


//import android.app.ActionBar
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
//import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.io.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menu?.add("New")
        menu?.add("Open")
        menu?.add("Save")
        menu?.add("Save as")
        menu?.add("Exit")
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val title: String = item.title.toString()
        when(title){
            "New"-> isModified("New")
            "Open"-> isModified("Open")
            "Save"-> createFile("Save")
            "Save as"-> createFile("Save as")
            "Exit"-> end()
        }

        return super.onOptionsItemSelected(item)
    }



    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        startActivityForResult(intent, OPEN_FILE)
    }


    private var pub_uri:Uri? = null


    private var doc_cont:StringBuilder = java.lang.StringBuilder()

    private fun createFile(string: String) {
        var string1:String = string
        if(string1 =="Save"&& pub_uri ==null) {string1 = "Save as"}
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"

                putExtra(Intent.EXTRA_TITLE, "invoice.txt")

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, "/Download")
        }
        when(string1){"New"->startActivityForResult(intent, NEW_FILE)
                     "Save"-> justSave(pub_uri)
        "Save as"-> startActivityForResult(intent, SAVE_FILE)}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val docuri: Uri? = data?.data
        pub_uri = docuri
        val textbox:TextInputEditText = findViewById(R.id.textbox)
        if(resultCode == Activity.RESULT_OK && docuri!=null){
        if (requestCode == SAVE_FILE || requestCode== NEW_FILE) {
            if(requestCode == NEW_FILE) {textbox.setText("")}
            val temparr: ByteArray = textbox.text.toString().toByteArray()
                try {
                    contentResolver.openFileDescriptor(docuri, "w")?.use {
                        FileOutputStream(it.fileDescriptor).use {
                            it.write(temparr)
                        }
                    }
                    doc_cont.clear()
                    doc_cont.append(textbox.text)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

        }
        if(requestCode == OPEN_FILE ){
            val contentResolver = applicationContext.contentResolver
            val stringBuilder: StringBuilder = java.lang.StringBuilder()

            contentResolver.openInputStream(docuri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line+"\n")
                        line = reader.readLine()
                    }
                }
            }
            doc_cont = stringBuilder
            val temp: CharArray = stringBuilder.toString().toCharArray()
            textbox.setText(temp,0,temp.size)
        }}

    }
    private fun justSave(uri: Uri?){
        val textbox : TextInputEditText? = findViewById(R.id.textbox)
        val temparr: ByteArray = textbox?.text.toString().toByteArray()
        if (uri != null)
        {
            try {
                contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { it.write(temparr)}
                    }
                Toast.makeText(this, "File saved successful", Toast.LENGTH_SHORT).show()
                doc_cont.clear()
                doc_cont.append( textbox.toString())
                }
            catch (e: FileNotFoundException) {e.printStackTrace()}
            catch (e: IOException) {e.printStackTrace()}
        }

    }

    private fun isModified(s: String) {
        val tbox: TextInputEditText? = findViewById(R.id.textbox)
        if(!tbox?.text.contentEquals(doc_cont.toString())){
            val dialbuild : AlertDialog.Builder = this.let{
                AlertDialog.Builder(it)
            }
            dialbuild.apply { setPositiveButton(R.string.dialog_save, DialogInterface.OnClickListener({dialog: DialogInterface, which: Int -> createFile("Save")
                when(s){"New" -> createFile(s) "Open"->openFile() }}))
                            setNegativeButton(R.string.dialog_unsave, DialogInterface.OnClickListener ({dialog: DialogInterface, which: Int -> if(s == "New"){createFile("New")}
                                else if(s == "Open"){openFile()}
                            }))}
            dialbuild.setMessage(R.string.dialog_msg)?.setTitle(R.string.dialog_title)
            dialbuild.show()
        }
        else{when(s){"New" -> createFile(s) "Open"->openFile() }}
    }

    private fun end(){
        super.finish()
    }
    companion object {
        // Request code for creating a PDF document.
        const val SAVE_FILE = 1
        const val OPEN_FILE = 2
        const val NEW_FILE =3
    }



}
