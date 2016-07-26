package tw.com.maxkit.simple.android.testgooglemap.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import tw.com.maxkit.simple.android.testgooglemap.R;
import tw.com.maxkit.simple.android.testgooglemap.adapter.MapInfoAdapter;
import tw.com.maxkit.simple.android.testgooglemap.data.LocationInfo;
import tw.com.maxkit.simple.android.testgooglemap.data.MarkerHelper;
import tw.com.maxkit.simple.android.testgooglemap.util.ConfigUtil;
import tw.com.maxkit.simple.android.testgooglemap.util.GsonUtil;
import tw.com.maxkit.simple.android.testgooglemap.util.PlaceJSONParser;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/*
 * 使用 Google Maps Adnroid API v2 完整客製化地圖(一)
 * http://blog.maxkit.com.tw/2015/04/google-maps-adnroid-api-v2.html
 * 
 * 使用 Google Maps Adnroid API v2 完整客製化地圖(二)
 * http://blog.maxkit.com.tw/2015/04/google-maps-adnroid-api-v2_24.html
 */

public class MainActivity extends BasicActivity implements LocationListener {
	
	private Button btn_lbs_find; // 下拉選單搜尋
	private EditText et_lbs_keyword;
	private Button btn_lbs_searchKeyword; // 關鍵字搜尋
	private SupportMapFragment frg_lbs_map;
	private ImageButton imgbtn_lbs_getLocation;  //share location

	private GoogleMap mGoogleMap;
	private GroundOverlay imageOverlay;
	private Spinner mSprPlaceType;
	private MapInfoAdapter mapInfoAdapter;

	private String[] mPlaceType = null;
	private String[] mPlaceTypeName = null;

	private double mLatitude = 0;
	private double mLongitude = 0;
	private int radius = 5000; // 地圖搜尋範圍

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// 載入物件
		initComponent();

		// 初始化
		initServerData();

