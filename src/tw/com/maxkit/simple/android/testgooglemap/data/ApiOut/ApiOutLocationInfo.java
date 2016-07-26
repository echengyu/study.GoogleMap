package tw.com.maxkit.simple.android.testgooglemap.data.ApiOut;

import java.util.List;

import tw.com.maxkit.simple.android.testgooglemap.data.LocationInfo;

public class ApiOutLocationInfo extends ApiOut {

	private List<LocationInfo> locationInfoList;

	public List<LocationInfo> getLocationInfoList() {
		return locationInfoList;
	}

	public void setLocationInfoList(List<LocationInfo> locationInfoList) {
		this.locationInfoList = locationInfoList;
	}
}
