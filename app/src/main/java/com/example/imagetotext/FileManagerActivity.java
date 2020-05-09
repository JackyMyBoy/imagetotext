package com.example.imagetotext;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileManagerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_manager_main);
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

    @Override
    protected void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && arePermissionsDenied()) {
                requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
                return;
        }
        if(!isFileManagerInitialised){
            final String rootPath = String.valueOf(Environment.
                    getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
            final File dir = new File(rootPath);
            files = dir.listFiles();
            final TextView pathOutput = findViewById(R.id.pathOutput);
            pathOutput.setText(rootPath.substring(rootPath.lastIndexOf('/')+1));
            filesFoundCount = files.length;

            final ListView listView = (ListView) findViewById(R.id.filesView);
            final TextAdapter textAdapter = new TextAdapter();
            listView.setAdapter(textAdapter);

            filesList = new ArrayList<>();

            for(int i =0;i<filesFoundCount;i++){
                filesList.add(String.valueOf(files[i].getAbsolutePath()));
            }

            textAdapter.setData(filesList);

            selection = new boolean[files.length];

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    selection[position]=!selection[position];
                    textAdapter.setSelection(selection);
                    boolean isAtleastOneSelected = false;
                    for(boolean aSelection : selection){
                        if(aSelection){
                            isAtleastOneSelected=true;
                            break;
                        }
                    }
                    if(isAtleastOneSelected){
                        findViewById(R.id.bottomButtons).setVisibility(View.VISIBLE);
                    }else {
                        findViewById(R.id.bottomButtons).setVisibility(View.GONE);
                    }
                    return false;
                }
            });

            final Button b1 = (Button) findViewById(R.id.b1);

            b1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder deleteDialog = new AlertDialog.Builder(FileManagerActivity.this);
                    deleteDialog.setTitle("Delete");
                    deleteDialog.setMessage("Do you really want to delete this file?");
                    deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for(int i =0;i< files.length;i++){
                                if(selection[i]){
                                    deleteFileOrFolder(files[i]);
                                    selection[i] = false;
                                }
                            }
                            files = dir.listFiles();
                            filesFoundCount = files.length;
                            filesList.clear();
                            for(int i =0;i<filesFoundCount;i++){
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

            isFileManagerInitialised = true;
        }
    }

    private void deleteFileOrFolder( File fileOrFolder){
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

    @SuppressLint("NewApi")
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
}
