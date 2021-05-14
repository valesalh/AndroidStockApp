package edu.sjsu.android.stockviewer.ui.search;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.JsonObject;

import edu.sjsu.android.stockviewer.CryptoActivity;
import edu.sjsu.android.stockviewer.InfoActivity;
import edu.sjsu.android.stockviewer.MainActivity;
import edu.sjsu.android.stockviewer.R;
import edu.sjsu.android.stockviewer.databinding.FragmentSearchBinding;

public class SearchFragment extends Fragment {

    private SearchViewModel searchViewModel;
    private FragmentSearchBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        searchViewModel = new SearchViewModel();

        binding.button.setOnClickListener(v -> {
            if(binding.radioGroup.getCheckedRadioButtonId() == binding.radioButtonStock.getId()) {
                stockSearch(binding.input.getText().toString());
            }
            else if(binding.radioGroup.getCheckedRadioButtonId() == binding.radioButtonCrypto.getId()) {
                cryptoSearch(binding.input.getText().toString());
            }
        });

        binding.uninstall.setOnClickListener(v -> {
            Intent delete = new Intent(Intent.ACTION_DELETE,
                    Uri.parse("package:" + getActivity().getPackageName()));
            startActivity(delete);
        });
        return view;
    }

    private boolean checkEmpty(String text) {
        if(text.isEmpty()) {
            Toast.makeText(getContext(), "Empty Input", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void stockSearch(String input) {
        String ticker = input;
        if(checkEmpty(ticker)) return;

        String overview = "https://www.alphavantage.co/query?function=OVERVIEW&symbol=" + ticker
                + "&apikey=" + MainActivity.API_KEY;
        String stockdata = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="
                + ticker + "&outputsize=full&apikey=" + MainActivity.API_KEY;

        JsonObject overviewJson = searchViewModel.searchOverview(overview);
        JsonObject dataJson = searchViewModel.searchData(stockdata);

        if(overviewJson.toString().equals("{}") || dataJson.toString().equals("{}")
                || overviewJson.has("Error Message") || dataJson.has("Error Message")) {
            binding.errorText.setText("An error has occurred. Perhaps the ticker is not valid?");
            return;
        }
        else if(overviewJson.has("Note") || dataJson.has("Note")) {
            binding.errorText.setText(R.string.warning);
            return;
        }
        else {
            binding.errorText.setText("");
        }

        Intent intent = new Intent(getActivity(), InfoActivity.class);

        String overviewString = overviewJson.toString();
        String path = searchViewModel.getOutput().getAbsolutePath();

        intent.putExtra("overview", overviewString);
        intent.putExtra("path", path);
        startActivity(intent);
    }

    public void cryptoSearch(String input) {
        String symbol = input;
        if(checkEmpty(symbol)) return;

        String url = "https://www.alphavantage.co/query?function=DIGITAL_CURRENCY_DAILY&symbol=" + symbol +
        "&market=USD&apikey=" + MainActivity.API_KEY;

        JsonObject dataJson = searchViewModel.searchData(url);

        if(dataJson.toString().equals("{}") || dataJson.has("Error Message")) {
            binding.errorText.setText("An error has occurred. Perhaps the symbol is not valid for the selected search type?");
            return;
        }
        else if(dataJson.has("Note")) {
            binding.errorText.setText(R.string.warning);
            return;
        }
        else {
            binding.errorText.setText("");
        }

        Intent intent = new Intent(getActivity(), CryptoActivity.class);
        String path = searchViewModel.getOutput().getAbsolutePath();
        intent.putExtra("path", path);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}