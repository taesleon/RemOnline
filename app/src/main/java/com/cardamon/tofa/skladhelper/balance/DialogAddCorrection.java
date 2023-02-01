package com.cardamon.tofa.skladhelper.balance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.cardamon.tofa.skladhelper.DateHelper;
import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;

import java.util.Calendar;
import java.util.Date;

public class DialogAddCorrection extends AlertDialog implements DatePickerDialog.OnDateSetListener {
    long date;
    private BalanceBv mBalanceBv;
    private EditText notate;
    private EditText sum;
    private EditText sells;
    private EditText payments;
    private TextView lastDate;
    private TextView date1;
    private TextView debtTv;

    double paymetsSum = 0.0;
    double salesSum = 0.0;
    double lastDebtSum = 0.0;

    protected DialogAddCorrection(BalanceBv activity) {
        super(activity);
        mBalanceBv = activity;
        View view = MyApplication.ACTIVITY.getLayoutInflater().inflate(R.layout.bv_dialog_add_cor, null);
        this.setView(view);
        this.sum = view.findViewById(R.id.sum);
        this.notate = view.findViewById(R.id.notate);
        sells = view.findViewById(R.id.sell);
        payments = view.findViewById(R.id.payment);
        debtTv = view.findViewById(R.id.debtTv);

        lastDate = view.findViewById(R.id.last_date);
        date1 = view.findViewById(R.id.date1);

        long dateFrom = BalanceBv.findLastCorrectionDate(0);
        long dateTo = Calendar.getInstance().getTimeInMillis();

        paymetsSum = BalanceBv.findPaymentsFromDate(dateFrom, dateTo);
        salesSum = BalanceBv.findSalesByGroupes(dateFrom, dateTo);
        lastDebtSum = BalanceBv.findLastCorrectionDebt();

        debtTv.setText(DateHelper.convertDoubleToStringNullDigit(lastDebtSum));

        setMainSum();
        sells.setText(DateHelper.convertDoubleToStringNullDigit(salesSum));
        payments.setText(DateHelper.convertDoubleToStringNullDigit(paymetsSum));

        lastDate.setText(DateHelper.convertMillisToDate(dateFrom));
        date1.setText(DateHelper.convertMillisToDate(dateTo));

        sells.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    salesSum = Double.parseDouble(sells.getText().toString().replaceAll("[^0-9[-]]", ""));
                } else
                    salesSum = 0d;
                setMainSum();
            }
        });

        payments.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    paymetsSum = Double.parseDouble(payments.getText().toString().replaceAll("[^0-9[-]]", ""));
                } else
                    paymetsSum = 0d;
                setMainSum();
            }
        });

        this.setButton(BUTTON_POSITIVE, "OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String request = "https://staub.com.ua/index.php?route=myscript/bvbalance&type=crr&sum=";
                request += lastDebtSum + salesSum - paymetsSum;
                request += "&date=";
                request += date;
                request += "&sales=";
                request += salesSum;
                request += "&payments=";
                request += paymetsSum;
                request += "&notate=";
                request += notate.getText().toString();

                mBalanceBv.sendRequest(request);
                mBalanceBv.onRefresh();
            }
        });

        this.setButton(BUTTON_NEGATIVE, "Отмена", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                activity,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();


    }

    private void setMainSum() {
        sum.setText(DateHelper.convertDoubleToStringNullDigit(salesSum + lastDebtSum - paymetsSum));
    }

    /*
    выбранная дата должна быть больше даты последней коррекции и меньше текущей даты
     */
    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayofmonth) {
        long lastCorrection = BalanceBv.findLastCorrectionDate(0);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, dayofmonth);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 00);

        date = calendar.getTimeInMillis();

        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.set(Calendar.HOUR_OF_DAY, 23);
        nowCalendar.set(Calendar.MINUTE, 59);
        nowCalendar.set(Calendar.SECOND, 00);

        if (!(calendar.getTimeInMillis() <= nowCalendar.getTimeInMillis() && calendar.getTimeInMillis() > lastCorrection)) {
            errorToast();
            date = nowCalendar.getTimeInMillis();
            new Handler().postDelayed(() -> DialogAddCorrection.this.show(), 1500L);
        } else
            show();
    }

    private void errorToast() {
        SuperActivityToast superActivityToast = new SuperActivityToast(mBalanceBv, Style.red());
        superActivityToast.setFrame(Style.FRAME_KITKAT);
        superActivityToast.setGravity(Gravity.CENTER);
        superActivityToast.setDuration(Style.DURATION_VERY_SHORT);
        superActivityToast.setText("Ошибка выбора даты! Ставим сегодня!");
        superActivityToast.show();
    }


    private void sendRequest() {


    }
}
