package com.sap.inspection.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sap.inspection.BaseActivity;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.FormFillActivity;
import com.sap.inspection.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.connection.APIHelper;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.config.formimbaspetir.FormImbasPetirConfig;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.responsemodel.CheckApprovalResponseModel;
import com.sap.inspection.model.value.ItemValueModel;
import com.sap.inspection.tools.DateTools;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.CommonUtil;
import com.sap.inspection.util.StringUtil;

import java.io.File;


public class PhotoItemRadio extends RelativeLayout {
	protected Context context;
	protected View rootView;
	protected View root;
	protected View photoRoot;
    protected View upload;
	protected TextView label;
	//	protected TextView failed;
	protected TextView latitude;
	protected TextView longitude;
	protected TextView accuracy;
	protected TextView photodate;
	protected TextView uploadstatus;
	protected TextView mandatory;
	protected EditText remark;
	protected EditText material_request;
	protected RadioGroup radioGroup;
	protected ImageButtonForList btnTakePicture;
	protected RadioButton ok;
	protected RadioButton nok;
	protected RadioButton na;
	protected View noPicture;
	protected ImageView imageView;
	protected ProgressBar progress;
	protected String scheduleId;
	protected int itemId;
	protected int operatorId;
	protected ItemValueModel value;
	protected String labelText;
	private ItemFormRenderModel itemFormRenderModel;
	private boolean onInit = false;
	private boolean isAudit;
    private Uri imageUri;

	public PhotoItemRadio(Context context) {
		super(context);
		init(context);
	}

