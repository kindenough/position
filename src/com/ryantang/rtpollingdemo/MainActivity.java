package com.ryantang.rtpollingdemo;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpParams;
import org.kymjs.kjframe.ui.ViewInject;
import org.kymjs.kjframe.utils.KJLoger;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationClientOption.AMapLocationProtocol;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MarkerOptions;
import com.example.usedaoforgen.bean.POSITION;
import com.example.usedaoforgen.dao.DaoMaster;
import com.example.usedaoforgen.dao.DaoSession;
import com.example.usedaoforgen.dao.POSITIONDao;
import com.example.usedaoforgen.dao.DaoMaster.DevOpenHelper;
import com.ryantang.rtpollingdemo.Utils;
//import com.example.usedaoforgen.bean.POSITION;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.widget.TextView;

public class MainActivity extends Activity {

	MapView mMapView = null;
	private MarkerOptions markerOption;
	private AMap aMap;
	private LatLng latlng = new LatLng(24.481128, 118.185798);

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;

	TextView tvResult;

	private static final String pathDB = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/DBpath/rtpollingdemo.DB";

	private SQLiteDatabase db = null;
	private DaoMaster master;
	private DaoSession session;
	private POSITIONDao positionDao;

	String szImei;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tvResult = (TextView) findViewById(R.id.textView1);

		TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		szImei = TelephonyMgr.getDeviceId();// 唯一标识码

		// Start polling service
		System.out.println("Start polling service...");
		PollingUtils.startPollingService(this, 6, PollingService.class,
				PollingService.ACTION);

		// 获取地图控件引用
		mMapView = (MapView) findViewById(R.id.map);
		// 在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
		mMapView.onCreate(savedInstanceState);

		// tvResult.setText("aMap == null");
		if (aMap == null) {
			aMap = mMapView.getMap();
			addMarkersToMap();// 往地图上添加marker
		}

