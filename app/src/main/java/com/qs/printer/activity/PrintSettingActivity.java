package com.qs.printer.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.qs.helper.printer.Device;
import com.qs.helper.printer.PrinterClass;
import com.qs.printer5802.R;

/**
 * ��ӡ������
 * @author 
 *
 */

public class PrintSettingActivity extends ListActivity {
	private String TAG = "BtSetting";
	private ArrayAdapter<String> mPairedDevicesArrayAdapter = null;
	public static ArrayAdapter<String> mNewDevicesArrayAdapter = null;
	public static List<Device> deviceList=new ArrayList<Device>();
	private Button bt_scan;
	public Handler mhandler;
	private LinearLayout layoutscan;
	private TextView tv_status;
	private Thread tv_update;
	private boolean tvFlag = true;
	Context _context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_printsetting);
	
//		�����Ͽ�ģʽ
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
				
		_context=this;
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.drawable.device_name);
		
		InitListView();		
		
		layoutscan = (LinearLayout) findViewById(R.id.layoutscan);
		layoutscan.setVisibility(View.GONE);

		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.drawable.device_name);
		deviceList=new ArrayList<Device>();
		bt_scan = (Button) findViewById(R.id.bt_scan);
		bt_scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(deviceList!=null)
				{
					deviceList.clear();
				}
				if (!MainActivity.pl.IsOpen()) {
					MainActivity.pl.open(_context);
				}
				layoutscan.setVisibility(View.VISIBLE);
				mNewDevicesArrayAdapter.clear();
				MainActivity.pl.scan();				
				deviceList = MainActivity.pl.getDeviceList();
				InitListView();
			}
		});

		tv_status = (TextView) findViewById(R.id.tv_status);
		tv_update = new Thread() {
			public void run() {
				while (tvFlag) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					tv_status.post(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							if (MainActivity.pl != null) {
								if (MainActivity.pl.getState() == PrinterClass.STATE_CONNECTED) {
									
									tv_status.setText(PrintSettingActivity.this
											.getResources().getString(
													R.string.str_connected));
									//�Ѿ�����ʱ�򣬽�����Ӧ�ã�������ӡ����ѡ�����
									MainActivity.checkState=true;
									tvFlag=false;
									Intent back = new Intent();
									back.putExtra("BACK_DATA_NAME",1);
									setResult(RESULT_OK,back);
									//��ֹɨ��
									MainActivity.pl.stopScan();
									PrintSettingActivity.this.finish();
									
								} else if (MainActivity.pl.getState() == PrinterClass.STATE_CONNECTING) {
									tv_status.setText(PrintSettingActivity.this
											.getResources().getString(
													R.string.str_connecting));
								} 
								else if(MainActivity.pl.getState()==PrinterClass.STATE_SCAN_STOP)
								{
									tv_status.setText(PrintSettingActivity.this
										.getResources().getString(
												R.string.str_scanover));
									layoutscan.setVisibility(View.GONE);
									InitListView();
								}
								else if(MainActivity.pl.getState()==PrinterClass.STATE_SCANING)
								{
									tv_status.setText(PrintSettingActivity.this
										.getResources().getString(
												R.string.str_scaning));
									InitListView();
								}
								else {
									int ss=MainActivity.pl.getState();
									tv_status.setText(PrintSettingActivity.this
											.getResources().getString(
													R.string.str_disconnected));
								}
							}
						}
					});
				}
			}
		};
		tv_update.start();
	}
	
	private void InitListView()
	{
		SimpleAdapter simpleAdapter=new SimpleAdapter(this,getData("simple-list-item-2"),android.R.layout.simple_list_item_2,new String[]{"title", "description"},new int[]{android.R.id.text1, android.R.id.text2});
		setListAdapter(simpleAdapter);
	}
	
	/**
     * �����˿�list�������
     */
    protected void onListItemClick(ListView listView, View v, int position, long id) {  
    	Map map = (Map)listView.getItemAtPosition(position);
    	String cmd=map.get("description").toString();
    	
       //����������ӡ��
    	MainActivity.pl.connect(cmd);
    	
		Log.e("cmd", "cmd:"+cmd);

    }
	
	/**
     * @param title
     * @return
     */
    private List<Map<String, String>> getData(String title) {
    	List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
    	if(deviceList!=null)
    	{
	    	for(int i=0;i<deviceList.size();i++)
	    	{
	    		Map<String, String> map = new HashMap<String, String>();
	    		map.put("title", deviceList.get(i).deviceName);
	    		map.put("description", deviceList.get(i).deviceAddress);
	    		listData.add(map);
	    	}
    	}
    	return listData;
    }

	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
    @Override
    public void onBackPressed() {
	new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.str_exit))
		.setIcon(android.R.drawable.ic_dialog_info)
		.setPositiveButton("YES", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			// ȷ���˳�������checkStateΪfalse
				MainActivity.checkState=false;
				finish();
		    }
		})
		.setNegativeButton("NO", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			
		    }
		}).show();
    }

}
