package com.mmu.fyp.sheetbasedcalculator.load;

/**
 * Created by User on 1/24/2017.
 */

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mmu.fyp.sheetbasedcalculator.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by User on 1/24/2017.
 */
public class LoadFileActivity extends ListActivity {

    private List<String> item = null;
    private List<String> path = null;
    private String root = "/";
    String savingDirectory;
    private TextView myPath;
    int returnResult;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_load);
        myPath = (TextView) findViewById(R.id.path);

        checkDir();

        if(returnResult == 1)
        {
            setResult(1);
            finish();
            return;
        }
        if(returnResult == 2)
        {
            setResult(2);
            finish();
            return;
        }

        getDir(savingDirectory);
    }

    private void getDir(String dirPath) {
        myPath.setText("Location: " + dirPath);

        item = new ArrayList<String>();
        path = new ArrayList<String>();

        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (!dirPath.equals(root)) {

            item.add(root);
            path.add(root);

            item.add("../");
            path.add(f.getParent());

        }

        if (files != null){
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isDirectory()){
                    item.add(file.getName() + "/");
                    path.add(file.getPath());
                }
                else{
                    if (file.getName().endsWith("txt")){
                        item.add(file.getName());
                        path.add(file.getPath());
                    }
                }
            }

        }

        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
        setListAdapter(fileList);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        final File file = new File(path.get(position));

        if (file.isDirectory()) {
            if (file.canRead())
                getDir(path.get(position));
            else {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(
                                "[" + file.getName()
                                        + "] folder can't be read!")
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // TODO Auto-generated method stub
                                    }
                                }).show();
            }
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Load [" + file.getName() + "] ?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            // TODO Auto-generated method stub
                            StringBuilder sb = new StringBuilder();
                            try {
                                File gpxfile = new File(savingDirectory, file.getName());
                                FileReader reader = new FileReader(gpxfile);
                                BufferedReader br = new BufferedReader(reader);
                                String line;

                                while ((line = br.readLine()) != null) {
                                    sb.append(line);
                                    sb.append('\n');
                                }
                                br.close();

                                Intent i = new Intent();
                                i.putExtra("LoadContent", sb.toString());
                                setResult(RESULT_OK, i);
                                finish();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0,
                                            int arg1) {
                            // TODO Auto-generated method stub

                        }

                    }).show();
        }
    }

    void checkDir(){
        savingDirectory = Environment.getExternalStorageDirectory().toString() +"/SheetCalculator";
        //Check if SD card is ready to be written
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
        {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        }
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
        {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        }
        else
        {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        //write to SDcard
        if(mExternalStorageAvailable&&mExternalStorageWriteable)
        {
            File file = new File(savingDirectory);
            if (!file.exists()) {
                file.mkdirs();
                Toast.makeText(LoadFileActivity.this, "mkdir", Toast.LENGTH_SHORT).show();
            }
        }
        else if(mExternalStorageAvailable == false)
            returnResult = 1;

        else
            returnResult = 2;
    }


}