		// 搜尋
		btn_lbs_find.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				et_lbs_keyword.setText("");
				int selectedPosition = mSprPlaceType.getSelectedItemPosition();
				StringBuilder sb = null;
				if (selectedPosition == 0) {
					// 自行車道
					searchKeyword("自行車道");
				} else {
					String type = mPlaceType[selectedPosition];
					sb = new StringBuilder(ConfigUtil.GOOGLE_SEARCH_API);
					sb.append("location=" + mLatitude + "," + mLongitude);
					sb.append("&radius=" + radius);
					sb.append("&types=" + type);
					sb.append("&sensor=true");
					sb.append("&key=" + ConfigUtil.API_KEY_GOOGLE_MAP);
					PlacesTask placesTask = new PlacesTask(MainActivity.this);
					placesTask.execute(sb.toString());
				}
			}
		});

		// 關鍵字搜尋
		btn_lbs_searchKeyword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 關閉鍵盤
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(et_lbs_keyword.getWindowToken(), 0); // et_setting_name為獲取焦點的EditText
				// 搜尋關鍵字
				String keyword = et_lbs_keyword.getText().toString();
				if (!TextUtils.isEmpty(keyword)) {
					searchKeyword(keyword);
				} else {
					Toast.makeText(MainActivity.this,
							getString(R.string.noKeyword), Toast.LENGTH_LONG)
							.show();
				}
			}
		});
		
		//share location
		imgbtn_lbs_getLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//取得map中心點座標
				Log.i(ConfigUtil.TAG, "中心點:" + mGoogleMap.getProjection().fromScreenLocation(new Point(frg_lbs_map.getView().getWidth()/2, frg_lbs_map.getView().getHeight()/2)));
			}
		});

		// map標識
		mGoogleMap
				.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

					@Override
					public void onInfoWindowClick(Marker arg0) {
						try {
							MarkerHelper markerHelper = GsonUtil.gson.fromJson(
									arg0.getSnippet(), MarkerHelper.class);
							Log.i(ConfigUtil.TAG, "phone:" + markerHelper.getTel());
							// 撥打電話
							Intent intent = new Intent(Intent.ACTION_CALL, Uri
									.parse("tel:" + markerHelper.getTel()));
							startActivity(intent);
						} catch (Exception e) {
							Log.e(ConfigUtil.TAG, "Exception:" + e);
						}
					}
				});
		
		//定位按鈕
		mGoogleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
			
			@Override
			public boolean onMyLocationButtonClick() {
				LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
					//如果GPS或網路定位開啟，更新位置
					Criteria criteria = new Criteria();
					String provider = locationManager.getBestProvider(criteria, true);  //取得定位裝置 ()
					Location location = locationManager.getLastKnownLocation(provider);
					if (location != null) {
						onLocationChanged(location);
					}
					locationManager.requestLocationUpdates(provider, 50000, 0, MainActivity.this);
					
				} else {
					Toast.makeText(MainActivity.this, "請打開定位功能", Toast.LENGTH_LONG).show();
				}
				return true;
			}
		});
	}

	// 連結實體物件
	private void initComponent() {
		btn_lbs_find = (Button) findViewById(R.id.btn_lbs_find);
		et_lbs_keyword = (EditText) findViewById(R.id.et_lbs_keyword);
		btn_lbs_searchKeyword = (Button) findViewById(R.id.btn_lbs_searchKeyword); // 關鍵字搜尋
		frg_lbs_map = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.frg_lbs_map);
		imgbtn_lbs_getLocation = (ImageButton) findViewById(R.id.imgbtn_lbs_getLocation);
	}

	// 初始化
	private void initServerData() {
		mPlaceType = getResources().getStringArray(R.array.place_type);
		mPlaceTypeName = getResources().getStringArray(R.array.place_type_name);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, mPlaceTypeName);
		mSprPlaceType = (Spinner) findViewById(R.id.spr_place_type);
		mSprPlaceType.setAdapter(adapter);

		//檢測 device 是否有安裝 google play services，且  google play services 版本是否符合需求
		int status = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(getBaseContext());

		if (status != ConnectionResult.SUCCESS) {
			int requestCode = 10;  //google map 最低 google play services 需求為 api 10
			Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
					requestCode);
			dialog.show();
		} else {
			mGoogleMap = frg_lbs_map.getMap();
			mGoogleMap.setMyLocationEnabled(true);  //顯示自己的位置
//			mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
			LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				//如果GPS或網路定位開啟，更新位置
				Criteria criteria = new Criteria();
				String provider = locationManager.getBestProvider(criteria, true);  //取得定位裝置 ()
				Location location = locationManager.getLastKnownLocation(provider);
				if (location != null) {
					onLocationChanged(location);
				}
				locationManager.requestLocationUpdates(provider, 50000, 0, this);
				
			} else {
				Toast.makeText(MainActivity.this, "請打開定位功能", Toast.LENGTH_LONG).show();
			}
		}
		
		//地圖上 marker 點擊後彈出的畫面
		mapInfoAdapter = new MapInfoAdapter(MainActivity.this, false);
		mGoogleMap.setInfoWindowAdapter(mapInfoAdapter);

		
		//準備預設要顯示的資料
		List<LocationInfo> locationInfoList = new ArrayList<LocationInfo>();
		//公司
		LocationInfo locationInfo1 = new LocationInfo("24.1651456", "120.66150479999999", "台中市西屯區漢口路二段151號13樓之9", "0423165803", "麥司奇科技股份有限公司", 1, R.drawable.maxkit);
		locationInfoList.add(locationInfo1);
		//客戶
		LocationInfo locationInfo2 = new LocationInfo("25.0322124", "121.52718989999994", "台北市大安區金山南路2段55號", null, "郵政總局", 2, null);
		locationInfoList.add(locationInfo2);
		LocationInfo locationInfo3 = new LocationInfo("25.078345", "121.56994299999997", "台北市11492內湖區瑞光路468號", null, "遠傳電信", 2, null);
		locationInfoList.add(locationInfo3);
		LocationInfo locationInfo4 = new LocationInfo("24.18106", "120.62026200000003", "臺中市西屯區台灣大道四段798號", "0800-021818", "台灣櫻花", 2, null);
		locationInfoList.add(locationInfo4);
		//自行車道
		LocationInfo locationInfo5 = new LocationInfo("24.136734", "120.69739600000003", "台中市東區東光園路446之1號", null, "東光園道自行車道", 3, R.drawable.bicycle_road1);
		locationInfoList.add(locationInfo5);
		LocationInfo locationInfo6 = new LocationInfo("24.2327708", "120.69442279999998", "台中市神岡區潭雅神綠園道", null, "潭雅神綠園道", 3, R.drawable.bicycle_road2);
		locationInfoList.add(locationInfo6);
		LocationInfo locationInfo7 = new LocationInfo("24.1734898", "120.70740120000005", "台中市北屯區旱溪西路三段", null, "旱溪「親水式」自行車道", 3, R.drawable.bicycle_road3);
		locationInfoList.add(locationInfo7);
		
		parseLocationAndShowMap(locationInfoList);
	}

	/**
	 * 用關鍵字搜尋地標
	 * 
	 * @param keyword
	 */
	private void searchKeyword(String keyword) {
		try {
			String unitStr = URLEncoder.encode(keyword, "utf8");  //字體要utf8編碼
			StringBuilder sb = new StringBuilder(ConfigUtil.GOOGLE_SEARCH_API);
			sb.append("location=" + mLatitude + "," + mLongitude);
			sb.append("&radius=" + radius);
			sb.append("&keyword=" + unitStr);
			sb.append("&sensor=true");
			sb.append("&key=" + ConfigUtil.API_KEY_GOOGLE_MAP);  //server key
			PlacesTask placesTask = new PlacesTask(MainActivity.this);
			placesTask.execute(sb.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.i(ConfigUtil.TAG, "Exception:" + e);
		}
	}

	/** A class, to download Google Places */
	private class PlacesTask extends AsyncTask<String, Integer, String> {

		private MainActivity context = null;
		String data = null;

		public PlacesTask(MainActivity context) {
			this.context = context;
		}

		@Override
		protected String doInBackground(String... url) {
			try {
				data = downloadUrl(url[0]);
			} catch (Exception e) {
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			context.dialog = ProgressDialog.show(context, "",
					context.getString(R.string.loading), true);
		}

		@Override
		protected void onPostExecute(String result) {
			context.dialog.dismiss();
			ParserTask parserTask = new ParserTask();
			parserTask.execute(result);
		}
	}

	/** A method to download json data from url */
	private String downloadUrl(String strUrl) throws IOException {
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.connect();
			iStream = urlConnection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			data = sb.toString();
			br.close();
		} catch (Exception e) {
			Log.d("Exception while downloading url", e.toString());
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	/** A class to parse the Google Places in JSON format */
	private class ParserTask extends
			AsyncTask<String, Integer, List<HashMap<String, String>>> {

		JSONObject jObject;

		@Override
		protected List<HashMap<String, String>> doInBackground(
				String... jsonData) {

			List<HashMap<String, String>> places = null;
			PlaceJSONParser placeJsonParser = new PlaceJSONParser();

			try {
				jObject = new JSONObject(jsonData[0]);
				places = placeJsonParser.parse(jObject);

			} catch (Exception e) {
				Log.d("Exception", e.toString());
			}
			return places;
		}

		@Override
		protected void onPostExecute(List<HashMap<String, String>> list) {

			// Clears all the existing markers
			mGoogleMap.clear();
			mapInfoAdapter.setKeyword(true);
			for (int i = 0; i < list.size(); i++) {
				MarkerOptions markerOptions = new MarkerOptions();
				HashMap<String, String> hmPlace = list.get(i);
				double lat = Double.parseDouble(hmPlace.get("lat"));
				double lng = Double.parseDouble(hmPlace.get("lng"));
				LatLng latLng = new LatLng(lat, lng);
				markerOptions.position(latLng);
				String name = hmPlace.get("place_name");
				markerOptions.title(name);
				String vicinity = hmPlace.get("vicinity");
				MarkerHelper markerHelper = new MarkerHelper(name, vicinity);
				String snippet = GsonUtil.gson.toJson(markerHelper);
				markerOptions.snippet(snippet);
				mGoogleMap.addMarker(markerOptions);
			}
			LatLng latLng = new LatLng(mLatitude, mLongitude);
			addMyLocationIcon(latLng);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		//location有異動才更新畫面
		if(mLatitude!=location.getLatitude() || mLongitude!=location.getLongitude()){
			mLatitude = location.getLatitude();
			mLongitude = location.getLongitude();
			LatLng latLng = new LatLng(mLatitude, mLongitude);

			mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

			// 在map上加上圖示(黃色小人)
			if (imageOverlay != null) {
				imageOverlay.remove();
			}
			addMyLocationIcon(latLng);
		}
	}

	/**
	 * 在 map上增加自己的位置
	 * @param latLng
	 */
	private void addMyLocationIcon(LatLng latLng) {
		GroundOverlayOptions newarkMap = new GroundOverlayOptions().image(
				BitmapDescriptorFactory.fromResource(R.drawable.man1))
				.position(latLng, 94, 200);
		imageOverlay = mGoogleMap.addGroundOverlay(newarkMap);
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	/**
	 * 在map上顯示門市句點的位置
	 * 
	 * @param locationInfoList
	 */
	public void parseLocationAndShowMap(List<LocationInfo> locationInfoList) {
		mGoogleMap.clear();
		mapInfoAdapter.setKeyword(false);
		
		// 標注據點位置
		for (LocationInfo locationInfo : locationInfoList) {
			MarkerOptions markerOptions = new MarkerOptions();
			double lat = Double.parseDouble(locationInfo.getLat());
			double lng = Double.parseDouble(locationInfo.getLng());
			LatLng latLng = new LatLng(lat, lng);
			markerOptions.position(latLng);

			String name = locationInfo.getName();
			int icon = R.drawable.location;
			switch (locationInfo.getAtype()) {
			case 1:
				// 公司
				icon = R.drawable.maxkit;
				break;
			case 2:
				// 客戶
				icon = R.drawable.custom;
				break;
			case 3:
				// 自行車道
				icon = R.drawable.bicycle;
				break;
			default:
				icon = R.drawable.location;
				break;
			}
			markerOptions.title(name); // map上icon點擊後顯示資料
			markerOptions.icon(BitmapDescriptorFactory.fromResource(icon)); // 設定map上顯示的圖示

			MarkerHelper markerHelper = new MarkerHelper(locationInfo);
			String snippet = GsonUtil.gson.toJson(markerHelper);
			markerOptions.snippet(snippet);
			mGoogleMap.addMarker(markerOptions);
		}
		LatLng latLng = new LatLng(mLatitude, mLongitude);
		addMyLocationIcon(latLng);
	}

}
