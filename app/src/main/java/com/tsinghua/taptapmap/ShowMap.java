package com.tsinghua.taptapmap;

import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.io.InputStream;
import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class ShowMap extends Activity {

    private MapView mMapView;
    private AMap aMap;
    private Button mPoiButton;
    private TextView mPoiText;
    private PoiSearch mPoiSearch;
    private Marker mCenterMarker;
    private PoiTable mPoiTable;
    private Spinner mBigCategorySpinner;

    public class PoiTable {
        public class PoiCategory {
            public int id;
            public String bigCategory;
            public String midCategory;
            public String subCategory;

            public PoiCategory(int _id, String _bigCategory, String _midCategory, String _subCategory) {
                id = _id;
                bigCategory = _bigCategory;
                midCategory = _midCategory;
                subCategory = _subCategory;
            }
        }

        private ArrayList<PoiCategory> mPoiList = new ArrayList<PoiCategory>();

        public PoiTable() {
            readPoiCode();
        }

        private void readPoiCode() {
            try {
                InputStream path = getAssets().open("amap_poicode.xls");
                Workbook book = Workbook.getWorkbook(path);
                int sheets_num = book.getNumberOfSheets();
                assert(sheets_num == 3);
                Sheet sheet = book.getSheet(2);
                int rows = sheet.getRows();
                int cols = sheet.getColumns();
                Cell[] poiCodeList = sheet.getColumn(1);
                Cell[] bigCategoryList = sheet.getColumn(2);
                Cell[] midCategoryList = sheet.getColumn(3);
                Cell[] subCategoryList = sheet.getColumn(4);
                int n = poiCodeList.length;
                for (int i = 1; i < n; i++) { // From 1 on because the first row is the title
                    if (!poiCodeList[i].getContents().isEmpty()) {
                        mPoiList.add(new PoiCategory(
                                Integer.parseInt(poiCodeList[i].getContents()),
                                bigCategoryList[i].getContents(),
                                midCategoryList[i].getContents(),
                                subCategoryList[i].getContents()
                        ));
                    }
                }
            } catch (Exception e) {
                Log.d("TapTap", e.toString());
            }
        }

        public ArrayList<PoiCategory> getBigCategory() {
            ArrayList<PoiCategory> result = new ArrayList<PoiCategory>();
            for (PoiCategory item : mPoiList) {
                if (item.id % 10000 == 0) { // xx0000 --> BigCategory
                    result.add(item);
                }
            }
            return result;
        }

        public ArrayList<PoiCategory> getPoiList() {
            return mPoiList;
        }
    }

    public class PoiAdapter extends BaseAdapter {
        public static final int BIG = 0;
        public static final int MID = 1;
        public static final int SUB = 2;
        private int mGrading;
        private ArrayList<PoiTable.PoiCategory> mPoiList;

        public PoiAdapter(ArrayList<PoiTable.PoiCategory> list, int grading) {
            mPoiList = list;
            mGrading = grading;
        }

        @Override
        public int getCount() {
            return mPoiList.size();
        }

        @Override
        public Object getItem(int position) {
            return mPoiList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = (TextView) new TextView(ShowMap.this);
            }

            TextView tv = (TextView)convertView;
            tv.setPadding(1,0,0,0);
            if (mGrading == BIG) {
                tv.setText(mPoiList.get(position).bigCategory);
            } else if (mGrading == MID) {
                tv.setText(mPoiList.get(position).midCategory);
            } else {
                tv.setText(mPoiList.get(position).subCategory);
            }

            return convertView;
        }
    }

    private void updatePoiInfo(LatLonPoint latLonPoint) {
        PoiSearch.Query query = new PoiSearch.Query(null, String.format("%06d",((PoiTable.PoiCategory)(mBigCategorySpinner.getSelectedItem())).id), null);
        query.setPageSize(20);
        query.setPageNum(0);
        mPoiSearch = new PoiSearch(ShowMap.this, query);
        mPoiSearch.setBound(new PoiSearch.SearchBound(latLonPoint, 10000));
        mPoiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult result, int rcode) {
                if (rcode == AMapException.CODE_AMAP_SUCCESS) {
                    if (result != null && result.getQuery() != null) {// 搜索poi的结果
                        if (result.getQuery().equals(query)) {// 是否是同一条
                            StringBuffer sb = new StringBuffer(256);
                            ArrayList<PoiItem> poiItems = result.getPois();
                            if (poiItems != null && poiItems.size() > 0) {
                                for (PoiItem item : poiItems) {
                                    sb.append(item.getTypeCode() + "|" + item.getTypeDes() + "|" + item.getTitle() + "|" + item.getDistance() + '\n');
                                }
                                mPoiText.setText(sb.toString());
                            } else {
                                mPoiText.setText("");
                            }
                        }
                    } else {
                        mPoiText.setText("No result");
                    }
                } else  {
                    mPoiText.setText("Query error");
                }
                mPoiText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPoiItemSearched(PoiItem result, int rcode) {

            }
        });
        mPoiSearch.searchPOIAsyn();
    }

    private void updatePoiInfoAtCameraPosition() {
        LatLng target = aMap.getCameraPosition().target;
        LatLonPoint latLonPoint = new LatLonPoint(target.latitude, target.longitude);
        updatePoiInfo(latLonPoint);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_map);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mPoiButton = (Button) findViewById(R.id.locate_button);
        mPoiText = (TextView) findViewById(R.id.poi_text);
        mBigCategorySpinner = (Spinner) findViewById(R.id.big_category_spinner);

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(1000);
        myLocationStyle.strokeWidth(5);
        myLocationStyle.strokeColor(Color.BLACK);
        myLocationStyle.radiusFillColor(Color.argb(0.2f,0f,0f,1f));
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        aMap = mMapView.getMap();
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
        aMap.showIndoorMap(true);
        UiSettings mUiSettings = aMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(false);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setScaleControlsEnabled(true);

        mPoiTable = new PoiTable();
        mBigCategorySpinner = (Spinner) findViewById(R.id.big_category_spinner);
        PoiAdapter mBigCategoryadapter = new PoiAdapter(mPoiTable.getBigCategory(), PoiAdapter.BIG);
        mBigCategorySpinner.setAdapter(mBigCategoryadapter);
        mBigCategorySpinner.setSelection(4, true);
        mBigCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePoiInfoAtCameraPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mPoiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location mLocation = aMap.getMyLocation();
                if (mLocation != null) {
                    CameraPosition cameraPosition = new CameraPosition(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()), 16, 0, 0);
                    aMap.moveCamera(new CameraUpdateFactory().newCameraPosition(cameraPosition));
                } else {
                    Toast.makeText(ShowMap.this, "未完成定位", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCenterMarker = aMap.addMarker(new MarkerOptions().position(aMap.getCameraPosition().target));
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mPoiText.setVisibility(View.INVISIBLE);
                mCenterMarker.setPosition(cameraPosition.target);
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                updatePoiInfoAtCameraPosition();
                mCenterMarker.setPosition(cameraPosition.target);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}