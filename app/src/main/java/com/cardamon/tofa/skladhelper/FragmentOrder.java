package com.cardamon.tofa.skladhelper;

import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.support.v4.app.LoaderManager;

public class FragmentOrder extends FragmentMy {
    private OrderCursorLoader mLoader;

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mSaleFilter = new SaleFilter();
        if (bundle != null && bundle.containsKey("selected")) {
            int[] arrn = bundle.getIntArray("selected");
            this.mSelectedFromBundle = new Integer[arrn.length];
            for (int i = 0; i < arrn.length; ++i) {
                this.mSelectedFromBundle[i] = arrn[i];
            }
        }
        MyApplication.ACTIVITY.fragmentOrder = this;
        this.getActivity().getSupportLoaderManager().initLoader(2, null, (LoaderManager.LoaderCallbacks)this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        super.onLoadFinished(loader, cursor);
    }

    public void onRefresh() {
        this.updateAllFragments();
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        if (bundle != null) {
            view.findViewById(R.id.header1).setBackgroundColor(this.getResources().getIntArray(R.array.tabs_colors)[MyApplication.ACTIVITY.getCurrentPage()]);
        }
    }



    @Override
    void createAdapter() {
        mAdapter = new AdapterOrder(this.getContext(), null, true);
        mListView.setAdapter((ListAdapter)this.mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            public void onItemClick(AdapterView<?> adapterView, View view, int n, long l) {
                if (n != 0) {
                    android.content.Intent intent = new android.content.Intent(MyApplication.getAppContext(), com.cardamon.tofa.skladhelper.ActivityOrder.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("uuid", ((TextView)view.findViewById(R.id.uuid)).getText().toString());
                    intent.putExtra("description", ((TextView)view.findViewById(R.id.description)).getText());
                    intent.putExtra("date", ((TextView)view.findViewById(R.id.date1)).getText());
                    intent.putExtra("name", ((TextView)view.findViewById(R.id.groupName)).getText());
                    intent.putExtra("sum", ((TextView)view.findViewById(R.id.sum)).getText());
                    intent.putExtra("cash", ((TextView)view.findViewById(R.id.cash)).getText());
                    intent.putExtra("noncash", ((TextView)view.findViewById(R.id.none_cash)).getText());
                    intent.putExtra("discount", ((TextView)view.findViewById(R.id.discount)).getText());
                    intent.putExtra("prefix", ((TextView)view.findViewById(R.id.prefix)).getText());
                    intent.putExtra("store_color", ((TextView)view.findViewById(R.id.colorid)).getText());
                    MyApplication.ACTIVITY.startActivity(intent);
                }
            }
        });
    }

    @Override
    public void update() {
        if (MyApplication.ACTIVITY != null) {
            if (this.getView() == null) {
                return;
            }
            long date1 = MyApplication.ACTIVITY.getFabMenu().getLeftDateForServer();
            long date2 = MyApplication.ACTIVITY.getFabMenu().getRightDateServer();
            DbHelper dbHelper = new DbHelper();
            double d = dbHelper.getOrderSum(date1, date2);
            double d2 = dbHelper.getOrderRealSum(date1, date2);
            this.mSum.setText((CharSequence)DateHelper.convertDoubleToString(d));
            this.mCashSum.setText((CharSequence)DateHelper.convertDoubleToString(d2));
            this.mNoneCashSum.setText((CharSequence)DateHelper.convertDoubleToString(d2 - d));
            (this.getView().findViewById(R.id.filter_add)).setVisibility(View.INVISIBLE);
            this.mLoader.setSqlParams(date1, date2);
            this.getActivity().getSupportLoaderManager().getLoader(2).forceLoad();
        }
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        MyApplication.ACTIVITY.getFabMenu().registerObserver(this);
    }


    public Loader<Cursor> onCreateLoader(int n, Bundle bundle) {
        mLoader =  new OrderCursorLoader(this.getContext());
        return mLoader;
    }
    static class OrderCursorLoader
            extends CursorLoader {
        private long date1;
        private long date2;
        private DbHelper dbHelper = new DbHelper();

        public OrderCursorLoader(Context context) {
            super(context);
        }

        public void commitContentChanged() {
            super.commitContentChanged();
        }

        public void forceLoad() {
            super.forceLoad();
        }

        public Cursor loadInBackground() {
            return this.dbHelper.getOrderCursor(this.date1, this.date2);
        }

        public void setSqlParams(long l, long l2) {
            this.date1 = l;
            this.date2 = l2;
        }
    }
}
