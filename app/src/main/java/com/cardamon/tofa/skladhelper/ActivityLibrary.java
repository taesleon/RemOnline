package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.afollestad.materialdialogs.MaterialDialog;
import com.crashlytics.android.Crashlytics;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/**
 * справочник групп, с возможностью замены цвета
 * Created by dima on 13.01.18.
 */

abstract class ActivityLibrary extends AppCompatActivity {
    //текущая активность
    private AppCompatActivity mActivity = this;
    //выбранный пункт listView
    private String mCurrentUuid;
    private int mCurrentListViewID;
    //адаптер для listView
    private AdapterLibrary mAdapter;
    //данные для адаптера
    protected ArrayList<HashMap<String, String>> mRows;
    //диалого выбора цвета
    private MaterialDialog mDialog;

    protected abstract void setup();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_group_lib);
        //установка toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setBackgroundColor(getResources().getColor(R.color.lib_title, null));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setup();
        //данные для адаптера


        String[] from = {"name"};
        int[] to = {R.id.groupName};
        ListView listView = findViewById(R.id.listview);

        mAdapter = new AdapterLibrary(getApplicationContext(),
                mRows, R.layout.lib_item, from, to, this);
        listView.setAdapter(mAdapter);

        //слушатель для диалога выбора цвета
        //тут происходит установка нового цвета
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
                DbHelper db = new DbHelper();
                //цвет выбирается правильно
                int color = view.getId();
                db.setNewColor(color + "", mCurrentUuid);
                //ошибка тут при фильтрованном массиве
                mRows.get(mAdapter.getFilteredIdFromOriginal(mCurrentListViewID)).put("color", color + "");
                mAdapter.notifyDataSetChanged();
            }
        };
        //создание отображения для диалога выбора цвета
        LinearLayout colView = new LinearLayout(getApplicationContext());
        colView.setOrientation(LinearLayout.VERTICAL);
        int[] colArray = getResources().getIntArray(R.array.color_array);
        LinearLayout l = null;
        for (int i = 0; i < colArray.length - 1; i++) {
            if (i % 3 == 0) {
                l = new LinearLayout(getApplicationContext());
                l.setOrientation(LinearLayout.HORIZONTAL);
            }
            View newView = new View(getApplicationContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(200, 200, 1);
            lp.setMargins(15, 15, 15, 15);
            newView.setLayoutParams(lp);
            newView.setId(i);
            newView.setBackgroundColor(MyApplication.getColorById(i));
            newView.setOnClickListener(listener);
            l.addView(newView);
            if ((i + 1) % 3 == 0)
                colView.addView(l);
        }

        //слушатель выбора пукта ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCurrentListViewID = i;
                mCurrentUuid = ((TextView)view.findViewById(R.id.uuid)).getText().toString();
                mDialog = new MaterialDialog.Builder(mActivity)
                        .title("Выбор цвета")
                        .customView(colView, true)
                        .titleColor(mActivity.getResources().getColor(R.color.alert, null))
                        .positiveText("OK")
                        .positiveColor(ContextCompat.getColor(mActivity, R.color.dialog_positive_btn))
                        .build();

                mDialog.show();
            }
        });
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


}
