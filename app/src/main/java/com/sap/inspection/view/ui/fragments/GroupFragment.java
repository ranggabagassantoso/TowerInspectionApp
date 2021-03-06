package com.sap.inspection.view.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sap.inspection.R;
import com.sap.inspection.listener.GroupActivityListener;
import com.sap.inspection.model.ScheduleGeneral;
import com.sap.inspection.model.form.WorkFormRowModel;
import com.sap.inspection.tools.DebugLog;
import com.sap.inspection.view.adapter.GroupsAdapter;

import org.parceler.Parcels;

public class GroupFragment extends BaseFragment {
	private final String KEY_SCHEDULE = "KEY_SCHEDULE";
	private final String KEY_GROUP_ITEMS = "KEY_GROUP_ITEMS";
	private final String KEY_WORK_TYPE_NAME = "KEY_WORK_TYPE_NAME";

	private GroupsAdapter adapter;
	private ListView list;
	private View back, mainmenu;
	private TextView title, subTitle;
	private WorkFormRowModel groupItems;
	private ScheduleGeneral schedule;
    private String workTypeName;

	private GroupActivityListener backPressedListener;

	public static GroupFragment newInstance() {
		return new GroupFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DebugLog.d("onCreate");
		adapter = new GroupsAdapter(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		DebugLog.d("onCreateView");
		View root = inflater.inflate(R.layout.fragment_navigation, null);
		list = root.findViewById(R.id.list);
		title = root.findViewById(R.id.header_title);
		subTitle = root.findViewById(R.id.header_subtitle);
		back = root.findViewById(R.id.action_left);
		mainmenu = root.findViewById(R.id.action_right);

		if (savedInstanceState != null) {
			schedule = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SCHEDULE));
			groupItems = Parcels.unwrap(savedInstanceState.getParcelable(KEY_GROUP_ITEMS));
			workTypeName = savedInstanceState.getString(KEY_WORK_TYPE_NAME);
		}

		title.setText(schedule.site.name);
		subTitle.setText(schedule.work_type.name);
		mainmenu.setOnClickListener(v -> getActivity().finish());
		list.setAdapter(adapter);
		adapter.setItems(groupItems);
		adapter.setScheduleId(schedule.id);
		adapter.setWorkTypeName(workTypeName);
		back.setOnClickListener(v -> {
			if (null != backPressedListener)
				backPressedListener.myOnBackPressed();
		});
		return root;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_SCHEDULE, Parcels.wrap(schedule));
		outState.putParcelable(KEY_GROUP_ITEMS, Parcels.wrap(groupItems));
		outState.putString(KEY_WORK_TYPE_NAME, workTypeName);
	}

	public void setSchedule(ScheduleGeneral schedule) {
		this.schedule = schedule;
	}

	public void setGroupActivityListener(GroupActivityListener backPressedListener) {
		this.backPressedListener = backPressedListener;
	}
	
	public void setGroupItems(WorkFormRowModel groupItems) {
		this.groupItems = groupItems;
	}

	public void setWorkTypeName(String workTypeName) {
	    this.workTypeName = workTypeName;
    }

    public void setItems(WorkFormRowModel groupItems) {
		adapter.setItems(groupItems);
	}
}
