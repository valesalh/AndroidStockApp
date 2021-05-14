package edu.sjsu.android.stockviewer;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import edu.sjsu.android.stockviewer.databinding.ActivityCryptoBinding;

public class CryptoActivity extends AppCompatActivity {

    private ActivityCryptoBinding binding;
    private List<TimeSeriesDaily> dailyData;
    private List<TimeSeriesDaily> dailyData1M;
    private List<TimeSeriesDaily> dailyData3M;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCryptoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        JsonObject stockData = null;
        try {
            Reader reader = new FileReader(intent.getStringExtra("path"));
            stockData = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        setup(stockData);

        binding.radioGroupGraph.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == binding.radioButton30.getId()) {
                displayData(dailyData1M);
            }
            else if(checkedId == binding.radioButton90.getId()) {
                displayData(dailyData3M);
            }
            else if(checkedId == binding.radioButtonMax.getId()) {
                displayData(dailyData);
            }
        });

        binding.sparkview.setScrubEnabled(true);
        binding.sparkview.setScrubListener(value -> {
            if(value != null) {
                TimeSeriesDaily temp = (TimeSeriesDaily) value;
                updateDisplay(temp);
            } else {
                updateDisplay(dailyData.get(dailyData.size() -1));
            }
        });

        binding.addToDB.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            values.put(WatchlistDB.SYMBOL, binding.infoSymbol.getText().toString());
            values.put(WatchlistDB.TYPE, WatchlistItem.TYPE_CRYPTO);

            AddToWatchlist task = new AddToWatchlist(values);
            Thread thread = new Thread(task);
            thread.start();
            try {
                thread.join();
                if(task.getRet() == -1) {
                    Toast.makeText(this, "This Symbol is already on your watchlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Added to Watchlist", Toast.LENGTH_SHORT).show();
                }
                binding.addToDB.setVisibility(View.GONE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    class AddToWatchlist implements Runnable {
        ContentValues values;
        int ret = 0;
        public AddToWatchlist(ContentValues values) {
            this.values = values;
        }
        @Override
        public void run() {
            if(getContentResolver().insert(MainActivity.CONTENT_URI, values) == null) {
                ret = -1;
            }
        }
        public int getRet() {
            return ret;
        }
    }

    private void displayData(List<TimeSeriesDaily> data) {
        DataSparkAdapter adapter = new DataSparkAdapter(data);
        binding.sparkview.setAdapter(adapter);
    }

    private void updateDisplay(TimeSeriesDaily data) {
        binding.infoOpen.setText("$" + data.getOpen());
        binding.infoHigh.setText("$" + data.getHigh());
        binding.infoLow.setText("$" + data.getLow());
        binding.infoClose.setText("$" + data.getClose());
        binding.infoDate.setText("Date: " + data.getDate());
    }

    private void setup(JsonObject stockData) {
        JsonObject overview = stockData.get("Meta Data").getAsJsonObject();
        JsonObject dailies = stockData.get("Time Series (Digital Currency Daily)").getAsJsonObject();

        binding.infoName.setText(overview.get("3. Digital Currency Name").getAsString());
        binding.infoSymbol.setText(overview.get("2. Digital Currency Code").getAsString());

        dailyData = new ArrayList<>();
        Iterator<String> iterator = dailies.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();

            String date = key;
            double open = dailies.get(key).getAsJsonObject().get("1b. open (USD)").getAsDouble();
            double high = dailies.get(key).getAsJsonObject().get("2b. high (USD)").getAsDouble();
            double low = dailies.get(key).getAsJsonObject().get("3b. low (USD)").getAsDouble();
            double close = dailies.get(key).getAsJsonObject().get("4b. close (USD)").getAsDouble();

            TimeSeriesDaily element = new TimeSeriesDaily(date, open, high, low, close, 0);
            dailyData.add(element);
        }

        updateDisplay(dailyData.get(0));

        if(dailyData.size() < 90) {
            double filler = dailyData.get(0).getOpen();
            for(int i = 0; i < 90; i++) {
                dailyData.add(new TimeSeriesDaily("N/A", filler, filler, filler, filler, 0));
            }
        }
        int size = dailyData.size();
        Collections.reverse(dailyData);
        dailyData1M = dailyData.subList(size - 30, size);
        dailyData3M = dailyData.subList(size - 90, size);
        displayData(dailyData3M);
    }
}