	public PhotoItemRadio(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	public PhotoItemRadio(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.context = context;

		root = LayoutInflater.from(context).inflate(R.layout.photo_layout, this, true);
		photoRoot = root.findViewById(R.id.photolayout);
		btnTakePicture = (ImageButtonForList) root.findViewById(R.id.btnTakePicture);
		btnTakePicture.setTag(this);
        upload = root.findViewById(R.id.upload);
        upload.setTag(this);
		noPicture =  root.findViewById(R.id.no_picture);
		imageView = (ImageView) root.findViewById(R.id.photo);
		imageView.setTag(this);
		latitude = (TextView) root.findViewById(R.id.latitude);
		longitude = (TextView) root.findViewById(R.id.longitude);
		mandatory = (TextView) root.findViewById(R.id.mandatory);
		mandatory.setTag(this);

		accuracy = (TextView) root.findViewById(R.id.accuracy);
		photodate = (TextView) root.findViewById(R.id.photodate);
		uploadstatus = (TextView) root.findViewById(R.id.uploadstatus);
		label = (TextView) root.findViewById(R.id.label);
		progress = (ProgressBar) root.findViewById(R.id.progress);
		remark = (EditText) root.findViewById(R.id.remark);
		remark.addTextChangedListener(textWatcher);
		radioGroup = (RadioGroup) root.findViewById(R.id.radioGroup);
		ok = (RadioButton) root.findViewById(R.id.radioOK);
		nok = (RadioButton) root.findViewById(R.id.radioNOK);
		na = (RadioButton) root.findViewById(R.id.radioNA);
		radioGroup.setOnCheckedChangeListener(changeListener);
		value = new ItemValueModel();

		toggleEditable();
	}

	public void setButtonTakePictureListener(OnClickListener buttonClickListener){
		btnTakePicture.setOnClickListener(buttonClickListener);
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
		if (value != null)
			value.scheduleId = scheduleId;
	}

	public void setItemFormRenderModel(ItemFormRenderModel argsItemFormRenderModel) {
		this.itemFormRenderModel = argsItemFormRenderModel;

		if (itemFormRenderModel.operator != null && itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("operator"))
			label.setText(itemFormRenderModel.operator.name+"\n"+itemFormRenderModel.workItemModel.label.replaceAll("(?i)Photo Pengukuran Tegangan KWH", ""));
		else if (itemFormRenderModel.workItemModel != null && itemFormRenderModel.workItemModel.label != null)
			label.setText(itemFormRenderModel.workItemModel.label.replaceAll("(?i)Photo Pengukuran Tegangan KWH", ""));

		if (itemFormRenderModel.workItemModel != null && itemFormRenderModel.workItemModel.mandatory)
			mandatory.setVisibility(VISIBLE);

		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP) &&
				(itemFormRenderModel.workItemModel.label.equalsIgnoreCase("Photo Penghancuran 1") ||
						itemFormRenderModel.workItemModel.label.equalsIgnoreCase("Photo Penghancuran 2"))) {

			// ignore mandatory when did not get any approval yet
			itemFormRenderModel.workItemModel.mandatory = false;
			itemFormRenderModel.workItemModel.save();
			disable();

			DebugLog.d("proceed approval checking ... ");
			CheckApprovalHandler checkApprovalHandler = new CheckApprovalHandler(scheduleId);
			APIHelper.getCheckApproval(context, checkApprovalHandler, scheduleId);

			/*if (!FormImbasPetirConfig.isScheduleApproved(scheduleId)) {

				disable();

				DebugLog.d("proceed approval checking ... ");
				CheckApprovalHandler checkApprovalHandler = new CheckApprovalHandler(scheduleId);
				APIHelper.getCheckApproval(context, checkApprovalHandler, scheduleId);

			} else {
				enable();
			}*/
		}
		DebugLog.d("itemFormRenderModel.wargaId = " + itemFormRenderModel.getWargaId());
		DebugLog.d("itemFormRenderModel.barangId = " + itemFormRenderModel.getBarangId());
	}

	public void setItemValue(ItemValueModel value, boolean initValue) {

		imageView.setImageResource(R.drawable.logo_app);

		if (initValue){
			radioGroup.setOnCheckedChangeListener(null);
			onInit = true;
		}
		setValue(value);
		notifyDataChanged();
		if (initValue){
			radioGroup.setOnCheckedChangeListener(changeListener);
			onInit = false;
		}
	}

	public void setValue(ItemValueModel value) {
		this.value = value;
	}

	public ItemValueModel getValue() {
		return value;
	}

	public void initValue(){
		if (value == null){
			value = new ItemValueModel();
			if (itemFormRenderModel != null){
				value.itemId = itemFormRenderModel.workItemModel.id;
				value.scheduleId = itemFormRenderModel.schedule.id;
				value.operatorId = itemFormRenderModel.operatorId;

				if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {

					value.wargaId  = itemFormRenderModel.getWargaId();
					value.barangId = itemFormRenderModel.getBarangId();

				}
			}
		}
	}

	public void notifyDataChanged() {

		if (value != null) {

			remark.setText("");
			if (!TextUtils.isEmpty(value.remark)) {
				remark.setText(value.remark);
			}

            // if no picture then show no picture icon
            noPicture.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(value.value)) {
                noPicture.setVisibility(View.GONE);
                photoRoot.setVisibility(VISIBLE);

                File photoFile = new File(value.value);
                setImage(photoFile, value.latitude, value.longitude, value.gpsAccuracy);
            }

            if (!TextUtils.isEmpty(value.photoStatus)) {

                switch (value.photoStatus) {

                    case Constants.OK :
                        ok.setChecked(true);
                        break;
                    case Constants.NOK :
                        nok.setChecked(true);
                        break;
                    case Constants.NA :
                        na.setChecked(true);
                        break;
                }
            }

            btnTakePicture.setVisibility(VISIBLE);
            mandatory.setVisibility(VISIBLE);

            if (!itemFormRenderModel.workItemModel.mandatory) {

                mandatory.setVisibility(GONE);

                if (!TextUtils.isEmpty(value.photoStatus)) {

                    if (nok.isChecked() || value.photoStatus.equalsIgnoreCase(Constants.NOK)) {

                        if (TextUtils.isEmpty(value.remark))
                            mandatory.setVisibility(VISIBLE);

                    }

                } else {

                    if (!TextUtils.isEmpty(value.remark)) {

                        mandatory.setVisibility(VISIBLE);
                        MyApplication.getInstance().toast("Centang tidak boleh kosong", Toast.LENGTH_LONG);
                    }
                }
            }

            if (na.isChecked() || value.photoStatus.equalsIgnoreCase(Constants.NA))
                btnTakePicture.setVisibility(GONE);

            save();

        }else {
			initValue();
			reset();
		}
	}

	private void setItemFormRenderedValue(){

		if (itemFormRenderModel.itemValue == null){
			DebugLog.d("itemFormRenderModel.itemValue == null");
			if(!itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("operator"))
				itemFormRenderModel.schedule.sumTaskDone+=itemFormRenderModel.schedule.operators.size();
			else
				itemFormRenderModel.schedule.sumTaskDone++;
			itemFormRenderModel.schedule.save();
		}
		DebugLog.d("task done : " + itemFormRenderModel.schedule.sumTaskDone);
		if (itemFormRenderModel != null){
			DebugLog.d("itemFormRenderModel != null");
			itemFormRenderModel.itemValue = value;
		}
	}

	public void save(){
		setItemFormRenderedValue();
		DebugLog.d( value.scheduleId +" | "+value.itemId+" | "+value.operatorId+" | "+ value.value+ " | " + value.wargaId + " | " + value.barangId + " | " + value.createdAt );
		if(itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("all")){
			DebugLog.d("scopeType : All");
			for (OperatorModel operatorModel : itemFormRenderModel.schedule.operators) {
				value.operatorId = operatorModel.id;
				value.save();
				DebugLog.d("== operator id : " + value.operatorId);
				DebugLog.d("== warga id : " + value.wargaId);
				DebugLog.d("== barang id : " + value.barangId);
			}
		}else{
			DebugLog.d("scopeType : operator");
			DebugLog.d("== operator id : " + value.operatorId);
			DebugLog.d("== warga id : " + value.wargaId);
			DebugLog.d("== barang id : " + value.barangId);
			value.save();
		}
	}

	TextWatcher textWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			DebugLog.d("remark photo : " + s.toString());

			if (onInit)
				return;

            value.remark = s.toString();
            notifyDataChanged();
		}
	};

	OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			//initValue();
			switch (checkedId) {
			case R.id.radioOK:

				value.photoStatus = Constants.OK;
				notifyDataChanged();
				break;

			case R.id.radioNOK:

			    value.photoStatus = Constants.NOK;
			    notifyDataChanged();
				break;

			case R.id.radioNA:

			    value.photoStatus = Constants.NA;
			    notifyDataChanged();
				break;
			}
		}
	};

	public void deletePhoto(){

		DebugLog.d( "delete photo");
		File fileTemp=new File(value.value.replaceFirst("^file\\:\\/\\/", ""));
		if (fileTemp.exists()) {

			try{
				fileTemp.delete();
				DebugLog.d( "file deleted : "+value.value);
			}catch(Exception e){
				DebugLog.d( "file not deleted");
				e.printStackTrace();
			}
			value.value = "";
			value.uploadStatus = ItemValueModel.UPLOAD_NONE;
			notifyDataChanged();
			return;
		}
		DebugLog.e("Gagal menghapus foto. Tidak ditemukan file foto di direktori : " + value.value);
		MyApplication.getInstance().toast("Gagal menghapus foto. Tidak ditemukan file foto di direktori : " + value.value, Toast.LENGTH_LONG);
	}

	public void setPhotoDate(String date) {
		value.photoDate = value.createdAt = date;
	}

	public void setUploadstatus(String uploadstatus) {
		this.uploadstatus.setText(uploadstatus);
	}

	public void setImage(File photoPath,String latitude, String longitude,int accuracy){

		if (value == null) {
			reset();
			return;
		}

		DebugLog.d("image path : " + photoPath.toString());

		// updating item value
		imageUri = FileProvider.getUriForFile(this.context, BuildConfig.APPLICATION_ID + ".fileProvider", photoPath);
		value.value = photoPath.getPath();
		value.latitude = latitude;
		value.longitude = longitude;
		value.gpsAccuracy = accuracy;
		notifyDataChanged();

		this.latitude.setText("Lat. : "+ latitude);
		this.longitude.setText("Long. : "+ longitude);
		this.accuracy.setText("Accurate up to : "+accuracy+" meters");
        this.photodate.setText("photo date : " + value.photoDate + "");

		BaseActivity.imageLoader.displayImage(imageUri.toString(),imageView,new ImageLoadingListener() {

			@Override
			public void onLoadingStarted(String arg0, View arg1) {
				progress.setVisibility(View.VISIBLE);
				photoRoot.setVisibility(View.VISIBLE);
				noPicture.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
				progress.setVisibility(View.GONE);
				photoRoot.setVisibility(View.GONE);
				noPicture.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
				progress.setVisibility(View.GONE);
				photoRoot.setVisibility(View.VISIBLE);
				if (value != null){
					if (value.value == null)
						noPicture.setVisibility(View.VISIBLE);
					if (value.photoStatus == null){
						radioGroup.check(R.id.radioOK);
					}
				}
			}

			@Override
			public void onLoadingCancelled(String arg0, View arg1) {
				progress.setVisibility(View.GONE);
				photoRoot.setVisibility(View.VISIBLE);
				noPicture.setVisibility(View.GONE);
			}

		});
	}

	private void reset(){
		DebugLog.d("value = null, reset ! ");
		progress.setVisibility(View.GONE);
		photoRoot.setVisibility(View.GONE);
		remark.setText("");
		noPicture.setVisibility(View.VISIBLE);
		radioGroup.clearCheck();
	}

	public void setAudit(boolean isAudit) {
		this.isAudit = isAudit;
	}

	public int getItemId() {
		return itemFormRenderModel.workItemModel.id;
	}

	private void toggleEditable() {
		if (MyApplication.getInstance().isInCheckHasilPm()) {
			DebugLog.d("input is disabled");
			disable();
		} else {
			enable();
		}
	}

	private void enable() {
        if (remark!=null) remark.setEnabled(true);
        if (material_request!=null) material_request.setEnabled(true);
        if (radioGroup!=null) radioGroup.setEnabled(true);
        if (ok!=null) ok.setEnabled(true);
        if (nok!=null) nok.setEnabled(true);
        if (na!=null) na.setEnabled(true);
        if (btnTakePicture!=null) btnTakePicture.setEnabled(true);
    }

    private void disable() {
		DebugLog.d("label disable : " + itemFormRenderModel.workItemModel.label);
        if (remark!=null) remark.setEnabled(false);
        if (material_request!=null) material_request.setEnabled(false);
        if (radioGroup!=null) radioGroup.setEnabled(false);
        if (ok!=null) ok.setEnabled(false);
        if (nok!=null) nok.setEnabled(false);
        if (na!=null) na.setEnabled(false);
        if (btnTakePicture!=null) btnTakePicture.setEnabled(false);
    }

    private class CheckApprovalHandler extends Handler {

        private String scheduleId;

        public CheckApprovalHandler(String scheduleId) {
            this.scheduleId = scheduleId;
        }

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();

            boolean isResponseOK = bundle.getBoolean("isresponseok");
            Gson gson = new Gson();

            if (isResponseOK) {

                if (bundle.getString("json") != null){

                    CheckApprovalResponseModel checkApprovalResponseModel = gson.fromJson(bundle.getString("json"), CheckApprovalResponseModel.class);
                    checkApprovalResponseModel.toString();

                    if (!checkApprovalResponseModel.status_code.equalsIgnoreCase("failed")) {

                        DebugLog.d("check approval success");

						itemFormRenderModel.workItemModel.mandatory = true;
						itemFormRenderModel.workItemModel.save();
                        FormImbasPetirConfig.setScheduleApproval(scheduleId, true);
                        enable();
                        return;
                    }

                    DebugLog.d("belum ada approval dari STP");
                    MyApplication.getInstance().toast("Photo penghancuran menunggu approval dari STP", Toast.LENGTH_LONG);

                } else {

                    MyApplication.getInstance().toast("Gagal mengecek approval. Response json = null", Toast.LENGTH_LONG);

                }
            } else {

                MyApplication.getInstance().toast("Gagal mengecek approval. Response not OK dari server", Toast.LENGTH_LONG);
                DebugLog.d("response not ok");
            }
        }
    }
}
