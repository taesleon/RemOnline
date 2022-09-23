package com.cardamon.tofa.skladhelper.moysklad;

import android.provider.BaseColumns;

/**
 * Created by dima on 13.12.17.
 */

public class Model {

    private Model() {
    }

    public static class Colors implements BaseColumns {
        public static final String TABLE_NAME = "colors";
        public static final String TYPE = "type";
        public static final String COLOR_ID = "color_id";
        public static final String EXCL_ID = "color_excl";
        public static final String CREATE_TABLE = "CREATE TABLE colors (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT, " +
                "object_id TEXT, " +
                "color_id STRING," +
                "color_excl STRING" +
                ")";
        public static final String INSERT_ROW_STATEMENT ="INSERT INTO colors(type, object_id, color_id) VALUES (?,?,?)";
    }

    public static class Retail implements BaseColumns {
        public static final String TABLE_NAME = "retail";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String DATE = "date";
        public static final String SUM = "sum";
        public static final String CASH = "cash";
        public static final String NONE_CASH = "none_cash";
        public static final String STORE_ID = "store_id";
        public static final String EMPLOYEE_ID = "employee_id";
        public static final String AGENT_ID = "agent_id";

        public static final String CREATE_TABLE = "CREATE TABLE " + Retail.TABLE_NAME + " (" +
                Retail._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Retail.UUID + " TEXT," +
                Retail.NAME + " TEXT," +
                Retail.DESCRIPTION + " TEXT," +
                Retail.DATE + " TEXT," +
                Retail.SUM + " REAL," +
                Retail.CASH + " REAL," +
                Retail.NONE_CASH + " REAL," +
                Retail.STORE_ID + " TEXT," +
                Retail.AGENT_ID + " TEXT," +
                Retail.EMPLOYEE_ID + " TEXT" +
                ");";

        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + Retail.TABLE_NAME + " (" +
                Retail.UUID + ", " +
                Retail.NAME + ", " +
                Retail.DESCRIPTION + ", " +
                Retail.DATE + ", " +
                Retail.SUM + ", " +
                Retail.CASH + ", " +
                Retail.NONE_CASH + ", " +
                Retail.STORE_ID + ", " +
                Retail.AGENT_ID + ", " +
                Retail.EMPLOYEE_ID +
                ") values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    }

    public static class Return implements BaseColumns {
        public static final String TABLE_NAME = "return";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String DATE = "date";
        public static final String SUM = "sum";
        public static final String STORE_ID = "store_id";
        public static final String DEMAND_ID = "demand_id";

        public static final String CREATE_TABLE = "CREATE TABLE " + Return.TABLE_NAME + " (" +
                Return._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Return.UUID + " TEXT," +
                Return.NAME + " TEXT," +
                Return.DESCRIPTION + " TEXT," +
                Return.DATE + " TEXT," +
                Return.SUM + " REAL," +
                Return.STORE_ID + " TEXT," +
                Return.DEMAND_ID + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + Return.TABLE_NAME + " (" +
                Return.UUID + ", " +
                Return.NAME + ", " +
                Return.DESCRIPTION + ", " +
                Return.DATE + ", " +
                Return.SUM + ", " +
                Return.STORE_ID + ", " +
                Return.DEMAND_ID +
                ") values(?, ?, ?, ?, ?, ?, ?)";
    }

    public static class Demand implements BaseColumns {
        public static final String TABLE_NAME = "demand";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String DESCRIPTION = "description";
        public static final String DATE = "date";
        public static final String SUM = "sum";
        public static final String AGENT_ID = "agent_id";
        public static final String EMPLOYEE_ID = "employee_id";