		try {
			DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, pathDB,
					null);
			db = helper.getWritableDatabase();
			master = new DaoMaster(db);
			session = master.newSession();
			positionDao = session.getPOSITIONDao();
		} catch (Exception ex) {
			// etTel.setText(ex.getMessage());
		}

		//getLocation();

		//隐藏窗口
		//moveTaskToBack(true);  
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Stop polling service
		System.out.println("Stop polling service...");
		PollingUtils.stopPollingService(this, PollingService.class,
				PollingService.ACTION);
	}

	private void addMarkersToMap() {
		// TODO Auto-generated method stub

		markerOption = new MarkerOptions()
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
				.position(latlng).draggable(true);
		aMap.addMarker(markerOption);
		aMap.moveCamera(CameraUpdateFactory.changeLatLng(latlng));
	}

	/**
	 * 定位
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void getLocation() {
		// 初始化client
		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = getDefaultOption();
		// 设置定位参数
		locationClient.setLocationOption(locationOption);
		// 设置定位监听
		locationClient.setLocationListener(locationListener);
		locationClient.startLocation();
	}

	/**
	 * 默认的定位参数
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private AMapLocationClientOption getDefaultOption() {
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);// 可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
		mOption.setGpsFirst(false);// 可选，设置是否gps优先，只在高精度模式下有效。默认关闭
		mOption.setHttpTimeOut(30000);// 可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
		mOption.setInterval(2000);// 可选，设置定位间隔。默认为2秒
		mOption.setNeedAddress(true);// 可选，设置是否返回逆地理地址信息。默认是true
		// mOption.setOnceLocation(true);
		mOption.setOnceLocation(false);// 可选，设置是否单次定位。默认是false
		mOption.setOnceLocationLatest(false);// 可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);// 可选，
																				// 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
		mOption.setSensorEnable(false);// 可选，设置是否使用传感器。默认是false
		mOption.setWifiScan(true); // 可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
		mOption.setLocationCacheEnable(true); // 可选，设置是否使用缓存定位，默认为true
		return mOption;
	}

	/**
	 * 获取GPS状态的字符串
	 * 
	 * @param statusCode
	 *            GPS状态码
	 * @return
	 */
	private String getGPSStatusString(int statusCode) {
		String str = "";
		switch (statusCode) {
		case AMapLocationQualityReport.GPS_STATUS_OK:
			str = "GPS状态正常";
			break;
		case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
			str = "手机中没有GPS Provider，无法进行GPS定位";
			break;
		case AMapLocationQualityReport.GPS_STATUS_OFF:
			str = "GPS关闭，建议开启GPS，提高定位质量";
			break;
		case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
			str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
			break;
		case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
			str = "没有GPS定位权限，建议开启gps定位权限";
			break;
		}
		return str;
	}

	/**
	 * 定位监听
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation location) {
			// tvResult.setText("onLocationChanged");
			if (null != location) {

				StringBuffer sb = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				// errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
				if (location.getErrorCode() == 0) {
					sb.append("定位成功" + "\n");
					sb.append("定位类型: " + location.getLocationType() + "\n");
					sb.append("经    度    : " + location.getLongitude() + "\n");
					sb.append("纬    度    : " + location.getLatitude() + "\n");
					sb.append("精    度    : " + location.getAccuracy() + "米"
							+ "\n");
					sb.append("提供者    : " + location.getProvider() + "\n");

					sb.append("速    度    : " + location.getSpeed() + "米/秒"
							+ "\n");
					sb.append("角    度    : " + location.getBearing() + "\n");
					// 获取当前提供定位服务的卫星个数
					sb.append("星    数    : " + location.getSatellites() + "\n");
					sb.append("国    家    : " + location.getCountry() + "\n");
					sb.append("省            : " + location.getProvince() + "\n");
					sb.append("市            : " + location.getCity() + "\n");
					sb.append("城市编码 : " + location.getCityCode() + "\n");
					sb.append("区            : " + location.getDistrict() + "\n");
					sb.append("区域 码   : " + location.getAdCode() + "\n");
					sb.append("地    址    : " + location.getAddress() + "\n");
					sb.append("兴趣点    : " + location.getPoiName() + "\n");
					// 定位完成的时间
					sb.append("定位时间: "
							+ Utils.formatUTC(location.getTime(),
									"yyyy-MM-dd HH:mm:ss") + "\n");

					if (aMap != null) {
						aMap.clear();
						latlng = new LatLng(location.getLatitude(),
								location.getLongitude());
						addMarkersToMap();
					}
				} else {
					// 定位失败
					sb.append("定位失败" + "\n");
					sb.append("错误码:" + location.getErrorCode() + "\n");
					sb.append("错误信息:" + location.getErrorInfo() + "\n");
					sb.append("错误描述:" + location.getLocationDetail() + "\n");
				}
				sb.append("***定位质量报告***").append("\n");
				sb.append("* WIFI开关：")
						.append(location.getLocationQualityReport()
								.isWifiAble() ? "开启" : "关闭").append("\n");
				sb.append("* GPS状态：")
						.append(getGPSStatusString(location
								.getLocationQualityReport().getGPSStatus()))
						.append("\n");
				sb.append("* GPS星数：")
						.append(location.getLocationQualityReport()
								.getGPSSatellites()).append("\n");
				sb.append("****************").append("\n");
				// 定位之后的回调时间
				sb.append("回调时间: "
						+ Utils.formatUTC(System.currentTimeMillis(),
								"yyyy-MM-dd HH:mm:ss") + "\n");

				// ///
				sb2.append("定位成功" + "\n");
				sb2.append("定位类型: " + location.getLocationType() + "\n");
				sb2.append("精    度    : " + location.getAccuracy() + "米" + "\n");
				sb2.append("地    址    : " + location.getAddress() + "\n");

				// 解析定位结果，
				String result = sb2.toString();
				tvResult.setText(result);

				// 本地数据库
				POSITION pos = new POSITION();
				pos.setTEL("test");
				pos.setX(location.getLongitude());
				pos.setY(location.getLatitude());
				pos.setSPEED(location.getSpeed());
				Date date = new Date(System.currentTimeMillis());
				pos.setTIME(date);
				pos.setSTRTIME(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(date));
				pos.setACCURACY(location.getAccuracy() + "米");
				pos.setDEVICEID("rtpollingdemo");
				positionDao.insert(pos);

				// 网络请求
//				KJHttp kjh = new KJHttp();
//				HttpParams params = new HttpParams();
//				params.put("tel", "");
//				params.put("rec", "");
//				params.put("x", Double.toString(location.getLongitude()));
//				params.put("y", Double.toString(location.getLatitude()));
//				params.put("speed", Float.toString(location.getSpeed()));
//				params.put("time", Utils.formatUTC(System.currentTimeMillis(),
//						"yyyy-MM-dd HH:mm:ss"));
//				params.put("accuracy", location.getAccuracy() + "米");
//				params.put("deviceid", szImei);
//				kjh.post("http://www.maomx.cn/position/postposition", params,
//						new HttpCallBack() {
//							@Override
//							public void onPreStart() {
//								super.onPreStart();
//								KJLoger.debug("即将开始http请求");
//								// et.setText("即将开始http请求");
//							}
//
//							@Override
//							public void onSuccess(String t) {
//								super.onSuccess(t);
//								ViewInject.longToast("请求成功");
//								KJLoger.debug("请求成功:" + t.toString());
//								// et.setText("请求成功:" + t.toString());
//							}
//
//							@Override
//							public void onFailure(int errorNo, String strMsg) {
//								super.onFailure(errorNo, strMsg);
//								KJLoger.debug("出现异常:" + strMsg);
//								// et.setText("出现异常:" + strMsg);
//							}
//
//							@Override
//							public void onFinish() {
//								super.onFinish();
//								KJLoger.debug("请求完成，不管成功还是失败");
//							}
//						});

			} else {
				tvResult.setText("定位失败，loc is null");
			}
		}
	};
}
