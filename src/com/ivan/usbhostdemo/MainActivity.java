package com.ivan.usbhostdemo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private static final String TAG = "USB_HOST";

	private UsbManager myUsbManager;
	private UsbDevice myUsbDevice;
	private UsbInterface myInterface;
	private UsbDeviceConnection myDeviceConnection;
	

	private final int VendorID = 34952;
	private final int ProductID = 3;

	private UsbEndpoint epOut;
	private UsbEndpoint epIn;
	
	
	private TextView ProcessInfo;	
	
	 private Handler handler = new Handler(){
		 public void handleMessage(android.os.Message msg) {
				 ProcessInfo.append((String)msg.obj);
		 };
	 };  
	  
	 private Runnable myRunnable= new Runnable() {
	    	int ret = -100;
	    	byte[] Receiveytes = new byte[256];
	    	ReentrantLock lock = new ReentrantLock();
	        public void run() {
	        	
	        	lock.lock();
	        	
//	            if (true) {
	            	if(myInterface == null){
						ProcessInfo.setText("连接失败,不能接收数据");
						return;
					}
	            	ret = myDeviceConnection.bulkTransfer(epIn, Receiveytes,
	    	                Receiveytes.length, 0);
	                  
//	            }
	            
	            if((ret > 0) && (ret<=256)){
	            	
	            	String reciveStr =  Tools.bytes2String(Arrays.copyOf(Receiveytes, ret));
	            	
//	            	ProcessInfo.append(String.valueOf(isEmpty(reciveStr)));
	            	
//	            	if(reciveStr.equals("")){
//	            		ProcessInfo.setText("");
//	            	}else {
	            		ProcessInfo.append("接收了" + String.valueOf(ret) + "byte数据,数据为：" + reciveStr + "\r\n");
//	            	}
	            	
	            	
	            	
//	            	ret = myDeviceConnection.bulkTransfer(epOut, Receiveytes,
//	    	                Receiveytes.length, 10000);         	
	            	    	
	            	
	            	
	            	
	            } else{  // 查看返回值
//		        	ProcessInfo.append("接收返回值失败，返回值为：" + String.valueOf(ret) + "\r\n");
		        }
	            handler.postDelayed(this, 0); 
	            lock.unlock();
	            
	            
	        }  
	    };
	
	
	private Thread myThread = new Thread(new Runnable() {
    	int ret = -100;
    	byte[] Receiveytes = new byte[256];
    	
		public void run() {
			while (true){
				
				synchronized (this){
					
					if(myInterface == null){
						ProcessInfo.setText("连接失败,不能接收数据");
						return;
					}
//		            Message msg0 = new Message();
//		            msg0.what = 0;
//		            msg0.obj = "statrt bulkTransfer...\n";
//		            handler.sendMessage(msg0);
		            
	            	ret = myDeviceConnection.bulkTransfer(epIn, Receiveytes,
	    	                Receiveytes.length, 0);
	            	
	            	
//	            	Message msg1 = new Message();
//	            	msg1.what = 0;
//	            	msg1.obj = "after bulkTransfer..." + ret + "\n";
//	            	handler.sendMessage(msg1);
	            	
		            
		            
	            	String reciveStr = "";
		            if((ret >= 0) && (ret<=256)){
		            	reciveStr += "接收了" + String.valueOf(ret) + "byte数据,数据为：" 
		            					+ Tools.bytes2String(Arrays.copyOf(Receiveytes, ret)) + "\r\n";
		            } else {
		            	reciveStr += "接收返回值失败，返回值为：" + String.valueOf(ret) + "\r\n";
			        }
		            
		            Message msg = new Message();
		            msg.what = 0;
		            msg.obj = reciveStr;
		            handler.sendMessage(msg);
				}

			}
		}
	});
	
	/**
	 * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
	 * 
	 * @param input
	 * @return boolean
	 */
	public static boolean isEmpty(String input) {
		if (input == null || "".equals(input))
			return true;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		ProcessInfo = (TextView) findViewById(R.id.info);
		ProcessInfo.setMovementMethod(ScrollingMovementMethod.getInstance());
		ProcessInfo.setText(null);
		
		//获取UsbManager
		myUsbManager = (UsbManager) getSystemService(USB_SERVICE);
		
		while(myUsbManager == null){
			ProcessInfo.append("获取UsbManager失败");
			setContentView(ProcessInfo);
		}
		
		enumerateDevice();
		
		findInterface();
		
		openDevice();

		assignEndpoint();
		
		
		Button button = (Button)findViewById(R.id.button1) ;
		button.setOnClickListener(new android.view.View.OnClickListener() {
			
			public void onClick(View v) {		
				ProcessInfo.setText("");
//				myThread.start();
				handler.post(myRunnable);
			}
		});
		 
		/**
		 * 如果你的程序经过上面的四步运行得到下面的打印信息，说明你可以进行通讯处理了
		 * 05-27 14:54:24.140: D/USB_HOST(10870): DeviceInfo: 34952 , 3
		 * 05-27 14:54:24.140: D/USB_HOST(10870): 枚举设备成功
		 * 05-27 14:54:24.140: D/USB_HOST(10870): interfaceCounts : 1
		 * 05-27 14:54:24.140: D/USB_HOST(10870): 找到我的设备接口
		 * 05-27 14:54:24.160: D/USB_HOST(10870): 打开设备成功
		 * 05-27 14:54:24.170: D/USB_HOST(10870): 到此为止：发现设备->枚举设备->找到设备的接口
		 * 										->连接设备->分配相应的端点，都已完成，下一步可以进行通讯处理。。祝你好运！
		 */
	}


	/**
	 * 分配端点，IN | OUT，即输入输出；此处我直接用1为OUT端点，0为IN，当然你也可以通过判断
	 */
	private void assignEndpoint() {
		
			if(myInterface == null){
				ProcessInfo.setText("连接失败");
				return;
			}
		
			if (myInterface.getEndpoint(1) != null) {
				ProcessInfo.append("out端不为null\r\n");
				epOut = myInterface.getEndpoint(1);
			}
			if (myInterface.getEndpoint(0) != null) {
				ProcessInfo.append("in端不为null\r\n");
				epIn = myInterface.getEndpoint(0);
			}
			
			ProcessInfo.append(getString(R.string.text) + "\r\n");
			Log.d(MainActivity.TAG, getString(R.string.text)
					);
		
	}

	/**
	 * 打开设备
	 *
	 */
	private void openDevice() {
	
	//判断myInterface是否为null	
	if (myInterface != null) {
		UsbDeviceConnection conn = null;
		// 在open前判断是否有连接权限；对于连接权限可以静态分配，也可以动态分配权限，可以查阅相关资料
		if (myUsbManager.hasPermission(myUsbDevice)) {
			conn = myUsbManager.openDevice(myUsbDevice);
			
			if (conn == null) {
				return;
			}				
			if (conn.claimInterface(myInterface, true)) {
				ProcessInfo.append("找到接口\r\n");
				myDeviceConnection = conn; // 到此你的android设备已经连上HID设备
				
				ProcessInfo.append("打开设备成功\r\n");
				Log.d(MainActivity.TAG, "打开设备成功");
			} else {
				conn.close();
			}
		}else {
			ProcessInfo.append("没有权限!\r\n");
		}			
	}else {
		ProcessInfo.append("没有找到接口!\r\n");
	}
}

	/**
	 * 找设备接口
	 */
	private void findInterface() {
		if (myUsbDevice != null) {
			ProcessInfo.append("interfaceCounts : " + myUsbDevice.getInterfaceCount() + "\r\n");
			Log.d(MainActivity.TAG, "interfaceCounts : " + myUsbDevice.getInterfaceCount());
			for (int i = 0; i < myUsbDevice.getInterfaceCount(); i++) {
				
				UsbInterface intf = myUsbDevice.getInterface(i);
				ProcessInfo.append(" InterfaceClass:" + intf.getInterfaceClass()
											+ "\r\n InterfaceSubclass:" + intf.getInterfaceSubclass() 
											+ "\r\n InterfaceProtocol:" + intf.getInterfaceProtocol() + "\r\n");
				// 根据手上的设备做一些判断，其实这些信息都可以在枚举到设备时打印出来
				if (intf.getInterfaceClass() == 3
						&& intf.getInterfaceSubclass() == 0
						&& intf.getInterfaceProtocol() == 0) {
					myInterface = intf;
					
					ProcessInfo.append("找到我的设备接口\r\n");
			        Log.d(MainActivity.TAG, "找到我的设备接口");
			        break;
				}				
			}
		}
	}

	/**
	 * 枚举设备
	 */
	private void enumerateDevice() {
		if (myUsbManager == null)
			return;

		HashMap<String, UsbDevice> deviceList = myUsbManager.getDeviceList();
		if (!deviceList.isEmpty()) { // deviceList不为空
			StringBuffer sb = new StringBuffer();
			for (UsbDevice device : deviceList.values()) {
				sb.append(device.toString());
				sb.append("\r\n");
				ProcessInfo.setText(sb);
				// 输出设备信息
				ProcessInfo.append("DeviceInfo: " + device.getVendorId() + " , " + device.getProductId() + "\r\n");
				Log.d(MainActivity.TAG, "DeviceInfo: " + device.getVendorId() + " , "
						+ device.getProductId());

				// 枚举到设备
				if (device.getVendorId() == VendorID
						&& device.getProductId() == ProductID) {
					myUsbDevice = device;
					ProcessInfo.append("枚举设备成功\r\n");
					Log.d(MainActivity.TAG, "枚举设备成功");
				}
			}
		}
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
