package com.sap.inspection.view.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.aakira.expandablelayout.Utils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sap.inspection.BuildConfig;
import com.sap.inspection.view.ui.MyApplication;
import com.sap.inspection.R;
import com.sap.inspection.constant.Constants;
import com.sap.inspection.listener.FormTextChange;
import com.sap.inspection.model.OperatorModel;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.model.form.WorkFormOptionsModel;
import com.sap.inspection.model.value.FormValueModel;
import com.sap.inspection.rules.SavingRule;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.util.StringUtil;
import com.sap.inspection.view.customview.FormInputText;
import com.sap.inspection.view.customview.MyTextView;
import com.sap.inspection.view.customview.PhotoItem;
import com.sap.inspection.view.customview.PhotoItemRadio;

import java.util.ArrayList;
import java.util.List;

public class FormFillAdapter extends MyBaseAdapter {

	private Context context;
	private ItemFormRenderModel itemFormRender;
	private ArrayList<ItemFormRenderModel> models;
	private ArrayList<ItemFormRenderModel> shown;
	private String scheduleId;
	private String workTypeName;
	private String workFormGroupName;
	private int workFormGroupId;

	// SAP only
	private String wargaId;
	private String barangId;
	private boolean isScheduleApproved;
	private boolean isChecklistOrSiteInformation;

	private OnClickListener photoListener;
	private OnClickListener uploadListener;
	private SparseArray<List<ItemFormRenderModel>> sparseArray = new SparseArray<>();
	private SavingRule savingRule;
	private SparseBooleanArray expandState = new SparseBooleanArray();

	public FormFillAdapter(Context context) {
		this.context = context;
		if (null == models)
			models = new ArrayList<>();
		if (null == shown)
			shown = new ArrayList<>();
		if (null == itemFormRender)
			itemFormRender = new ItemFormRenderModel();
	}

	public void setWorkTypeName(String workTypeName) {
		this.workTypeName = workTypeName;
	}

	public void setWorkFormGroupId(int workFormGroupId) {
		this.workFormGroupId = workFormGroupId;
	}

	public void setWorkFormGroupName(String workFormGroupName) {
		this.workFormGroupName = workFormGroupName;
		isChecklistOrSiteInformation = workFormGroupName.equalsIgnoreCase("checklist") ||
									  workFormGroupName.equalsIgnoreCase("site information");
	}

	public void setSavingRule(SavingRule savingRule) {
		this.savingRule = savingRule;
	}

	public void setPhotoListener(OnClickListener photoListener) {
		this.photoListener = photoListener;
	}

