package tw.com.maxkit.simple.android.testgooglemap.adapter;

import java.io.File;
import java.io.IOException;

import tw.com.maxkit.simple.android.testgooglemap.R;
import tw.com.maxkit.simple.android.testgooglemap.data.MarkerHelper;
import tw.com.maxkit.simple.android.testgooglemap.util.ConfigUtil;
import tw.com.maxkit.simple.android.testgooglemap.util.GsonUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

public class MapInfoAdapter implements InfoWindowAdapter {
	
	private Context context;
	private boolean isKeyword;

	public MapInfoAdapter(Context ctx, boolean isKeyword) {
		super();
		this.context = ctx; // 抓Activity
		this.isKeyword = isKeyword;
	}

	@Override
	public View getInfoContents(Marker marker) {
		// 連結xml中實體物件
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View infoWindow = inflater.inflate(R.layout.mapinfo_sbl, null);
	    TextView tv_title = (TextView)infoWindow.findViewById(R.id.tv_mapInfo_title);
	    TextView tv_addr = (TextView)infoWindow.findViewById(R.id.tv_mapInfo_addr);
	    TextView tv_tel = (TextView)infoWindow.findViewById(R.id.tv_mapInfo_tel);
	    ImageView imgv_pic = (ImageView)infoWindow.findViewById(R.id.imgv_mapInfo_pic);
	    
	    MarkerHelper markerHelper = GsonUtil.gson.fromJson(marker.getSnippet(),
				MarkerHelper.class);
	    
	    //設定title
	    tv_title.setText(marker.getTitle());
	    
	    //設定地址
	    String addr = markerHelper.getAddr();
	    if(TextUtils.isEmpty(addr)){
	    	tv_addr.setText("");
	    }else{
	    	tv_addr.setText(addr);
	    }
	    
	    //設定電話
	    String tel = markerHelper.getTel();
	    if(TextUtils.isEmpty(tel)){
	    	tv_tel.setText("");
	    }else{
	    	tv_tel.setText(tel);
	    }
	    
	    if(!this.isKeyword){  //關鍵字搜尋不用顯示圖片
	    	imgv_pic.setVisibility(View.VISIBLE);
		    Integer path = markerHelper.getPic();
		    if(path!=null){
				imgv_pic.setImageResource(path);
		    }else{
		    	Log.i(ConfigUtil.TAG, "no image");
		    }
	    }else{
	    	imgv_pic.setVisibility(View.GONE);
	    }
	    
		return infoWindow;
	}

	@Override
	public View getInfoWindow(Marker arg0) {
		return null;
	}

	public void setKeyword(boolean isKeyword) {
		this.isKeyword = isKeyword;
	}
}
