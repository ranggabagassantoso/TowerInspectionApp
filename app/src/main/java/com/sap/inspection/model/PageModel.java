package com.sap.inspection.model;

import android.os.Parcel;

public class PageModel extends BaseModel {
    public int current;
    public int limit;
    public int total;
    public int records;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}
}