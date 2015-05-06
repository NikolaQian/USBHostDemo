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
						ProcessInfo.setText("����ʧ��,���ܽ�������");
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
	            		ProcessInfo.append("������" + String.valueOf(ret) + "byte����,����Ϊ��" + reciveStr + "\r\n");
//	            	}
	            	
	            	
	            	
//	            	ret = myDeviceConnection.bulkTransfer(epOut, Receiveytes,
//	    	                Receiveytes.length, 10000);         	
	            	    	
	            	
	            	
	            	
	            } else{  // �鿴����ֵ
//		        	ProcessInfo.append("���շ���ֵʧ�ܣ�����ֵΪ��" + String.valueOf(ret) + "\r\n");
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
						ProcessInfo.setText("����ʧ��,���ܽ�������");
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
		            	reciveStr += "������" + String.valueOf(ret) + "byte����,����Ϊ��" 
		            					+ Tools.bytes2String(Arrays.copyOf(Receiveytes, ret)) + "\r\n";
		            } else {
		            	reciveStr += "���շ���ֵʧ�ܣ�����ֵΪ��" + String.valueOf(ret) + "\r\n";
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
	 * �жϸ����ַ����Ƿ�հ״��� �հ״���ָ�ɿո��Ʊ�����س��������з���ɵ��ַ��� �������ַ���Ϊnull����ַ���������true
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
		
		//��ȡUsbManager
		myUsbManager = (UsbManager) getSystemService(USB_SERVICE);
		
		while(myUsbManager == null){
			ProcessInfo.append("��ȡUsbManagerʧ��");
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
		 * �����ĳ��򾭹�������Ĳ����еõ�����Ĵ�ӡ��Ϣ��˵������Խ���ͨѶ������
		 * 05-27 14:54:24.140: D/USB_HOST(10870): DeviceInfo: 34952 , 3
		 * 05-27 14:54:24.140: D/USB_HOST(10870): ö���豸�ɹ�
		 * 05-27 14:54:24.140: D/USB_HOST(10870): interfaceCounts : 1
		 * 05-27 14:54:24.140: D/USB_HOST(10870): �ҵ��ҵ��豸�ӿ�
		 * 05-27 14:54:24.160: D/USB_HOST(10870): ���豸�ɹ�
		 * 05-27 14:54:24.170: D/USB_HOST(10870): ����Ϊֹ�������豸->ö���豸->�ҵ��豸�Ľӿ�
		 * 										->�����豸->������Ӧ�Ķ˵㣬������ɣ���һ�����Խ���ͨѶ������ף����ˣ�
		 */
	}


	/**
	 * ����˵㣬IN | OUT��������������˴���ֱ����1ΪOUT�˵㣬0ΪIN����Ȼ��Ҳ����ͨ���ж�
	 */
	private void assignEndpoint() {
		
			if(myInterface == null){
				ProcessInfo.setText("����ʧ��");
				return;
			}
		
			if (myInterface.getEndpoint(1) != null) {
				ProcessInfo.append("out�˲�Ϊnull\r\n");
				epOut = myInterface.getEndpoint(1);
			}
			if (myInterface.getEndpoint(0) != null) {
				ProcessInfo.append("in�˲�Ϊnull\r\n");
				epIn = myInterface.getEndpoint(0);
			}
			
			ProcessInfo.append(getString(R.string.text) + "\r\n");
			Log.d(MainActivity.TAG, getString(R.string.text)
					);
		
	}

	/**
	 * ���豸
	 *
	 */
	private void openDevice() {
	
	//�ж�myInterface�Ƿ�Ϊnull	
	if (myInterface != null) {
		UsbDeviceConnection conn = null;
		// ��openǰ�ж��Ƿ�������Ȩ�ޣ���������Ȩ�޿��Ծ�̬���䣬Ҳ���Զ�̬����Ȩ�ޣ����Բ����������
		if (myUsbManager.hasPermission(myUsbDevice)) {
			conn = myUsbManager.openDevice(myUsbDevice);
			
			if (conn == null) {
				return;
			}				
			if (conn.claimInterface(myInterface, true)) {
				ProcessInfo.append("�ҵ��ӿ�\r\n");
				myDeviceConnection = conn; // �������android�豸�Ѿ�����HID�豸
				
				ProcessInfo.append("���豸�ɹ�\r\n");
				Log.d(MainActivity.TAG, "���豸�ɹ�");
			} else {
				conn.close();
			}
		}else {
			ProcessInfo.append("û��Ȩ��!\r\n");
		}			
	}else {
		ProcessInfo.append("û���ҵ��ӿ�!\r\n");
	}
}

	/**
	 * ���豸�ӿ�
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
				// �������ϵ��豸��һЩ�жϣ���ʵ��Щ��Ϣ��������ö�ٵ��豸ʱ��ӡ����
				if (intf.getInterfaceClass() == 3
						&& intf.getInterfaceSubclass() == 0
						&& intf.getInterfaceProtocol() == 0) {
					myInterface = intf;
					
					ProcessInfo.append("�ҵ��ҵ��豸�ӿ�\r\n");
			        Log.d(MainActivity.TAG, "�ҵ��ҵ��豸�ӿ�");
			        break;
				}				
			}
		}
	}

	/**
	 * ö���豸
	 */
	private void enumerateDevice() {
		if (myUsbManager == null)
			return;

		HashMap<String, UsbDevice> deviceList = myUsbManager.getDeviceList();
		if (!deviceList.isEmpty()) { // deviceList��Ϊ��
			StringBuffer sb = new StringBuffer();
			for (UsbDevice device : deviceList.values()) {
				sb.append(device.toString());
				sb.append("\r\n");
				ProcessInfo.setText(sb);
				// ����豸��Ϣ
				ProcessInfo.append("DeviceInfo: " + device.getVendorId() + " , " + device.getProductId() + "\r\n");
				Log.d(MainActivity.TAG, "DeviceInfo: " + device.getVendorId() + " , "
						+ device.getProductId());

				// ö�ٵ��豸
				if (device.getVendorId() == VendorID
						&& device.getProductId() == ProductID) {
					myUsbDevice = device;
					ProcessInfo.append("ö���豸�ɹ�\r\n");
					Log.d(MainActivity.TAG, "ö���豸�ɹ�");
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