    public void setUploadListener(OnClickListener uploadListener) {
        this.uploadListener = uploadListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
		this.onCheckedChangeListener = onCheckedChangeListener;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	// SAP only
	public void setWargaId(String wargaId) {
		DebugLog.d("wargaid = " + wargaId);
		this.wargaId = wargaId;
	}

	// SAP only
	public void setBarangId(String barangId) {
		DebugLog.d("barangid = " + barangId);
		this.barangId = barangId;
	}

	public void setItems(ArrayList<ItemFormRenderModel> models){
		this.models = models;
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		shown.clear();

		// add all children item models into the list (flattening)
		for (ItemFormRenderModel model : models) {
			shown.addAll(model.getModels());
		}

		// remove corrective item from shown list
		for (int i = 0; i < shown.size(); i++) {
			ItemFormRenderModel model = shown.get(i);
			if (model.column!=null && !TextUtils.isEmpty(model.column.column_name)
					&& model.column.column_name.equalsIgnoreCase("corrective"))
				shown.remove(i);
		}

		DebugLog.d("item size = " + shown.size());

		// add all items' label into the shown list
		List<String> strings = new ArrayList<>();
		for (int i = 0; i < this.shown.size(); i++) {
			ItemFormRenderModel item = this.shown.get(i);
			if (item.type==ItemFormRenderModel.TYPE_EXPAND)
				strings.add(item.workItemModel.label);
		}

		// removing item from shown which has type = TYPE HEADER
		for (int i = 0; i < strings.size(); i++) {
			String s = strings.get(i);
			for (int j = 0; j < shown.size(); j++) {
				if (s.equalsIgnoreCase(shown.get(j).getLabel()) && shown.get(j).type==ItemFormRenderModel.TYPE_HEADER) {
					shown.remove(j);
					break;
				}
			}
		}

		expandState.clear();
		for (int i = 0; i < models.size(); i++) {
			expandState.append(i, true);
		}
		super.notifyDataSetChanged();
	}

	public void updateView() {
		DebugLog.d("aaa shown size="+shown.size()+" sparseArray size="+sparseArray.size());
		super.notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return shown.size();
	}

	@Override
	public int getViewTypeCount() {
		return ItemFormRenderModel.MAX_TYPE;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).type;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public ItemFormRenderModel getItem(int position) {
		return shown.get(position);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
        DebugLog.d("position="+position+" type="+getItemViewType(position));
		if (convertView == null) {
			DebugLog.d("convertView == null");
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case ItemFormRenderModel.TYPE_CHECKBOX:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_checkbox,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				holder.checkBox = convertView.findViewById(R.id.item_form_check);
				holder.mandatory = convertView.findViewById(R.id.item_form_mandatory);
				break;
			case ItemFormRenderModel.TYPE_COLUMN:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_column,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_HEADER_DIVIDER:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_header_devider,null);
				break;
			case ItemFormRenderModel.TYPE_HEADER:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_header,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				((MyTextView) convertView.findViewById(R.id.item_form_label)).setBold(context, true);
				holder.upload_status = convertView.findViewById(R.id.item_form_upload_status);
				holder.colored = convertView.findViewById(R.id.item_form_colored);
				holder.plain = convertView.findViewById(R.id.item_form_plain);
				break;
			case ItemFormRenderModel.TYPE_LABEL:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_label,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_LINE_DEVIDER:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_line_devider,null);
				break;
			case ItemFormRenderModel.TYPE_OPERATOR:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_operator,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_PICTURE_RADIO:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_photo_radio,null);
				holder.photoRadio = convertView.findViewById(R.id.item_form_photo);
				holder.upload = convertView.findViewById(R.id.item_form_upload);
				holder.photoRadio.setAudit(isAudit());
				holder.photoRadio.setButtonTakePictureListener(photoListener);
				holder.upload.setOnClickListener(uploadListener);
				break;
			case ItemFormRenderModel.TYPE_PICTURE:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_photo,null);
				holder.mandatory = convertView.findViewById(R.id.item_form_mandatory);
				holder.photo = convertView.findViewById(R.id.item_form_photo);
				holder.upload = convertView.findViewById(R.id.item_form_upload);
				holder.photo.setAudit(isAudit());
				holder.photo.setButtonTakePictureListener(photoListener);
                holder.upload.setOnClickListener(uploadListener);
				holder.photo.setSavingRule(savingRule);
				break;
			case ItemFormRenderModel.TYPE_RADIO:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_radio,null);
				holder.mandatory = convertView.findViewById(R.id.item_form_mandatory);
				holder.label = convertView.findViewById(R.id.item_form_label);
				holder.radio = convertView.findViewById(R.id.item_form_radio);
				break;
			case ItemFormRenderModel.TYPE_TEXT_INPUT:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_text_field,null);
				holder.label = convertView.findViewById(R.id.item_form_label);
				holder.description = convertView.findViewById(R.id.item_form_description);
				holder.input = convertView.findViewById(R.id.item_form_input);
				holder.mandatory = convertView.findViewById(R.id.item_form_mandatory);
				break;
			case ItemFormRenderModel.TYPE_EXPAND:
				convertView = LayoutInflater.from(context).inflate(R.layout.item_form_expand,null);
				holder.label = convertView.findViewById(R.id.item_form_expand_title);
				((MyTextView) convertView.findViewById(R.id.item_form_expand_title)).setBold(context, true);
				holder.expandButton = convertView.findViewById(R.id.item_form_expand_button);
				break;
			default:
				DebugLog.d("============== get default view : "+getItemViewType(position));
				convertView = new View(context);
				break;
			}
			holder.picture = convertView.findViewById(R.id.picture);
			convertView.setTag(holder);
		} else {
			DebugLog.d("convertView != null");
			holder = (ViewHolder) convertView.getTag();
			DebugLog.d("convertView tag : " + holder);
		}
		
		if (getItem(position).workItemModel != null)
			DebugLog.d( "picture : " + getItem(position).workItemModel.pictureEndPoint);
		if (holder.picture != null){
			if (getItem(position).workItemModel != null && getItem(position).workItemModel.pictureEndPoint != null){
				DebugLog.d( "picture show : "+getItem(position).workItemModel.pictureEndPoint);
				holder.picture.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().displayImage("file://"+getItem(position).workItemModel.pictureEndPoint, holder.picture);
			}
			else
				holder.picture.setVisibility(View.GONE);
		}

		switch (getItemViewType(position)) {

			case ItemFormRenderModel.TYPE_COLUMN:
				DebugLog.d("TYPE_COLUMN");
				holder.label.setText(getItem(position).column.column_name);
				DebugLog.d("label : " + getItem(position).column.column_name);
				break;
			case ItemFormRenderModel.TYPE_LABEL:
				DebugLog.d("TYPE_LABEL");
				holder.label.setText(getItem(position).workItemModel.label);
				DebugLog.d("label : " + getItem(position).workItemModel.label);
				break;
			case ItemFormRenderModel.TYPE_OPERATOR:
				DebugLog.d("TYPE_OPERATOR");
				holder.label.setText(getItem(position).operator.name);
				DebugLog.d(getItem(position).operator.name);
				break;
			case ItemFormRenderModel.TYPE_CHECKBOX:
				DebugLog.d("TYPE_CHECKBOX");
				holder.label.setText(getItem(position).workItemModel.label);
				DebugLog.d("checkbox itemvalue : "+(getItem(position).itemValue == null ? getItem(position).itemValue : getItem(position).itemValue.value));
				reviseCheckBox(holder.checkBox, getItem(position), getItem(position).itemValue == null ? null : getItem(position).itemValue.value.split("[,]"), getItem(position).rowId, getItem(position).operatorId);
				setMandatoryVisibility(holder,getItem(position));
				break;
			case ItemFormRenderModel.TYPE_RADIO:
				DebugLog.d("TYPE_RADIO");
				DebugLog.d("label : " + getItem(position).workItemModel.label);
				DebugLog.d("radio button itemvalue : "+(getItem(position).itemValue == null ? getItem(position).itemValue : getItem(position).itemValue.value));
				holder.label.setText(getItem(position).workItemModel.label);
				reviseRadio(holder.radio, getItem(position), getItem(position).itemValue == null ? null : getItem(position).itemValue.value.split("[|]"), getItem(position).rowId, getItem(position).operatorId);
				setMandatoryVisibility(holder,getItem(position));

				break;
			case ItemFormRenderModel.TYPE_HEADER:
				DebugLog.d("TYPE HEADER");
				DebugLog.d("workFormGroupId = " + workFormGroupId);
                DebugLog.d("workFormGroupName = " + workFormGroupName);
				DebugLog.d("workTypeName = " + workTypeName);
				if (workFormGroupName.equalsIgnoreCase("Photograph") && BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
					DebugLog.d("Parent label : " + getItem(position).label);
					holder.upload_status.setVisibility(View.VISIBLE);
					int i = 0;
					for (ItemFormRenderModel child : getItem(position).children) {
						i++;
						DebugLog.d("=child ke-" + i);
						DebugLog.d("=child label : " + child.label);
						DebugLog.d("=child type : " + child.type);
						if (child.type == ItemFormRenderModel.TYPE_PICTURE_RADIO) {

							if (null != child.itemValue)
							{
								DebugLog.d("=child scheduleId : " + child.itemValue.scheduleId);
								DebugLog.d("=child itemId : " + child.itemValue.itemId);
								DebugLog.d("=child operatorId : " + child.itemValue.itemId);
								DebugLog.d("=child uploadStatus : " + child.itemValue.uploadStatus);
								DebugLog.d("=child photoStatus : " + child.itemValue.photoStatus);
								DebugLog.d("=child remark : " + child.itemValue.remark);

								if (child.itemValue.uploadStatus == FormValueModel.UPLOAD_DONE) {
									holder.upload_status.setText("SUCCESS");
								} else
								if (child.itemValue.uploadStatus == FormValueModel.UPLOAD_NONE) {
									holder.upload_status.setText("NOT COMPLETE");
									break;
								} else
								if (child.itemValue.uploadStatus == FormValueModel.UPLOAD_FAIL) {
									holder.upload_status.setText("FAILED");
									break;
								}
							}
							else {
								DebugLog.d("=child.itemValue = null");
								holder.upload_status.setText("NOT COMPLETE");
								break;
							}

						}
					}
				}
				holder.label.setText(getItem(position).workItemModel.labelHeader);
				holder.colored.setText(getItem(position).getPercent());
				holder.plain.setText(getItem(position).getWhen());
				break;
			case ItemFormRenderModel.TYPE_PICTURE_RADIO:
				DebugLog.d("TYPE_PICTURE_RADIO");
				holder.photoRadio.setScheduleId(scheduleId);
				holder.photoRadio.setWorkTypeName(workTypeName);
				holder.photoRadio.setItemFormRenderModel(getItem(position));
				holder.photoRadio.setItemValue(getItem(position).itemValue,true);
				holder.upload.setTag(position);
				setUploadButtonVisibility(holder);
				break;
			case ItemFormRenderModel.TYPE_PICTURE:
				DebugLog.d("TYPE_PICTURE");
				holder.photo.setItemFormRenderModel(getItem(position));
				holder.photo.setItemValue(getItem(position).itemValue,true);
				holder.upload.setTag(position);
				setMandatoryVisibility(holder,getItem(position));
				break;
			case ItemFormRenderModel.TYPE_TEXT_INPUT:
				DebugLog.d("TYPE_TEXT_INPUT");
				holder.label.setText(getItem(position).workItemModel.label);
				if(getItem(position).workItemModel.description == null){
					holder.description.setVisibility(View.GONE);
				}
				else{
					holder.description.setVisibility(View.VISIBLE);
					holder.description.setText(getItem(position).workItemModel.description);
				}
				if (getItem(position).workItemModel.default_value != null) {

					holder.input.setInputType(InputType.TYPE_CLASS_NUMBER);

					DebugLog.d("default value not null");
					if (getItem(position).workItemModel.default_value.isEmpty()) {

						holder.input.setHint("0");

					} else {
						holder.input.setHint(getItem(position).workItemModel.default_value);
					}

				}

				holder.input.setTextChange(null);
				holder.input.setTag(getItem(position));
				holder.input.setText("");
				if (getItem(position).itemValue != null)
					holder.input.setText(getItem(position).itemValue.value);

				holder.input.setTextChange(formTextChange);
				holder.input.setEnabled(isInputItemEnabled(getItem(position).workItemModel));
				setMandatoryVisibility(holder,getItem(position));
				break;
			case ItemFormRenderModel.TYPE_EXPAND:
				DebugLog.d("TYPE_EXPAND");
				holder.label.setText(getItem(position).workItemModel.label);
				holder.label.setOnClickListener(view -> processExpand(holder,position));
				DebugLog.d("position="+position+" state="+expandState.get(position));
				holder.expandButton.setRotation(expandState.get(position) ? 180f : 0f);
				holder.expandButton.setOnClickListener(view -> processExpand(holder,position));
				break;
			default:
				break;
		}

		return convertView;
	}

	private void setMandatoryVisibility(ViewHolder viewHolder, ItemFormRenderModel itemFormRenderModel) {
		if (itemFormRenderModel.workItemModel.mandatory) {
			viewHolder.mandatory.setVisibility(View.VISIBLE);
		} else {
			viewHolder.mandatory.setVisibility(View.GONE);
		}
	}

	private void setUploadButtonVisibility(ViewHolder holder) {

		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
			if (StringUtil.isNotNullAndNotEmpty(wargaId) || StringUtil.isNotNullAndNotEmpty(barangId)) {
				holder.upload.setVisibility(View.INVISIBLE);
			} else
				holder.upload.setVisibility(View.VISIBLE);
		}
	}

	private void reviseCheckBox(LinearLayout linear,ItemFormRenderModel item, String[] split, int rowId, int operatorId){
		boolean isHorizontal;
		boolean isEnabled = isInputItemEnabled(item.workItemModel);

		isHorizontal = 3 >= item.workItemModel.options.size();
		DebugLog.d("isHorizontal : " + isHorizontal);
		DebugLog.d("linear child count after addview : " + linear.getChildCount());
		for (int i = 0; i< linear.getChildCount(); i++){
			CheckBox checkBox = (CheckBox) linear.getChildAt(i);
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(false);
			checkBox.setEnabled(isEnabled);
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		DebugLog.d("linear child count before addview : " + linear.getChildCount());
		for (int i = 0; i< linear.getChildCount(); i++){
			//binding checkbox
			if (i < item.workItemModel.options.size()) {
				CheckBox checkBox = (CheckBox) linear.getChildAt(i);
				checkBox.setVisibility(View.VISIBLE);
				checkBox.setText(item.workItemModel.options.get(i).label);
				isHorizontal = item.workItemModel.options.get(i).label.length() < 4;
				//				checkBox.setTag(rowId+"|"+item.workItemModel.id+"|"+operatorId+"|"+item.workItemModel.options.get(i).value+"|0");
				checkBox.setTag(item);
				checkBox.setOnCheckedChangeListener(null);
				if (split != null)
					for(int j = 0; j < split.length; j++) {
						if (item.workItemModel.options.get(i).value.equalsIgnoreCase(split[j]))
							checkBox.setChecked(true);
					}
				else
					checkBox.setChecked(false);
				checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
			}
			//remove unused checkbox
			else linear.getChildAt(i).setVisibility(View.GONE);
		}

		//adding and binding if some checkbox is missing
		for(int i = linear.getChildCount(); i < item.workItemModel.options.size(); i++){
			CheckBox checkBox = new CheckBox(context);
			checkBox.setText(item.workItemModel.options.get(i).label);
			isHorizontal = item.workItemModel.options.get(i).label.length() < 4;
			//			checkBox.setTag(rowId+"|"+item.workItemModel.id+"|"+operatorId+"|"+item.workItemModel.options.get(i).value+"|0");
			checkBox.setTag(item);
			checkBox.setEnabled(isEnabled);
			linear.addView(checkBox);
			checkBox.setOnCheckedChangeListener(null);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.workItemModel.options.get(i).value.equalsIgnoreCase(split[j]))
						checkBox.setChecked(true);
				}
			else
				checkBox.setChecked(false);
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		linear.setOrientation(isHorizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
	}

	private void reviseRadio(RadioGroup radioGroup, ItemFormRenderModel item, String[] split, int rowId, int operatorId){

		boolean isHorizontal;
		boolean isEnabled = isInputItemEnabled(item.workItemModel);

		for (int i = 0; i< radioGroup.getChildCount(); i++){
			RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
			radioButton.setOnCheckedChangeListener(null);
			radioButton.setEnabled(isEnabled);
		}
		radioGroup.clearCheck();
		isHorizontal = 3 >= item.workItemModel.options.size();
		DebugLog.d("isHorizontal : " + isHorizontal);
		for (int i = 0; i< radioGroup.getChildCount(); i++){
			//binding checkbox
			if (i < item.workItemModel.options.size()){
				RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
				radioButton.setVisibility(View.VISIBLE);
				radioButton.setText(item.workItemModel.options.get(i).label);
				isHorizontal = item.workItemModel.options.get(i).label.length() < 4;
				//				radioButton.setTag(rowId+"|"+item.workItemModel.id+"|"+operatorId+"|"+item.workItemModel.options.get(i).value+"|0");
				radioButton.setTag(item);
				if (split != null)
					for(int j = 0; j < split.length; j++){
						if (item.workItemModel.options.get(i).value.equalsIgnoreCase(split[j]))
							radioGroup.check(radioButton.getId());
					}
				DebugLog.d("checkedChangeListener ... ");
				radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
			}
			//remove unused checkbox
			else radioGroup.getChildAt(i).setVisibility(View.GONE);
		}

		//adding and binding if some checkbox is missing
		for(int i = radioGroup.getChildCount(); i < item.workItemModel.options.size(); i++){
			RadioButton radioButton = new RadioButton(context);
			radioButton.setText(item.workItemModel.options.get(i).label);
			isHorizontal = item.workItemModel.options.get(i).label.length() < 4;
			radioButton.setTag(item);
			radioButton.setEnabled(isEnabled);
			radioGroup.addView(radioButton);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.workItemModel.options.get(i).value.equalsIgnoreCase(split[j]))
						radioGroup.check(radioButton.getId());
				}
			radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
		}
		radioGroup.setOrientation(isHorizontal ? RadioGroup.HORIZONTAL : RadioGroup.VERTICAL);
	}

	private class ViewHolder {
		TextView label;
		TextView upload_status;
		TextView colored;
		TextView plain;
		TextView description;
		PhotoItemRadio photoRadio;
		PhotoItem photo;
		LinearLayout checkBox;
		FormInputText input;
		RadioGroup radio;
		ImageView picture;
		TextView mandatory;
		ImageView upload;
		LinearLayout expandButton;
	}

	OnCheckedChangeListener onCheckedChangeListener = (buttonView, isChecked) -> {
		ItemFormRenderModel item = (ItemFormRenderModel) buttonView.getTag();
		String value = null;
		for (WorkFormOptionsModel option : item.workItemModel.options)
			if (option.label.equals(buttonView.getText())){
				value = option.value;
				break;
			}
		DebugLog.d( "-=-=-=- value : " + value);
		saveValue(item, isChecked, true, value);
	};

	FormTextChange formTextChange = (string, view) -> {
		if (view.getTag() != null){
			ItemFormRenderModel item = (ItemFormRenderModel) view.getTag();
			saveValue(item, !string.equalsIgnoreCase(""),false,string);
		}
	};

	private boolean isInputItemEnabled(WorkFormItemModel workFormItem) {

		boolean isEnabled = false;
		if (!workFormItem.disable) {
			if ((MyApplication.getInstance().IS_CHECKING_HASIL_PM() && isChecklistOrSiteInformation) || !MyApplication.getInstance().IS_CHECKING_HASIL_PM())
				isEnabled = true;
		}
		DebugLog.d("workItemDisable : " + workFormItem.disable);
		DebugLog.d("is SAP ? : " + BuildConfig.FLAVOR.equalsIgnoreCase("sap"));
		DebugLog.d("is checking hasil pm ? " + MyApplication.getInstance().IS_CHECKING_HASIL_PM());
		DebugLog.d("is checklist or site information ? " + isChecklistOrSiteInformation);
		DebugLog.d("is enabled ? " + isEnabled);
		return isEnabled;
	}

	private void saveValue(ItemFormRenderModel itemFormRenderModel, boolean isAdding, boolean isCompundButton, String value) {

		DebugLog.d("\n== SAVING VALUE ===");
		if (itemFormRenderModel.itemValue == null){
			itemFormRenderModel.itemValue = new FormValueModel();
			itemFormRenderModel.itemValue.operatorId = itemFormRenderModel.operatorId;
			itemFormRenderModel.itemValue.itemId = itemFormRenderModel.workItemModel.id;
			itemFormRenderModel.itemValue.scheduleId = itemFormRenderModel.schedule.id;
			itemFormRenderModel.itemValue.rowId = itemFormRenderModel.rowId;
			itemFormRenderModel.itemValue.value = value;
		}

		// SAP only
		if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
			itemFormRenderModel.itemValue.wargaId = wargaId;
			itemFormRenderModel.itemValue.barangId = barangId;
		}

		if (isCompundButton){
			if (isAdding){ //adding value on check box
				DebugLog.d("goto adding");
				// value still null or blank
				if (TextUtils.isEmpty(itemFormRenderModel.itemValue.value)){
					itemFormRenderModel.itemValue.value = value;
					itemFormRenderModel.schedule.sumTaskDone ++;
				}
				else{
					// any value apply before
					String[] chkBoxValue = itemFormRenderModel.itemValue.value.split("[,]");
					for(int i = 0; i < chkBoxValue.length; i++){ 
						if (chkBoxValue[i].equalsIgnoreCase(value))
							break;
						if (i == chkBoxValue.length - 1)
							itemFormRenderModel.itemValue.value += ","+value;
					}
				}

				itemFormRenderModel.itemValue.uploadStatus = FormValueModel.UPLOAD_NONE;
				saveAfterCheck(itemFormRenderModel);

				DebugLog.d("=== ITEM UPDATES ===");
				DebugLog.d("isAdding="+isAdding+", isCompundButton="+isCompundButton+", value="+value);
				DebugLog.d("item scheduleid : " + itemFormRenderModel.itemValue.scheduleId);
				DebugLog.d("item operatorid : " + itemFormRenderModel.itemValue.operatorId);
				DebugLog.d("item itemid : " + itemFormRenderModel.itemValue.itemId);
				DebugLog.d("item siteid : " + itemFormRenderModel.itemValue.siteId);
				DebugLog.d("item gpsaccur : " + itemFormRenderModel.itemValue.gpsAccuracy);
				DebugLog.d("item rowid : " + itemFormRenderModel.itemValue.rowId);
				DebugLog.d("item remark : " + itemFormRenderModel.itemValue.remark);
				DebugLog.d("item photostatus : " + itemFormRenderModel.itemValue.photoStatus);
				DebugLog.d("item latitude : " + itemFormRenderModel.itemValue.latitude);
				DebugLog.d("item longitude : " + itemFormRenderModel.itemValue.longitude);
				DebugLog.d("item value : " + itemFormRenderModel.itemValue.value);
				DebugLog.d("item uploadstatus : " + itemFormRenderModel.itemValue.uploadStatus);
				DebugLog.d("item photodate : " + itemFormRenderModel.itemValue.photoDate);
				DebugLog.d("item value : " + itemFormRenderModel.itemValue.value);
				DebugLog.d("item wargaid : " + itemFormRenderModel.itemValue.wargaId);
				DebugLog.d("item barangid : " + itemFormRenderModel.itemValue.barangId);
				DebugLog.d("task done : "+itemFormRenderModel.schedule.sumTaskDone);

			} else { // deleting on checkbox

				DebugLog.d("goto deleting");
				String[] chkBoxValue = itemFormRenderModel.itemValue.value.split("[,]");
				itemFormRenderModel.itemValue.value = "";

				for(int i = 0; i < chkBoxValue.length; i++){ 
					if (!chkBoxValue[i].equalsIgnoreCase(value))
						if (i == chkBoxValue.length - 1 || chkBoxValue[chkBoxValue.length - 1].equalsIgnoreCase(value))
							itemFormRenderModel.itemValue.value += chkBoxValue[i];
						else
							itemFormRenderModel.itemValue.value += chkBoxValue[i]+','; 
				}
				chkBoxValue = null;
				if (itemFormRenderModel.itemValue.value.equalsIgnoreCase("")){
					deleteAfterCheck(itemFormRenderModel);
					itemFormRenderModel.schedule.sumTaskDone--;
				}
				else{
					itemFormRenderModel.itemValue.uploadStatus = FormValueModel.UPLOAD_NONE;
					saveAfterCheck(itemFormRenderModel);
				}
			}
		}
		else{
			if (!isAdding){
				DebugLog.d("goto deleting");
				if (itemFormRenderModel.itemValue.value != null) {

					if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
						FormValueModel.delete(itemFormRenderModel.itemValue.scheduleId, itemFormRenderModel.itemValue.itemId, itemFormRenderModel.itemValue.operatorId, itemFormRenderModel.itemValue.wargaId, itemFormRenderModel.itemValue.barangId);
					else
						FormValueModel.delete(itemFormRenderModel.itemValue.scheduleId, itemFormRenderModel.itemValue.itemId, itemFormRenderModel.itemValue.operatorId);
				}
				itemFormRenderModel.itemValue = null;
				itemFormRenderModel.schedule.sumTaskDone--;
			} else{
				DebugLog.d("goto adding");
				if (itemFormRenderModel.itemValue.value == null)
					itemFormRenderModel.schedule.sumTaskDone++;
				itemFormRenderModel.itemValue.value = value;
				itemFormRenderModel.itemValue.uploadStatus = FormValueModel.UPLOAD_NONE;
				itemFormRenderModel.itemValue.save();

				DebugLog.d("=== ITEM UPDATES ===");
				DebugLog.d("isAdding="+isAdding+", isCompundButton="+isCompundButton+", value="+value);
				DebugLog.d("item scheduleid : " + itemFormRenderModel.itemValue.scheduleId);
				DebugLog.d("item operatorid : " + itemFormRenderModel.itemValue.operatorId);
				DebugLog.d("item itemid : " + itemFormRenderModel.itemValue.itemId);
				DebugLog.d("item siteid : " + itemFormRenderModel.itemValue.siteId);
				DebugLog.d("item gpsaccur : " + itemFormRenderModel.itemValue.gpsAccuracy);
				DebugLog.d("item rowid : " + itemFormRenderModel.itemValue.rowId);
				DebugLog.d("item remark : " + itemFormRenderModel.itemValue.remark);
				DebugLog.d("item photostatus : " + itemFormRenderModel.itemValue.photoStatus);
				DebugLog.d("item latitude : " + itemFormRenderModel.itemValue.latitude);
				DebugLog.d("item longitude : " + itemFormRenderModel.itemValue.longitude);
				DebugLog.d("item value : " + itemFormRenderModel.itemValue.value);
				DebugLog.d("item uploadstatus : " + itemFormRenderModel.itemValue.uploadStatus);
				DebugLog.d("item photodate : " + itemFormRenderModel.itemValue.photoDate);
				DebugLog.d("item value : " + itemFormRenderModel.itemValue.value);
				DebugLog.d("item wargaid : " + itemFormRenderModel.itemValue.wargaId);
				DebugLog.d("item barangid : " + itemFormRenderModel.itemValue.barangId);
				DebugLog.d("task done : "+itemFormRenderModel.schedule.sumTaskDone);
			}
		}
		itemFormRenderModel.schedule.save();
	}
	
	private void saveAfterCheck(ItemFormRenderModel itemFormRenderModel){
		if (itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("all")){
			for (OperatorModel operator : itemFormRenderModel.schedule.operators){
				itemFormRenderModel.itemValue.operatorId = operator.id;
				itemFormRenderModel.itemValue.save();
			}
		}else
			itemFormRenderModel.itemValue.save();
	}
	
	private void deleteAfterCheck(ItemFormRenderModel itemFormRenderModel){

		if (itemFormRenderModel.workItemModel.scope_type.equalsIgnoreCase("all")) {

			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP)) {
				FormValueModel.deleteAllBy(itemFormRenderModel.itemValue.scheduleId, itemFormRenderModel.itemValue.itemId, itemFormRenderModel.itemValue.wargaId, itemFormRenderModel.itemValue.barangId);
			} else
				FormValueModel.deleteAllBy(itemFormRenderModel.itemValue.scheduleId, itemFormRenderModel.itemValue.itemId);

		} else {

			if (BuildConfig.FLAVOR.equalsIgnoreCase(Constants.APPLICATION_SAP))
				FormValueModel.delete(itemFormRenderModel.itemValue.scheduleId, itemFormRenderModel.itemValue.itemId, itemFormRenderModel.itemValue.operatorId, itemFormRenderModel.itemValue.wargaId, itemFormRenderModel.itemValue.barangId);
			else
				FormValueModel.delete(itemFormRenderModel.itemValue.scheduleId, itemFormRenderModel.itemValue.itemId, itemFormRenderModel.itemValue.operatorId);
		}
	}

	private boolean isAudit() {
		if (workTypeName == null)
			return false;
		return workTypeName.equalsIgnoreCase("SITE AUDIT");
	}

	public ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
		ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
		animator.setDuration(200);
		animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
		return animator;
	}

	private void processExpand(ViewHolder holder, int position) {
		if (shown.size()>position+1) {
			DebugLog.d("aaa position=" + position);
			if (expandState.get(position)) {
				createRotateAnimator(holder.expandButton, 180f, 0f).start();
				expandState.put(position, false);
				DebugLog.d("aaa position=" + position + " state=false");
//				shownX.clear();
				List<ItemFormRenderModel> models = new ArrayList<>();
				boolean collapse = true;
				while (collapse) {
					ItemFormRenderModel item = shown.get(position + 1);
					if (item.type != ItemFormRenderModel.TYPE_EXPAND) {
						models.add(shown.remove(position + 1));
//						shownX.add(shown.remove(position + 1));
					} else {
						collapse = false;
					}
				}
				sparseArray.put(position,models);
			} else {
				createRotateAnimator(holder.expandButton, 0f, 180f).start();
				expandState.put(position, true);
				DebugLog.d("aaa position=" + position + " state=true");
				List<ItemFormRenderModel> models = sparseArray.get(position);
				if (models!=null && !models.isEmpty()) {
					for (int i = 0; i < models.size(); i++) {
						shown.add(position + i + 1, models.get(i));
					}
				}
			}
			updateView();
		}
	}
}