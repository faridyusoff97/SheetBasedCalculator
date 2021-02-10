package com.mmu.fyp.sheetbasedcalculator.save;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.mmu.fyp.sheetbasedcalculator.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 1/24/2017.
 */

public class FileBrowserActivity extends ListActivity {

    private List<String> item = null;
    private List<String> path = null;
    private String root = "/";
    String savingDirectory;
    private TextView myPath;
    int returnResult;

    String saveText;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);

        Intent i = getIntent();
        saveText = i.getExtras().getString("SaveContent");

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

        Button btnNew = (Button) findViewById(R.id.buttonNew);
//        btnNew.setVisibility(0);
        btnNew.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                final EditText input = new EditText(FileBrowserActivity.this);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                new AlertDialog.Builder(FileBrowserActivity.this)
                        .setTitle("Enter file name: ")
                        .setView(input)
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String value = input.getText().toString();
                                // Save the content as "input.txt"
                                try {
                                    File gpxfile = new File(savingDirectory, value +".txt");
                                    FileWriter writer = new FileWriter(gpxfile);
                                    writer.append(saveText);
                                    writer.flush();
                                    writer.close();
                                    setResult(RESULT_OK);
                                    finish();
                                } catch (IOException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        }).show();

            }
        });

        myPath = (TextView) findViewById(R.id.path);
        getDir(savingDirectory);
    }

    private void getDir(String dirPath) {
        myPath.setText("Location: " + dirPath);

        item = new ArrayList<String>();
        path = new ArrayList<String>();

        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (files != null){
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                path.add(file.getPath());
                if (file.isDirectory())
                    item.add(file.getName() + "/");
                else
                    item.add(file.getName());
            }
        }

        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this,
                R.layout.row, item);
        setListAdapter(fileList);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        final File file = new File(path.get(position));

        if (file.isDirectory()) {

        }
        else {	//shows overwrite dialog
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Overwrite: " +"[" + file.getName() + "] ?")
                    .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            // TODO Auto-generated method stub
                            try {
                                File gpxfile = new File(savingDirectory, file.getName());
                                FileWriter writer = new FileWriter(gpxfile);
                                writer.append(saveText);
                                writer.flush();
                                writer.close();
                                setResult(RESULT_OK);
                                finish();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            // TODO Auto-generated method stub
                        }
                    }).show();
        }
    }

    void checkDir()
    {
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
                file.mkdirs();	//
            }
        }
        else if(mExternalStorageAvailable == false)
            returnResult = 1;

        else
            returnResult = 2;

    }
}