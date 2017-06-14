package com.example.boss.smssender;

import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

class ReadFiles {
    private static final String LOG_TAG = "my_logs";
    private String[] nameList = {"egais.sms", "sales.sms", "sales_cbu.sms", "ton_weight1.sms", "ton_weight2.sms"};

    private Date date = new Date();
    private SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

    void ReadFile(List<String> list, ArrayAdapter adapter) {

        List<String[]> fullTextOnFile = new ArrayList<>();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] listFiles = path.listFiles();
        fullTextOnFile.clear();
        for (String name : nameList) {
            for (int i = 0; i < listFiles.length; i++)
                if (listFiles[i].getName().equals(name)) {
                    File file = new File(path, name);
                    list.add("\n" + formatForDateNow.format(date) + " : Сканирование файла " + name);
                    adapter.notifyDataSetChanged();
                    MainActivity.scrollMyListViewToBottom(adapter);
                    Log.d(LOG_TAG, "Сканирование файла " + name);
                    try {
                        String s;
                        FileInputStream fstream = new FileInputStream(file);
                        DataInputStream in = new DataInputStream(fstream);
                        BufferedReader br = new BufferedReader(new InputStreamReader(in, "Cp1251"));
                        while ((s = br.readLine()) != null) {
                            fullTextOnFile.add(s.split(";"));
                        }

                        String firstText = Arrays.toString(fullTextOnFile.get(1)).replaceAll("[^A-Za-zА-Яа-я0-9]", " ") + "\n";
                        StringBuilder stringBuilder = new StringBuilder(firstText);

                        for (int u = 2; u < fullTextOnFile.size(); u++) {
                            stringBuilder.append("").append(Arrays.toString(fullTextOnFile.get(u)).replaceAll("[^A-Za-zА-Яа-я0-9]", " ") + "\n");
                        }

                        String text = String.valueOf(stringBuilder);

                        for (String phn : fullTextOnFile.get(0)) {
                            sendSMS(String.valueOf(phn), text, adapter, list);
                        }

                        fullTextOnFile.clear();
                        fstream.close();
                        in.close();
                        br.close();
                    } catch (FileNotFoundException e) {
                        list.add("\n" + formatForDateNow.format(date) + " : Файл " + name + " не найден.");
                        adapter.notifyDataSetChanged();
                        MainActivity.scrollMyListViewToBottom(adapter);
                        Log.d(LOG_TAG, "Файл " + name + " не найден.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e) {
                        list.add("\n" + formatForDateNow.format(date) + " : Неверное количество элементов в файле " + name);
                        adapter.notifyDataSetChanged();
                        MainActivity.scrollMyListViewToBottom(adapter);
                        Log.d(LOG_TAG, "Неверное количество элементов в файле " + name);
                    }
                    file.delete();
                }
        }
    }

    void sendSMS(String phoneNumber, String textSms, ArrayAdapter adapter, List list) {
        try {
            //SmsManager.getDefault().sendTextMessage(phoneNumber, null, textSms, null, null);
            Log.d(LOG_TAG, "Отправлено смс на номер: " + phoneNumber + " с текстом : " + textSms);
            list.add("\n" + formatForDateNow.format(date) + " : Отправлено смс на номер: " + phoneNumber + " \n с текстом : \n" + textSms);
            adapter.notifyDataSetChanged();
            MainActivity.scrollMyListViewToBottom(adapter);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Не удалось отправить сообщение: " + e);
            list.add("\n" + "Не удалось отправить сообщение: " + e);
            adapter.notifyDataSetChanged();
            MainActivity.scrollMyListViewToBottom(adapter);
        }
    }
}

