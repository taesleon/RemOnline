package com.cardamon.tofa.skladhelper;

import android.app.DatePickerDialog;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.gordonwong.materialsheetfab.MaterialSheetFab;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dima on 08.10.17.
 */

public class OwnFabMenu extends MaterialSheetFab implements DateSetObservable {
    //текущий месяц
    private Date mCurrentMonth;
    //меню
    private View mSheet;
    private ActivityMain mActivity;
    // поля для верхних дат
    private int[] leftFields = {R.id.left_day, R.id.left_month, R.id.left_year};
    private int[] rightFields = {R.id.right_day, R.id.right_month, R.id.right_year};
    //текущие даты !
    private Date mLeftDate, mRightDate;
    //слайдер месяца
    private TextSwitcher mTextSwitcher;
    //иконка для кнопки фаб
    private Drawable mIcon;
    //список объектов которые наблюдают за изменением даты
    private List<DateSetObserver> observers = new LinkedList<>();

    /**
     * создание меню
     * установка текущего месяца
     * установка текущего дня
     *
     * @param view
     * @param sheet
     * @param overlay
     * @param sheetColor
     * @param fabColor
     * @param a
     */
    public OwnFabMenu(View view, View sheet, View overlay, int sheetColor, int fabColor, ActivityMain a) {
        super(view, sheet, overlay, sheetColor, fabColor);
        mSheet = sheet;
        mActivity = a;
        Calendar calendar = Calendar.getInstance();
        //при создании меню ставим текущую дату
        mLeftDate = calendar.getTime();
        mRightDate = calendar.getTime();
        setFields();
    }

    /**
     * очистка всех пунктов меню
     */
    private void clearSelected() {
        mSheet.findViewById(R.id.fab_sheet_item_1).setSelected(false);
        mSheet.findViewById(R.id.fab_sheet_item_7).setSelected(false);
        mSheet.findViewById(R.id.fab_sheet_item_multi).setSelected(false);
        setMonthItemSelectMode(0);
    }

    /**
     * инициализация меню
     * создание фабрики для формирования текста пункта месяц
     * установка анимации месяца
     * слушатели стрелок месяца
     * слушатель клика по меню
     */
    public void init() {
        mTextSwitcher = (TextSwitcher) mSheet.findViewById(R.id.month_switcher);
        MonthFactory monthFactory = new MonthFactory();
        mTextSwitcher.setFactory(monthFactory);
        final Animation slideInLeftAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.month_left);
        final Animation slideInRightAnimation = AnimationUtils.loadAnimation(mActivity, R.anim.month_right);
        setCurrentMonth();
        checkForInterval();
        //обработчик клика левой стрелки месяца

        ImageView leftArrow = (ImageView) mSheet.findViewById(R.id.month_left);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelected();
                mLeftDate = parseMonthFromSwitcher();
                mTextSwitcher.setInAnimation(slideInLeftAnimation);
                mTextSwitcher.setText(getMonthName(-1));
                setMonthItemSelectMode(1);
                changeMonth(-1);
            }
        });

        //обработчик клика правой стрелки месяца

        ImageView rightArrow = (ImageView) mSheet.findViewById(R.id.month_right);
        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearSelected();
                mLeftDate = parseMonthFromSwitcher();
                mTextSwitcher.setInAnimation(slideInRightAnimation);
                mTextSwitcher.setText(getMonthName(1));
                setMonthItemSelectMode(1);
                changeMonth(1);
            }
        });

        //обработчик клика по меню

        View.OnClickListener fabMenuListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                clearSelected();
                mIcon = null;
                if (view.getId() != R.id.month_item)
                    setCurrentMonth();
                switch (view.getId()) {
                    case R.id.fab_sheet_item_1:
                        mIcon = mActivity.getDrawable(R.drawable.ic_calendar_1);
                        mLeftDate = new Date();
                        mRightDate = new Date();
                        hideMenu();
                        break;
                    case R.id.fab_sheet_item_7:
                        mIcon = mActivity.getDrawable(R.drawable.ic_calendar_7);
                        mLeftDate = DateInterval.getFirstDayOfWeek(new Date());
                        mRightDate = DateInterval.getLastDayOfWeek(new Date());
                        hideMenu();
                        break;
                    case R.id.month_item:
                        mIcon = mActivity.getDrawable(R.drawable.ic_calendar_31);
                        mLeftDate = DateInterval.getFirstDayOfMonth(parseMonthFromSwitcher());
                        mRightDate = DateInterval.getLastDayOfMonth(mLeftDate);
                        hideMenu();
                        break;
                    case R.id.fab_sheet_item_multi:
                        mIcon = mActivity.getDrawable(R.drawable.ic_calendar_multi);
                        newDatePicker(1, true);
                        newDatePicker(0, false);

                        break;
                    case R.id.leftDateAllFields:

                        mSheet.findViewById(R.id.fab_sheet_item_multi).setSelected(true);
                        newDatePicker(0, true);
                        break;
                    case R.id.rightDateAllFields:

                        mSheet.findViewById(R.id.fab_sheet_item_multi).setSelected(true);
                        newDatePicker(1, true);
                }
                setFields();
