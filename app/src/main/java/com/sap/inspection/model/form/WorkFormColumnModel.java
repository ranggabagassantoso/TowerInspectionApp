package com.sap.inspection.model.form;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.sap.inspection.TowerApplication;
import com.sap.inspection.model.BaseModel;
import com.sap.inspection.model.DbManager;
import com.sap.inspection.model.DbRepository;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class WorkFormColumnModel extends BaseModel {
	
    public int id;
    public String column_name;
    public int position;
    public String work_form_group_id;
    public String created_at;
    public String updated_at;

	public static String createDB(){
		return "create table if not exists " + DbManager.mWorkFormColumn
				+ " (" + DbManager.colID + " integer, "
				+ DbManager.colName + " varchar, "
				+ DbManager.colPosition + " integer, "
				+ DbManager.colWorkFormGroupId + " integer, "
				+ DbManager.colCreatedAt + " varchar, "
				+ DbManager.colUpdatedAt + " varchar, "
				+ "PRIMARY KEY (" + DbManager.colID + "))";
	}
	
	public void save(Context context){
		save();
	}

	public static void delete(Context ctx){
		DbRepository.getInstance().open(TowerApplication.getInstance());
		String sql = "DELETE FROM " + DbManager.mWorkFormColumn;
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		stmt.executeUpdateDelete();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public void save(){

		String sql = String
				.format("INSERT OR REPLACE INTO %s(%s,%s,%s,%s,%s,%s) VALUES(?,?,?,?,?,?)",
						DbManager.mWorkFormColumn, DbManager.colID,
						DbManager.colName, DbManager.colPosition, 
						DbManager.colWorkFormGroupId, DbManager.colCreatedAt,
						DbManager.colUpdatedAt);

		DbRepository.getInstance().open(TowerApplication.getInstance());
		SQLiteStatement stmt = DbRepository.getInstance().getDB().compileStatement(sql);
		
		stmt.bindLong(1, id);
		bindAndCheckNullString(stmt, 2, column_name);
		stmt.bindLong(3, position);
		bindAndCheckNullString(stmt, 4, work_form_group_id);
		bindAndCheckNullString(stmt, 5, created_at);
		bindAndCheckNullString(stmt, 6, updated_at);

		stmt.executeInsert();
		stmt.close();
		DbRepository.getInstance().close();
	}

	public ArrayList<WorkFormColumnModel> getAllItemByWorkFormGroupId(Context context, int workFormGroupId) {

		ArrayList<WorkFormColumnModel> result = getAllItemByWorkFormGroupId(workFormGroupId);

		return result;
	}

	public static ArrayList<WorkFormColumnModel> getAllItemByWorkFormGroupId(int workFormGroupId) {

		ArrayList<WorkFormColumnModel> result = new ArrayList<WorkFormColumnModel>();
		String table = DbManager.mWorkFormColumn;
		String[] columns = null;
		String where =DbManager.colWorkFormGroupId + "=?";
		String[] args = new String[] {String.valueOf(workFormGroupId)};
		String order = DbManager.colPosition+" ASC";

		DbRepository.getInstance().open(TowerApplication.getInstance());
		Cursor cursor = DbRepository.getInstance().getDB().query(table, columns, where, args, null, null, order, null);

		if (!cursor.moveToFirst()) {
			cursor.close();
			DbRepository.getInstance().close();
			return result;
		}
		do {
			result.add(getColumnFromCursor(cursor));
		} while(cursor.moveToNext());

		cursor.close();
		DbRepository.getInstance().close();
		return result;
	}

	private static WorkFormColumnModel getColumnFromCursor(Cursor c) {
		WorkFormColumnModel item= new WorkFormColumnModel();

		if (null == c)
			return item;

		item.id = (c.getInt(c.getColumnIndex(DbManager.colID)));
		item.position = (c.getInt(c.getColumnIndex(DbManager.colPosition)));
		item.column_name = (c.getString(c.getColumnIndex(DbManager.colName)));
		item.work_form_group_id = (c.getString(c.getColumnIndex(DbManager.colWorkFormGroupId)));
		item.created_at = (c.getString(c.getColumnIndex(DbManager.colCreatedAt)));
		item.updated_at = (c.getString(c.getColumnIndex(DbManager.colUpdatedAt)));

		return item;
	}

}
