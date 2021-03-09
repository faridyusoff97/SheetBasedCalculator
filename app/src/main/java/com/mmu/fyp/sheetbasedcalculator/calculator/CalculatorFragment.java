package com.mmu.fyp.sheetbasedcalculator.calculator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.mmu.fyp.sheetbasedcalculator.R;
import com.mmu.fyp.sheetbasedcalculator.about.AboutActivity;
import com.mmu.fyp.sheetbasedcalculator.graph.GraphActivity;
import com.mmu.fyp.sheetbasedcalculator.help.HelpActivity;
import com.mmu.fyp.sheetbasedcalculator.load.LoadFileActivity;
import com.mmu.fyp.sheetbasedcalculator.save.FileBrowserActivity;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static java.math.RoundingMode.HALF_UP;

// import android.support.v4.app.Fragment;
// import android.support.v4.content.ContextCompat;


public class CalculatorFragment extends Fragment implements CalculatorContract.View {

    CalculatorContract.Presenter presenter;
    private static final int RequestPermissionCode = 1;

    private EditText screen;
    private Button btnSP, b1, b2, b3, b4, b5, b6, b7, b8, b9, b0, bDot, btnCP, bAdd, bMinus, bMulti, bDiv, bEqua, bEnter, bDel;
    private Button bSin, bASin, bTan, bATan, bCos, bACos, bLog, bLn, bExp, bVa, bVb, bExpo, bX, bY, bPow, bEval, bPi, bFunction, bGraph, bPrecision, bSDigits;
    private Button btnSDigits, btnPrecision, btnFunction, btnGraph;
    public static CustomStreamTokenizer st;
    public static int token;
    public static int number = CustomStreamTokenizer.TT_NUMBER;
    public static int variable = CustomStreamTokenizer.TT_WORD;
    public static HashMap<String, Apfloat> hm = new HashMap<String, Apfloat>();
    public static HashMap<String, Apfloat> fm = new HashMap<String, Apfloat>();
    public static HashMap<String, String> functionName = new HashMap<String, String>();
    public static String equation;
    public static String function = "";
    public static Scanner sc;
    public static Scanner fc;
    public static int precision = 1000;
    public static int significantDigits = 10;
    public static int fKey = 0;
    public static int fvKey = 0;
    public static int gKey = 0;
    private boolean shouldRestore = true;
    public Gson gson = new Gson();
    public static SharedPreferences preffs;

