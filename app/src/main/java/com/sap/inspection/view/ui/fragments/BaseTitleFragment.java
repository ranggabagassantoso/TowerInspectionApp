package com.sap.inspection.view.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arifariyan.baseassets.R;

public abstract class BaseTitleFragment extends BaseFragment {
	protected FrameLayout frameLayout;
	private TextView title;
	private RelativeLayout header;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_with_titleheader, null, false);
		header = root.findViewById(R.id.header);
		View headerTitleLayout = inflater.inflate(R.layout.header_title, header, true);
		title = headerTitleLayout.findViewById(R.id.header_title);
		title.setText(getTitle());
		frameLayout = root.findViewById(R.id.fragment_layout);
		frameLayout.addView(onGetLayout(inflater));
		return root;
	}
	
	public abstract View onGetLayout(LayoutInflater inflater);
	
	public abstract String getTitle(); 
}
