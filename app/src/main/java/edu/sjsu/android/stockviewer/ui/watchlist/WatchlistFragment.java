package edu.sjsu.android.stockviewer.ui.watchlist;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.sjsu.android.stockviewer.MainActivity;
import edu.sjsu.android.stockviewer.RecyclerAdapter;
import edu.sjsu.android.stockviewer.WatchlistDB;
import edu.sjsu.android.stockviewer.databinding.FragmentWatchlistBinding;

public class WatchlistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private FragmentWatchlistBinding binding;
    RecyclerAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWatchlistBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        LoaderManager.getInstance(this).restartLoader(0, null, this);

        binding.recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.recyclerView.getContext(),
        DividerItemDecoration.VERTICAL);
        binding.recyclerView.addItemDecoration(dividerItemDecoration);
        binding.recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerAdapter(getContext());
        binding.recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }
                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                         int swipeDir) {
                        String removeThis = adapter.delete(viewHolder.getAdapterPosition());
                        RemoveFromWatchlist task = new RemoveFromWatchlist(removeThis);
                        Thread t = new Thread(task);
                        t.start();
                    }
                };

        ItemTouchHelper itemTouch = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouch.attachToRecyclerView(binding.recyclerView);

        binding.deleteAll.setOnClickListener(v -> {
            DeleteAllFromWatchlist task = new DeleteAllFromWatchlist();
            Thread t = new Thread(task);
            t.start();
            adapter.deleteAll();
        });

        return view;
    }

    class RemoveFromWatchlist implements Runnable {
        String symbol;
        public RemoveFromWatchlist(String symbol) {
            this.symbol = symbol;
        }
        @Override
        public void run() {
            System.out.println("---------------------------" + symbol + "--------------------------");
            if(getContext().getContentResolver().delete(MainActivity.CONTENT_URI, WatchlistDB.getSYMBOL()+"=?",
                    new String[]{symbol}) > 1) {
                System.out.print("DELETE SUCCESS");
            }
        }
    }

    class DeleteAllFromWatchlist implements Runnable {
        @Override
        public void run() {
            getContext().getContentResolver().delete(MainActivity.CONTENT_URI, null, null);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(getContext(), MainActivity.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        adapter.fillData(data);
        data.close();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}