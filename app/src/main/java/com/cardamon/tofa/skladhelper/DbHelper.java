package com.cardamon.tofa.skladhelper;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.cardamon.tofa.skladhelper.moysklad.GoodLibraryTransfer;
import com.cardamon.tofa.skladhelper.moysklad.Model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by dima on 13.12.17.
 */

public class DbHelper extends SQLiteOpenHelper {
    public static String Lock = "dblock";

    public DbHelper() {
        super(MyApplication.ACTIVITY.getApplicationContext(), "database", null, 1);
    }

    public boolean checkOldDocs() {
        return MyApplication.getSqlDataBase().rawQuery("SELECT uuid FROM retail", null).getCount() <= 0;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Model.Return.CREATE_TABLE);
        db.execSQL(Model.ReturnRows.CREATE_TABLE);
        db.execSQL(Model.Demand.CREATE_TABLE);
        db.execSQL(Model.DemandRows.CREATE_TABLE);
        db.execSQL(Model.Retail.CREATE_TABLE);
        db.execSQL(Model.RetailRows.CREATE_TABLE);
        db.execSQL(Model.Agent.CREATE_TABLE);
        db.execSQL(Model.Good.CREATE_TABLE);
        db.execSQL(Model.Group.CREATE_TABLE);
        db.execSQL(Model.Store.CREATE_TABLE);
        db.execSQL(Model.User.CREATE_TABLE);
        db.execSQL(Model.RetailStore.CREATE_TABLE);
        db.execSQL(Model.Colors.CREATE_TABLE);
        db.execSQL(Model.Cashbox.CREATE_TABLE);
        db.execSQL(Model.Order.CREATE_TABLE);
        db.execSQL(Model.OrderRows.CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void insertInTable(String statement, ArrayList<String[]> list) {

        SQLiteDatabase db = MyApplication.getSqlDataBase();
        db.beginTransactionNonExclusive();
        SQLiteStatement stmt = db.compileStatement(statement);
        for (int i = 0; i < list.size(); i++) {
            stmt.bindAllArgsAsStrings(list.get(i));
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public String getToDaySum() {
        SQLiteDatabase db = MyApplication.getSqlDataBase();

        long toDay = (new Date()).getTime();
        String date1 = DateHelper.prepareDateForRequest(toDay, 0) + "";
        String date2 = DateHelper.prepareDateForRequest(toDay, 1) + "";

        String sql = "SELECT SUM(D.sum)/100 FROM demand D WHERE D.date BETWEEN ? AND ? UNION ALL " +
                "SELECT SUM(R.sum)/100 FROM retail R WHERE R.date BETWEEN ? AND ?";

        Cursor cursor = db.rawQuery(sql, new String[]{date1, date2, date1, date2});
        float sum = 0;
        while (cursor.moveToNext())
            sum += cursor.getDouble(0);
        cursor.close();
        db.close();
        return DateHelper.convertDoubleToString(sum);
    }

    public void createColors(ArrayList<String[]> list, String type) {
        int lastColor = getNextColor(type);
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        //если уже есть в базе, не вставляем

        Cursor cursor = db.rawQuery("SELECT object_id FROM colors", null);
        ArrayList<String> uuids = new ArrayList<>();
        while (cursor.moveToNext())
            uuids.add(cursor.getString(0));

        for (int i = 0; i < list.size(); i++) {
            if (uuids.contains(list.get(i)[0])) {
                list.remove(i);
                i--;
            }
        }

        db.beginTransactionNonExclusive();
        String statement = Model.Colors.INSERT_ROW_STATEMENT;
        SQLiteStatement stmt = db.compileStatement(statement);
        for (int i = 0; i < list.size(); i++) {
            stmt.bindString(1, type);
            stmt.bindString(2, list.get(i)[0]);

            if (lastColor >= 49)
                lastColor = -1;

            stmt.bindLong(3, ++lastColor);
            stmt.executeInsert();
            stmt.clearBindings();
        }
        db.setTransactionSuccessful();
        db.endTransaction();

        db.close();

    }

    private int getNextColor(String type) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        int color_id = -1;
        String sql = "SELECT DISTINCT color_id FROM colors WHERE `type`=? AND `_id` =(SELECT MAX(_id) FROM colors)";
        Cursor cursor = db.rawQuery(sql, new String[]{type});
        while (cursor.moveToNext())
            color_id = cursor.getInt(0);
        cursor.close();
        db.close();
        return color_id;
    }

    public double[] getRetailSum(long moment1, long moment2, String filter) {
        double[] sums = new double[3];
        SQLiteDatabase db = MyApplication.getSqlDataBase();

        String sql = "SELECT DISTINCT SUM(R.sum)/100, SUM(R.cash)/100, SUM(R.none_cash)/100 FROM retail AS R " +
                "INNER JOIN retail_store AS S ON R.store_id=S.uuid " +
                "WHERE date BETWEEN ? AND ? " +
                filter;

        Cursor cursor = db.rawQuery(sql, new String[]{moment1 + "", moment2 + ""});
        if (cursor != null) {
            cursor.moveToFirst();
            sums[0] = cursor.getDouble(0);
            sums[1] = cursor.getDouble(1);
            sums[2] = cursor.getDouble(2);
        }
        cursor.close();
        db.close();
        return sums;
    }

    public double getDemandSum(long moment1, long moment2, String filter) {
        double sum = 0;
        SQLiteDatabase db = MyApplication.getSqlDataBase();

        String sql = "SELECT DISTINCT SUM(D.sum)/100 FROM " +
                "demand AS D INNER JOIN agent AS S ON D.agent_id=S.uuid WHERE date BETWEEN ? AND ?" + filter;
        Cursor cursor = db.rawQuery(sql, new String[]{moment1 + "", moment2 + ""});
        if (cursor != null) {
            cursor.moveToFirst();
            sum = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return sum;
    }

    public void updateRetail(ArrayList<String[]> rows, ArrayList<String[]> expRows, long dateFrom, long dateTo) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        double sum = 0d;
        double sumNew = 0d;
        int count, countNew;
        String expr = "SELECT sum FROM retail WHERE date BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(expr, new String[]{dateFrom + "", dateTo + ""});
        count = cursor.getCount();
        countNew = rows.size();
        while (cursor.moveToNext())
            sum += cursor.getDouble(0);


        ArrayList<String> uuids = new ArrayList<>();
        for (String a[] : rows) {
            sumNew += Double.parseDouble(a[4]);
            uuids.add(a[0]);
        }

        if (sum == sumNew && count == countNew) {
            cursor.close();
            db.close();
            return;

        }

        expr = "SELECT uuid, name, description, date, sum, cash, none_cash, store_id, employee_id FROM retail WHERE date BETWEEN ? AND ?";
        cursor = db.rawQuery(expr, new String[]{dateFrom + "", dateTo + ""});
        ArrayList<String> uuidsDb = new ArrayList<>();
        //проверка на удаленные с сервера накладные
        while (cursor.moveToNext()) {
            uuidsDb.add(cursor.getString(0));
            if (!uuids.contains(cursor.getString(0))) {
                db.delete(Model.Retail.TABLE_NAME, "uuid=?", new String[]{cursor.getString(0)});
                db.delete("retail_rows", "retail_id=?", new String[]{cursor.getString(0)});
            }

        }

        //проверка на новые накладные
        ArrayList<String[]> newRow = new ArrayList<>();
        ArrayList<String[]> newExpRow = new ArrayList<>();
        for (int i = 0; i < uuids.size(); i++) {
            if (!uuidsDb.contains(uuids.get(i))) {
                newRow.add(rows.get(i));
                for (int j = 0; j < expRows.size(); j++) {
                    if (expRows.get(j)[0].equals(uuids.get(i)))
                        newExpRow.add(expRows.get(j));
                }
            }
        }
        insertInTable(Model.Retail.INSERT_ROW_STATEMENT, newRow);
        insertInTable(Model.RetailRows.INSERT_ROW_STATEMENT, newExpRow);

        if (sum != sumNew) {
            MyApplication.vibrate(MyApplication.SHORT_VIBRATE);
            MyApplication.sound();
            NotificationManager notificationManager =
                    (NotificationManager) MyApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(14, SumNotification.getNotification(getToDaySum()));
        }

        cursor.close();
        db.close();
    }


    public void updateDemand(ArrayList<String[]> rows, ArrayList<String[]> expRows, long dateFrom, long dateTo) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        double sum = 0d;
        double sumNew = 0d;
        int count, countNew;
        String expr = "SELECT DISTINCT sum FROM demand WHERE date BETWEEN ? AND ?";
        Cursor cursor = db.rawQuery(expr, new String[]{dateFrom + "", dateTo + ""});
        count = cursor.getCount();
        countNew = rows.size();
        while (cursor.moveToNext())
            sum += cursor.getDouble(0);

        ArrayList<String> uuids = new ArrayList<>();
        for (String a[] : rows) {
            sumNew += Double.parseDouble(a[4]);
            uuids.add(a[0]);
        }


        if (sum == sumNew && count == countNew) {
            cursor.close();
            db.close();
            return;

        }


        expr = "SELECT uuid, name, description, date, sum, agent_id, employee_id FROM demand WHERE date BETWEEN ? AND ?";
        cursor = db.rawQuery(expr, new String[]{dateFrom + "", dateTo + ""});
        ArrayList<String> uuidsDb = new ArrayList<>();
        //проверка на удаленные с сервера накладные
        while (cursor.moveToNext()) {
            uuidsDb.add(cursor.getString(0));
            if (!uuids.contains(cursor.getString(0))) {
                db.delete("demand", "uuid=?", new String[]{cursor.getString(0)});
                db.delete("demand_rows", "demand_id=?", new String[]{cursor.getString(0)});
            }

        }

        //проверка на новые накладные
        ArrayList<String[]> newRow = new ArrayList<>();
        ArrayList<String[]> newExpRow = new ArrayList<>();
        for (int i = 0; i < uuids.size(); i++) {
            if (!uuidsDb.contains(uuids.get(i))) {
                newRow.add(rows.get(i));
                for (int j = 0; j < expRows.size(); j++) {
                    if (expRows.get(j)[0].equals(uuids.get(i)))
                        newExpRow.add(expRows.get(j));
                }
            }
        }
        insertInTable(Model.Demand.INSERT_ROW_STATEMENT, newRow);
        insertInTable(Model.DemandRows.INSERT_ROW_STATEMENT, newExpRow);

        if (sum != sumNew) {
            MyApplication.vibrate(MyApplication.SHORT_VIBRATE);
            MyApplication.sound();
            NotificationManager notificationManager =
                    (NotificationManager) MyApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(14, SumNotification.getNotification(getToDaySum()));
        }

        cursor.close();
        db.close();
    }


    public Cursor getRetailCursor(long d1, long d2, String filter) {

        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String sql = "SELECT DISTINCT R.*, S.name AS prefix, C.color_id AS store_color, A.name AS buyer, " +
                "ROUND(100*(1-(SUM(W.price*W.qnt*(100-W.discount)/100)/SUM(W.price*W.qnt)))) AS discount " +
                "FROM retail AS R " +
                "LEFT JOIN retail_rows AS W ON W.retail_id=R.uuid " +
                "INNER JOIN retail_store AS S ON R.store_id=S.uuid " +
                "INNER JOIN agent AS A ON A.uuid=R.agent_id " +
                "INNER JOIN colors AS C ON object_id=S.uuid " +
                "WHERE R.date BETWEEN ? AND ? " +
                filter +
                "GROUP BY R.uuid " +
                "ORDER BY R.date DESC ";

        Cursor cursor = db.rawQuery(sql, new String[]{d1 + "", d2 + ""});
        return cursor;
    }

    public Cursor getDemandCursor(long d1, long d2, String filter) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String sql = "SELECT DISTINCT D._id, D.uuid, D.name, D.description, D.date, D.sum, S.name AS agent, C.color_id AS color, \n" +
                "ROUND(100*(1-(SUM(W.price*W.qnt*(100-W.discount)/100)/SUM(W.price*W.qnt)))) AS discount \n" +
                "FROM demand D INNER JOIN agent S ON S.uuid=D.agent_id \n" +
                "INNER JOIN demand_rows W ON W.demand_id=D.uuid\n" +
                "INNER JOIN colors AS C ON C.object_id=S.uuid " +
                "WHERE \n" +
                "D.date BETWEEN ? AND ?\n" +
                filter +
                " GROUP BY D.uuid \n" +
                "ORDER BY D.date DESC\n";

        Cursor cursor = db.rawQuery(sql, new String[]{d1 + "", d2 + ""});
        return cursor;
    }


