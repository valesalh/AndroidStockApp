package edu.sjsu.android.stockviewer;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import edu.sjsu.android.stockviewer.databinding.RowLayoutBinding;
import edu.sjsu.android.stockviewer.ui.search.SearchViewModel;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    protected static ArrayList<WatchlistItem> data = new ArrayList<>();
    private Context context;

    public RecyclerAdapter(Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    public void fillData(Cursor cursor) {
        String symbol;
        int type;
        while(cursor.moveToNext()) {
            symbol = cursor.getString(cursor.getColumnIndex(WatchlistDB.getSYMBOL()));
            type = cursor.getInt(cursor.getColumnIndex(WatchlistDB.getTYPE()));
            WatchlistItem item = new WatchlistItem(symbol, type);
            if(item == null) {
                return;
            }
            data.add(item);
        }
        this.notifyDataSetChanged();
        cursor.close();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RowLayoutBinding row = RowLayoutBinding.inflate(inflater);
        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final WatchlistItem current = data.get(position);
        holder.binding.watchlistSymbol.setText(current.getSymbol());
        holder.binding.mainRowLayout.setOnClickListener(v -> {
            WatchlistItem item = data.get(holder.getAdapterPosition());
            SearchViewModel searchViewModel = new SearchViewModel();
            if(item.getType() == WatchlistItem.TYPE_STOCK) {
                String overview = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=" + item.getSymbol()
                        + "&apikey=" + MainActivity.API_KEY;
                String stockdata = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="
                        + item.getSymbol() + "&outputsize=full&apikey=" + MainActivity.API_KEY;

                JsonObject overviewJson = searchViewModel.searchOverview(overview);
                JsonObject dataJson = searchViewModel.searchData(stockdata);

                if(overviewJson.has("Note") || dataJson.has("Note")) {
                    Toast.makeText(context, "Please wait a minute and try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(context, InfoActivity.class);

                String overviewString = overviewJson.toString();
                String path = searchViewModel.getOutput().getAbsolutePath();

                intent.putExtra("overview", overviewString);
                intent.putExtra("path", path);
                context.startActivity(intent);
            }
            else if(item.getType() == WatchlistItem.TYPE_CRYPTO) {
                String url = "https://www.alphavantage.co/query?function=DIGITAL_CURRENCY_DAILY&symbol=" + item.getSymbol() +
                        "&market=USD&apikey=" + MainActivity.API_KEY;

                JsonObject dataJson = searchViewModel.searchData(url);

                if(dataJson.has("Note")) {
                    Toast.makeText(context, "Please wait a minute and try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(context, CryptoActivity.class);
                String path = searchViewModel.getOutput().getAbsolutePath();
                intent.putExtra("path", path);
                context.startActivity(intent);
            }
        });
    }

    public String delete(int index) {
        String ret = data.get(index).getSymbol();
        data.remove(index);
        this.notifyItemRemoved(index);
        return ret;
    }

    public void deleteAll() {
        data.clear();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if(data != null)
            return data.size();
        else
            return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected final RowLayoutBinding binding;

        public ViewHolder(RowLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
