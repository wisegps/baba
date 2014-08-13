package sql;

import pubclas.Constant;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class DBHelper extends SQLiteOpenHelper{
	private static final int VERSION = 1;
	private static final String DB_NAME = "DB_baba";
	//基础表
	private static final String CREATE_TB_Base = "create table " + Constant.TB_Base + "(_id integer primary key autoincrement,Cust_id text,Title text,Content text)";
	//我的爱车
	private static final String CREATE_TB_Vehicle = "create table " + Constant.TB_Vehicle + "(_id integer primary key autoincrement,Cust_id text,obj_id int,obj_name text,car_brand text,car_series text,car_type text,engine_no text,frame_no text,insurance_company text,insurance_date text,annual_inspect_date text,maintain_company text,maintain_last_mileage text,maintain_next_mileage text,buy_date text,reg_no text,vio_location text,device_id text,serial text,maintain_last_date text,car_brand_id text,car_series_id text,car_type_id text,vio_city_name text,insurance_tel text,maintain_tel text,gas_no text,Content text)";
	//我的终端
	private static final String CREATE_TB_Devices = "create table " + Constant.TB_Devices + "(_id integer primary key autoincrement,Cust_id text,DeviceID int,Content text)";
	//我的收藏
	private static final String CREATE_TB_Collection = "create table " + Constant.TB_Collection + "(_id integer primary key autoincrement,Cust_id text,favorite_id text,name text,address text,tel text,lon text,lat text,Content text)";
	//我的消息
	private static final String CREATE_TB_Sms = "create table " + Constant.TB_Sms + "(_id integer primary key autoincrement,cust_id text,noti_id int,message text)";
	
	private static final String CREATE_TB_IllegalCity = "create table " + Constant.TB_IllegalCity + "(_id integer primary key autoincrement,json_data text)";
	public DBHelper(Context context){
		super(context,DB_NAME,null,VERSION);
	}
	public DBHelper(Context context, String name, CursorFactory factory,int version) {
		super(context, name, factory, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TB_Base);
		db.execSQL(CREATE_TB_Vehicle);
		db.execSQL(CREATE_TB_Devices);
		db.execSQL(CREATE_TB_Collection);
		db.execSQL(CREATE_TB_Sms);
		db.execSQL(CREATE_TB_IllegalCity);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
