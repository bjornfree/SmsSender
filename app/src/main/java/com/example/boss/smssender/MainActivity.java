package com.example.boss.smssender;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "my_logs";
    private Button btn_Start;
    private Button btn_Stop;
    private static ListView lv_log;
    Date date = new Date();
    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
    SimpleDateFormat formatSaveLog = new SimpleDateFormat("dd.MM.yyyy_hh.mm");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv_log = (ListView) findViewById(R.id.lv_log);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET}
                , 1);

    }


    public void Start(View v) {
        final List<String> log = new ArrayList<>();

        if (!isOnline(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
            log.add("\n" + formatForDateNow.format(date) + " Нет подключения к интернету");
            return;
        }

        btn_Start = (Button) findViewById(R.id.btn_Start);
        btn_Stop = (Button) findViewById(R.id.btn_Stop);
        btn_Start.setClickable(false);
        btn_Start.setEnabled(false);
        btn_Stop.setClickable(true);
        btn_Stop.setEnabled(true);

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, log);
        lv_log.setAdapter(adapter);

        log.add("\n" + formatForDateNow.format(date) + " : Обработка запущена");
        adapter.notifyDataSetChanged();
        Log.d(LOG_TAG, "Обработка запущена");

        processing(log, adapter);
    }

    private void processing(final List<String> log, final ArrayAdapter adapter) {
        final FTPConnection ftp = new FTPConnection();
        final Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (ftp.uploadFiles(log)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ReadFiles readFiles = new ReadFiles();
                            readFiles.ReadFile(log, adapter);

                            btn_Stop.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    btn_Stop.setClickable(false);
                                    btn_Stop.setEnabled(false);
                                    btn_Start.setEnabled(true);
                                    btn_Start.setClickable(true);
                                    timer.cancel();
                                    log.add("\n Обработка остановленна");
                                    Log.d(LOG_TAG, "Обработка остановленна");
                                    scrollMyListViewToBottom(adapter);
                                    adapter.notifyDataSetChanged();

                                    File logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + "SmsSenderlog_" + formatSaveLog.format(date) + ".txt");

                                    try {
                                        PrintWriter pw = new PrintWriter(logFile, "Cp1251");
                                        pw.write(String.valueOf(log).replaceAll(",", ""));
                                        pw.flush();
                                        pw.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    FTPConnection.sendLogToFtp(logFile);
                                }
                            });
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            scrollMyListViewToBottom(adapter);
                        }
                    });
                    timer.cancel();
                    processing(log, adapter);
                }
            }
        }, 5000, 60000);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static void scrollMyListViewToBottom(final ArrayAdapter adapter) {
        lv_log.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                lv_log.setSelection(adapter.getCount() - 1);
            }
        });
    }
}
