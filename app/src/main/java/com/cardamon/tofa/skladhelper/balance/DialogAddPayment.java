package com.cardamon.tofa.skladhelper.balance;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.cardamon.tofa.skladhelper.MyApplication;
import com.cardamon.tofa.skladhelper.R;
import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;

import java.util.Calendar;

public class DialogAddPayment extends AlertDialog implements DatePickerDialog.OnDateSetListener {
    long date;
    private BalanceBv mBalanceBv;
    private EditText notate;
    private EditText payment;
    private TextView dateTv;

    protected DialogAddPayment(BalanceBv activity) {
        super(activity);
        mBalanceBv = activity;
        View view = MyApplication.ACTIVITY.getLayoutInflater().inflate(R.layout.bv_dialog_add_sum, null);
        this.setView(view);
        this.setMessage("Сумма выплаты в грн.");
        this.payment = view.findViewById(R.id.sum);
        this.notate = view.findViewById(R.id.notate);
        this.dateTv = view.findViewById(R.id.date1);

        this.setButton(BUTTON_POSITIVE, "OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String request = "https://staub.com.ua/index.php?route=myscript/bvbalance&type=add&date=";
                request += date;
                request += "&sum=";
                request+=payment.getText();
                request += "&notate=";
                request+=notate.getText();
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
            new Handler().postDelayed(() -> DialogAddPayment.this.show(), 1500L);
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
