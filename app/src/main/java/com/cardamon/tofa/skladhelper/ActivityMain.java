package com.cardamon.tofa.skladhelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cardamon.tofa.skladhelper.moysklad.AgentDownloader;
import com.cardamon.tofa.skladhelper.moysklad.BrokenRequest;
import com.cardamon.tofa.skladhelper.moysklad.CashBoxDownloader;
import com.cardamon.tofa.skladhelper.moysklad.CashBoxRowsDownloader;
import com.cardamon.tofa.skladhelper.moysklad.DemandDownloader;
import com.cardamon.tofa.skladhelper.moysklad.Downloader;
import com.cardamon.tofa.skladhelper.moysklad.GoodDownloader;
import com.cardamon.tofa.skladhelper.moysklad.GroupDownloader;
import com.cardamon.tofa.skladhelper.moysklad.Model;
import com.cardamon.tofa.skladhelper.moysklad.RetailDownloader;
import com.cardamon.tofa.skladhelper.moysklad.RetailStoreDownloader;
import com.cardamon.tofa.skladhelper.moysklad.StoreDownloader;
import com.cardamon.tofa.skladhelper.remonline.Token;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.InputStream;

public class ActivityMain extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BrokenRequest {
    //меню выбора дат
    private OwnFabMenu mFabMenu;
    //кнопка меню выбора дат
    private FabButton mFabButton;
    //боковое меню
    private DrawerLayout mNavigationDrawer;
    //фрагменты вкладок, для доступа из других мест
    public DateSetObserver fragmentDemand;
    public DateSetObserver fragmentRetail;
    public DateSetObserver fragmentJust;
    //слайдер фрагментов
    private ViewPager mViewPager;
    private DbHelper db;


    protected void onCreate(Bundle savedInstanceState) {
        //для доступа из любого места приложения
        MyApplication.ACTIVITY = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//рудимент от моего склада
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString("password", "Basic c3Rhc0BzaGFraF9idjp0ZmZpazM=");
        editor.apply();


        db = new DbHelper();
        /*
        if (db.checkOldDocs()) {
            //this.addOldData();
            //this.startActivity(new Intent(getApplicationContext(), NewUser.class));
        }
        */


        //если нет авторизации, запускаем активность нового пользователя

        /*
        if (mPrefs.getString("password", "").equals("")) {
            finish();
            startActivity(new Intent(this, NewUser.class));
        }
        */

        //создаем фрагменты
        final FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentPagerItemAdapter mAdapter = new FragmentPagerItemAdapter(
                fragmentManager, FragmentPagerItems.with(this)
                .add("РЕТЕЙЛ", FragmentRetail.class)
                .add("БЕЗНАЛ", FragmentDemand.class)
                .add("ЗАКАЗЫ", FragmentOrder.class)
                .add("ИТОГО", FragmentJust.class)
                .create());

        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mAdapter);
        final SmartTabLayout viewPagerTab = findViewById(R.id.viewpagertab);
        viewPagerTab.setViewPager(mViewPager);

        //установка toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //данные для fabMenu, инициация
        View sheetView = findViewById(R.id.fab_sheet);
        mFabButton = findViewById(R.id.fab);
        final View overlay = findViewById(R.id.overlay);
        int sheetColor = ContextCompat.getColor(this, R.color.white);
        int fabColor = ContextCompat.getColor(this, R.color.colorAccent);
        mFabMenu = new OwnFabMenu(mFabButton, sheetView, overlay, sheetColor, fabColor, this);
        mFabMenu.init();

        //если активность восстанавливается, восстанавливаем даты, текущую вкладку и ее цвет
        if (savedInstanceState != null) {
            mFabMenu.restoreState(savedInstanceState.getLong("leftDate"),
                    savedInstanceState.getLong("rightDate"));
            mViewPager.setCurrentItem(savedInstanceState.getInt("page"));
            toolbar.setBackgroundColor(getResources().getIntArray(R.array.tabs_colors)[getCurrentPage()]);
            viewPagerTab.setBackgroundColor(getResources().getIntArray(R.array.tabs_colors)[getCurrentPage()]);
        }

