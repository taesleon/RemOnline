package com.cardamon.tofa.skladhelper;

/**
 * Created by dima on 14.01.18.
 */

public class ActLibGroup extends ActivityLibrary {
    @Override
    protected void setup() {
        DbHelper db = new DbHelper();
        mRows = db.getGroups();
        getSupportActionBar().setTitle("группы товаров");
    }
}
