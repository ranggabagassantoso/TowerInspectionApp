package com.sap.inspection;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.sap.inspection.listener.FormTextChange;
import com.sap.inspection.model.DbRepository;
import com.sap.inspection.model.ScheduleBaseModel;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.form.ColumnModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.ItemUpdateResultViewModel;
import com.sap.inspection.model.form.RowModel;
import com.sap.inspection.model.value.DbRepositoryValue;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.util.ImageUtil;
import com.sap.inspection.view.FormItem;
import com.sap.inspection.view.PhotoItemRadio;
import com.sap.inspection.views.adapter.FormFillAdapter;

public class FormFillActivity extends BaseActivity implements FormTextChange{

	public static final int REQUEST_CODE = 100;
	private static final int MenuShootImage = 101;
	private LinearLayout root;
	private RowModel rowModel;
	private ArrayList<ColumnModel> column;
	private int workFormGroupId;
	private int rowId;
	private ScheduleBaseModel schedule;
	private ItemValueModel itemValueForShare;
	private Uri mImageUri;
	private HashMap<Integer, ItemUpdateResultViewModel> itemValuesProgressView;
	public ArrayList<Integer> indexes;
	public ArrayList<String> labels;
	public ArrayList<FormItem> formItems;
	private ArrayList<ItemFormRenderModel> formModels;
	private PhotoItemRadio photoItem;
	private ScrollView scroll;
	private AutoCompleteTextView search;
	private ListView list;
	
	private FormFillAdapter adapter;

	private LocationManager locationManager;
	private LocationListener locationListener;
	private LatLng currentGeoPoint;
	private int accuracy;
	private String make;
	private String model;
	private String imei;
	private TextView title;
	private String pageTitle;
	private boolean finishInflate;

	private ProgressDialog progressDialog;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (indexes == null)
			indexes = new ArrayList<Integer>();
		indexes.add(0);
		if (labels == null)
			labels = new ArrayList<String>();
		if (formItems == null)
			formItems = new ArrayList<FormItem>();
		if (formModels == null)
			formModels = new ArrayList<ItemFormRenderModel>();

		locationListener = new LocationListener() {

			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub

			}

			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

			public void onLocationChanged(Location location) {
				// TODO Update the Latitude and Longitude of the location
				accuracy = initiateLocation();
				setCurrentGeoPoint(new LatLng(location.getLatitude(), location.getLongitude()));
			}
		};
//
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//		locationManager.removeUpdates(locationListener);
		//dummygeopoint
