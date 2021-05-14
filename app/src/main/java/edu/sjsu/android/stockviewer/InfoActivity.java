package edu.sjsu.android.stockviewer;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import edu.sjsu.android.stockviewer.databinding.ActivityInfoBinding;

public class InfoActivity extends AppCompatActivity {

    private ActivityInfoBinding binding;
    private List<TimeSeriesDaily> dailyData;
    private List<TimeSeriesDaily> dailyData1M;
    private List<TimeSeriesDaily> dailyData3M;
    private List<TimeSeriesDaily> dailyData1Y;
    private List<TimeSeriesDaily> dailyData5Y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String overviewString = intent.getStringExtra("overview");

        JsonObject overview = JsonParser.parseString(overviewString).getAsJsonObject();
        JsonObject stockData = null;
        try {
            Reader reader = new FileReader(intent.getStringExtra("path"));
            stockData = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        setup(stockData, overview);

        binding.radioGroupGraph.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == binding.radioButton30.getId()) {
                displayData(dailyData1M);
            }
            else if(checkedId == binding.radioButton90.getId()) {
                displayData(dailyData3M);
            }
            else if(checkedId == binding.radioButton1Y.getId()) {
                displayData(dailyData1Y);
            }
            else if(checkedId == binding.radioButton5Y.getId()) {
                displayData(dailyData5Y);
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

        binding.addStockToDB.setOnClickListener(v -> {
            ContentValues values = new ContentValues();
            values.put(WatchlistDB.SYMBOL, binding.infoSymbol.getText().toString());
            values.put(WatchlistDB.TYPE, WatchlistItem.TYPE_STOCK);

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
                binding.addStockToDB.setVisibility(View.GONE);
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
        binding.infoClose.setText("Adjusted Close: $" + data.getClose());
        binding.infoDate.setText("Date: " + data.getDate());
        binding.infoVolume.setText("" + data.getVolume());
    }

    private void setup(JsonObject stockData, JsonObject overview) {
        JsonObject dailies = stockData.get("Time Series (Daily)").getAsJsonObject();

        binding.infoName.setText(overview.get("Name").getAsString());
        binding.infoSymbol.setText(overview.get("Symbol").getAsString());
        binding.about.setText(overview.get("Description").getAsString());
        if(overview.get("MarketCapitalization").getAsString().equals("None")) {
            binding.infoMarketCap.setText(overview.get("MarketCapitalization").getAsString());
        } else {
            if(overview.get("MarketCapitalization").getAsString().length() > 9) {
                String number = overview.get("MarketCapitalization").getAsString();
                System.out.println(number);
                double cap = Long.parseLong(number) / 1000000000.00;
                double val  = Math.round(cap * 100.0) / 100.0;
                binding.infoMarketCap.setText("$" + val + "B");
            }
            else if(overview.get("MarketCapitalization").getAsString().length() > 6) {
                String number = overview.get("MarketCapitalization").getAsString();
                System.out.println(number);
                double cap = Long.parseLong(number) / 1000000.00;
                double val  = Math.round(cap * 100.0) / 100.0;
                binding.infoMarketCap.setText("$" + val + "M");
            }
            else {
                binding.infoMarketCap.setText("$" + overview.get("MarketCapitalization").getAsString());
            }
        }
        binding.infoPE.setText(overview.get("PERatio").getAsString());
        binding.infoEPS.setText(overview.get("EPS").getAsString());

        dailyData = new ArrayList<>();
        Iterator<String> iterator = dailies.keySet().iterator();
        int count = 1;
        while (iterator.hasNext() && count < 1300) {
            String key = iterator.next();

            String date = key;
            double open = dailies.get(key).getAsJsonObject().get("1. open").getAsDouble();
            double high = dailies.get(key).getAsJsonObject().get("2. high").getAsDouble();
            double low = dailies.get(key).getAsJsonObject().get("3. low").getAsDouble();
            double closeTemp = dailies.get(key).getAsJsonObject().get("5. adjusted close").getAsDouble();
            double close  = Math.round(closeTemp * 100.0) / 100.0;
            long volume = dailies.get(key).getAsJsonObject().get("6. volume").getAsLong();

            TimeSeriesDaily element = new TimeSeriesDaily(date, open, high, low, close, volume);
            dailyData.add(element);
            count++;
        }

        updateDisplay(dailyData.get(0));


        if(dailyData.size() < 1265) {
            double filler = dailyData.get(0).getOpen();
            for(int i = 0; i < 1265; i++) {
                dailyData.add(new TimeSeriesDaily("N/A", filler, filler, filler, filler, 0));
            }
        }
        int size = dailyData.size();
        Collections.reverse(dailyData);
        dailyData1M = dailyData.subList(size - 21, size);
        dailyData3M = dailyData.subList(size - 63, size);
        dailyData1Y = dailyData.subList(size - 253, size);
        dailyData5Y = dailyData.subList(size - 1265, size);
        displayData(dailyData3M);
    }
}