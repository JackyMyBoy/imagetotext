package com.example.imagetotext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EditFileActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_file_main);

        Button saveBtn = (Button) findViewById(R.id.edit_save);
        Button cancelBtn = (Button) findViewById(R.id.edit_cancel);
        final EditText editText = (EditText) findViewById(R.id.editFileText);

        Intent intent = getIntent();
        final String text = intent.getStringExtra("EXTRA_TEXT");
        final String uri = intent.getStringExtra("EXTRA_URI");

        editText.setText(text);

        final String filename = uri.substring(uri.lastIndexOf('/')+1);

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editedText = editText.getText().toString();
                //Toast.makeText(getApplicationContext(),editedText,Toast.LENGTH_LONG).show();
                saveTextAsFile(filename,editedText);
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void saveTextAsFile(String filename, String content){
        String fileName = filename;
        //path
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator + "ImageToText" + File.separator + fileName;
        //create file
        File delFile = new File(path);
        delFile.delete();
        File file = new File(path);

            //write to file
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(content.getBytes());
                fos.close();
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "File not found", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error saving", Toast.LENGTH_SHORT).show();
            }
    }
}