    public ArrayList<HashMap<String, String>> getRetailRows(String uuid) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        ArrayList<HashMap<String, String>> positions = new ArrayList<>();
        String sql = "SELECT W.qnt, W.price, W.discount, G.name, G.code, GR.name, C.color_id AS group_color FROM retail_rows AS W \n" +
                "LEFT JOIN good AS G ON W.good_id=G.uuid \n" +
                "INNER JOIN groupe AS GR ON G.group_id=GR.uuid \n" +
                "INNER JOIN colors AS C ON C.object_id=GR.uuid " +
                "WHERE W.retail_id=?";
        Cursor cursor = db.rawQuery(sql, new String[]{uuid});
        HashMap<String, String> hm = new HashMap<>();
        while (cursor.moveToNext()) {
            String line = cursor.getInt(0) + " X " +
                    (100 - cursor.getFloat(2)) / 100 * cursor.getFloat(1) / 100 + " = ";
            float sum = cursor.getInt(0) * cursor.getFloat(1) / 100 * (100 - cursor.getFloat(2)) / 100;
            line += sum;
            hm.put("line", line);
            if (cursor.getInt(2) > 0)
                hm.put("discount", "-" + cursor.getInt(2) + "%");
            else
                hm.put("discount", "");
            hm.put("name", cursor.getString(3));
            hm.put("code", cursor.getString(4));
            hm.put("group_name", cursor.getString(5));
            hm.put("group_color", cursor.getString(6));
            hm.put("sum", sum + "");
            positions.add(hm);
            hm = new HashMap<>();
        }
        cursor.close();
        db.close();
        return positions;
    }

    public ArrayList<HashMap<String, String>> getDemandRows(String uuid) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        ArrayList<HashMap<String, String>> positions = new ArrayList<>();
        String sql = "SELECT W.qnt, W.price, W.discount, G.name, G.code, GR.name, C.color_id AS group_color FROM demand_rows AS W \n" +
                "LEFT JOIN good AS G ON W.good_id=G.uuid \n" +
                "INNER JOIN groupe AS GR ON G.group_id=GR.uuid \n" +
                "INNER JOIN colors AS C ON C.object_id=GR.uuid " +
                "WHERE W.demand_id=?";
        Cursor cursor = db.rawQuery(sql, new String[]{uuid});
        HashMap<String, String> hm = new HashMap<>();
        while (cursor.moveToNext()) {
            String line = cursor.getInt(0) + " X " +
                    (100 - cursor.getFloat(2)) / 100 * cursor.getFloat(1) / 100 + " = ";
            float sum = cursor.getInt(0) * cursor.getFloat(1) / 100 * (100 - cursor.getFloat(2)) / 100;
            line += sum;
            hm.put("line", line);
            if (cursor.getInt(2) > 0)
                hm.put("discount", "-" + cursor.getInt(2) + "%");
            else
                hm.put("discount", "");
            hm.put("name", cursor.getString(3));
            hm.put("code", cursor.getString(4));
            hm.put("group_name", cursor.getString(5));
            hm.put("group_color", cursor.getString(6));
            hm.put("sum", sum + "");
            positions.add(hm);
            hm = new HashMap<>();
        }
        cursor.close();
        db.close();
        return positions;
    }

    public void deleteLibs() {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        db.execSQL("DELETE from agent", new String[]{});
        db.execSQL("DELETE from groupe", new String[]{});
        db.execSQL("DELETE from retail_store", new String[]{});
        db.execSQL("DELETE from store", new String[]{});
        db.execSQL("DELETE from good", new String[]{});
        db.execSQL("DELETE from cashbox", new String[]{});
        db.close();
    }

    public void deleteDocs() {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        db.execSQL("DELETE from demand", new String[]{});
        db.execSQL("DELETE from demand_rows", new String[]{});
        db.execSQL("DELETE from retail", new String[]{});
        db.execSQL("DELETE from retail_rows", new String[]{});
        db.execSQL("DELETE from return", new String[]{});
        db.execSQL("DELETE from return_rows", new String[]{});
        db.close();
    }

    public void deleteUser() {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        db.execSQL("DELETE from user", new String[]{});
        db.close();
    }

    public ArrayList<ArrayList<String>> getFilterRetail(long date1, long date2) {
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String sql = "SELECT DISTINCT S._id, S.name FROM retail R\n" +
                "INNER JOIN retail_store S ON R.store_id=S.uuid\n" +
                "WHERE date BETWEEN ? AND ? ORDER BY S.name";
        Cursor cursor = db.rawQuery(sql, new String[]{date1 + "", date2 + ""});
        while (cursor.moveToNext()) {
            values.add(cursor.getString(0));
            names.add(cursor.getString(1));
        }
        result.add(values);
        result.add(names);
        cursor.close();
        db.close();
        return result;
    }

    public ArrayList<ArrayList<String>> getFilterDemand(long date1, long date2) {
        ArrayList<String> values = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String sql = "SELECT DISTINCT A._id, A.name FROM demand D\n" +
                "INNER JOIN agent A ON D.agent_id=A.uuid\n" +
                "WHERE date BETWEEN ? AND ? ORDER BY A.name";
        Cursor cursor = db.rawQuery(sql, new String[]{date1 + "", date2 + ""});
        while (cursor.moveToNext()) {
            values.add(cursor.getString(0));
            names.add(cursor.getString(1));
        }
        result.add(values);
        result.add(names);
        cursor.close();
        db.close();
        return result;
    }

    public boolean checkNotIssetPositions(String table) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        switch (table) {
            case Model.Demand.TABLE_NAME:
                try (Cursor cursor = db.rawQuery("SELECT agent_id FROM demand WHERE agent_id NOT IN (SELECT uuid FROM agent)", null)) {
                    if (cursor.getCount() > 0) {
                        db.close();
                        return true;
                    }
                }
                break;
            case Model.DemandRows.TABLE_NAME:
                try (Cursor cursor = db.rawQuery("SELECT good_id FROM demand_rows WHERE good_id NOT IN (SELECT uuid FROM good)", null)) {
                    if (cursor.getCount() > 0) {
                        db.close();
                        return true;
                    }

                }
                break;
            case Model.Retail.TABLE_NAME:
                try (Cursor cursor = db.rawQuery("SELECT store_id, agent_id FROM retail WHERE store_id NOT IN (SELECT uuid FROM retail_store) OR agent_id NOT IN(SELECT uuid FROM agent)", null)) {
                    if (cursor.getCount() > 0) {
                        db.close();
                        return true;
                    }
                }
                break;
            case Model.RetailRows.TABLE_NAME:
                try (Cursor cursor = db.rawQuery("SELECT good_id FROM retail_rows WHERE good_id NOT IN (SELECT uuid FROM good)", null)) {
                    if (cursor.getCount() > 0) {
                        db.close();
                        return true;
                    }
                }
                break;
            case Model.Return.TABLE_NAME:
                try (Cursor cursor = db.rawQuery("SELECT store_id FROM return WHERE store_id NOT IN (SELECT uuid FROM retail_store)", null)) {
                    if (cursor.getCount() > 0) {
                        db.close();
                        return true;
                    }
                }
                break;
            case Model.ReturnRows.TABLE_NAME:
                try (Cursor cursor = db.rawQuery("SELECT good_id FROM return_rows WHERE good_id NOT IN (SELECT uuid FROM good)", null)) {
                    if (cursor.getCount() > 0) {
                        db.close();
                        return true;
                    }
                    break;

                }

        }
        db.close();
        return false;
    }

    public void deleteTable(String tableName) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        db.execSQL("DELETE from " + tableName, new String[]{});
        db.close();
    }

    public void log() {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        Cursor cursor = db.rawQuery("SELECT * FROM agent", null);
        while (cursor.moveToNext())
            Log.d("mimi", cursor.getString(2));
        cursor.close();
        db.close();
    }

    public double getTotalSumForPeriod(long date1, long date2) {

        double sum = 0d;
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String request =
                "SELECT SUM(sum)/100 FROM( " +
                        "SELECT SUM(sum) AS sum FROM retail " +
                        "WHERE date BETWEEN ? AND ? " +
                        "UNION ALL " +
                        "SELECT SUM(sum) FROM demand " +
                        "WHERE date BETWEEN ? AND ?)";

        try (Cursor cursor = db.rawQuery(request, new String[]{Long.toString(date1), Long.toString(date2), Long.toString(date1), Long.toString(date2)})) {
            while (cursor.moveToNext()) {
                sum = cursor.getDouble(0);
            }
        }

        db.close();
        return sum;
    }

    public LinkedHashMap<String, Double> getSalesByGroupe(long d1, long d2) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String request = "SELECT\n" +
                "   substr(name , 0, 30) name,\n" +
                "   SUM(amount) as s \n" +
                "FROM\n" +
                "   (\n" +
                "      SELECT G.name,\n" +
                "      SUM(W.price*W.qnt*(100 - W.discount) / 100) / 100 AS amount \n" +
                "   FROM\n" +
                "      groupe G\n" +
                "\t  INNER JOIN retail_rows W \n" +
                "      ON W.good_id = D.uuid\n" +
                "\t  INNER JOIN good D \n" +
                "      ON D.group_id = G.uuid\n" +
                "\t  INNER JOIN retail T \n" +
                "      ON T.uuid = W.retail_id\n" +
                "\t  WHERE T.date BETWEEN ? AND ? \n" +
                "\t  GROUP BY 1\n" +
                "\t  UNION ALL\n" +
                "\t  SELECT G.name,\n" +
                "      SUM(W.price*W.qnt*(100 - W.discount) / 100) / 100 AS amount \n" +
                "   FROM\n" +
                "      groupe G\n" +
                "\t  INNER JOIN demand_rows W \n" +
                "      ON W.good_id = D.uuid\n" +
                "\t  INNER JOIN good D \n" +
                "      ON D.group_id = G.uuid\n" +
                "\t  INNER JOIN demand T \n" +
                "      ON T.uuid = W.demand_id\n" +
                "\t  WHERE T.agent_id = '24748071'\n" +
                "\t  AND T.date BETWEEN ? AND ? \n" +
                "\t  GROUP BY 1\n" +
                "   )\n" +
                "   GROUP BY 1\n" +
                "   ORDER BY s ASC";
        //"LIMIT 3";
        Cursor cursor = db.rawQuery(request, new String[]{d1 + "", d2 + "", d1 + "", d2 + ""});

        int i = 0;
        LinkedHashMap<String, Double> hm = new LinkedHashMap<>();
        while (cursor.moveToNext()) {
            hm.put(cursor.getString(0), cursor.getDouble(1));
        }
        cursor.close();
        db.close();
        return hm;
    }

    public LinkedHashMap<String, Double> getSalesByGood(long d1, long d2) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String request = "SELECT substr(name ,0, 30) name, SUM(amount) s FROM(\n" +
                "\tSELECT G.name, SUM(W.price*W.qnt*(100-W.discount)/100)/100 AS amount FROM good G\n" +
                "\tINNER JOIN retail_rows W ON W.good_id=G.uuid\n" +
                "\tINNER JOIN retail T ON T.uuid=W.retail_id\n" +
                "\tWHERE T.date BETWEEN ? AND ?\n" +
                "\tGROUP BY 1\n" +
                "\tUNION ALL\n" +
                "\tSELECT G.name, SUM(W.price*W.qnt*(100-W.discount)/100)/100 AS amount FROM good G\n" +
                "\tINNER JOIN demand_rows W ON W.good_id=G.uuid\n" +
                "\tINNER JOIN demand T ON T.uuid=W.demand_id\n" +
                "\tWHERE T.date BETWEEN ? AND ?\n" +
                "\tGROUP BY 1\n" +
                ")\n" +
                "GROUP BY 1\n" +
                "ORDER BY s DESC\n" +
                "LIMIT 3\n";
        Cursor cursor = db.rawQuery(request, new String[]{d1 + "", d2 + "", d1 + "", d2 + ""});

        int i = 0;
        LinkedHashMap<String, Double> hm = new LinkedHashMap<>();
        while (cursor.moveToNext()) {
            hm.put(cursor.getString(0), cursor.getDouble(1));
        }
        cursor.close();
        db.close();
        return hm;
    }

    public LinkedHashMap<String, Double> getSalesByAgent(long d1, long d2) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String request = "SELECT substr(name ,0, 30) name, SUM(s)/100 ss FROM(\n" +
                "SELECT A.name name, SUM(R.sum) s FROM retail R\n" +
                "INNER JOIN agent A ON A.uuid=R.agent_id\n" +
                "WHERE R.date BETWEEN ? AND ?\n" +
                "GROUP BY 1\n" +
                "UNION ALL\n" +
                "SELECT A.name name, SUM(D.sum) s FROM demand D\n" +
                "INNER JOIN agent A ON A.uuid=D.agent_id\n" +
                "WHERE D.date BETWEEN ? AND ?\n" +
                "GROUP BY 1\n" +
                ")\n" +
                "GROUP BY 1\n" +
                "ORDER BY ss DESC\n" +
                "LIMIT 3";
        Cursor cursor = db.rawQuery(request, new String[]{d1 + "", d2 + "", d1 + "", d2 + ""});

        int i = 0;
        LinkedHashMap<String, Double> hm = new LinkedHashMap<>();
        while (cursor.moveToNext()) {
            hm.put(cursor.getString(0), cursor.getDouble(1));
        }
        cursor.close();
        db.close();
        return hm;
    }

    public ArrayList<HashMap<String, String>> getGroups() {
        ArrayList<HashMap<String, String>> groups = new ArrayList<>();
        SQLiteDatabase db = MyApplication.getSqlDataBase();

        Cursor cursor = db.rawQuery("SELECT G.name, C.color_id, C.color_excl, G.uuid FROM groupe G INNER JOIN colors C ON C.object_id=G.uuid ORDER BY G.name ASC", null);
        while (cursor.moveToNext()) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("name", cursor.getString(0));
            hm.put("color", cursor.getString(1));
            hm.put("exclusive", cursor.getString(2));
            hm.put("id", cursor.getString(3));
            groups.add(hm);
        }
        db.close();
        cursor.close();
        return groups;
    }

    public void setNewColor(String colorID, String objectID) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        db.execSQL("UPDATE colors SET color_id=" + colorID + " WHERE object_id='" + objectID + "';");
        db.close();
    }

    public ArrayList<HashMap<String, String>> getRetailStores() {
        ArrayList<HashMap<String, String>> stores = new ArrayList<>();
        SQLiteDatabase db = MyApplication.getSqlDataBase();

        Cursor cursor = db.rawQuery("SELECT R.name, C.color_id, C.color_excl, R.uuid FROM retail_store R INNER JOIN colors C ON C.object_id=R.uuid ORDER BY R.name ASC", null);
        while (cursor.moveToNext()) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("name", cursor.getString(0));
            hm.put("color", cursor.getString(1));
            hm.put("exclusive", cursor.getString(2));
            hm.put("id", cursor.getString(3));
            stores.add(hm);
        }
        db.close();
        cursor.close();
        return stores;
    }

    public ArrayList<HashMap<String, String>> getAgents() {
        ArrayList<HashMap<String, String>> agents = new ArrayList<>();
        SQLiteDatabase db = MyApplication.getSqlDataBase();

        Cursor cursor = db.rawQuery("SELECT A.name, C.color_id, C.color_excl, A.uuid FROM agent A INNER JOIN colors C ON C.object_id=A.uuid ORDER BY A.name ASC", null);
        while (cursor.moveToNext()) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("name", cursor.getString(0));
            hm.put("color", cursor.getString(1));
            hm.put("exclusive", cursor.getString(2));
            hm.put("id", cursor.getString(3));
            agents.add(hm);
        }
        db.close();
        cursor.close();
        return agents;
    }

    public GoodLibraryTransfer getGoodLibrary() {


        SQLiteDatabase db = MyApplication.getSqlDataBase();

        // коллекция для групп
        ArrayList<HashMap<String, String>> groupData = new ArrayList<>();

        // коллекция для элементов одной группы
        ArrayList<HashMap<String, String>> childDataItem = null;

        // общая коллекция для коллекций элементов
        ArrayList<ArrayList<HashMap<String, String>>> childData = new ArrayList<>();
        // в итоге получится childData = ArrayList<childDataItem>

        // список атрибутов группы или элемента
        HashMap<String, String> m;

        GoodLibraryTransfer transer = new GoodLibraryTransfer();
        String prevGroupName = "";


        String request = "SELECT G.name, G.code, G.article, G.sale_price/100, G.buy_price/100, GR.name, C.color_id, G.stock FROM good G\n" +
                "INNER JOIN groupe GR ON G.group_id=GR.uuid\n" +
                "INNER JOIN colors C ON C.object_id=GR.uuid " +
                "WHERE GR.uuid NOT LIKE '' " +
                "ORDER BY GR.name ASC";
        Cursor cursor = db.rawQuery(request, null);


        while (cursor.moveToNext()) {
            if (!cursor.getString(5).equals(prevGroupName)) {
                prevGroupName = cursor.getString(5);
                m = new HashMap<>();
                m.put("name", prevGroupName);
                groupData.add(m);

                if (childDataItem != null)
                    childData.add(childDataItem);

                childDataItem = new ArrayList<>();
            }

            m = new HashMap<>();
            m.put("name", cursor.getString(0));
            m.put("code", "код: " + cursor.getString(1));
            m.put("ref", "арт: " + cursor.getString(2));
            m.put("sale", "розница: " + DateHelper.convertDoubleToString(cursor.getDouble(3)));
            m.put("buy", "закупка: " + DateHelper.convertDoubleToString(cursor.getDouble(4)));
            if (cursor.getInt(7) > 0)
                m.put("stock", cursor.getString(7));
            else
                m.put("stock", "");
            childDataItem.add(m);

        }

        childData.add(childDataItem);


        transer.groupNames = groupData;
        transer.values = childData;
        cursor.close();
        db.close();
        return transer;
    }

    public double getForecast() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);
        double sum = 0;
        int d = 0;
        for (int i = 0; i < 6; i += 2) {
            long[] l = DateHelper.getDayOfWeekByYear(calendar.get(Calendar.YEAR));
            double s = getTotalSumForPeriod(l[0], l[1]);
            sum += s;
            if (s > 0)
                d++;
            calendar.add(Calendar.YEAR, -1);
        }

        return sum / d;

    }

    public void updateStock(ArrayList<String[]> allRows) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        db.execSQL("UPDATE good SET stock=0");


        String request1 = "UPDATE good\n" +
                "    SET stock = CASE uuid \n";
        String request3 = "\tEND\n" +
                "WHERE uuid IN (SELECT uuid FROM good)";
        String request2 = "";

        for (int i = 0; i < allRows.size(); i++) {
            int stock = (int) Float.parseFloat(allRows.get(i)[1]);
            if (stock > 0) {
                request2 += "WHEN '" + allRows.get(i)[0];
                if (i != allRows.size() - 1)
                    request2 += "' THEN " + stock + " \n";
            }
        }
        String mainRequest = request1 + request2 + request3;
        db.execSQL(mainRequest);

        db.close();
    }

    public ArrayList<String[]> getCashBoxes() {
        SQLiteDatabase sQLiteDatabase = MyApplication.getSqlDataBase();
        Cursor cursor = sQLiteDatabase.rawQuery("SELECT uuid, type FROM cashbox", null);
        ArrayList arrayList = new ArrayList();
        while (cursor.moveToNext()) {
            String[] arrstring = new String[]{cursor.getString(0), cursor.getString(1)};
            arrayList.add((Object) arrstring);
        }
        sQLiteDatabase.close();
        cursor.close();
        return arrayList;
    }

    /**
     * кассовые отчет
     * 0 - наличная касса
     * 1 - безнальная касса
     *
     * @param paymentList
     * @param boxUuid
     */
    public void updateRetailSellType(ArrayList<String[]> paymentList, String boxUuid) {
        SQLiteDatabase sQLiteDatabase = MyApplication.getSqlDataBase();

        Cursor cursor = sQLiteDatabase.rawQuery("SELECT DISTINCT type FROM cashbox WHERE uuid=?", new String[]{boxUuid});
        cursor.moveToFirst();
        int boxTypeInt = cursor.getInt(0);

        for (int i = 0; i < paymentList.size(); i++) {
            long moment = Long.parseLong(paymentList.get(i)[0]);
            long sum = Long.parseLong(paymentList.get(i)[1]);
            String fieldType;

            switch (boxTypeInt) {
                case 0:
                    fieldType = "cash";
                    break;
                case 1:
                    fieldType = "none_cash";
                    break;
                default:
                    fieldType = "";
            }

            String sql = "UPDATE retail SET ";
            sql += fieldType;
            sql += "=sum WHERE date BETWEEN ";
            sql += moment - 100000L;
            sql += " AND ";
            sql += moment + 100000L;
            sql += " AND sum BETWEEN ";
            sql += sum - 10l;
            sql += " AND ";
            sql += sum + 10l;

            sQLiteDatabase.execSQL(sql);
        }
        return;
    }

    public void updateOrders(ArrayList<String[]> rows, String prefix) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String sql = "SELECT SUM(sum) FROM orders WHERE store_name=?";
        Cursor cursor = db.rawQuery(sql, new String[]{prefix});
        cursor.moveToNext();
        //текущая сумма заказов по сайту, by prefix
        double currentSum = cursor.getDouble(0);
        //удаляем все строки by prefix
        db.execSQL("DELETE from orders WHERE store_name=?", new String[]{prefix});

        this.insertInTable("INSERT INTO orders (order_id, store_name, firstname, lastname, email, phone, payment, address, city, shipping_method, comment, sum, date_added) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", rows);
        cursor = db.rawQuery(sql, new String[]{prefix});
        cursor.moveToNext();
        //новая сумма заказов
        double newSum = cursor.getDouble(0);

        if (currentSum != newSum) {
            MyApplication.vibrate(MyApplication.SHORT_VIBRATE);
            MyApplication.sound();
        }
        cursor.close();
        db.close();
    }

    public void updateOrderRows(ArrayList<String[]> rows, String prefix) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        db.execSQL("DELETE from order_rows WHERE store_prefix=?", new String[]{prefix});
        insertInTable("INSERT INTO order_rows (order_id, store_prefix, good_id, name, model, qnt, price, total, stock, domo_price) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", rows);
        db.close();
    }

    public ArrayList<HashMap<String, String>> getOrderRows(String string2, String string3) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        ArrayList arrayList = new ArrayList();
        Cursor cursor = db.rawQuery("SELECT * FROM order_rows WHERE order_id=? AND store_prefix=?", new String[]{string2, string3});
        HashMap hashMap = new HashMap();
        while (cursor.moveToNext()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(cursor.getInt(6));
            stringBuilder.append(" X ");
            stringBuilder.append(DateHelper.convertDoubleToString(cursor.getDouble(8)));
            stringBuilder.append(" = ");
            stringBuilder.append(DateHelper.convertDoubleToString(cursor.getDouble(9)));
            hashMap.put((Object) "line", (Object) stringBuilder.toString());
            hashMap.put((Object) "name", (Object) cursor.getString(4));
            hashMap.put((Object) "qnt", (Object) cursor.getString(6));
            hashMap.put((Object) "model", (Object) cursor.getString(5));
            hashMap.put((Object) "stock", (Object) cursor.getString(7));
            hashMap.put((Object) "price", (Object) cursor.getString(8));
            hashMap.put((Object) "total", (Object) cursor.getString(9));
            hashMap.put((Object) "attention", (Object) "instock");
            if (cursor.getInt(7) == 0) {
                hashMap.put((Object) "attention", (Object) "notstock");
            }
            arrayList.add((Object) hashMap);
            hashMap = new HashMap();
        }
        cursor.close();
        db.close();
        return arrayList;
    }

    public HashMap<String, String> getOrderInfo(String string2, String string3) {
        HashMap hashMap = new HashMap();
        SQLiteDatabase sQLiteDatabase = MyApplication.getSqlDataBase();
        Cursor cursor = sQLiteDatabase.rawQuery("SELECT lastname, firstname, phone, email, city, address, payment, shipping_method FROM orders WHERE order_id=? AND store_name=?", new String[]{string2, string3});
        while (cursor.moveToNext()) {
            hashMap.put((Object) "lastname", (Object) cursor.getString(0));
            hashMap.put((Object) "firstname", (Object) cursor.getString(1));
            hashMap.put((Object) "phone", (Object) cursor.getString(2));
            hashMap.put((Object) "email", (Object) cursor.getString(3));
            hashMap.put((Object) "city", (Object) cursor.getString(4));
            hashMap.put((Object) "address", (Object) cursor.getString(5));
            hashMap.put((Object) "payment", (Object) cursor.getString(6));
            hashMap.put((Object) "shipping", (Object) cursor.getString(7));
        }
        sQLiteDatabase.close();
        cursor.close();
        return hashMap;
    }

    public double getOrderSum(long l, long l2) {
        SQLiteDatabase sQLiteDatabase = MyApplication.getSqlDataBase();
        String[] arrstring = new String[2];
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(l);
        stringBuilder.append("");
        arrstring[0] = stringBuilder.toString();
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(l2);
        stringBuilder2.append("");
        arrstring[1] = stringBuilder2.toString();
        Cursor cursor = sQLiteDatabase.rawQuery("SELECT SUM(R.total) FROM order_rows AS R INNER JOIN orders AS O ON O.order_id=R.order_id AND O.store_name=R.store_prefix WHERE O.date_added BETWEEN ? AND ?", arrstring);
        cursor.moveToFirst();
        double d = cursor.getDouble(0);
        cursor.close();
        sQLiteDatabase.close();
        return d;
    }

    public double getOrderRealSum(long l, long l2) {
        SQLiteDatabase sQLiteDatabase = MyApplication.getSqlDataBase();
        String[] arrstring = new String[2];
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(l);
        stringBuilder.append("");
        arrstring[0] = stringBuilder.toString();
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(l2);
        stringBuilder2.append("");
        arrstring[1] = stringBuilder2.toString();
        Cursor cursor = sQLiteDatabase.rawQuery("SELECT SUM(R.total) FROM order_rows R JOIN orders O ON R.order_id=O.order_id AND R.store_prefix=O.store_name WHERE O.date_added BETWEEN ? AND ? AND R.stock>0", arrstring);
        cursor.moveToFirst();
        double d = cursor.getDouble(0);
        cursor.close();
        sQLiteDatabase.close();
        return d;
    }

    public Cursor getOrderCursor(long date1, long date2) {
        SQLiteDatabase db = MyApplication.getSqlDataBase();
        String sql = "SELECT O.*, SUM(R.total) AS full_sum, SUM(R.stock*R.price) AS real_sum  FROM orders AS O LEFT JOIN order_rows AS R ON R.order_id=O.order_id AND O.store_name=R.store_prefix WHERE O.date_added BETWEEN ? AND ? GROUP BY O.order_id ORDER BY date_added DESC";
        return db.rawQuery(sql, new String[]{date1 + "", date2 + ""});
    }

    public ArrayList<ArrayList<HashMap<String, String>>> getOwnerSums(long date1, long date2) {
        ArrayList res = new ArrayList();
        SQLiteDatabase sQLiteDatabase = MyApplication.getSqlDataBase();
        Cursor cursor = sQLiteDatabase.rawQuery("SELECT owner FROM groupe WHERE owner NOT NULL AND owner<>'' GROUP BY owner", null);
        while (cursor.moveToNext()) {
            ArrayList semiRes = new ArrayList();
            String sql = "SELECT E.name AS grp, ROUND(SUM(R.price*R.qnt*(100-R.discount)/10000),0) full_sum, SUM(CASE WHEN T.cash>0 THEN ROUND((R.price*R.qnt*(100-R.discount)/10000),0) ELSE 0 END) AS cash, SUM(CASE WHEN T.none_cash>0 THEN ROUND((R.price*R.qnt*(100-R.discount)/10000),0) ELSE 0 END) AS none_cash FROM retail_rows AS R LEFT JOIN retail T ON T.uuid=R.retail_id LEFT JOIN good G ON G.uuid=R.good_id LEFT JOIN groupe E ON E.uuid=G.group_id WHERE E.uuid IN(SELECT uuid FROM groupe WHERE owner='";
            sql +=cursor.getString(0);
            sql +="') AND T.date BETWEEN ? AND ? GROUP BY grp ORDER BY E.name";

            Cursor cursor2 = sQLiteDatabase.rawQuery(sql,new String[]{date1+"", date2+""});
            while (cursor2.moveToNext()) {
                HashMap hashMap = new HashMap();
                hashMap.put("name", cursor2.getString(0));
                hashMap.put("sum", cursor2.getString(1));
                hashMap.put("cash", cursor2.getString(2));
                hashMap.put("none_cash", cursor2.getString(3));
                semiRes.add(hashMap);
            }
            cursor2.close();
            res.add(semiRes);
        }
        cursor.close();
        sQLiteDatabase.close();
        return res;
    }
}
