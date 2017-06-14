package com.example.boss.smssender;

import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class FTPConnection extends FTP {
    private static final String LOG_TAG = "my_logs";


    private static String server = "ftp.ellada.com.ua";
    private static String user = "TEST";
    private static String pass = "12345678";
    static Date date = new Date();
    SimpleDateFormat formatForDateNow = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
    static SimpleDateFormat formatSaveLog = new SimpleDateFormat("dd.MM.yyyy_hh-mm-ss");


    private String[] nameList = {"egais.sms", "sales.sms", "sales_cbu.sms", "ton_weight1.sms", "ton_weight2.sms"};


    private static FTPClient mFTP = new FTPClient();


    boolean uploadFiles(List list) {
        try {
            mFTP.connect(server);
            mFTP.login(user, pass);
            mFTP.enterLocalPassiveMode();
            FTPFile[] files = mFTP.listFiles();

            for (String remoteFileName : nameList) {
                for (FTPFile ftpFile : files) {
                    if (ftpFile.getName().equals(remoteFileName)) {
                        FileOutputStream fos = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + remoteFileName);
                        mFTP.retrieveFile(remoteFileName, fos);
                        mFTP.dele(remoteFileName);
                        fos.close();
                    }
                }
            }
            mFTP.logout();
            mFTP.disconnect();
            return true;
        } catch (ConnectException e) {
            Log.d(LOG_TAG, "Нет подключения к фтп.");
            list.add("\n" + formatForDateNow.format(date) + " : Невозможно подключиться к фтп");
            return false;
        } catch (UnknownHostException e) {
            Log.d(LOG_TAG, "Нет подключения к фтп.");
            list.add("\n" + formatForDateNow.format(date) + " : Невозможно подключиться к фтп");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (mFTP.isConnected()) {
                    mFTP.logout();
                    mFTP.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    static void sendLogToFtp(final File localLog) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mFTP.connect(server);
                    mFTP.login(user, pass);
                    mFTP.enterLocalPassiveMode();
                    FileInputStream ios = new FileInputStream(localLog);
                    mFTP.changeWorkingDirectory("SmsSenderLogs");
                    mFTP.appendFile("SmsSenderlog_" + formatSaveLog.format(date) + ".txt", ios);
                    localLog.delete();
                    ios.close();

                    mFTP.logout();
                    mFTP.disconnect();

                } catch (IOException e) {
                    System.out.println("Ошибка подключения");
                } finally {
                    try {
                        if (mFTP.isConnected()) {
                            mFTP.logout();
                            mFTP.disconnect();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }
}

