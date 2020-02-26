package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;

import com.cardamon.tofa.skladhelper.moysklad.BrokenRequest;
import com.cardamon.tofa.skladhelper.moysklad.Downloader;
import com.cardamon.tofa.skladhelper.moysklad.GoodDownloader;
import com.cardamon.tofa.skladhelper.moysklad.GoodLibraryTransfer;
import com.cardamon.tofa.skladhelper.moysklad.StockDownloader;
import com.crashlytics.android.Crashlytics;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.fabric.sdk.android.Fabric;

/**
 * Created by dima on 14.01.18.
 */

public class ActLibGood extends AppCompatActivity implements BrokenRequest{
    private AdapterGood mAdapter;
    //текущая активность
    private AppCompatActivity mActivity = this;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_good_lib);
        //установка toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("товары");
        toolbar.setBackgroundColor(getResources().getColor(R.color.lib_title, null));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        Runnable r = new Runnable() {
            @Override
            public void run() {
               newDbRequest();
            }
        };

        ExecutorService exService = Executors.newFixedThreadPool(1);
        exService.execute(new StockDownloader(mActivity, Downloader.SHOW_DIALOG_MSG, Downloader.UPDATE_MSG));
        exService.execute(r);


    }


    /**
     * создание searchView
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MaterialSearchView searchView = findViewById(R.id.search_view);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);

                //если нажата кнопка ок на клавиатуре, закрываем клавиатуру
                View view = mActivity.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return true;
            }

            /**
             * фильтруем результат
             *
             * @param newText
             * @return
             */
            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

            }
        });

        return true;
    }

    @Override
    public void breakRequest() {
        newDbRequest();
    }

    public void newDbRequest(){
                new AsyncTask<Void, Void, GoodLibraryTransfer>(){
                    @Override
                    protected void onPostExecute(GoodLibraryTransfer data) {
                        super.onPostExecute(data);
                        // список атрибутов элементов для чтения
                        String childFrom[] = new String[] {"name", "code", "ref", "sale", "buy", "stock"};
                        // список ID view-элементов, в которые будет помещены атрибуты элементов
                        int childTo[] = new int[] {R.id.name, R.id.code, R.id.ref, R.id.sale, R.id.buy, R.id.stock};



                        String[] groupFrom = new String[]{"name"};
                        int[] groupTo = new int[]{R.id.groupName};

                        mAdapter = new AdapterGood(
                                mActivity,
                                data.groupNames,
                                R.layout.good_lib_item_group,
                                groupFrom,
                                groupTo,
                                data.values,
                                R.layout.good_lib_item,
                                childFrom,
                                childTo);

                        ExpandableListView elvMain = (ExpandableListView) findViewById(R.id.expListView);
                        elvMain.setAdapter(mAdapter);


                    }

                    @Override
                    protected GoodLibraryTransfer doInBackground(Void... voids) {
                        DbHelper db = new DbHelper();
                        GoodLibraryTransfer data = db.getGoodLibrary();
                        db.close();
                        return data;
                    }
                }.execute();
    }
}