//		currentGeoPoint = new GeoPoint(0, 0);
		// initiate the location using GPS
		setCurrentGeoPoint(new LatLng(0, 0));
		accuracy = initiateLocation();
		Log.d(getClass().getName(), "oncreate "+ String.valueOf(getCurrentGeoPoint().latitude)+" || "+String.valueOf(getCurrentGeoPoint().longitude));

		setContentView(R.layout.activity_form_fill);

		list = (ListView) findViewById(R.id.list);
		list.setOnItemSelectedListener(itemSelected);
		adapter = new FormFillAdapter(this);
		adapter.setPhotoListener(photoClickListener);
        adapter.setUploadListener(uploadClickListener);
		list.setAdapter(adapter);
		progressDialog = new ProgressDialog(activity);
		Bundle bundle = getIntent().getExtras();
		rowId = bundle.getInt("rowId");
		workFormGroupId = bundle.getInt("workFormGroupId");

		DbRepository.getInstance().open(activity);
		DbRepositoryValue.getInstance().open(activity);
		schedule = new ScheduleGeneral();
		schedule = schedule.getScheduleById(bundle.getString("scheduleId"));
		adapter.setWorkType(schedule.work_type.name);

		scroll = (ScrollView) findViewById(R.id.scroll);
		search = (AutoCompleteTextView) findViewById(R.id.search);
		search.setOnItemClickListener(searchClickListener);
		root = (LinearLayout) findViewById(R.id.root);
		title = (TextView) findViewById(R.id.header_title);

		progressDialog.setMessage("Generating form...");
		progressDialog.show();
		progressDialog.setCancelable(false);
		
		FormLoader loader = new FormLoader();
		loader.execute();
	}
	
	OnItemSelectedListener itemSelected = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> listView, View view, int position, long id)
		{
			log("==================== on item selected");
			FormFillAdapter adapter = (FormFillAdapter) listView.getAdapter();
		    if (adapter.getItemViewType(position) == ItemFormRenderModel.TYPE_TEXT_INPUT)
		    {
		    	log("here is the text input");
		        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		        view.findViewById(R.id.item_form_input).requestFocus();
		    }
		    else if (adapter.getItemViewType(position) == ItemFormRenderModel.TYPE_PICTURE_RADIO){
		    	log("here is the picture");
		        listView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
		        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioGroup);
		        if (radioGroup.getCheckedRadioButtonId() == R.id.radioNOK)
		        	view.findViewById(R.id.remark).requestFocus();
		        else
			    {
			        if (!listView.isFocused())
			        {
			            // listView.setItemsCanFocus(false);

			            // Use beforeDescendants so that the EditText doesn't re-take focus
			            listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
			            listView.requestFocus();
			        }
			    }

		    }
		    else
		    {
		        if (!listView.isFocused())
		        {
		            // listView.setItemsCanFocus(false);

		            // Use beforeDescendants so that the EditText doesn't re-take focus
		            listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		            listView.requestFocus();
		        }
		    }
		}

		public void onNothingSelected(AdapterView<?> listView)
		{
			log("==================== on nothing selected");
		    // This happens when you start scrolling, so we need to prevent it from staying
		    // in the afterDescendants mode if the EditText was focused 
		    listView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		}
	};


	private void setPercentage(int rowId){
		if (null != itemValuesProgressView.get(rowId) && finishInflate){
			int taskDone = itemValueForShare.countTaskDone(schedule.id, rowId);
			if (taskDone != 0){
				itemValuesProgressView.get(rowId).colored.setText(String.valueOf(
						itemValuesProgressView.get(rowId).getPercentage(
								taskDone)+"%"));
				//TODO change to match the database
				SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
				itemValuesProgressView.get(rowId).plain.setText(" on "+df.format(Calendar.getInstance().getTime().getTime()));
			}else{
				itemValuesProgressView.get(rowId).colored.setText("");
				itemValuesProgressView.get(rowId).plain.setText("No action yet");
			}

		}
	}

	OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (buttonView.getTag() != null){
				log((String)buttonView.getTag());
				String[] split = ((String)buttonView.getTag()).split("[|]");
				for (int i = 0; i < split.length; i++) {
					log("=== "+split[i]);
				}
				saveValue(split, isChecked, true);
			}
		}
	};

	private void saveValue(String[] itemProperties,boolean isAdding,boolean isCompundButton){


		if (itemProperties.length < 5){
			log("invalid component to saved");
			return;
		}
		//
		if (itemValueForShare == null)
			itemValueForShare = new ItemValueModel();

		itemValueForShare = itemValueForShare.getItemValue(schedule.id, Integer.parseInt(itemProperties[1]), Integer.parseInt(itemProperties[2]));
		if (itemValueForShare == null){
			itemValueForShare = new ItemValueModel();
			itemValueForShare.scheduleId = schedule.id;
			itemValueForShare.rowId = Integer.parseInt(itemProperties[0]);
			itemValueForShare.itemId = Integer.parseInt(itemProperties[1]);
			itemValueForShare.operatorId = Integer.parseInt(itemProperties[2]);
			itemValueForShare.value = "";
			itemValueForShare.typePhoto = itemProperties[4].equalsIgnoreCase("1");
		}
		log("=================================================================");
		log("===== value : "+itemValueForShare.value);
		if (isCompundButton){
			if (isAdding){ //adding value on check box
				log("goto adding");
				// value still null or blank
				if (itemValueForShare.value == null | itemValueForShare.value.equalsIgnoreCase(""))
					itemValueForShare.value = itemProperties[3];
				// any value apply before
				else{
					String[] chkBoxValue = itemValueForShare.value.split("[,]");
					for(int i = 0; i < chkBoxValue.length; i++){ 
						if (chkBoxValue[i].equalsIgnoreCase(itemProperties[3]))
							break;
						if (i == chkBoxValue.length - 1)
							itemValueForShare.value += ","+itemProperties[3];
					}
				}
				itemValueForShare.uploadStatus = ItemValueModel.UPLOAD_NONE;
				itemValueForShare.save();
			}else{ // deleting on checkbox
				log("goto deleting");
				String[] chkBoxValue = itemValueForShare.value.split("[,]");
				itemValueForShare.value = "";
				//removing unchecked checkbox value
				for(int i = 0; i < chkBoxValue.length; i++){ 
					if (!chkBoxValue[i].equalsIgnoreCase(itemProperties[3]))
						if (i == chkBoxValue.length - 1 || chkBoxValue[chkBoxValue.length - 1].equalsIgnoreCase(itemProperties[3]))
							itemValueForShare.value += chkBoxValue[i];
						else
							itemValueForShare.value += chkBoxValue[i]+','; 
				}
				chkBoxValue = null;
				if (itemValueForShare.value.equalsIgnoreCase(""))
					itemValueForShare.delete(schedule.id, itemValueForShare.itemId, itemValueForShare.operatorId);
				else{
					itemValueForShare.uploadStatus = ItemValueModel.UPLOAD_NONE;
					itemValueForShare.save();
				}
			}
		}
		else{
			if (!isAdding)
				itemValueForShare.delete(schedule.id, itemValueForShare.itemId, itemValueForShare.operatorId);
			else{
				itemValueForShare.value = itemProperties[3];
				itemValueForShare.uploadStatus = ItemValueModel.UPLOAD_NONE;
				itemValueForShare.save();
			}
		}
		log("===== value : "+itemValueForShare.value);
		log("row id : "+ itemValueForShare.rowId);
		log("task done : "+itemValueForShare.countTaskDone(schedule.id, itemValueForShare.rowId));
		setPercentage(itemValueForShare.rowId);
	}

	private int getTaskDone(int rowId,String scheduleId){
		ItemValueModel valueModel = new ItemValueModel();
		return valueModel.countTaskDone(scheduleId, rowId);

	}

	@Override
	public void onTextChange(String string, View view) {
		if (view.getTag() != null){
			log((String)view.getTag());
			String[] split = ((String)view.getTag()).split("[|]");
			split[3] = string;
			for (int i = 0; i < split.length; i++) {
				log("=== "+split[i]);
			}
			saveValue(split, !string.equalsIgnoreCase(""),false);
		}
	}
	protected void onStop() {
		super.onStop();
		DbRepository.getInstance().close();
		DbRepositoryValue.getInstance().close();
	};

	OnClickListener photoClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			photoItem = (PhotoItemRadio) v.getTag();
			takePicture(photoItem.getItemId());
		}
	};

    OnClickListener uploadClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
			Toast.makeText(FormFillActivity.this,"upload",Toast.LENGTH_SHORT).show();
