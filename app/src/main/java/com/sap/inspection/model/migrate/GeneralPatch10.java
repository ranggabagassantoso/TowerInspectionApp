package com.sap.inspection.model.migrate;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sap.inspection.model.DbManager;

public class GeneralPatch10 extends DBPatch {

    /**
     * 8 May 2018
     * add column site_id_customer to Site table
     * */
    @Override
    public void apply(SQLiteDatabase db) {
        Log.d(getClass().getName(), "general patch 10");
        db.execSQL("ALTER TABLE "+ DbManager.mSchedule+" ADD COLUMN "+DbManager.colHiddenItemIds+" VARCHAR");
    }

    @Override
    public void revert(SQLiteDatabase db) {
        Log.d(getClass().getName(), "revert patch to 9");
        db.execSQL("ALTER TABLE " + DbManager.mSchedule + " RENAME TO " + DbManager.mSchedule +"temp");
        DbManager.createDB(db);
        db.execSQL("INSERT INTO " + DbManager.mSchedule + " SELECT "
                + DbManager.colID + ", "
                + DbManager.colUserId + ", "
                + DbManager.colSiteId + ", "
                + DbManager.colOperatorIds + ", "
                + DbManager.colProjectId + ", "
                + DbManager.colProjectName + ", "
                + DbManager.colWorkTypeId + ", "
                + DbManager.colDayDate + ", "
                + DbManager.colWorkDate + ", "
                + DbManager.colWorkDateStr + ", "
                + DbManager.colProgress + ", "
                + DbManager.colStatus + ", "
                + DbManager.colSumTask + ", "
                + DbManager.colSumDone + ", "
                + DbManager.colOperatorNumber
                + " FROM " + DbManager.mSchedule +"temp");
        db.execSQL("DROP TABLE " + DbManager.mSchedule+"temp");
    }
}