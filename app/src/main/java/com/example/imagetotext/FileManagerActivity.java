package com.example.imagetotext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileManagerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    String moveFileText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager_main);

        drawerLayout = findViewById(R.id.fileManagerDrawerLayout);
        navigationView = findViewById(R.id.fileManagerNavView);

        toolbar = (Toolbar) findViewById(R.id.fileManagerToolbar);
        setSupportActionBar(toolbar);

        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    class TextAdapter extends BaseAdapter{

        private List<String> data = new ArrayList<>();

        private boolean[] selection;

        public void setData(List<String> data){
            if(data!=null){
                this.data.clear();
                if(data.size() > 0){
                    this.data.addAll(data);
                }
                notifyDataSetChanged();
            }
        }

        void setSelection(boolean[] selection){
            if(selection!=null){
                this.selection = new boolean[selection.length];
                for (int i=0;i<selection.length;i++){
                    this.selection[i]= selection[i];
                }
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public String getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_view_item, parent, false);
                convertView.setTag(new ViewHolder((TextView) convertView.findViewById(R.id.textItem)));
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            final String item = getItem(position);
            holder.info.setText(item.substring(item.lastIndexOf('/')+1));
            if(selection!=null){
                if (selection[position]){
                    holder.info.setBackgroundColor(Color.argb(100,9,9,9));
                }else {
                    holder.info.setBackgroundColor(Color.WHITE);
                }
            }
            return convertView;
        }

        class ViewHolder{
            TextView info;
            ViewHolder(TextView info){
                this.info = info;
            }
        }
    }

    private static final int REQUEST_PERMISSIONS = 1234;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int PERMISSIONS_COUNT = 2;

    @SuppressLint("NewApi")
    private boolean arePermissionsDenied(){
        int p = 0;
        while (p<PERMISSIONS_COUNT){
            if(checkSelfPermission(PERMISSIONS[p]) != PackageManager.PERMISSION_GRANTED){
                return true;
            }
            p++;
        }
        return false;
    }

    private boolean isFileManagerInitialised = false;

    private boolean[] selection;

    private File[] files;

    private List<String> filesList;

    private  int filesFoundCount;

    private boolean fileMoved = false;

    private File filetodelete;

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()) {
                requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
                return;
        }
        if(!isFileManagerInitialised){
            final String rootPath = String.valueOf(Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + File.separator + "ImageToText");
            final File dir = new File(rootPath);
            //Toast.makeText(this,rootPath, Toast.LENGTH_LONG).show();
            files = dir.listFiles();
            //final TextView pathOutput = findViewById(R.id.pathOutput);
            //pathOutput.setText(rootPath.substring(rootPath.lastIndexOf('/')+1));
            if(files != null) {
                filesFoundCount = files.length;


                final ListView listView = (ListView) findViewById(R.id.filesView);
                final TextAdapter textAdapter = new TextAdapter();
                listView.setAdapter(textAdapter);

                filesList = new ArrayList<>();

                for (int i = 0; i < filesFoundCount; i++) {
                    filesList.add(String.valueOf(files[i].getAbsolutePath()));
                }

                textAdapter.setData(filesList);

                selection = new boolean[files.length];

                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        selection[position] = !selection[position];
                        textAdapter.setSelection(selection);
                        boolean isAtLeastOneSelected = false;
                        for (boolean aSelection : selection) {
                            if (aSelection) {
                                isAtLeastOneSelected = true;
                                break;
                            }
                        }
                        if (isAtLeastOneSelected) {
                            findViewById(R.id.bottomButtons).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.bottomButtons).setVisibility(View.GONE);
                        }
                        return false;
                    }
                });

                final ImageButton deleteBtn = findViewById(R.id.delBtn);

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(FileManagerActivity.this);
                        deleteDialog.setTitle("Delete");
                        deleteDialog.setMessage("Do you really want to delete this file?");
                        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (int i = 0; i < files.length; i++) {
                                    if (selection[i]) {
                                        deleteFileOrFolder(files[i]);
                                        selection[i] = false;
                                    }
                                }
                                files = dir.listFiles();
                                filesFoundCount = files.length;
                                filesList.clear();
                                for (int i = 0; i < filesFoundCount; i++) {
                                    filesList.add(String.valueOf(files[i].getAbsolutePath()));
                                }

                                textAdapter.setData(filesList);
                            }
                        });
                        deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        deleteDialog.show();
                    }
                });

                final ImageButton shareBtn = findViewById(R.id.shareBtn);

                shareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(int i =0;i<files.length;i++){
                            if(selection[i]){
                                String apkPath = files[i].toString();
                                Uri path = FileProvider.getUriForFile(getApplicationContext(), "com.example.imagetotext.FileManagerActivity", new File(apkPath));
                                //Toast.makeText(getApplicationContext(),files[i].toString(),Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("application/vnd.android.package-archive");
                                intent.putExtra(Intent.EXTRA_STREAM, path);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(intent, "ShareVia"));
                            }
                        }
                    }
                });

                final ImageButton editBtn = findViewById(R.id.editBtn);

                editBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(int i =0;i<files.length;i++) {
                            if (selection[i]) {
                                File file = new File(files[i].toString());
                                StringBuilder text = new StringBuilder();
                                try {
                                    BufferedReader br = new BufferedReader(new FileReader(file));
                                    String line;
                                    while ((line = br.readLine())!=null){
                                        text.append(line);
                                        text.append("\n");
                                    }
                                    br.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(getApplicationContext(), EditFileActivity.class);
                                String intentText = text.toString();
                                intent.putExtra("EXTRA_TEXT", intentText);
                                intent.putExtra("EXTRA_URI", files[i].toString());
                                startActivity(intent);
                            }
                        }
                    }
                });

                final ImageButton moveBtn = findViewById(R.id.moveBtn);

                moveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(int i =0;i<files.length;i++) {
                            if (selection[i]) {
                                String filename = files[i].toString().substring(files[i].toString().lastIndexOf('/')+1);
                                File file = new File(files[i].toString());
                                StringBuilder text = new StringBuilder();
                                try {
                                    BufferedReader br = new BufferedReader(new FileReader(file));
                                    String line;
                                    while ((line = br.readLine())!=null){
                                        text.append(line);
                                        text.append("\n");
                                    }
                                    br.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                                filetodelete = file;
                                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("text/plain");
                                intent.putExtra(Intent.EXTRA_TITLE,filename);
                                moveFileText = text.toString();
                                startActivityForResult(intent,1);
                                selection[i] = false;
                                files = dir.listFiles();
                                filesFoundCount = files.length;
                                filesList.clear();
                                for (int l = 0; l < filesFoundCount; l++) {
                                    filesList.add(String.valueOf(files[l].getAbsolutePath()));
                                }

                                textAdapter.setData(filesList);
                            }
                        }
                    }
                });

                final ImageButton renameBtn = findViewById(R.id.renameBtn);

                renameBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for(int i =0;i<files.length;i++) {
                            if (selection[i]) {
                                String filename = files[i].toString().substring(files[i].toString().lastIndexOf('/')+1);
                                File file = new File(files[i].toString());
                                StringBuilder text = new StringBuilder();
                                try {
                                    BufferedReader br = new BufferedReader(new FileReader(file));
                                    String line;
                                    while ((line = br.readLine())!=null){
                                        text.append(line);
                                        text.append("\n");
                                    }
                                    br.close();
                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                                Intent intent = new Intent(getApplicationContext(), SaveFileActivity.class);
                                intent.putExtra("EXTRA_TEXT", text.toString());
                                intent.putExtra("RESTART", "true");
                                intent.putExtra("FILE_NAME",filename);
                                startActivity(intent);
                            }
                        }
                    }
                });
            }

            isFileManagerInitialised = true;
        }
    }

    protected void restartActivity(){
        startActivity(getIntent());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode== RESULT_OK){
                try {
                    Uri uri = data.getData();
                    OutputStream outputStream = getContentResolver().openOutputStream(uri);
                    outputStream.write(moveFileText.getBytes());
                    outputStream.close();
                    Toast.makeText(getApplicationContext(),"File moved successfully",Toast.LENGTH_SHORT).show();
                    filetodelete.delete();
                    restartActivity();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Failed to move file",Toast.LENGTH_SHORT).show();
                }
            }else {
                restartActivity();
                //Toast.makeText(getApplicationContext(),"Error moving file",Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void deleteFileOrFolder(File fileOrFolder){
        if(fileOrFolder.isDirectory()){
            if(fileOrFolder.list().length==0){
                fileOrFolder.delete();
            }else {
                String files[] = fileOrFolder.list();
                for(String temp:files){
                    File fileToDelete = new File(fileOrFolder, temp);
                    deleteFileOrFolder(fileToDelete);
                }
                if(fileOrFolder.list().length==0){
                    fileOrFolder.delete();
                }
            }
        }else {
            fileOrFolder.delete();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions,
                                           final int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions,grantResults);
        if(requestCode==REQUEST_PERMISSIONS && grantResults.length>0){
            if(arePermissionsDenied()){
                ((ActivityManager) Objects.requireNonNull(this.getSystemService((ACTIVITY_SERVICE)))).
                        clearApplicationUserData();
                recreate();
            }
            else {
                onResume();
            }
        }
    }

    //actionbar menu


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.homeNav:
                Intent intent1 = new Intent(FileManagerActivity.this, MainActivity.class);
                startActivity(intent1);
                break;
            case R.id.translateNav:
                Intent intent2 = new Intent(FileManagerActivity.this, TranslateActivity.class);
                startActivity(intent2);
                break;
            case R.id.filesNav:
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