//			photoItem = (PhotoItemRadio) v.getTag();
//			takePicture(photoItem.getItemId());
        }
    };


	public boolean takePicture(int itemId){
		//		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		//		startActivityForResult(intent,CAMERA);

		Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		File photo;
		try
		{
			// place where to store camera taken picture
			photo = this.createTemporaryFile("picture-"+schedule.id+"-"+itemId+"-"+Calendar.getInstance().getTimeInMillis()+"-", ".jpg");
			log("photo url : "+photo.getName());
			photo.delete();
		}
		catch(Exception e)
		{
            log(e.getMessage());
			log("Can't create file to take picture!");
			Toast.makeText(activity, "Please check SD card! Image shot is impossible!", 10000).show();
			return false;
		}
		mImageUri = Uri.fromFile(photo);
		log("mimage url : "+mImageUri.getPath());
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

		//        intent.putExtra("crop", "true");
		intent.putExtra("outputX", 1080);
		intent.putExtra("outputY", 720);
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("outputFormat",Bitmap.CompressFormat.JPEG.toString());
		//start camera intent
		//	    activity.startActivityForResult(this, intent, MenuShootImage);
		activity.startActivityForResult(intent, MenuShootImage);
		return true;
	}

	private File createTemporaryFile(String part, String ext) throws Exception
	{
		File tempDir= Environment.getExternalStorageDirectory();
		tempDir=new File(tempDir.getAbsolutePath()+"/.temp"); // create temp folder
		if(!tempDir.exists())
		{
			tempDir.mkdir();
		}
        tempDir=new File(tempDir.getAbsolutePath()+"/"+schedule.id+"/"); // create schedule folder
        if(!tempDir.exists())
        {
            tempDir.mkdir();
        }
		return File.createTempFile(part, ext, tempDir);
	}

	//called after camera intent finished
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(requestCode==MenuShootImage && resultCode==RESULT_OK)
		{
			if (photoItem != null && mImageUri != null){
				photoItem.initValue();
				photoItem.deletePhoto();
				ImageUtil.resizeAndSaveImage(mImageUri.toString(), schedule.id);
				Log.d(getClass().getName(), String.valueOf(currentGeoPoint.latitude)+" || "+String.valueOf(currentGeoPoint.longitude));
				photoItem.setImage(mImageUri.toString(),String.valueOf(currentGeoPoint.latitude),String.valueOf(currentGeoPoint.longitude),accuracy);
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	/*
	 * called when image is stored
	 */
	public boolean storeByteImage(byte[] data){
		// Create the <timestamp>.jpg file and modify the exif data
		String filename = "/sdcard"+String.format("/%d.jpg", System.currentTimeMillis());
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(filename);
			try {
				fileOutputStream.write(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fileOutputStream.flush();
			fileOutputStream.close();
			ExifInterface exif = new ExifInterface(filename);
			createExifData(exif);
			exif.saveAttributes();
			return true;

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * called when exif data profile is created
	 */
	public void createExifData(ExifInterface exif){
		// create a reference for Latitude and Longitude
		double lat = currentGeoPoint.latitude;
		if (lat < 0) {
			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
			lat = -lat;
		} else {
			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
		}

		exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE,
				formatLatLongString(lat));

		double lon = currentGeoPoint.longitude;
		if (lon < 0) {
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
			lon = -lon;
		} else {
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
		}
		exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE,
				formatLatLongString(lon));
		try {
			exif.saveAttributes();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		make = android.os.Build.MANUFACTURER; // get the make of the device
		model = android.os.Build.MODEL; // get the model of the divice

		exif.setAttribute(ExifInterface.TAG_MAKE, make);
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();
		exif.setAttribute(ExifInterface.TAG_MODEL, model+" - "+imei);

		exif.setAttribute(ExifInterface.TAG_DATETIME, (new Date(System.currentTimeMillis())).toString()); // set the date & time

	}

	/*
	 * format the Lat Long values according to standard exif format
	 */
	private static String formatLatLongString(double d) {
		// format latitude and longitude according to exif format
		StringBuilder b = new StringBuilder();
		b.append((int) d);
		b.append("/1,");
		d = (d - (int) d) * 60;
		b.append((int) d);
		b.append("/1,");
		d = (d - (int) d) * 60000;
		b.append((int) d);
		b.append("/1000");
		return b.toString();
	}

	public int initiateLocation(){
		if (locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null){
//			setCurrentGeoPoint(new LatLng( 
//					(int)(locationManager.getLastKnownLocation(
//							LocationManager.GPS_PROVIDER).getLatitude()*1000000.0),
//							(int)(locationManager.getLastKnownLocation(
//									LocationManager.GPS_PROVIDER).getLongitude()*1000000.0)));
			return (int) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getAccuracy();
		}
		else if (locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) != null){
//			setCurrentGeoPoint(new LatLng( 
//					(int)(locationManager.getLastKnownLocation(
//							LocationManager.NETWORK_PROVIDER).getLatitude()*1000000.0),
//							(int)(locationManager.getLastKnownLocation(
//									LocationManager.NETWORK_PROVIDER).getLongitude()*1000000.0)));
			return (int) locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getAccuracy();
		}
		setCurrentGeoPoint(new LatLng(0,0));
		return 0;

	}

	public void setCurrentGeoPoint(LatLng currentGeoPoint) {
		this.currentGeoPoint = currentGeoPoint;
	}

	public LatLng getCurrentGeoPoint() {
		return currentGeoPoint;
	}

	private class FormLoader extends AsyncTask<Void, Integer, Void>{
		String lastLable = null;

		@Override
		protected Void doInBackground(Void... params) {
			rowModel = new RowModel();
			rowModel = rowModel.getItemById(workFormGroupId, rowId);
			ColumnModel colModel = new ColumnModel();
			column = colModel.getAllItemByWorkFormGroupId(workFormGroupId);
			ItemFormRenderModel form = null;
			setPageTitle();
			//check if the head has a form
			for(int i = 0; i < rowModel.row_columns.size(); i++){
				if (rowModel.row_columns.get(i).items.size() > 0){
					finishInflate = false;
					log("-----------------------------------------------");
					log("========================= head row id : "+rowModel.id);
					log("========================= head row ancestry : "+rowModel.ancestry);
					checkHeaderName(rowModel);
					log("-----------------------------------------------");
					
					form = new ItemFormRenderModel();
					form.setSchedule(schedule);
					form.setColumn(column);
					form.setRowColumnModels(rowModel.row_columns, null);
					if (form.hasInput){
						log("========================= head row has input : ");
						indexes.add(indexes.get(indexes.size()-1) + form.getCount());
						labels.add(form.getLabel());
						formModels.add(form);
					}
					else if (form.hasPicture){
						log("========================= head row has picture : ");
						labels.add(form.getLabel());
						formModels.add(form);
					}
					break;
				}
			}

			int x = 0;
			//check if the child has a form
			String parentLabel = null;
			String ancestry = null;
			for (RowModel model : rowModel.children) {
				checkHeaderName(model);
				x++;
				publishProgress(x*100/rowModel.children.size());
				finishInflate = false;
				log("-----------------------------------------------");
				log("========================= child row id : "+model.id);
				log("========================= child row ancestry : "+rowModel.ancestry);
				log("-----------------------------------------------");
				form = new ItemFormRenderModel();
				form.setSchedule(schedule);
				form.setColumn(column);
				form.setRowColumnModels(model.row_columns,parentLabel);
				if (form.hasInput){
					indexes.add(indexes.get(indexes.size()-1) + form.getCount());
					String label = form.getLabel();
					while (labels.indexOf(label) != -1){
						label = label+".";
					}
					labels.add(label);
					formModels.add(form);
				}else
					parentLabel = form.getLabel();
//				setPercentage(model.id);
			}
			return null;
		}
		
		private void checkHeaderName(RowModel rowModel){
			if (rowModel.row_columns != null && 
					rowModel.row_columns.size() > 0 && 
					rowModel.row_columns.get(0).items != null &&
					rowModel.row_columns.get(0).items.size() > 0){
				log("========================= head row label : "+rowModel.row_columns.get(0).items.get(0).label);
				if (rowModel.row_columns.get(0).items.get(0).label != null && !rowModel.row_columns.get(0).items.get(0).label.equalsIgnoreCase(""))
					this.lastLable = rowModel.row_columns.get(0).items.get(0).label;
					rowModel.row_columns.get(0).items.get(0).labelHeader = this.lastLable;
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.setMessage("Generating form "+values[0]+" % complete");
		}

		@Override
		protected void onPostExecute(Void result) {
			title.setText(pageTitle);
			progressDialog.dismiss();
			adapter.setItems(formModels);
//			SearchAdapter searchAdapter = new SearchAdapter(activity, android.R.layout.select_dialog_item, android.R.id.text1, indexes);
			ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(activity, android.R.layout.select_dialog_item, labels);
			search.setAdapter(searchAdapter);
			super.onPostExecute(result);
		}
	}
	
	OnItemClickListener searchClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			list.setSelection(indexes.get(labels.indexOf(parent.getItemAtPosition(position))));
			log("===== selected : "+parent.getItemAtPosition(position)+" | "+indexes.get(labels.indexOf(parent.getItemAtPosition(position))));
		}
	};
	
	private void setPageTitle(){
		if (rowModel.row_columns.size() > 0 && rowModel.row_columns.get(0).items.size() > 0)
			pageTitle = rowModel.row_columns.get(0).items.get(0).label;
	}
	
}