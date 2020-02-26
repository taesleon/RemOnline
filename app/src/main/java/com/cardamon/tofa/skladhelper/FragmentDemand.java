package com.cardamon.tofa.skladhelper;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cardamon.tofa.skladhelper.moysklad.DemandDownloader;
import com.cardamon.tofa.skladhelper.moysklad.Downloader;
import com.cardamon.tofa.skladhelper.moysklad.RetailDownloader;
import com.crashlytics.android.Crashlytics;


import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by dima on 27.12.17.
 */

public class FragmentDemand extends FragmentMy {
    private FragmentDemand.DemandCursorLoader mLoader;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSaleFilter = new SaleFilter();
        if (savedInstanceState != null && savedInstanceState.containsKey("selected")) {
            int[] saved = savedInstanceState.getIntArray("selected");
            mSelectedFromBundle = new Integer[saved.length];
            for (int i = 0; i < saved.length; i++)
                mSelectedFromBundle[i] = saved[i];
        }


        MyApplication.ACTIVITY.fragmentDemand = this;
        getActivity().getSupportLoaderManager().initLoader(1, null, (LoaderManager.LoaderCallbacks<Cursor>) this);
    }


    @Override
    void createAdapter() {
        mAdapter = new AdapterDemand(getContext(), null, true);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0)
                    --position;
                else return;
                Intent intent = new Intent(MyApplication.ACTIVITY, ActivityDemand.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                String uuid = ((TextView) view.findViewById(R.id.uuid)).getText().toString();
                intent.putExtra("uuid", uuid);
                intent.putExtra("description", ((TextView) view.findViewById(R.id.description)).getText());
                intent.putExtra("date", ((TextView) view.findViewById(R.id.date)).getText());
                intent.putExtra("name", ((TextView) view.findViewById(R.id.groupName)).getText());
                intent.putExtra("sum", ((TextView) view.findViewById(R.id.sum)).getText());
                intent.putExtra("cash", ((TextView) view.findViewById(R.id.cash)).getText());
                intent.putExtra("noncash", ((TextView) view.findViewById(R.id.none_cash)).getText());
                intent.putExtra("discount", ((TextView) view.findViewById(R.id.discount)).getText());
                intent.putExtra("prefix", ((TextView) view.findViewById(R.id.prefix)).getText());
                intent.putExtra("store_color", ((TextView) view.findViewById(R.id.colorid)).getText());
                MyApplication.ACTIVITY.startActivity(intent);
            }
        });
    }

    @Override
    public void update() {
        if(MyApplication.ACTIVITY==null || getView()==null)
            return;
        long date1 = MyApplication.ACTIVITY.getFabMenu().getLeftDateForServer();
        long date2 = MyApplication.ACTIVITY.getFabMenu().getRightDateServer();
        DbHelper db = new DbHelper();
        mSaleFilter.setDialog(db.getFilterDemand(date1, date2));
        if (mSelectedFromBundle != null) {
            mSaleFilter.setSelectedItems(mSelectedFromBundle);
            mSelectedFromBundle = null;
        }
        ImageView img = getView().findViewById(R.id.filter_add);
        if (mSaleFilter.isNull())
            img.setImageDrawable(getActivity().getDrawable(R.drawable.ic_filter));
        else
            img.setImageDrawable(getActivity().getDrawable(R.drawable.ic_filter_selected));

        double sum = db.getDemandSum(date1, date2, mSaleFilter.getString());
        mSum.setText(DateHelper.convertDoubleToString(sum));
        mCashSum.setText("");
        mNoneCashSum.setText("");
        mLoader.setSqlParams(date1, date2, mSaleFilter);
        getActivity().getSupportLoaderManager().getLoader(1).forceLoad();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        super.onLoadFinished(loader, data);
    }

    @Override
    public void onRefresh() {
        new DemandDownloader(getActivity(), Downloader.SHOW_REFRESH_MSG, Downloader.UPDATE_MSG, this).start();
        List<Fragment> fragments = MyApplication.ACTIVITY.getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof FragmentRetail) {
                ((FragmentMy) fragment).startAnimation();
                new RetailDownloader(getActivity(), Downloader.SHOW_REFRESH_MSG, Downloader.UPDATE_MSG, ((FragmentRetail) fragment)).start();
            }
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        MyApplication.ACTIVITY.getFabMenu().registerObserver(this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mLoader = new DemandCursorLoader(getContext());
        return mLoader;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            RelativeLayout relativeLayout = view.findViewById(R.id.header1);
            relativeLayout.setBackgroundColor(getResources().getIntArray(R.array.tabs_colors)[MyApplication.ACTIVITY.getCurrentPage()]);
        }

    }

    static class DemandCursorLoader extends CursorLoader {
        private DbHelper dbHelper;
        private long date1, date2;
        private SaleFilter saleFilter;

        public DemandCursorLoader(@NonNull Context context) {
            super(context);
            this.saleFilter = new SaleFilter();
            dbHelper = new DbHelper();
        }

        @Override
        public void forceLoad() {
            super.forceLoad();
        }

        @Override
        public void commitContentChanged() {
            super.commitContentChanged();
        }

        @Override
        public Cursor loadInBackground() {
            return dbHelper.getDemandCursor(date1, date2, saleFilter.getString());
        }

        public void setSqlParams(long date1, long date2, SaleFilter saleFilter) {
            this.date1 = date1;
            this.date2 = date2;
            this.saleFilter = saleFilter;
        }
    }


}
