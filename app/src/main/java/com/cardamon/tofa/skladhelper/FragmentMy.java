package com.cardamon.tofa.skladhelper;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.cardamon.tofa.skladhelper.moysklad.CashBoxRowsDownloader;
import com.cardamon.tofa.skladhelper.moysklad.DemandDownloader;
import com.cardamon.tofa.skladhelper.moysklad.RetailDownloader;
import com.cardamon.tofa.skladhelper.moysklad.stopRefreshing;
import com.cardamon.tofa.skladhelper.remonline.Token;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.carlom.stikkyheader.core.StikkyHeaderBuilder;

/**
 * Created by dima on 27.12.17.
 */

abstract class FragmentMy extends Fragment implements
        DateSetObserver,
        SwipeRefreshLayout.OnRefreshListener,
        stopRefreshing,
        LoaderManager.LoaderCallbacks<Cursor>

{

    protected TextView mSum;
    protected TextView mCashSum;
    protected TextView mNoneCashSum;
    protected SwipeRefreshLayout mSwipeRefresh;
    protected CursorAdapter mAdapter;
    protected ListView mListView;
    protected SaleFilter mSaleFilter;
    protected Integer[] mSelectedFromBundle;

    abstract void createAdapter();


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    public void onResume() {
        super.onResume();
        update();
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSum = view.findViewById(R.id.sum);
        mCashSum = view.findViewById(R.id.cash);
        mNoneCashSum = view.findViewById(R.id.none_cash);

        mSwipeRefresh = view.findViewById(R.id.swipe_container);
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeResources(
                android.R.color.holo_red_light,
                android.R.color.holo_green_light,
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light
        );

        mListView = getView().findViewById(R.id.lvMain);

        createAdapter();

        StikkyHeaderBuilder.stickTo(mListView)
                .setHeader(R.id.header1, (ViewGroup) getView().findViewById(R.id.layout_container))
                .minHeightHeader(150)
                .setRefreshLayout(mSwipeRefresh)
                .build();


        ImageView filterAdd = view.findViewById(R.id.filter_add);
        filterAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterDialog();
            }
        });


    }
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Integer[] selected = mSaleFilter.getSelectedItems();
        int[] intArray = new int[selected.length];
        for (int i = 0; i < selected.length; i++) {
            intArray[i] = selected[i];
        }
        outState.putIntArray("selected", intArray);
        super.onSaveInstanceState(outState);
    }

    public void stopAnimation() {
        mSwipeRefresh.setRefreshing(false);
        update();
    }

    public void startAnimation() {
        mSwipeRefresh.setRefreshing(true);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void showFilterDialog() {

        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.filter_dialog_title)
                .items(mSaleFilter.getFilterNames())
                .positiveColor(ContextCompat.getColor(getActivity(), R.color.dialog_positive_btn))
                .negativeColor(ContextCompat.getColor(getActivity(), R.color.dialog_positive_btn))
                .positiveText("OK")
                .negativeText("ОТМЕНА")

                .itemsCallbackMultiChoice(mSaleFilter.getSelectedItems(), new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        mSaleFilter.setSelectedItems(which);
                        update();
                        return false;
                    }

                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                    }
                })
                .show();
    }

    protected void updateAllFragments() {
        ExecutorService executorService = Executors.newFixedThreadPool((int)1);
        executorService.execute((Runnable)new Token());
        for (Fragment fragment : MyApplication.ACTIVITY.getSupportFragmentManager().getFragments()) {
            boolean isFragmentRetail = fragment instanceof FragmentRetail;
            if (fragment instanceof FragmentRetail) {
                FragmentRetail fragmentRetail = (FragmentRetail)fragment;
                fragmentRetail.startAnimation();
                executorService.execute((Runnable)new RetailDownloader((AppCompatActivity)this.getActivity(), 4, 2, fragmentRetail));
            }
            if (fragment instanceof FragmentDemand) {

                FragmentDemand fragmentDemand = (FragmentDemand)fragment;
                fragmentDemand.startAnimation();
                executorService.execute((Runnable)new DemandDownloader((AppCompatActivity)this.getActivity(), 4, 2, fragmentDemand));

            }
            /*
            if (fragment instanceof FragmentOrders) {
                FragmentOrders fragmentOrders = (FragmentOrders)fragment;
                fragmentOrders.startAnimation();
                new StaubOrderDownloader((AppCompatActivity)this.getActivity(), 4, 2, fragmentOrders).start();
                new StaubOrderRowsDownloader((AppCompatActivity)this.getActivity(), 4, 2, fragmentOrders).start();
                new LacocotteOrderDownloader((AppCompatActivity)this.getActivity(), 4, 2, fragmentOrders).start();
                new LacocotteOrderRowsDownloader((AppCompatActivity)this.getActivity(), 4, 2, fragmentOrders).start();
            }
            */

            //кассовые отчеты по 4 кассам
            if (isFragmentRetail) {
                ArrayList<String[]> arrayList = new DbHelper().getCashBoxes();
                for (int i = 0; i < arrayList.size(); ++i) {
                    CashBoxRowsDownloader cashBoxRowsDownloader = new CashBoxRowsDownloader(getActivity(), 4, 2, (FragmentRetail) fragment, ((String[]) arrayList.get(i))[0]);
                    executorService.execute((Runnable) cashBoxRowsDownloader);
                }
            }
        }
    }
}
