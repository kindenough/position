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
import com.amap.api.maps2d.model.LatLng;
import com.example.usedaoforgen.bean.POSITION;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
/**
 * Polling service
 * @Author Ryan
 * @Create 2013-7-13 上午10:18:44
 */
public class PollingService extends Service {

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	
	public static final String ACTION = "com.ryantang.service.PollingService";
	
	private Notification mNotification;
	private NotificationManager mManager;

	String szImei;

	PowerManager pm;
	WakeLock wakeLock = null; //锁屏后继续运行，不起作用
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		initNotifiManager();

		TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		szImei = TelephonyMgr.getDeviceId();// 唯一标识码
		
		acquireWakeLock();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		new PollingThread().start();
		//getLocation();
	}

	private void initNotifiManager() {
		mManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_launcher;
		mNotification = new Notification();
		mNotification.icon = icon;
		mNotification.tickerText = "New Message";
		mNotification.defaults |= Notification.DEFAULT_SOUND;
		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
	}

	private void showNotification() {
		mNotification.when = System.currentTimeMillis();
		//Navigator to the new activity when click the notification title
		Intent i = new Intent(this, MessageActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i,
				Intent.FLAG_ACTIVITY_NEW_TASK);
		mNotification.setLatestEventInfo(this,
				getResources().getString(R.string.app_name), "Notification "+Utils.formatUTC(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss"), pendingIntent);
		mManager.notify(0, mNotification);
		
		getLocation();
	}

	/**
	 * Polling thread
	 * @Author Ryan
	 * @Create 2013-7-13 上午10:18:34
	 */
	int count = 0;
	class PollingThread extends Thread {
		@Override
		public void run() {
			System.out.println("Polling...");
			count ++;
			if (count % 5 == 0) {
				showNotification();
				System.out.println("New message!");
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		System.out.println("Service:onDestroy");
		releaseWakeLock();
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
		if(locationClient==null)
		{
			locationClient = new AMapLocationClient(this.getApplicationContext());
			locationOption = getDefaultOption();
			// 设置定位参数
			locationClient.setLocationOption(locationOption);
			// 设置定位监听
			locationClient.setLocationListener(locationListener);
		}
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
		mOption.setOnceLocation(true);// 可选，设置是否单次定位。默认是false
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
				///tvResult.setText(result);

				// 网络请求
				KJHttp kjh = new KJHttp();
				HttpParams params = new HttpParams();
				params.put("tel", "");
				params.put("rec", "");
				params.put("x", Double.toString(location.getLongitude()));
				params.put("y", Double.toString(location.getLatitude()));
				params.put("speed", Float.toString(location.getSpeed()));
				params.put("time", Utils.formatUTC(System.currentTimeMillis(),
						"yyyy-MM-dd HH:mm:ss"));
				params.put("accuracy", location.getAccuracy() + "米");
				params.put("deviceid", szImei);
				kjh.post("http://www.maomx.cn/position/postposition", params,
						new HttpCallBack() {
							@Override
							public void onPreStart() {
								super.onPreStart();
								KJLoger.debug("即将开始http请求");
								// et.setText("即将开始http请求");
							}

							@Override
							public void onSuccess(String t) {
								super.onSuccess(t);
								ViewInject.longToast("请求成功");
								KJLoger.debug("请求成功:" + t.toString());
								// et.setText("请求成功:" + t.toString());
							}

							@Override
							public void onFailure(int errorNo, String strMsg) {
								super.onFailure(errorNo, strMsg);
								KJLoger.debug("出现异常:" + strMsg);
								// et.setText("出现异常:" + strMsg);
							}

							@Override
							public void onFinish() {
								super.onFinish();
								KJLoger.debug("请求完成，不管成功还是失败");
							}
						});

			} else {
				//tvResult.setText("定位失败，loc is null");
			}
		}
	};

	//获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行  
    private void acquireWakeLock()  
    {  
        if (null == wakeLock)  
        {  
            pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);  
//            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, getClass().getCanonicalName());
            
            if (null != wakeLock)  
            {  
                wakeLock.acquire();  
            }  
        }  
    }  
      
    //释放设备电源锁  
    private void releaseWakeLock()  
    {  
        if (null != wakeLock)  
        {  
            wakeLock.release();  
            wakeLock = null;  
        }  
    } 
}