//если клик по месяцу, с задержкой выделяем месяц, если нет другой пункт меню
                if (view.getId() == R.id.month_item || view.getId() == R.id.month_switcher)
                    new Handler() {
                    }.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setMonthItemSelectMode(1);
                        }
                    }, 300);
                else
                    new Handler() {
                    }.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            view.setSelected(true);
                        }
                    }, 300);
            }
        };

        //назначение обработчика пунктам меню
        int[] values = {R.id.fab_sheet_item_1,
                R.id.fab_sheet_item_7,
                R.id.fab_sheet_item_multi,
                R.id.month_item,
                R.id.leftDateAllFields,
                R.id.rightDateAllFields};
        for (int i : values)
            mSheet.findViewById(i).setOnClickListener(fabMenuListener);
    }

    /**
     * выделение пункта меню месяц
     *
     * @param mode 1 - выделено, 0 - выделение снято
     */
    private void setMonthItemSelectMode(int mode) {
        int[] icons = {R.id.month_right, R.id.month_left, R.id.month_img};
        Drawable icon;
        int color;
        if (mode == 1) {
            (mSheet.findViewById(R.id.month_item)).setSelected(true);
            color = ContextCompat.getColor(mActivity, R.color.white);
        } else {
            (mSheet.findViewById(R.id.month_item)).setSelected(false);
            color = ContextCompat.getColor(mActivity, R.color.black);
        }
        TextSwitcher switcher = (TextSwitcher) mSheet.findViewById(R.id.month_switcher);
        ((TextView) switcher.getCurrentView()).setTextColor(color);
        for (int i : icons) {
            ImageView img = (ImageView) mActivity.findViewById(i);
            icon = img.getDrawable();
            icon.setTint(color);
        }
    }

    /**
     * строковое представление следующего, предыдущего месяца
     *
     * @param delta -1 / +1 месяц
     * @return янв 17
     */
    private String getMonthName(int delta) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yy");
        calendar.setTime(mCurrentMonth);
        calendar.add(Calendar.MONTH, delta);
        mCurrentMonth = calendar.getTime();
        return monthFormat.format(mCurrentMonth);
    }

    /**
     * установка дат в верхние поля
     */
    private void setFields() {
        String[] params = getTextFields(mLeftDate);
        for (int i = 0; i < 3; i++) {
            ((TextView) mSheet.findViewById(leftFields[i])).setText(params[i]);
        }
        params = getTextFields(mRightDate);
        for (int i = 0; i < 3; i++) {
            ((TextView) mSheet.findViewById(rightFields[i])).setText(params[i]);
        }
    }

    /**
     * текстовое представление для верхних полей
     *
     * @param date
     * @return массив вида {30, янв, 2017}
     */
    private String[] getTextFields(Date date) {
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String[] result = new String[3];
        result[0] = calendar.get(Calendar.DAY_OF_MONTH) + "";
        result[1] = monthFormat.format(date);
        result[2] = calendar.get(Calendar.YEAR) + "";
        return result;
    }

    /**
     * изменение месяца на следующий / предыдущий
     *
     * @param delta +1 / -1
     */
    private void changeMonth(int delta) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mLeftDate);
        calendar.add(Calendar.MONTH, delta);
        mLeftDate = DateInterval.getFirstDayOfMonth(calendar.getTime());
        mRightDate = DateInterval.getLastDayOfMonth(mLeftDate);
        setFields();
    }

    /**
     * установка в поле текущего месяца
     */
    private void setCurrentMonth() {
        mCurrentMonth = new Date();
        mTextSwitcher.setCurrentText(getMonthName(0));
    }

    /**
     * прячем меню
     */
    private void hideMenu() {
        mActivity.getFabButton().setImageDrawable(mIcon);
        chekForError();
        mActivity.getFabMenu().hideSheet();
        notifyObservers();
    }

    /**
     * обработчик выбора даты
     *
     * @param field левое поле 0, правое поле 1
     * @param close закрытие меню
     * @return
     */
    private DatePickerDialog.OnDateSetListener getDialogListener(final int field, final boolean close) {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(i, i1, i2);
                if (field == 0)
                    mLeftDate = calendar.getTime();
                else
                    mRightDate = calendar.getTime();
                setFields();
                if (close) {
                    checkForInterval();
                    hideMenu();
                }
            }
        };
        return listener;
    }

    /**
     * создание диалога выбора даты
     *
     * @param field левое поле 0, правое поле 1
     * @param close закрыть диалог после выбора
     */
    private void newDatePicker(int field, boolean close) {
        Calendar calendar = Calendar.getInstance();

        switch (field) {
            case 0:
                calendar.setTime(mLeftDate);
                break;
            case 1:
                calendar.setTime(mRightDate);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                mActivity,
                getDialogListener(field, close),
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    /**
     * получение месяца установленого в textSwitcher
     *
     * @return дата
     */
    private Date parseMonthFromSwitcher() {
        String strDate = (String) ((TextView) mTextSwitcher.getCurrentView()).getText();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM yy");
        Date date = new Date();
        try {
            date = monthFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * определение не принадлежит ли диапазон интервалу (сегодя, текущая неделя, текущий месяц)
     * при положительном ответе, переключает менею на соответствующий пункт
     */
    private void checkForInterval() {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(mLeftDate);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(mRightDate);
        mIcon = mActivity.getDrawable(R.drawable.ic_calendar_multi);
        mSheet.findViewById(R.id.fab_sheet_item_multi).setSelected(true);
        if (DateInterval.simpleCompareDate(mLeftDate, DateInterval.getFirstDayOfWeek(mLeftDate))
                && DateInterval.simpleCompareDate(mRightDate, DateInterval.getLastDayOfWeek(mLeftDate))) {
            clearSelected();
            mIcon = mActivity.getDrawable(R.drawable.ic_calendar_7);
            mSheet.findViewById(R.id.fab_sheet_item_7).setSelected(true);

        }

        if (DateInterval.simpleCompareDate(mLeftDate, DateInterval.getFirstDayOfMonth(mLeftDate))
                && DateInterval.simpleCompareDate(mRightDate, DateInterval.getLastDayOfMonth(mLeftDate))) {
            clearSelected();

            mCurrentMonth = mLeftDate;
            mTextSwitcher.setText(getMonthName(0));

            mIcon = mActivity.getDrawable(R.drawable.ic_calendar_31);
            setMonthItemSelectMode(1);
        }
        if (DateInterval.simpleCompareDate(mLeftDate, DateInterval.getToDay())
                && DateInterval.simpleCompareDate(mRightDate, DateInterval.getToDay())) {
            clearSelected();
            mIcon = mActivity.getDrawable(R.drawable.ic_calendar_1);
            mSheet.findViewById(R.id.fab_sheet_item_1).setSelected(true);
        }
        //установка иконки для кнопки фаб
        mActivity.getFabButton().setImageDrawable(mIcon);
    }

    /**
     * на случай програмной установки диапазона, или восстановления после закрытия системо
     *
     * @param left
     * @param right
     */
    public void restoreState(long left, long right) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(left);
        mLeftDate = calendar.getTime();
        calendar.setTimeInMillis(right);
        mRightDate = calendar.getTime();
        clearSelected();
        setFields();
        checkForInterval();
        chekForError();
    }

    /**
     * проверка на ошибочный выбор диапазона
     */
    private void chekForError() {
        if (mRightDate.before(mLeftDate)) {
            mRightDate = mLeftDate;
            clearSelected();
            setFields();
            checkForInterval();
            SuperActivityToast superActivityToast = new SuperActivityToast(mActivity, Style.deepOrange());
            superActivityToast.setText(mActivity.getResources().getString(R.string.date_error));
            superActivityToast.setFrame(Style.FRAME_KITKAT);
            superActivityToast.setDuration(Style.DURATION_SHORT);
            superActivityToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
            superActivityToast.show();
        }
    }

    @Override
    public void registerObserver(DateSetObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(DateSetObserver o) {
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        if (observers != null) {
            for (DateSetObserver observer : observers)
                observer.update();
        }
    }
    public long getLeftDateForServer(){
        return DateHelper.prepareDateForRequest(mLeftDate.getTime(), 0);
    }
    public long getRightDateServer(){
        return DateHelper.prepareDateForRequest(mRightDate.getTime(), 1);
    }
    public long getLeftDate(){return mLeftDate.getTime();}
    public long getRightDate(){return mRightDate.getTime();}
}