        public static final String CREATE_TABLE = "CREATE TABLE " + Demand.TABLE_NAME + " (" +
                Demand._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Demand.UUID + " TEXT," +
                Demand.NAME + " TEXT," +
                Demand.DESCRIPTION + " TEXT," +
                Demand.DATE + " TEXT," +
                Demand.SUM + " REAL," +
                Demand.AGENT_ID + " TEXT," +
                Demand.EMPLOYEE_ID + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + Demand.TABLE_NAME + " (" +
                Demand.UUID + ", " +
                Demand.NAME + ", " +
                Demand.DESCRIPTION + ", " +
                Demand.DATE + ", " +
                Demand.SUM + ", " +
                Demand.AGENT_ID + ", " +
                Demand.EMPLOYEE_ID +
                ") values(?, ?, ?, ?, ?, ?, ?)";
    }

    public static class RetailRows implements BaseColumns {
        public static final String TABLE_NAME = "retail_rows";
        public static final String RETAIL_ID = "retail_id";
        public static final String QNT = "qnt";
        public static final String PRICE = "price";
        public static final String DISCOUNT = "discount";
        public static final String GOOD_ID = "good_id";

        public static final String CREATE_TABLE = "CREATE TABLE " + RetailRows.TABLE_NAME + " (" +
                RetailRows._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RetailRows.RETAIL_ID + " TEXT," +
                RetailRows.QNT + " INTEGER," +
                RetailRows.PRICE + " REAL," +
                RetailRows.DISCOUNT + " REAL," +
                RetailRows.GOOD_ID + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + RetailRows.TABLE_NAME + " (" +
                RetailRows.RETAIL_ID + ", " +
                RetailRows.QNT + ", " +
                RetailRows.PRICE + ", " +
                RetailRows.DISCOUNT + ", " +
                RetailRows.GOOD_ID +
                ") values(?, ?, ?, ?, (SELECT DISTINCT uuid FROM good WHERE code=?))";
    }

    public static class ReturnRows implements BaseColumns {
        public static final String TABLE_NAME = "return_rows";
        public static final String RETURN_ID = "return_id";
        public static final String QNT = "qnt";
        public static final String PRICE = "price";
        public static final String DISCOUNT = "discount";
        public static final String GOOD_ID = "good_id";

        public static final String CREATE_TABLE = "CREATE TABLE " + ReturnRows.TABLE_NAME + " (" +
                ReturnRows._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ReturnRows.RETURN_ID + " TEXT," +
                ReturnRows.QNT + " INTEGER," +
                ReturnRows.PRICE + " REAL," +
                ReturnRows.DISCOUNT + " REAL," +
                ReturnRows.GOOD_ID + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + ReturnRows.TABLE_NAME + " (" +
                ReturnRows.RETURN_ID + ", " +
                ReturnRows.QNT + ", " +
                ReturnRows.PRICE + ", " +
                ReturnRows.DISCOUNT + ", " +
                ReturnRows.GOOD_ID +
                ") values(?, ?, ?, ?, ?)";
    }

    public static class DemandRows implements BaseColumns {
        public static final String TABLE_NAME = "demand_rows";
        public static final String DEMAND_ID = "demand_id";
        public static final String QNT = "qnt";
        public static final String PRICE = "price";
        public static final String DISCOUNT = "discount";
        public static final String GOOD_ID = "good_id";

        public static final String CREATE_TABLE = "CREATE TABLE " + DemandRows.TABLE_NAME + " (" +
                DemandRows._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DemandRows.DEMAND_ID + " TEXT," +
                DemandRows.QNT + " INTEGER," +
                DemandRows.PRICE + " REAL," +
                DemandRows.DISCOUNT + " REAL," +
                DemandRows.GOOD_ID + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + DemandRows.TABLE_NAME + " (" +
                DemandRows.DEMAND_ID + ", " +
                DemandRows.QNT + ", " +
                DemandRows.PRICE + ", " +
                DemandRows.DISCOUNT + ", " +
                DemandRows.GOOD_ID +
                ") values(?, ?, ?, ?, (SELECT DISTINCT uuid FROM good WHERE code=?))";
    }

    public static class Agent implements BaseColumns {
        public static final String TABLE_NAME = "agent";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String SUPPLIER = "supplier";

