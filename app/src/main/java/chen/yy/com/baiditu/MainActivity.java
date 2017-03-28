package chen.yy.com.baiditu;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextOptions;
import com.baidu.mapapi.model.LatLng;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



public class MainActivity extends AppCompatActivity implements View.OnClickListener, BaiduMap
		.OnMapLoadedCallback {
	MapView mp;
	private BaiduMap map;
	private LocationClient mLocation;
	private MyLocation myLocationListener;
	private boolean isFirst;
	private BitmapDescriptor arrow;
	private ImageView back_location;
	private double myLatid;
	private double myLong;
	private OrientationListener mOrientation;
	private ClusterManager<MyItem> mClusterManager;
	private MapStatus ms;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		initBaidu();
		initPosition();
		initLocation();

		back_location = (ImageView) findViewById(R.id.back_location);
		back_location.setOnClickListener(this);
	}

	private void initOver() {
		mClusterManager=new ClusterManager<MyItem>(this,map);
		//添加覆盖点
		addMarkers();
		map.setOnMarkerClickListener(mClusterManager);
		map.setOnMapStatusChangeListener(mClusterManager);
		mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MyItem>() {
			@Override
			public void onClusterItemInfoWindowClick(MyItem item) {
				Toast.makeText(MainActivity.this, "niaho", Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void initLocation() {
		myLocationListener = new MyLocation();
		LocationClientOption llo = new LocationClientOption();
		llo.setCoorType("gcj02");
		llo.setOpenGps(true);
		llo.setIsNeedAddress(true);
		llo.setScanSpan(1000);
		map.setMyLocationEnabled(true);
		arrow = BitmapDescriptorFactory.fromResource(R.drawable.arrow);
		mLocation = new LocationClient(this, llo);
		mLocation.registerLocationListener(myLocationListener);
		mLocation.start();
		mOrientation = new OrientationListener(this);
		mOrientation.setmOtl(new OrientationListener.OrientationChangeListener() {
			@Override
			public void onOrientationChangeListener(float x) {
				if (isFirst) {
					MyLocationData my = new MyLocationData.Builder()
//						.accuracy(bdLocation.getRadius())
							.direction(x)
							.latitude(myLatid)
							.longitude(myLong)
							.build();
					map.setMyLocationData(my);
				}
			}
		});
		mOrientation.start();
	}

	private void initPosition() {
		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		LatLng position = new LatLng(50, 50
		);
		OverlayOptions options = new TextOptions()
				.bgColor(Color.BLACK)
				.text("chenrongfa")
				.rotate(-30)
				.fontColor(Color.RED)
				.position(position);
		map.addOverlay(options);

	}

	private void initBaidu() {

		mp = (MapView) findViewById(R.id.id_bmapView);
		map = mp.getMap();
		map.setIndoorEnable(false);
		MapView.setMapCustomEnable(true);
		setMapCustomFile(this);
		map.setOnMapLoadedCallback(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.baidu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.baidu_common:
				map.setMapType(BaiduMap.MAP_TYPE_NORMAL);
				break;
			case R.id.baidu_satellite:
				map.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
				break;
			case R.id.baidu_over:
				ms = new MapStatus.Builder().target(new LatLng(39.914935, 116.403119)).zoom(8).build();
				map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
				//// TODO: 2017/3/28
				initOver();
				break;
			case R.id.baidu_traffic:
				if (map.isTrafficEnabled()) {
					map.setTrafficEnabled(false);
				} else {
					map.setTrafficEnabled(true);
				}

				break;
			default:
				break;
		}


		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//在activity执行onDestroy时执行mp.onDestroy()，实现地图生命周期管理
		map.setMyLocationEnabled(false);
		mLocation.stop();
		mLocation.unRegisterLocationListener(myLocationListener);
		mLocation = null;

		mOrientation.stop();
		mp.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//在activity执行onResume时执行mp. onResume ()，实现地图生命周期管理  
		mp.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//在activity执行onPause时执行mp. onPause ()，实现地图生命周期管理  
		mp.onPause();
	}

	//	private class MyPoiOverlay extends poi {
//		public MyPoiOverlay(BaiduMap baiduMap) {
//			super(map);
//		}
//		@Override
//		public boolean onPoiClick(int index) {
//			super.onPoiClick(index);
//			return true;
//		}
//	}
// 设置个性化地图config文件路径
	private void setMapCustomFile(Context context) {
		FileOutputStream out = null;
		InputStream inputStream = null;
		String moduleName = null;
		try {
			inputStream = context.getAssets()
					.open("customConfigdir/custom_config.txt");
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);

			moduleName = context.getFilesDir().getAbsolutePath();
			File f = new File(moduleName + "/" + "custom_config.txt");
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			out = new FileOutputStream(f);
			out.write(b);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		MapView.setCustomMapStylePath(moduleName + "/custom_config.txt");

	}

	@Override
	public void onClick(View v) {
		if (v == back_location) {
			LatLng loction = new LatLng(myLatid, myLong);
			MapStatus staus = new MapStatus.Builder().target(loction)
					.build();
			MapStatusUpdate mapStatus = MapStatusUpdateFactory.newMapStatus(staus);

// map.setMapStatus(mapStatus);
			map.animateMapStatus(mapStatus);
		}
	}

	@Override
	public void onMapLoaded() {
		Log.e("shenme", "onMapLoaded:yong  " );

		ms = new MapStatus.Builder().zoom(9).build();
		map.animateMapStatus(MapStatusUpdateFactory.newMapStatus(ms));
	}

	//位置监听
	class MyLocation implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation bdLocation) {


			myLatid = bdLocation.getLatitude();
			myLong = bdLocation.getLongitude();
			MyLocationData my = new MyLocationData.Builder()
					.accuracy(bdLocation.getRadius())
					.latitude(bdLocation.getLatitude())
					.longitude(bdLocation.getLongitude())
					.build();
			map.setMyLocationData(my);
			if (!isFirst) {
				isFirst = true;
				Toast.makeText(MainActivity.this, "bd" + bdLocation.getAddrStr(), Toast
						.LENGTH_SHORT).show();

				MyLocationConfiguration config = new MyLocationConfiguration(
						MyLocationConfiguration.LocationMode.NORMAL, true,
						arrow, (int) bdLocation.getLatitude(), (int) bdLocation.getLongitude()

				);
				map.setMyLocationConfigeration(config);
				LatLng loction = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
				MapStatus staus = new MapStatus.Builder().target(loction)
						.build();
				MapStatusUpdate mapStatus = MapStatusUpdateFactory.newMapStatus(staus);

// map.setMapStatus(mapStatus);
				map.animateMapStatus(mapStatus);
			}
		}

		@Override
		public void onConnectHotSpotMessage(String s, int i) {

		}


	}
	public void addMarkers() {
		// 添加Marker点
		LatLng llA = new LatLng(39.963175, 116.400244);
		LatLng llB = new LatLng(39.942821, 116.369199);
		LatLng llC = new LatLng(39.939723, 116.425541);
		LatLng llD = new LatLng(39.906965, 116.401394);
		LatLng llE = new LatLng(39.956965, 116.331394);
		LatLng llF = new LatLng(39.886965, 116.441394);
		LatLng llG = new LatLng(39.996965, 116.411394);

		List<MyItem> items = new ArrayList<MyItem>();
		items.add(new MyItem(llA));
		items.add(new MyItem(llB));
		items.add(new MyItem(llC));
		items.add(new MyItem(llD));
		items.add(new MyItem(llE));
		items.add(new MyItem(llF));
		items.add(new MyItem(llG));

		mClusterManager.addItems(items);

	}
	public class MyItem implements ClusterItem {
		private final LatLng mPosition;

		public MyItem(LatLng latLng) {
			mPosition = latLng;
		}

		@Override
		public LatLng getPosition() {
			return mPosition;
		}

		@Override
		public BitmapDescriptor getBitmapDescriptor() {
			return BitmapDescriptorFactory
					.fromResource(R.drawable.icon_gcoding);
		}
	}
}
