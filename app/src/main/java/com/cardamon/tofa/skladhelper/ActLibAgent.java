package com.cardamon.tofa.skladhelper;

/**
 * Created by dima on 14.01.18.
 */

public class ActLibAgent extends ActivityLibrary {
    @Override
    protected void setup() {
        DbHelper db = new DbHelper();
        mRows = db.getAgents();
        getSupportActionBar().setTitle("контрагенты");
    }
}