        //при смене вкладки меняем цвета всех заголовков в том числе соседних !
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                RelativeLayout headerFragment0, headerFragment1, headerFragment2;
                headerFragment0 = mAdapter.getPage(0).getView().findViewById(R.id.header1);
                headerFragment1 = mAdapter.getPage(1).getView().findViewById(R.id.header1);
                headerFragment2 = mAdapter.getPage(2).getView().findViewById(R.id.header1);

                int colors[] = getResources().getIntArray(R.array.tabs_colors);
                switch (position) {
                    case 0:
                        toolbar.setBackgroundColor(colors[position]);
                        viewPagerTab.setBackgroundColor(colors[position]);
                        headerFragment1.setBackgroundColor(colors[position]);
                        headerFragment0.setBackgroundColor(colors[position]);
                        break;
                    case 1:
                        toolbar.setBackgroundColor(colors[position]);
                        viewPagerTab.setBackgroundColor(colors[position]);
                        headerFragment0.setBackgroundColor(colors[position]);
                        headerFragment1.setBackgroundColor(colors[position]);
                        headerFragment2.setBackgroundColor(colors[position]);
                        break;
                    case 2:
                        toolbar.setBackgroundColor(colors[position]);
                        viewPagerTab.setBackgroundColor(colors[position]);
                        headerFragment2.setBackgroundColor(colors[position]);
                        headerFragment1.setBackgroundColor(colors[position]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //настроки показа бокового меню
        mNavigationDrawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mNavigationDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mNavigationDrawer.addDrawerListener(toggle);
        toggle.syncState();

        //назначение обработчиков для бокового меню
        final NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        final Menu menu = navigationView.getMenu().getItem(1).getSubMenu();
        final CompoundButton switchCompat = (SwitchCompat) menu.getItem(2).getActionView().findViewById(R.id.service_switch);

        //на всякий случай, останавливаем сервис
        stopService(new Intent(getApplicationContext(), ServiceCheck.class));

        //если сервис нужно запустить, запускаем
        if (mPrefs.getBoolean("service", false)) {
            switchCompat.setChecked(true);
            startService(new Intent(getApplicationContext(), ServiceCheck.class));
        }

        //обработчик запуска / стопа сервиса
        switchCompat.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                editor.putBoolean("service", true);
                startService(new Intent(getApplicationContext(), ServiceCheck.class));
            } else {
                editor.putBoolean("service", false);
                stopService(new Intent(getApplicationContext(), ServiceCheck.class));
            }
            editor.apply();
        });

        //кнопка logout
        ImageView log_out = navigationView.getHeaderView(0).findViewById(R.id.log_out);
        log_out.setOnClickListener(view -> logout());
    }

    private void addOldData() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.retail);
            db.getWritableDatabase().execSQL(ActivityMain.convertStreamToString(inputStream));

            inputStream = getResources().openRawResource(R.raw.rows);
            db.getWritableDatabase().execSQL(ActivityMain.convertStreamToString(inputStream));

            inputStream = getResources().openRawResource(R.raw.demand);
            this.db.getWritableDatabase().execSQL(ActivityMain.convertStreamToString(inputStream));

            inputStream = getResources().openRawResource(R.raw.demand_rows);
            this.db.getWritableDatabase().execSQL(ActivityMain.convertStreamToString(inputStream));
            return;
        } catch (Exception exception) {
            exception.printStackTrace();
            return;
        }
    }

    public static String convertStreamToString(InputStream inputStream) throws Exception {
        String string2;
        BufferedReader bufferedReader = new BufferedReader((Reader) new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        while ((string2 = bufferedReader.readLine()) != null) {
            stringBuilder.append(string2);
            stringBuilder.append("\n");
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    /**
     * получить меню выбора даты
     *
     * @return меню
     */
    public OwnFabMenu getFabMenu() {
        return mFabMenu;
    }

    /**
     * получить кнопку меню выбора даты
     *
     * @return кнопка
     */
    public FabButton getFabButton() {
        return mFabButton;
    }

    /**
     * при выборе пункта бокового меню
     *
     * @param item выбранный пункт
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        ExecutorService exService = Executors.newFixedThreadPool(1);

        //получаем новый токен !!
        exService.execute(new Token());

        DbHelper db = new DbHelper();
        switch (item.getItemId()) {
            //обновить справочники
            case R.id.update_lib:
                //удалить старые справочники
                db.deleteLibs();

                exService.execute(new GoodDownloader(this, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                exService.execute(new AgentDownloader(this, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                exService.execute(new CashBoxDownloader(this, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                exService.execute(new RetailStoreDownloader(this, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                exService.execute(new StoreDownloader(this, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                exService.execute(new GroupDownloader(this, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));

                break;
            //обновить документы
            case R.id.update_doc:
                //удалить старые документы
                db.deleteDocs();
                addOldData();
                exService = Executors.newFixedThreadPool(1);
                exService.execute(new RetailDownloader(this, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                //exService.execute(new ReturnDownloader(this, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                exService.execute(new DemandDownloader(this, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG));
                ArrayList<String[]> boxList = db.getCashBoxes();
                for (int i = 0; i < boxList.size(); ++i) {
                    exService.execute(new CashBoxRowsDownloader(this, 4, 2, boxList.get(i)[0]));
                }

                break;
            case R.id.library_groups:
                startActivity(new Intent(this, ActLibGroup.class));
                break;
            case R.id.library_agents:
                startActivity(new Intent(this, ActLibAgent.class));
                break;
            case R.id.library_stores:
                startActivity(new Intent(this, ActLibStore.class));
                break;
            case R.id.library_goods:
                startActivity(new Intent(this, ActLibGood.class));
                break;
            case R.id.developer:
                new MaterialDialog.Builder(this)
                        .title(R.string.developer_dialog_title)
                        .content(R.string.developer_info)
                        .titleColor(MyApplication.getAppContext().getResources().getColor(R.color.icon_dark, null))
                        .positiveText("OK")
                        .contentGravity(GravityEnum.CENTER)
                        .positiveColor(ContextCompat.getColor(this, R.color.dialog_positive_btn))
                        .show();

        }
        mNavigationDrawer.closeDrawers();
        return false;
    }

    /**
     * при сохранении перед уничтожением активности, для последующего восстановления
     *
     * @param outState объект для передачи значений
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("leftDate", mFabMenu.getLeftDate());
        outState.putLong("rightDate", mFabMenu.getRightDate());
        outState.putInt("page", mViewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }

    /**
     * выход из учетной записи, удаляем все таблицы
     * стартуем активность новый пользователь
     */
    private void logout() {
        new MaterialDialog.Builder(this)
                .title(R.string.logout_dialog_title)
                .content(R.string.logout_dialog_content)
                .titleColor(MyApplication.getAppContext().getResources().getColor(R.color.alert, null))
                .positiveText("OK")
                .contentGravity(GravityEnum.CENTER)
                .negativeText("ОТМЕНА")
                .positiveColor(ContextCompat.getColor(this, R.color.dialog_positive_btn))
                .negativeColor(ContextCompat.getColor(this, R.color.dialog_positive_btn))
                .onPositive((dialog, which) -> {
                    DbHelper db = new DbHelper();
                    db.deleteLibs();
                    db.deleteDocs();
                    db.deleteUser();
                    finish();
                    MyApplication.ACTIVITY.fragmentRetail = null;
                    startActivity(new Intent(MyApplication.ACTIVITY, NewUser.class));
                })
                .show();
    }

    /**
     * подгрузка новых данных, после проверки контроля целостности данных
     * нужно куда то убрать
     *
     * @param table таблица, которую нужно скачать
     */

    public static void Download(final String table) {
        MyApplication.ACTIVITY.runOnUiThread(() -> {
            DbHelper db = new DbHelper();
            db.deleteTable(table);
            switch (table) {
                case Model.Agent.TABLE_NAME:
                    new AgentDownloader(MyApplication.ACTIVITY, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG).start();
                    break;
                case Model.Good.TABLE_NAME:
                    new GoodDownloader(MyApplication.ACTIVITY, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG).start();
                    break;
                case Model.RetailStore.TABLE_NAME:
                    new RetailStoreDownloader(MyApplication.ACTIVITY, Downloader.SHOW_DIALOG_MSG, Downloader.INSERT_MSG).start();
                    break;
            }

        });

    }

    /**
     * возврат номера текущего фрагмента
     *
     * @return номер текущего фрагмента
     */
    public int getCurrentPage() {
        return mViewPager.getCurrentItem();
    }

    @Override
    public void breakRequest() {

    }
}