    @Override
    public void setPresenter(CalculatorContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public CalculatorFragment() { ;
    }

    public static CalculatorFragment newInstance() {
        return new CalculatorFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (shouldRestore) {
            preffs = this.getContext().getSharedPreferences("test", MODE_PRIVATE);
            if (preffs != null) {
                String storedHashMapString = preffs.getString("hashString", "oopsDintWork");
                java.lang.reflect.Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                try
                {
                    HashMap<String, String> testHashMap2 = gson.fromJson(storedHashMapString, type);
                    functionName.putAll(testHashMap2);
                }
                catch (IllegalStateException | JsonSyntaxException exception)
                { }

            }


            SharedPreferences prefs = getActivity().getPreferences(0);
            String restoredText = prefs.getString("text", null);
            if (restoredText != null) {
                screen.setText(restoredText, TextView.BufferType.EDITABLE);

                int selectionStart = prefs.getInt("selection-start", -1);
                int selectionEnd = prefs.getInt("selection-end", -1);
                if (selectionStart != -1 && selectionEnd != -1) {
                    screen.setSelection(selectionStart, selectionEnd);
                }
            }
        }

        shouldRestore = true;
    }


    @Override
    public void onPause() {
        super.onPause();
        //save in shared prefs/

        // SharedPreferences preffs = this.getContext().getSharedPreferences("test", MODE_PRIVATE);


        String hashMapString = gson.toJson(functionName);
        preffs.edit().putString("hashString", hashMapString).apply();


        SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
        editor.putString("text", screen.getText().toString());
        editor.putInt("selection-start", screen.getSelectionStart());
        editor.putInt("selection-end", screen.getSelectionEnd());
        editor.commit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.calculator_frag, container, false);
        screen = (EditText) root.findViewById(R.id.etInput);
        screen.setMovementMethod(new ScrollingMovementMethod());
        screen.setTextIsSelectable(true);
        screen.setEnabled(true);
        screen.setClickable(true);
        screen.setLongClickable(true);
        screen.setFocusable(true);
        screen.setFocusableInTouchMode(true);

        setHasOptionsMenu(true);

        btnSP = (Button) root.findViewById(R.id.btnStartParentheses);
        btnSP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "(");
            }
        });

        btnCP = (Button) root.findViewById(R.id.btnEndParentheses);
        btnCP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), ")");
            }
        });

        b1 = (Button) root.findViewById(R.id.btn1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "1");
            }
        });

        b2 = (Button) root.findViewById(R.id.btn2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "2");
            }
        });

        b3 = (Button) root.findViewById(R.id.btn3);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "3");
            }
        });

        b4 = (Button) root.findViewById(R.id.btn4);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "4");
            }
        });

        b5 = (Button) root.findViewById(R.id.btn5);
        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "5");
            }
        });

        b6 = (Button) root.findViewById(R.id.btn6);
        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "6");
            }
        });

        b7 = (Button) root.findViewById(R.id.btn7);
        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "7");
            }
        });

        b8 = (Button) root.findViewById(R.id.btn8);
        b8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "8");
            }
        });

        b9 = (Button) root.findViewById(R.id.btn9);
        b9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "9");
            }
        });

        b0 = (Button) root.findViewById(R.id.btn0);
        b0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "0");
            }
        });

        bDot = (Button) root.findViewById(R.id.btnDot);
        bDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), ".");
            }
        });

        bAdd = (Button) root.findViewById(R.id.btnPlus);
        bAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), " + ");
            }
        });

        bMinus = (Button) root.findViewById(R.id.btnMinus);
        bMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), " - ");
            }
        });

        bMulti = (Button) root.findViewById(R.id.btnMultiply);
        bMulti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), " * ");
            }
        });

        bDiv = (Button) root.findViewById(R.id.btnDivide);
        bDiv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), " / ");
            }
        });

        bEqua = (Button) root.findViewById(R.id.btnEqual);
        bEqua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), " = ");
            }
        });

        bEnter = (Button) root.findViewById(R.id.btnEnter);
        bEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "\n");
            }
        });

        bDel = (Button) root.findViewById(R.id.btnDelete);
        bDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                try {
                    int start = screen.getSelectionStart();
                    int end = screen.getSelectionEnd();
                    if (start == end) {
                        screen.getText().replace(start - 1, start, "");
                    } else
                        screen.getText().replace(Math.min(start, end),
                                Math.max(start, end), "");
                } catch (Exception e) {
                }
            }
        });

        bSin = (Button) root.findViewById(R.id.btnSin);
        bSin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "sin ");
            }
        });

        bASin = (Button) root.findViewById(R.id.btnArSin);
        bASin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "arcsin ");
            }
        });

        bTan = (Button) root.findViewById(R.id.btnTan);
        bTan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "tan ");
            }
        });

        bATan = (Button) root.findViewById(R.id.btnArTan);
        bATan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "arctan ");
            }
        });

        bCos = (Button) root.findViewById(R.id.btnCos);
        bCos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "cos ");
            }
        });

        bACos = (Button) root.findViewById(R.id.btnArCos);
        bACos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "arccos ");
            }
        });

        bX = (Button) root.findViewById(R.id.btnX);
        bX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "X ");
            }
        });

        bY = (Button) root.findViewById(R.id.btnY);
        bY.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "Y ");
            }
        });

        bPow = (Button) root.findViewById(R.id.btnPower);
        bPow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), " ^ ");
            }
        });

        bLog = (Button) root.findViewById(R.id.btnLog);
        bLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "log ");
            }
        });

        bLn = (Button) root.findViewById(R.id.btnLn);
        bLn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "ln ");
            }
        });

        bExp = (Button) root.findViewById(R.id.btnExp);
        bExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "e ");
            }
        });

        /*
        bVa = (Button) root.findViewById(R.id.btnA);
        bVa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "A ");
            }
        });

        bVb = (Button) root.findViewById(R.id.btnB);
        bVb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "B ");
            }
        });
        */


        bExpo = (Button) root.findViewById(R.id.btnE);
        bExpo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "E");

            }
        });

        bFunction = (Button) root.findViewById(R.id.btnFunction);
        bFunction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "functions ");

            }
        });

        bGraph = (Button) root.findViewById(R.id.btnGraph);
        bGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "graph [ ]");
                Editable text;
                int position;
                text = screen.getText();
                for (position = end; position < text.length(); position++)
                    if (text.charAt(position) == '\n')
                        break;

                screen.setSelection(position-2);


            }
        });

        bPrecision = (Button) root.findViewById(R.id.btnPrecision);
        bPrecision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "precision = ");

            }
        });

        bSDigits = (Button) root.findViewById(R.id.btnSDigits);
        bSDigits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "DPlaces = ");

            }
        });

        bPi = (Button) root.findViewById(R.id.btnPi);
        bPi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                int start = screen.getSelectionStart();
                int end = screen.getSelectionEnd();
                screen.getText().replace(Math.min(start, end),
                        Math.max(start, end), "pi ");

            }
        });




        bEval = (Button) root.findViewById(R.id.btnEvaluate);
        bEval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                try {
                    String answer = calculateExpression();

                    int end = screen.getSelectionEnd();
                    Editable text;
                    int position;
                    setupTokenizer();
                    text = screen.getText();
                    for (position = end; position < text.length(); position++)
                        if (text.charAt(position) == '\n')
                            break;

                    screen.setSelection(position);

                    int start = screen.getSelectionStart();
                    int end2 = screen.getSelectionEnd();
                    screen.getText().replace(Math.min(start, end2),
                            Math.max(start, end), "\n" + answer);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!checkPermission()) {
            //Request permission
            requestPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (!checkPermission()) {
                    getActivity().finish();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK)    //File saved successful
        {
            Toast.makeText(getActivity(), "File Saved",
                    Toast.LENGTH_SHORT).show();
        }
        if (requestCode == 0 && resultCode == RESULT_CANCELED)    //File not saved
        {
            Toast.makeText(getActivity(), "Not Saved",
                    Toast.LENGTH_SHORT).show();
        }
        if (requestCode == 0 && resultCode == 1)    //SD card not found.
        {
            Toast.makeText(getActivity(), "SD card not found.",
                    Toast.LENGTH_SHORT).show();
        }
        if (requestCode == 0 && resultCode == 2)    //SD card not writable
        {
            Toast.makeText(getActivity(), "SD card not writable.",
                    Toast.LENGTH_SHORT).show();
        }
        if (requestCode == 1 && resultCode == RESULT_OK)    //Load file
        {
            shouldRestore = false;
            screen.setText(data.getExtras().getString("LoadContent"));
        }
        if (requestCode == 1 && resultCode == 1)    //SD card not found
        {
            Toast.makeText(getActivity(), "SD card not found.",
                    Toast.LENGTH_SHORT).show();
        }
        if (requestCode == 1 && resultCode == 2)    //SD card not writable
        {
            Toast.makeText(getActivity(), "SD card not writable.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void requestPermission() {
        requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, RequestPermissionCode);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getContext(),
                READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(),
                WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra.vibrate(80);
                Intent helpIntent = new Intent(getContext(), HelpActivity.class);
                startActivity(helpIntent);
                break;
            case R.id.clear_sheet:
                Vibrator vibra1 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra1.vibrate(80);
                screen.setText("");
                screen.setSelection(screen.getText().length());
                break;
            case R.id.save:
                Vibrator vibra2 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra2.vibrate(80);
                Intent saveIntent = new Intent(getActivity(), FileBrowserActivity.class);
                String saveContent = screen.getText().toString();
                saveIntent.putExtra("SaveContent", saveContent);
                startActivityForResult(saveIntent, 0);
                break;
            case R.id.load:
                Vibrator vibra3 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra3.vibrate(80);
                Intent loadIntent = new Intent(getActivity(), LoadFileActivity.class);
                startActivityForResult(loadIntent, 1);
                break;
            case R.id.about:
                Vibrator vibra4 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra4.vibrate(80);
                Intent aboutIntent = new Intent(getContext(), AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.deleteFunctions:
                Vibrator vibra5 = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                vibra5.vibrate(80);
                functionName.clear();
                break;
        }
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calculator_frag_menu, menu);
    }

    private String calculateExpression() throws IOException {
        String answer = "";
        //double currentValue;
        Apfloat currentValue = new Apfloat(0,precision);
        //double result;
        Apfloat result = new Apfloat(0,precision);
        Apfloat rsize = new Apfloat(0);
        Apfloat apZero = new Apfloat(0);
        Apfloat apOne = new Apfloat(1);
        Apfloat apNegOne = new Apfloat(-1);
        long rsize_scale_long;
        String var;
        String funVar;

        int startSelection = screen.getSelectionStart();
        int endSelection = screen.getSelectionEnd();
        int ds;
        String exp = "";
        int length = 0;

        ds = startSelection - endSelection; // ds is the length in number of tokens of the selected area

        if (ds == 0) { // if no text is selected
            for (String currentWord : screen.getText().toString().split("\n")) {
                length = length + currentWord.length() + 1;
                if (length > startSelection) {
                    exp = currentWord;
                    break;
                }
            }
        } else {
            exp = screen.getText().toString().substring(startSelection, endSelection);
        }

        sc = new Scanner(exp);

        while (sc.hasNextLine()) {
            equation = sc.nextLine();
            setupTokenizer();

            try {
                if (token == variable ) {
                    var = st.sval;
                    token = st.nextToken();

                    // FUNCTION PART ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                    if ((!var.equalsIgnoreCase("cos")) && (!var.equalsIgnoreCase("tan")) && (!var.equalsIgnoreCase("sin")) && (!var.equalsIgnoreCase("arccos")) && (!var.equalsIgnoreCase("arcsin")) && (!var.equalsIgnoreCase("arctan")) && (!var.equalsIgnoreCase("log")) && (!var.equalsIgnoreCase("ln")) && (!var.equalsIgnoreCase("e")) && (!var.equalsIgnoreCase("pi"))) {

                        if (token == (int) '(') {
                            token = st.nextToken();
                            if (token == variable) {
                                funVar = st.sval;
                                token = st.nextToken();
                                if (funVar.equalsIgnoreCase("x")) {
                                    if (token == (int) ')') {
                                        token = st.nextToken();
                                        if (token == (int) '=') {
                                            token = st.nextToken();
                                            function = "";
                                            fKey = 1;
                                            currentValue = expression();
                                            result = currentValue;
                                            answer = answer + "=" + function + "\n";

                                            fKey = 0;
                                            functionName.put(var, function);
                                        } else if (token == st.TT_EOF) {
                                            functionName.get(var);
                                            if (functionName.get(var) != null) {
                                                answer = answer + "=" + functionName.get(var) + "\n";
                                            } else {
                                                throw new CalculationException("Function does not exist!");
                                            }
                                        }
                                    }

                                }
                            } else if (token == number) {
                                double tempNum = st.nval;
                                Apfloat tempAp = new Apfloat(tempNum);

                                token = st.nextToken();
                                if (functionName.get(var) != null) {
                                    fm.put("X", tempAp);
                                    fm.put("x", tempAp);

                                    String tempName = functionName.get(var);

                                    fc = new Scanner(tempName);
                                    while (fc.hasNextLine()) {
                                        equation = fc.nextLine();
                                        setupTokenizer();
                                        try {
                                            fvKey = 1;
                                            result = expression();
                                            rsize = result.floor();
                                            // Because scale() for 0 is infinity.
                                            if (rsize.equals(apZero)) {
                                                rsize_scale_long = 1;
                                            } else {
                                                rsize_scale_long = rsize.scale();
                                            }
                                            long tempSize = rsize_scale_long;
                                            if (tempSize < 7) {
                                                tempSize = (significantDigits + tempSize);
                                            } else if (tempSize >= 7) {
                                                tempSize = significantDigits;
                                            }
                                            if (rsize.equals(apNegOne) || rsize.equals(apZero)){
                                                result = result.add(apOne);
                                                result = ApfloatMath.round(result, tempSize, HALF_UP);
                                                result = result.subtract(apOne);
                                            }else {
                                                result = ApfloatMath.round(result, tempSize, HALF_UP);
                                            }

                                            if (rsize_scale_long >= 7) {
                                                answer = answer + "=" + result + "\n";
                                            } else {
                                                String strResult = result.toString(true);
                                                answer = answer + "=" + strResult + "\n";
                                            }

                                            fvKey = 0;
                                        } catch (CalculationException e) {
                                            e.printStackTrace();
                                            new AlertDialog.Builder(getActivity())
                                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                                    .setTitle("Error")
                                                    .setMessage(e.getMessage())
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                        }
                                                    })
                                                    .show();
                                        }


                                        fm.clear();
                                        return answer;
                                    }


                                } else {
                                    throw new CalculationException("Function does not exist!");
                                }

                            }

                        }
                    }
                    // FUNCTION PART END~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

                    // Display all functions that have been defined
                    if (var.equalsIgnoreCase("functions")){
                        token = st.nextToken();
                        int number = 1;
                        if(token == st.TT_EOF) {
                            answer = answer + "\n" + "List of defined functions are: " + "\n";
                            Iterator it = functionName.entrySet().iterator();
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry) it.next();
                                answer = answer + number + ". " + pair.getKey() + "(x) = " + pair.getValue() + "\n";
                                number += 1;
                                System.out.println(pair.getKey() + " = " + pair.getValue());
                            }
                        }
                    }

                    // Graph part
                    // Graph activity

                    if (var.equalsIgnoreCase("graph")){
                        Vibrator vibra = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        vibra.vibrate(80);
                        Intent graphIntent = new Intent(getContext(), GraphActivity.class);

                        // Max X value
                        int max_X_Value = 20;

                        // Placeholder
                        int t = 200;
                        graphIntent.putExtra("testnum", t);

                        // Array for y values
                        double[] yArray = new double[10000];
                        if (token == (int) '['){
                            token = st.nextToken();
                            //Calculate y values and store all into array to be passed to graph activity
                            if (token == variable){
                                var = st.sval;
                                token = st.nextToken();
                                if (token == number){
                                    double tempNum = st.nval;
                                    max_X_Value = (int) tempNum;
                                }
                                //Check if function exists
                                if (functionName.get(var) != null){
                                    String tempName = functionName.get(var);
                                    int i = 0;
                                    //Loop as many times as the max value of X * 10
                                    while (i < (max_X_Value*10)) {
                                        fc = new Scanner(tempName);
                                        while (fc.hasNextLine()) {
                                            equation = fc.nextLine();
                                            setupTokenizer();
                                            try {
                                                fvKey = 1;
                                                gKey = 1;
                                                int tempPres = precision;
                                                precision = 20;
                                                //Loop values of x to calculate y
                                                Apfloat tempAp = new Apfloat(i);
                                                Apfloat divideThis = new Apfloat(10);
                                                tempAp = tempAp.divide(divideThis);
                                                fm.put("X", tempAp);
                                                fm.put("x", tempAp);

                                                result = expression();
                                                yArray[i] = result.doubleValue();
                                                i++;
                                                precision = tempPres;
                                                gKey = 0;
                                                fvKey = 0;
                                            } catch (CalculationException e) {
                                                e.printStackTrace();
                                                new AlertDialog.Builder(getActivity())
                                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                                        .setTitle("Error")
                                                        .setMessage(e.getMessage())
                                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                            }
                                                        })
                                                        .show();
                                            }
                                        }
                                        fm.clear();
                                    }
                                }
                            }
                        }

                        graphIntent.putExtra("max_x", max_X_Value);
                        graphIntent.putExtra("passArrayIntent", yArray);
                        startActivity(graphIntent);

                    }

                    // Precision and significant figures display settings
                    Log.d("varvar",var);
                    if (token == (int) '=') {
                        // Adjust the precision and round the numbers up according to user preference
                        if (var.equalsIgnoreCase("precision")){
                            token = st.nextToken();
                            if (token == number){
                                double tempNum = st.nval;
                                precision = (int) tempNum;
                                precision = precision + 1;
                                token = st.nextToken();
                            }
                        }
                        // Adjust the number of digits that is displayed before the answers are transformed into scientific notation. eg 1000
                        else if (var.equalsIgnoreCase("DPlaces")){
                            token = st.nextToken();
                            if (token == number){
                                double tempNum = st.nval;
                                significantDigits = (int) tempNum;
                                token = st.nextToken();
                            }
                        }
                        else if ((!var.equalsIgnoreCase("cos")) && (!var.equalsIgnoreCase("tan")) && (!var.equalsIgnoreCase("sin")) && (!var.equalsIgnoreCase("arccos")) && (!var.equalsIgnoreCase("arcsin")) && (!var.equalsIgnoreCase("arctan")) && (!var.equalsIgnoreCase("log")) && (!var.equalsIgnoreCase("ln")) && (!var.equalsIgnoreCase("e"))&& (!var.equalsIgnoreCase("pi"))){

                            token = st.nextToken();
                            Log.d("var",var);
                            currentValue = expression();
                            result = currentValue;
                            rsize = result.floor();
                            // Because scale() for 0 is infinity.
                            if ( rsize.equals(apZero) ){
                                rsize_scale_long = 1;
                            }
                            else{
                                rsize_scale_long = rsize.scale();
                            }
                            long tempSize = rsize_scale_long;
                            if (tempSize < 7)
                            {
                                tempSize=  (significantDigits+tempSize);
                            }else if(tempSize >= 7){
                                tempSize = significantDigits;
                            }
                            if (rsize.equals(apNegOne) || rsize.equals(apZero)){
                                result = result.add(apOne);
                                result = ApfloatMath.round(result, tempSize, HALF_UP);
                                result = result.subtract(apOne);
                            }else {
                                result = ApfloatMath.round(result, tempSize, HALF_UP);
                            }
                            if (rsize_scale_long >= 7 ){
                                answer = answer + "=" + result + "\n";
                            }else{
                                String strResult = result.toString(true);
                                answer = answer + "=" + strResult + "\n";
                            }
                            hm.put(var, currentValue);
                        }
                        else{
                            throw new CalculationException("Cannot assign value to a function!");
                        }
                    }else if (token != CustomStreamTokenizer.TT_EOF){
                        setupTokenizer();
                        try {
                            result = expression();
                            rsize = result.floor();
                            // Because scale() for 0 is infinity.
                            if ( rsize.equals(apZero) ){
                                rsize_scale_long = 1;
                            }
                            else{
                                rsize_scale_long = rsize.scale();
                            }
                            long tempSize = rsize_scale_long;
                            Log.d("checkk rsize_Scale_long", String.valueOf(rsize_scale_long));
                            if (tempSize < 7)
                            {
                                tempSize=  (significantDigits+tempSize);
                            }else if(tempSize >= 7){
                                tempSize = significantDigits;
                            }
                            Log.d("checkk rsize", rsize.toString());
                            Log.d("checkk tempsize", String.valueOf(tempSize));
                            if (rsize.equals(apNegOne) || rsize.equals(apZero)){
                                result = result.add(apOne);
                                result = ApfloatMath.round(result, tempSize, HALF_UP);
                                result = result.subtract(apOne);
                            }else {
                                result = ApfloatMath.round(result, tempSize, HALF_UP);
                            }
                            if (rsize_scale_long >= 7 ){
                                answer = answer + "=" + result + "\n";
                            }else{
                                String strResult = result.toString(true);
                                answer = answer + "=" + strResult + "\n";
                            }
                        }
                        catch (CalculationException e) {
                            e.printStackTrace();
                            new AlertDialog.Builder(getActivity())
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Error")
                                    .setMessage(e.getMessage())
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                        }
                    }


                } else if (token == (int) '=') {
                    token = st.nextToken();
                    try {
                        result = expression();
                        rsize = result.floor();
                        // Because scale() for 0 is infinity.
                        if ( rsize.equals(apZero) ){
                            rsize_scale_long = 1;
                        }
                        else{
                            rsize_scale_long = rsize.scale();
                        }
                        long tempSize = rsize_scale_long;

                        if (tempSize < 7)
                        {
                            tempSize=  (significantDigits+tempSize);
                        }else if(tempSize >= 7){
                            tempSize = significantDigits;
                        }
                        if (rsize.equals(apNegOne) || rsize.equals(apZero)){
                            result = result.add(apOne);
                            result = ApfloatMath.round(result, tempSize, HALF_UP);
                            result = result.subtract(apOne);
                        }else {
                            result = ApfloatMath.round(result, tempSize, HALF_UP);
                        }
                        if (rsize_scale_long >= 7 ){
                            answer = answer + "=" + result + "\n";
                        }else{
                            String strResult = result.toString(true);
                            answer = answer + "=" + strResult + "\n";
                        }
                    } catch (CalculationException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(getActivity())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Error")
                                .setMessage(e.getMessage())
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                } else {
                    try {
                        result = expression();
                        rsize = result.floor();
                        // Because scale() for 0 is infinity.
                        if ( rsize.equals(apZero) ){
                            rsize_scale_long = 1;
                        }
                        else{
                            rsize_scale_long = rsize.scale();
                        }
                        long tempSize = rsize_scale_long;
                        if (tempSize < 7)
                        {
                            tempSize=  (significantDigits+tempSize);
                        }else if(tempSize >= 7){
                            tempSize = significantDigits;
                        }

                        if (rsize.equals(apNegOne) || rsize.equals(apZero)){
                            result = result.add(apOne);
                            result = ApfloatMath.round(result, tempSize, HALF_UP);
                            result = result.subtract(apOne);
                        }else {
                            result = ApfloatMath.round(result, tempSize, HALF_UP);
                        }

                        if (rsize_scale_long >= 7 ){
                            answer = answer + "=" + result + "\n";
                        }else{
                            String strResult = result.toString(true);
                            answer = answer + "=" + strResult + "\n";
                        }

                    } catch (CalculationException e) {
                        e.printStackTrace();
                        new AlertDialog.Builder(getActivity())
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Error")
                                .setMessage(e.getMessage())
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
                    }
                }

                if (token != CustomStreamTokenizer.TT_EOF) {
                    throw new CalculationException("Please complete your expression!");
                }

            } catch (CalculationException e) {
                // System.out.println(e.getMessage());
                e.printStackTrace();

                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Error")
                        .setMessage(e.getMessage())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        }
        return answer;
    }

    public static void setupTokenizer() {
        Reader r = new StringReader(equation);
        st = new CustomStreamTokenizer(r);
        st.ordinaryChar('.');
        st.ordinaryChar('-');
        st.ordinaryChar('/');
        st.ordinaryChar('E');

        try {
            getToken(); // go to 1st token
        } catch (IOException e) {
            System.out.println("GET TOKEN");
        }
    }

    public static void getToken() throws IOException {
        token = st.nextToken();
    }

    public static Apfloat expression() throws CalculationException, IOException {
        //double currentValue, nextValue;
        Apfloat currentValue = new Apfloat(0,precision);
        Apfloat nextValue = new Apfloat(0,precision);
        //double result = 0;
        Apfloat result = new Apfloat(0,precision);
        if ((token != (int) '(') && (token != number) && (token != variable) && (token != (int) '-')) {
            throw new CalculationException("The error occurred at " + st.toString());
        }
        currentValue = term();
        result = expressionP(currentValue);

        if ((token != st.TT_EOF) && (token != (int) ')')) {
            throw new CalculationException("The error occurred at " + st.toString());
        }
        return result;
    }

    public static Apfloat expressionP(Apfloat a) throws CalculationException, IOException {
        // double currentValue, nextValue, result;
        Apfloat currentValue = new Apfloat(0,precision);
        Apfloat nextValue = new Apfloat(0,precision);
        Apfloat result = new Apfloat(0, precision);
        Apfloat b = new Apfloat(0, precision);
        // result = 0;
        if (token == (int) '+') {
            if (fKey == 1)
            {
                function = function + "+";
            }
            token = st.nextToken();
            b = term();
            if (fKey != 1) {
                result = result.add(a);
                result = result.add(b);
            }


            return expressionP(result);

        } else if (token == (int) '-') {
            if (fKey == 1)
            {
                function = function + "-";
            }
            token = st.nextToken();
            b = term();
            if (fKey != 1) {
                result = a.subtract(b);
            }
            return expressionP(result);

        } else
            return a;
    }

    public static Apfloat term() throws CalculationException, IOException {
       // double currentValue, nextValue;
        Apfloat currentValue = new Apfloat(0,precision);
        Apfloat nextValue = new Apfloat(0, precision);
        // double result = 0;
        Apfloat result = new Apfloat(0, precision);
        if ((token != (int) '(') && (token != number) && (token != variable) && (token != (int) '-')) {
            throw new CalculationException("The error occurred at " + st.toString());
        }
        currentValue = power();
        result = termP(currentValue);

        if ((token != st.TT_EOF) && (token != (int) ')') && (token != (int) '+') && (token != (int) '-')) {
            throw new CalculationException("The error occurred at " + st.toString());
        }
        return result;
    }

    public static Apfloat termP(Apfloat c) throws CalculationException, IOException {
        //double currentValue, nextValue, result;
        Apfloat currentValue = new Apfloat (0,precision);
        Apfloat nextValue = new Apfloat(0,precision);
        Apfloat result = new Apfloat(0,precision);
        Apfloat d = new Apfloat(0,precision);
        if (token == (int) '*') {
            if (fKey == 1)
            {
                function = function + "*";
            }
            token = st.nextToken();
            d = power();
            if (fKey != 1) {
                result = c.multiply(d);
            }
            return termP(result);

        } else if (token == (int) '/') {
            if (fKey == 1)
            {
                function = function + "/";
            }

            token = st.nextToken();
            d = power();
            Apfloat zero = new Apfloat(0);
            if (fKey != 1){
                if (gKey == 1 && d.equals(zero)){
                    d = d.add(new Apfloat (0.000000000001));
                    result = c.divide(d);
                }
                else{
                    result = c.divide(d);
                }
            }

            return termP(result);

        } else
            return c;
    }

    public static Apfloat power() throws CalculationException, IOException {
        //double currentValue, nextValue;
        Apfloat currentValue = new Apfloat(0,precision);
        Apfloat nextValue = new Apfloat(0,precision);
        //double result = 0;
        Apfloat result = new Apfloat(0,precision);
        if ((token != (int) '(') && (token != number) && (token != variable) && (token != (int) '-')) {
            throw new CalculationException("The error occurred at " + st.toString());
        }
        currentValue = factor();
        result = powerP(currentValue);

        if ((token != st.TT_EOF) && (token != ')') && (token != (int) '+') && (token != (int) '-') && (token != (int) '*') && (token != (int) '/')) {
            System.out.println(st.nval);
            throw new CalculationException("The error occurred at " + st.toString());
        }
        return result;
    }

    public static Apfloat powerP(Apfloat b) throws CalculationException, IOException {
        // double Presult, result;
        Apfloat Presult = new Apfloat(0,precision);
        Apfloat result = new Apfloat(0, precision);
        Apfloat a = new Apfloat(0,precision);
        Apfloat apTemp = new Apfloat(0,precision);
        Apfloat apTempOne = new Apfloat(10,precision);

        // result = 0;
        if (token == (int) '^') {
            if (fKey == 1)
            {
                function = function + "^";
            }
            token = st.nextToken();
            a = factor();
            if (fKey != 1) {
                result = ApfloatMath.pow(b, a);
            }
            return (powerP(result));

        } else if (token == (int) 'E') {
            if (fKey == 1)
            {
                function = function + "E";
            }
            token = st.nextToken();
            a = factor();
            if (fKey != 1) {
                apTempOne = ApfloatMath.pow(apTempOne, a);
                result = b.multiply(apTempOne);
            }
            return (powerP(result));
        } else
            return b;
    }

    public static Apfloat factor() throws CalculationException, IOException {
        //double currentValue, result;
        Apfloat currentValue = new Apfloat(0,precision);
        Apfloat result = new Apfloat(0,precision);
        Apfloat aPi = new Apfloat(0,precision);
        aPi = ApfloatMath.pi(precision);
        Apfloat a180 = new Apfloat (180);
        String fName = st.sval;
        Apfloat csize = new Apfloat(0);
        functionName.get(fName);

        //result = 0;
        if (token == (int) '(') {
            if (fKey == 1)
            {
                function = function + "(";
            }
            token = st.nextToken();
            currentValue = expression();
            result = currentValue;
            if (token == (int) ')') {
                if (fKey == 1)
                {
                    function = function + ")";
                }
                token = st.nextToken();
            } else {
                throw new CalculationException("closing ')' expected!");
            }
            return result;
        } else if (token == number) {
            double tempNum = st.nval;
            Apfloat test = new Apfloat(tempNum);
            currentValue = test;
            if (fKey == 1)
            {
                currentValue = currentValue.precision(precision);
                csize = currentValue.floor();
                if (csize.size() >= significantDigits ){
                    function = function + currentValue;
                }else{
                    String strValue = currentValue.toString(true);
                    function = function  + strValue;
                }
            }
            token = st.nextToken();
            return currentValue;
        } else if (token == (int) '-') {
            if (fKey == 1)
            {
                function = function + " - ";
            }
            token = st.nextToken();
            currentValue = expression();
            if (fKey != 1) {
                Apfloat zero = new Apfloat(0);
                result = zero.subtract(currentValue);
            }
            return result;
        } else if (token == variable) {
            if ((st.sval).equals("cos")) {
                if (fKey == 1)
                {
                    function = function + "cos";
                }
                token = st.nextToken();
                currentValue = expression();
                if (fKey != 1) {
                    result = aPi.divide(a180);
                    result = result.multiply(currentValue);
                    result = ApfloatMath.cos(result);
                }
                //result = Math.cos((Math.PI / 180) * currentValue);
                return result;
            } else if ((st.sval).equals("sin")) {
                if (fKey == 1)
                {
                    function = function + "sin";
                }
                token = st.nextToken();
                currentValue = expression();
                if (fKey != 1) {
                    result = aPi.divide(a180);
                    result = result.multiply(currentValue);
                    result = ApfloatMath.sin(result);
                }
                //result = Math.sin((Math.PI / 180 * currentValue));
                return result;
            } else if ((st.sval).equals("tan")) {
                if (fKey == 1)
                {
                    function = function + "tan";
                }
                token = st.nextToken();
                currentValue = expression();
                if (fKey != 1) {
                    result = aPi.divide(a180);
                    result = result.multiply(currentValue);
                    result = ApfloatMath.tan(result);
                }
                //result = Math.tan((Math.PI / 180 * currentValue));
                return result;
            } else if ((st.sval).equals("arccos")) {
                if (fKey == 1)
                {
                    function = function + "arccos";
                }
                token = st.nextToken();
                currentValue = expression();
                if (fKey != 1) {
                    try {
                        currentValue = ApfloatMath.acos(currentValue);
                        result = a180.divide(aPi);
                        result = result.multiply(currentValue);
                    }catch(Exception E){throw new CalculationException("Arccos takes a value between -1 and 1"); }
                }
                // result = (180 / Math.PI) * (Math.acos(currentValue));
                return result;
            } else if ((st.sval).equals("arcsin")) {
                if (fKey == 1)
                {
                    function = function + "arcsin";
                }

                token = st.nextToken();
                currentValue = expression();
                if (fKey != 1) {
                    try {
                        currentValue = ApfloatMath.asin(currentValue);
                        result = a180.divide(aPi);
                        result = result.multiply(currentValue);
                    }catch(Exception E){throw new CalculationException("Arcsin takes a value between -1 and 1"); }
                }
                // result = (180 / Math.PI) * (Math.asin(currentValue));
                return result;
            } else if ((st.sval).equals("arctan")) {
                if (fKey == 1)
                {
                    function = function + "arctan";
                }
                token = st.nextToken();
                currentValue = expression();
                if (fKey != 1) {
                    try {
                        currentValue = ApfloatMath.asin(currentValue);
                        result = a180.divide(aPi);
                        result = result.multiply(currentValue);
                    }catch(Exception E){throw new CalculationException("Arctan takes a value between -1 and 1"); }
                }
                //result = (180 / Math.PI) * (Math.atan(currentValue));
                return result;
            } else if ((st.sval).equals("log")) {
                if (fKey == 1)
                {
                    function = function + "log";
                }
                token = st.nextToken();
                currentValue = expression();
                if (fKey != 1) {
                    Apfloat temp = new Apfloat(10);
                    result = ApfloatMath.log(currentValue, temp);
                }
                // result = (Math.log(currentValue) / Math.log(10));
                return result;
            } else if ((st.sval).equals("ln")) {
                if (fKey == 1)
                {
                    function = function + "ln";
                }
                token = st.nextToken();
                currentValue = expression();
                if (fKey != 1) {
                    result = ApfloatMath.log(currentValue);
                }
                // result = Math.log(currentValue);
                return result;
            } else if ((st.sval).equals("e")) {
                if (fKey == 1)
                {
                    function = function + "e";
                }
                token = st.nextToken();
                currentValue = expression();
                if (fKey != 1) {
                    result = ApfloatMath.exp(currentValue);
                }
                // result = Math.exp (currentValue);
                return result;
            }else if(functionName.get(fName) != null){
                token = st.nextToken();
                // FUNCTION PART ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                if (token == (int) '(')
                {
                    token = st.nextToken();
                    if (token == variable)
                    {
                        String funVar = st.sval;
                        token = st.nextToken();
                        if (funVar.equalsIgnoreCase("x"))
                        {
                            if (token == (int) ')')
                            {
                                if (fKey == 1){
                                    function = function + "("+ functionName.get(fName) + ")";
                                }
                                token = st.nextToken();
                            }
                        }
                    }
                }
            }else if ((st.sval).equals("pi")) {
                Apfloat apPi = new Apfloat(0);
                apPi = ApfloatMath.pi(precision);
                currentValue = apPi;
                if (fKey == 1)
                {
                    currentValue = currentValue.precision(precision);
                    csize = currentValue.floor();
                    if (csize.size() >= significantDigits ){
                        function = function + currentValue;
                    }else{
                        String strValue = currentValue.toString(true);
                        function = function  + strValue;
                    }
                }
                token = st.nextToken();
                return currentValue;
            }
            else{
                String id = st.sval;
                hm.get(id);
                fm.get(id);
                if (fKey == 1)
                {
                    function = function + id;
                }


                if (fvKey == 1) {
                    if (fm.get(id) != null) {
                        result = fm.get(id);
                    }
                }else {
                    if (hm.get(id) != null) {
                        result = hm.get(id);
                    } else {
                        if (fKey == 0) {
                            throw new CalculationException(st.sval + "is not initialized");
                        }
                    }
                }

                token = st.nextToken();
            }
        }
        else {
            throw new CalculationException("The error occurred at " + st.toString());
        }
        return result;
    }


}

