package com.example.imagetotext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveFileActivity extends Activity {
    private String restart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_save_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.8),(int)(height*.4));

        ImageButton saveBtn = findViewById(R.id.button_save);
        ImageButton cancelBtn = findViewById(R.id.button_cancel);
        final EditText srcText = (EditText) findViewById(R.id.fileName);

        Intent intent = getIntent();
        final String text = intent.getStringExtra("EXTRA_TEXT");
        restart = intent.getStringExtra("RESTART");
        final String oldFileName = intent.getStringExtra("FILE_NAME");

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(restart!=null){
                    String filename = srcText.getText().toString();
                    File delFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator + "ImageToText" + File.separator + oldFileName);
                    delFile.delete();
                    saveTextAsFile(filename, text);
                    Intent intent = new Intent(getApplicationContext(), FileManagerActivity.class);
                    startActivity(intent);
                }else {
                    String filename = srcText.getText().toString();
                    saveTextAsFile(filename, text);
                    finish();
                }
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
        String fileName = filename + ".txt";
        //path
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+ File.separator + "ImageToText" + File.separator + fileName;
        //create file
        File file = new File(path);
        //write to file
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content.getBytes());
            fos.close();
            Toast.makeText(this,"Saved",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this,"File not found",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"Error saving",Toast.LENGTH_SHORT).show();
        }
    }
}
