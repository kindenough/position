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
		szImei = TelephonyMgr.getDeviceId();// Ψһ��ʶ��

		// Start polling service
		System.out.println("Start polling service...");
		PollingUtils.startPollingService(this, 6, PollingService.class,
				PollingService.ACTION);

		// ��ȡ��ͼ�ؼ�����
		mMapView = (MapView) findViewById(R.id.map);
		// ��activityִ��onCreateʱִ��mMapView.onCreate(savedInstanceState)��������ͼ
		mMapView.onCreate(savedInstanceState);

		// tvResult.setText("aMap == null");
		if (aMap == null) {
			aMap = mMapView.getMap();
			addMarkersToMap();// ����ͼ�����marker
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
	 * ��λ
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private void getLocation() {
		// ��ʼ��client
		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = getDefaultOption();
		// ���ö�λ����
		locationClient.setLocationOption(locationOption);
		// ���ö�λ����
		locationClient.setLocationListener(locationListener);
		locationClient.startLocation();
	}

	/**
	 * Ĭ�ϵĶ�λ����
	 * 
	 * @since 2.8.0
	 * @author hongming.wang
	 *
	 */
	private AMapLocationClientOption getDefaultOption() {
		AMapLocationClientOption mOption = new AMapLocationClientOption();
		mOption.setLocationMode(AMapLocationMode.Hight_Accuracy);// ��ѡ�����ö�λģʽ����ѡ��ģʽ�и߾��ȡ����豸�������硣Ĭ��Ϊ�߾���ģʽ
		mOption.setGpsFirst(false);// ��ѡ�������Ƿ�gps���ȣ�ֻ�ڸ߾���ģʽ����Ч��Ĭ�Ϲر�
		mOption.setHttpTimeOut(30000);// ��ѡ��������������ʱʱ�䡣Ĭ��Ϊ30�롣�ڽ��豸ģʽ����Ч
		mOption.setInterval(2000);// ��ѡ�����ö�λ�����Ĭ��Ϊ2��
		mOption.setNeedAddress(true);// ��ѡ�������Ƿ񷵻�������ַ��Ϣ��Ĭ����true
		// mOption.setOnceLocation(true);
		mOption.setOnceLocation(false);// ��ѡ�������Ƿ񵥴ζ�λ��Ĭ����false
		mOption.setOnceLocationLatest(false);// ��ѡ�������Ƿ�ȴ�wifiˢ�£�Ĭ��Ϊfalse.�������Ϊtrue,���Զ���Ϊ���ζ�λ��������λʱ��Ҫʹ��
		AMapLocationClientOption.setLocationProtocol(AMapLocationProtocol.HTTP);// ��ѡ��
																				// �������������Э�顣��ѡHTTP����HTTPS��Ĭ��ΪHTTP
		mOption.setSensorEnable(false);// ��ѡ�������Ƿ�ʹ�ô�������Ĭ����false
		mOption.setWifiScan(true); // ��ѡ�������Ƿ���wifiɨ�衣Ĭ��Ϊtrue���������Ϊfalse��ͬʱֹͣ����ˢ�£�ֹͣ�Ժ���ȫ������ϵͳˢ�£���λλ�ÿ��ܴ������
		mOption.setLocationCacheEnable(true); // ��ѡ�������Ƿ�ʹ�û��涨λ��Ĭ��Ϊtrue
		return mOption;
	}

	/**
	 * ��ȡGPS״̬���ַ���
	 * 
	 * @param statusCode
	 *            GPS״̬��
	 * @return
	 */
	private String getGPSStatusString(int statusCode) {
		String str = "";
		switch (statusCode) {
		case AMapLocationQualityReport.GPS_STATUS_OK:
			str = "GPS״̬����";
			break;
		case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
			str = "�ֻ���û��GPS Provider���޷�����GPS��λ";
			break;
		case AMapLocationQualityReport.GPS_STATUS_OFF:
			str = "GPS�رգ����鿪��GPS����߶�λ����";
			break;
		case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
			str = "ѡ��Ķ�λģʽ�в�����GPS��λ������ѡ�����GPS��λ��ģʽ����߶�λ����";
			break;
		case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
			str = "û��GPS��λȨ�ޣ����鿪��gps��λȨ��";
			break;
		}
		return str;
	}

	/**
	 * ��λ����
	 */
	AMapLocationListener locationListener = new AMapLocationListener() {
		@Override
		public void onLocationChanged(AMapLocation location) {
			// tvResult.setText("onLocationChanged");
			if (null != location) {

				StringBuffer sb = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				// errCode����0����λ�ɹ���������Ϊ��λʧ�ܣ�����Ŀ��Բ��չ�����λ������˵��
				if (location.getErrorCode() == 0) {
					sb.append("��λ�ɹ�" + "\n");
					sb.append("��λ����: " + location.getLocationType() + "\n");
					sb.append("��    ��    : " + location.getLongitude() + "\n");
					sb.append("γ    ��    : " + location.getLatitude() + "\n");
					sb.append("��    ��    : " + location.getAccuracy() + "��"
							+ "\n");
					sb.append("�ṩ��    : " + location.getProvider() + "\n");

					sb.append("��    ��    : " + location.getSpeed() + "��/��"
							+ "\n");
					sb.append("��    ��    : " + location.getBearing() + "\n");
					// ��ȡ��ǰ�ṩ��λ��������Ǹ���
					sb.append("��    ��    : " + location.getSatellites() + "\n");
					sb.append("��    ��    : " + location.getCountry() + "\n");
					sb.append("ʡ            : " + location.getProvince() + "\n");
					sb.append("��            : " + location.getCity() + "\n");
					sb.append("���б��� : " + location.getCityCode() + "\n");
					sb.append("��            : " + location.getDistrict() + "\n");
					sb.append("���� ��   : " + location.getAdCode() + "\n");
					sb.append("��    ַ    : " + location.getAddress() + "\n");
					sb.append("��Ȥ��    : " + location.getPoiName() + "\n");
					// ��λ��ɵ�ʱ��
					sb.append("��λʱ��: "
							+ Utils.formatUTC(location.getTime(),
									"yyyy-MM-dd HH:mm:ss") + "\n");

					if (aMap != null) {
						aMap.clear();
						latlng = new LatLng(location.getLatitude(),
								location.getLongitude());
						addMarkersToMap();
					}
				} else {
					// ��λʧ��
					sb.append("��λʧ��" + "\n");
					sb.append("������:" + location.getErrorCode() + "\n");
					sb.append("������Ϣ:" + location.getErrorInfo() + "\n");
					sb.append("��������:" + location.getLocationDetail() + "\n");
				}
				sb.append("***��λ��������***").append("\n");
				sb.append("* WIFI���أ�")
						.append(location.getLocationQualityReport()
								.isWifiAble() ? "����" : "�ر�").append("\n");
				sb.append("* GPS״̬��")
						.append(getGPSStatusString(location
								.getLocationQualityReport().getGPSStatus()))
						.append("\n");
				sb.append("* GPS������")
						.append(location.getLocationQualityReport()
								.getGPSSatellites()).append("\n");
				sb.append("****************").append("\n");
				// ��λ֮��Ļص�ʱ��
				sb.append("�ص�ʱ��: "
						+ Utils.formatUTC(System.currentTimeMillis(),
								"yyyy-MM-dd HH:mm:ss") + "\n");

				// ///
				sb2.append("��λ�ɹ�" + "\n");
				sb2.append("��λ����: " + location.getLocationType() + "\n");
				sb2.append("��    ��    : " + location.getAccuracy() + "��" + "\n");
				sb2.append("��    ַ    : " + location.getAddress() + "\n");

				// ������λ�����
				String result = sb2.toString();
				tvResult.setText(result);

				// �������ݿ�
				POSITION pos = new POSITION();
				pos.setTEL("test");
				pos.setX(location.getLongitude());
				pos.setY(location.getLatitude());
				pos.setSPEED(location.getSpeed());
				Date date = new Date(System.currentTimeMillis());
				pos.setTIME(date);
				pos.setSTRTIME(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(date));
				pos.setACCURACY(location.getAccuracy() + "��");
				pos.setDEVICEID("rtpollingdemo");
				positionDao.insert(pos);

				// ��������
//				KJHttp kjh = new KJHttp();
//				HttpParams params = new HttpParams();
//				params.put("tel", "");
//				params.put("rec", "");
//				params.put("x", Double.toString(location.getLongitude()));
//				params.put("y", Double.toString(location.getLatitude()));
//				params.put("speed", Float.toString(location.getSpeed()));
//				params.put("time", Utils.formatUTC(System.currentTimeMillis(),
//						"yyyy-MM-dd HH:mm:ss"));
//				params.put("accuracy", location.getAccuracy() + "��");
//				params.put("deviceid", szImei);
//				kjh.post("http://www.maomx.cn/position/postposition", params,
//						new HttpCallBack() {
//							@Override
//							public void onPreStart() {
//								super.onPreStart();
//								KJLoger.debug("������ʼhttp����");
//								// et.setText("������ʼhttp����");
//							}
//
//							@Override
//							public void onSuccess(String t) {
//								super.onSuccess(t);
//								ViewInject.longToast("����ɹ�");
//								KJLoger.debug("����ɹ�:" + t.toString());
//								// et.setText("����ɹ�:" + t.toString());
//							}
//
//							@Override
//							public void onFailure(int errorNo, String strMsg) {
//								super.onFailure(errorNo, strMsg);
//								KJLoger.debug("�����쳣:" + strMsg);
//								// et.setText("�����쳣:" + strMsg);
//							}
//
//							@Override
//							public void onFinish() {
//								super.onFinish();
//								KJLoger.debug("������ɣ����ܳɹ�����ʧ��");
//							}
//						});

			} else {
				tvResult.setText("��λʧ�ܣ�loc is null");
			}
		}
	};
}
