package com.sap.inspection.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sap.inspection.R;
import com.sap.inspection.model.form.ItemFormRenderModel;
import com.sap.inspection.model.form.WorkFormItemModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.customview.FormInputText;
import com.sap.inspection.view.customview.PhotoItemRadio;

import java.util.ArrayList;

public class Copy_2_of_FormFillAdapter extends MyBaseAdapter {

	private Context context;
	private ArrayList<ItemFormRenderModel> models;
	private ArrayList<ItemFormRenderModel> shown;
	private String scheduleId;
	private OnCheckedChangeListener onCheckedChangeListener;

	public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
		this.onCheckedChangeListener = onCheckedChangeListener;
	}

	public void setScheduleId(String scheduleId) {
		this.scheduleId = scheduleId;
	}

	public Copy_2_of_FormFillAdapter(Context context) {
		this.context = context;
		if (null == models)
			models = new ArrayList<ItemFormRenderModel>();
		if (null == shown)
			shown = new ArrayList<ItemFormRenderModel>();
	}

	public void setItems(ArrayList<ItemFormRenderModel> models){
		this.models = models;
		notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetChanged() {
		shown.clear();
		for (ItemFormRenderModel model : models) {
			shown.addAll(model.getModels());
		}
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return shown.size();
	}

	@Override
	public int getViewTypeCount() {
		return 10;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).getType();
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
		View view = convertView;
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			switch (getItemViewType(position)) {
			case ItemFormRenderModel.TYPE_CHECKBOX:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_checkbox,null);
				holder.label = view.findViewById(R.id.item_form_label);
				holder.checkBox = view.findViewById(R.id.item_form_check);
				break;
			case ItemFormRenderModel.TYPE_COLUMN:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_column,null);
				holder.label = view.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_HEADER:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_header,null);
				holder.label = view.findViewById(R.id.item_form_label);
				holder.colored = view.findViewById(R.id.item_form_colored);
				holder.plain = view.findViewById(R.id.item_form_plain);
				break;
			case ItemFormRenderModel.TYPE_LABEL:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_label,null);
				holder.label = view.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_LINE_DEVIDER:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_line_devider,null);
				break;
			case ItemFormRenderModel.TYPE_OPERATOR:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_operator,null);
				holder.label = view.findViewById(R.id.item_form_label);
				break;
			case ItemFormRenderModel.TYPE_PICTURE_RADIO:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_photo_radio,null);
				holder.photo = view.findViewById(R.id.item_form_photo);
				break;
			case ItemFormRenderModel.TYPE_RADIO:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_radio,null);
				holder.label = view.findViewById(R.id.item_form_label);
				holder.radio = view.findViewById(R.id.item_form_radio);
				break;
			case ItemFormRenderModel.TYPE_TEXT_INPUT:
				view = LayoutInflater.from(context).inflate(R.layout.item_form_text_field,null);
				holder.label = view.findViewById(R.id.item_form_label);
				holder.description = view.findViewById(R.id.item_form_description);
				holder.input = view.findViewById(R.id.item_form_input);
				break;
			default:
				DebugLog.d("============== get default view : "+getItemViewType(position));
				view = new View(context);
				break;
			}
			view.setTag(holder);
		} else
			holder = (ViewHolder) view.getTag();

		switch (getItemViewType(position)) {
		case ItemFormRenderModel.TYPE_COLUMN:
			holder.label.setText(getItem(position).getColumn().column_name);
			break;
		case ItemFormRenderModel.TYPE_LABEL:
			holder.label.setText(getItem(position).getWorkItemModel().label);
			break;
		case ItemFormRenderModel.TYPE_OPERATOR:
			holder.label.setText(getItem(position).getOperator().name);
			break;
		case ItemFormRenderModel.TYPE_CHECKBOX:
			if (getItem(position).getWorkItemModel().label != null)
				holder.label.setText(getItem(position).getWorkItemModel().label);
			reviseCheckBox(holder.checkBox, getItem(position).getWorkItemModel(), getItem(position).getItemValue() == null ? null : getItem(position).getItemValue().value.split("[|]"), getItem(position).getRowId(), getItem(position).getOperatorId());
			break;
		case ItemFormRenderModel.TYPE_RADIO:
			holder.label.setText(getItem(position).getWorkItemModel().label);
			reviseRadio(holder.radio, getItem(position).getWorkItemModel(), getItem(position).getItemValue() == null ? null : getItem(position).getItemValue().value.split("[|]"), getItem(position).getRowId(), getItem(position).getOperatorId());
			break;
		case ItemFormRenderModel.TYPE_HEADER:
			holder.label.setText(getItem(position).getWorkItemModel().label);
			holder.colored.setText(getItem(position).getPercentage());
			holder.plain.setText(getItem(position).getWhenExact());
			break;
		case ItemFormRenderModel.TYPE_PICTURE_RADIO:
			holder.photo.setItemValue(getItem(position).getItemValue(), true);
			break;
		case ItemFormRenderModel.TYPE_TEXT_INPUT:
			holder.label.setText(getItem(position).getWorkItemModel().label);
			if(getItem(position).getWorkItemModel().description == null)
				holder.description.setVisibility(View.GONE);
			else{
				holder.description.setVisibility(View.VISIBLE);
				holder.description.setText(getItem(position).getWorkItemModel().description);
			}
			if (getItem(position).getItemValue() != null)
				holder.input.setText(getItem(position).getItemValue().value);
			else
				holder.input.setText("");
			break;
		default:
			DebugLog.d("============== get default view : "+getItemViewType(position));
			break;
		}

		return view; 
	}

	private void reviseCheckBox(LinearLayout linear,WorkFormItemModel item,String[] split,int rowId, int operatorId){
		boolean isHorizontal = true;
		isHorizontal = 3 >= item.options.size();
		for (int i = 0; i< linear.getChildCount(); i++){
			//binding checkbox
			if (i < item.options.size()){
				CheckBox checkBox = (CheckBox) linear.getChildAt(i);
				checkBox.setVisibility(View.VISIBLE);
				checkBox.setText(item.options.get(i).label);
				isHorizontal = item.options.get(i).label.length() < 4;
				checkBox.setTag(rowId+"|"+item.id+"|"+operatorId+"|"+item.options.get(i).value+"|0");
				checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
				if (split != null)
					for(int j = 0; j < split.length; j++){
						if (item.options.get(i).value.equalsIgnoreCase(split[j]))
							checkBox.setChecked(true);
					}
			}
			//remove unused checkbox
			else linear.getChildAt(i).setVisibility(View.GONE);
		}

		//adding and binding if some checkbox is missing
		for(int i = linear.getChildCount(); i < item.options.size(); i++){
			CheckBox checkBox = new CheckBox(context);
			checkBox.setText(item.options.get(i).label);
			isHorizontal = item.options.get(i).label.length() < 4;
			checkBox.setTag(rowId+"|"+item.id+"|"+operatorId+"|"+item.options.get(i).value+"|0");
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
			linear.addView(checkBox);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.options.get(i).value.equalsIgnoreCase(split[j]))
						checkBox.setChecked(true);
				}
		}
		linear.setOrientation(isHorizontal ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
	}

	private void reviseRadio(RadioGroup radioGroup,WorkFormItemModel item,String[] split,int rowId, int operatorId){
		boolean isHorizontal = true;
		for (int i = 0; i< radioGroup.getChildCount(); i++){
			RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
			radioButton.setOnCheckedChangeListener(null);
		}
		isHorizontal = 3 >= item.options.size();
		for (int i = 0; i< radioGroup.getChildCount(); i++){
			//binding checkbox
			if (i < item.options.size()){
				RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
				radioButton.setVisibility(View.VISIBLE);
				radioButton.setText(item.options.get(i).label);
				isHorizontal = item.options.get(i).label.length() < 4;
				radioButton.setTag(rowId+"|"+item.id+"|"+operatorId+"|"+item.options.get(i).value+"|0");
				radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
				if (split != null)
					for(int j = 0; j < split.length; j++){
						if (item.options.get(i).value.equalsIgnoreCase(split[j]))
							radioGroup.check(radioButton.getId());
					}
			}
			//remove unused checkbox
			else radioGroup.getChildAt(i).setVisibility(View.GONE);
		}

		//adding and binding if some checkbox is missing
		for(int i = radioGroup.getChildCount(); i < item.options.size(); i++){
			RadioButton radioButton = new RadioButton(context);
			radioButton.setText(item.options.get(i).label);
			isHorizontal = item.options.get(i).label.length() < 4;
			radioButton.setTag(rowId+"|"+item.id+"|"+operatorId+"|"+item.options.get(i).value+"|0");
			radioButton.setOnCheckedChangeListener(onCheckedChangeListener);
			radioGroup.addView(radioButton);
			if (split != null)
				for(int j = 0; j < split.length; j++){
					if (item.options.get(i).value.equalsIgnoreCase(split[j]))
						radioGroup.check(radioButton.getId());
				}
		}
		radioGroup.setOrientation(isHorizontal ? RadioGroup.HORIZONTAL : RadioGroup.VERTICAL);
	}

	private class ViewHolder {
		TextView label;
		TextView colored;
		TextView plain;
		TextView description;
		PhotoItemRadio photo;
		LinearLayout checkBox;
		FormInputText input;
		RadioGroup radio;
	}

}
