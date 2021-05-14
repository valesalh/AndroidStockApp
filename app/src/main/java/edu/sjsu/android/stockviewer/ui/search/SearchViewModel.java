package edu.sjsu.android.stockviewer.ui.search;

import android.os.Environment;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import edu.sjsu.android.stockviewer.MainActivity;

public class SearchViewModel {

    private File output;

    public SearchViewModel() {}

    public JsonObject searchOverview(String sURL) {
        class Task implements Runnable {
            JsonObject obj;
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(sURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JsonElement root = null;
                try {
                    root = JsonParser.parseReader(new InputStreamReader(urlConnection.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
                obj = root.getAsJsonObject();
            }
            public JsonObject getObj() {
                return obj;
            }
        }

        Task task = new Task();
        Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join();
            return task.getObj();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    public JsonObject searchData(String sURL) {
        class Task implements Runnable {
            JsonObject obj;
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(sURL);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                HttpURLConnection urlConnection = null;
                try {
                    urlConnection = (HttpURLConnection) url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JsonElement root = null;
                try {
                    root = JsonParser.parseReader(new InputStreamReader(urlConnection.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    urlConnection.disconnect();
                }
                obj = root.getAsJsonObject();
                writeToFile(obj);
            }

            public JsonObject getJsonObj() {
                return obj;
            }
        }

        Task task = new Task();
        Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join();
            return task.getJsonObj();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeToFile(JsonObject obj) {
        class WriteThread implements Runnable{
            @Override
            public void run() {
                if (!isExternalStorageWritable()) {
                    return;
                }
                File path = MainActivity.path;
                output = new File(path, "json.txt");
                if(saveFile(obj, output)) {
                    System.out.println("------------WRITE FILE SUCCESS--------------");
                }
            }
        }
        WriteThread task = new WriteThread();
        Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private boolean saveFile(JsonObject obj, File output){
        try {
            FileWriter writer = new FileWriter(output);
            writer.write(obj.toString());
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public File getOutput() {
        return output;
    }
}