        public static final String CREATE_TABLE = "CREATE TABLE " + Agent.TABLE_NAME + " (" +
                Agent._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Agent.UUID + " TEXT, " +
                Agent.NAME + " TEXT," +
                Agent.SUPPLIER + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + Agent.TABLE_NAME + " (" +
                Agent.UUID + ", " +
                Agent.NAME + ", " +
                Agent.SUPPLIER +
                ") values(?, ?, ?)";
    }

    public static class Store implements BaseColumns {
        public static final String TABLE_NAME = "store";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String CREATE_TABLE = "CREATE TABLE " + Store.TABLE_NAME + " (" +
                Store._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Store.UUID + " TEXT, " +
                Store.NAME + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + Store.TABLE_NAME + " (" +
                Store.UUID + ", " +
                Store.NAME +
                ") values(?, ?)";
    }

    public static class RetailStore implements BaseColumns {
        public static final String TABLE_NAME = "retail_store";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String CREATE_TABLE = "CREATE TABLE " + RetailStore.TABLE_NAME + " (" +
                RetailStore._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                RetailStore.UUID + " TEXT, " +
                RetailStore.NAME + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + RetailStore.TABLE_NAME + " (" +
                RetailStore.UUID + ", " +
                RetailStore.NAME +
                ") values(?, ?)";
    }

    public static class Group implements BaseColumns {
        public static final String TABLE_NAME = "groupe";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String OWNER = "owner";

        public static final String CREATE_TABLE = "CREATE TABLE " + Group.TABLE_NAME + " (" +
                Group._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Group.UUID + " TEXT, " +
                Group.NAME + " TEXT," +
                Group.OWNER + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + Group.TABLE_NAME + " (" +
                Group.UUID + ", " +
                Group.NAME + ", " +
                Group.OWNER +
                ") values(?, ?, ?)";
    }

    public static class User implements BaseColumns {
        public static final String TABLE_NAME = "user";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String CREATE_TABLE = "CREATE TABLE " + User.TABLE_NAME + " (" +
                User._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                User.UUID + " TEXT, " +
                User.NAME + " TEXT" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + User.TABLE_NAME + " (" +
                User.UUID + ", " +
                User.NAME +
                ") values(?, ?)";
    }

    public static class Good implements BaseColumns {
        public static final String TABLE_NAME = "good";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String CODE = "code";
        public static final String ARTICLE = "article";
        public static final String GROUP_ID = "group_id";
        public static final String SALE = "sale_price";
        public static final String BUY = "buy_price";
        public static final String STOCK = "stock";


        public static final String CREATE_TABLE = "CREATE TABLE " + Good.TABLE_NAME + " (" +
                Good._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Good.UUID + " TEXT, " +
                Good.NAME + " TEXT, " +
                Good.CODE + " TEXT, " +
                Good.ARTICLE + " TEXT, " +
                Good.GROUP_ID + " TEXT, " +
                Good.SALE + " REAL, " +
                Good.BUY + " REAL, " +
                Good.STOCK + " INTEGER" +
                ");";
        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + Good.TABLE_NAME + " (" +
                Good.UUID + ", " +
                Good.NAME + ", " +
                Good.CODE + ", " +
                Good.ARTICLE + ", " +
                Good.GROUP_ID + ", " +
                Good.SALE + ", " +
                Good.BUY +
                ") values(?, ?, ?, ?, ?, ?, ?)";
    }

    public static class Cashbox implements BaseColumns {
        public static final String TABLE_NAME = "cashbox";
        public static final String UUID = "uuid";
        public static final String NAME = "name";
        public static final String TYPE = "type";


        public static final String CREATE_TABLE = "CREATE TABLE " + Cashbox.TABLE_NAME + " (" +
                Cashbox._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Cashbox.UUID + " TEXT, " +
                Cashbox.NAME + " TEXT, " +
                Cashbox.TYPE + " TEXT"+
                ");";

        public static final String INSERT_ROW_STATEMENT = "INSERT INTO " + Cashbox.TABLE_NAME + " (" +
                Cashbox.UUID + ", " +
                Cashbox.NAME + ", " +
                Cashbox.TYPE+
                ") values(?, ?, ?)";
    }
}
