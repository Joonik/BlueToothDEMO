package zj.com.cn.bluetooth.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import zj.com.cn.bluetooth.sdk.R;
import zj.com.command.sdk.Command;
import zj.com.command.sdk.PrintPicture;
import zj.com.command.sdk.PrinterCommand;
import zj.com.customize.sdk.Other;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class Main_Activity extends Activity implements OnClickListener{
/******************************************************************************************************/
	// Debugging
	private static final String TAG = "Main_Activity";
	private static final boolean DEBUG = true;
/******************************************************************************************************/
	// Message types sent from the BluetoothService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_CONNECTION_LOST = 6;
	public static final int MESSAGE_UNABLE_CONNECT = 7;
/*******************************************************************************************************/
	// Key names received from the BluetoothService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;
	private static final int REQUEST_CHOSE_BMP = 3;
	private static final int REQUEST_CAMER = 4;
	
	//QRcode
	private static final int QR_WIDTH = 350;
	private static final int QR_HEIGHT = 350;
/*******************************************************************************************************/
	private static final String CHINESE = "GBK";
	private static final String THAI = "CP874";
	private static final String KOREAN = "EUC-KR";
	private static final String BIG5 = "BIG5";

/*********************************************************************************/	
	private TextView mTitle;
	EditText editText;
	ImageView imageViewPicture;
	private static boolean is58mm = true;
	private RadioButton width_58mm, width_80;
	private RadioButton thai, big5, Simplified, Korean;
	private CheckBox hexBox;
	private Button sendButton = null;
	private Button testButton = null;
	private Button printbmpButton = null;
	private Button btnScanButton = null;
	private Button btnClose = null;
	private Button btn_BMP = null;
	private Button btn_ChoseCommand = null;
	private Button btn_prtsma = null;
	private Button btn_prttableButton = null;
	private Button btn_prtcodeButton = null;
	private Button btn_scqrcode = null;
	private Button btn_camer = null;

/******************************************************************************************************/
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the services
	private BluetoothService mService = null;
	
/***************************   指                 令****************************************************************/
    final String[] items = { "复位打印机", "打印并走纸", "标准ASCII字体", "压缩ASCII字体", "正常大小",
			"二倍高倍宽", "三倍高倍宽", "四倍高倍宽", "取消加粗模式", "选择加粗模式", "取消倒置打印", "选择倒置打印", "取消黑白反显", "选择黑白反显",
			"取消顺时针旋转90°", "选择顺时针旋转90°", "走纸到切刀位置并切纸", "蜂鸣指令", "标准钱箱指令", 
			"实时弹钱箱指令", "进入字符模式", "进入中文模式", "打印自检页", "禁止按键", "取消禁止按键" ,
			"设置汉字字符下划线", "取消汉字字符下划线", "进入十六进制模式" };
    final String[] itemsen = { "Print Init", "Print and Paper", "Standard ASCII font", "Compressed ASCII font", "Normal size",			
    "Double high power wide", "Twice as high power wide", "Three times the high-powered wide", "Off emphasized mode", "Choose bold mode", "Cancel inverted Print", "Invert selection Print", "Cancel black and white reverse display", "Choose black and white reverse display",
	"Cancel rotated clockwise 90 °", "Select the clockwise rotation of 90 °", "Feed paper Cut", "Beep", "Standard CashBox", 
	"Open CashBox", "Char Mode", "Chinese Mode", "Print SelfTest", "DisEnable Button", "Enable Button" ,
	"Set Underline", "Cancel Underline", "Hex Mode" };
	final byte[][] byteCommands = { 
			{ 0x1b, 0x40, 0x0a },// 复位打印机
			{ 0x0a }, //打印并走纸
			{ 0x1b, 0x4d, 0x00 },// 标准ASCII字体
			{ 0x1b, 0x4d, 0x01 },// 压缩ASCII字体
			{ 0x1d, 0x21, 0x00 },// 字体不放大
			{ 0x1d, 0x21, 0x11 },// 宽高加倍
			{ 0x1d, 0x21, 0x22 },// 宽高加倍
			{ 0x1d, 0x21, 0x33 },// 宽高加倍
			{ 0x1b, 0x45, 0x00 },// 取消加粗模式
			{ 0x1b, 0x45, 0x01 },// 选择加粗模式
			{ 0x1b, 0x7b, 0x00 },// 取消倒置打印
			{ 0x1b, 0x7b, 0x01 },// 选择倒置打印
			{ 0x1d, 0x42, 0x00 },// 取消黑白反显
			{ 0x1d, 0x42, 0x01 },// 选择黑白反显
			{ 0x1b, 0x56, 0x00 },// 取消顺时针旋转90°
			{ 0x1b, 0x56, 0x01 },// 选择顺时针旋转90°
			{ 0x0a, 0x1d, 0x56, 0x42, 0x01, 0x0a },//切刀指令
			{ 0x1b, 0x42, 0x03, 0x03 },//蜂鸣指令
			{ 0x1b, 0x70, 0x00, 0x50, 0x50 },//钱箱指令
			{ 0x10, 0x14, 0x00, 0x05, 0x05 },//实时弹钱箱指令
			{ 0x1c, 0x2e },// 进入字符模式
			{ 0x1c, 0x26 }, //进入中文模式
			{ 0x1f, 0x11, 0x04 }, //打印自检页
			{ 0x1b, 0x63, 0x35, 0x01 }, //禁止按键
			{ 0x1b, 0x63, 0x35, 0x00 }, //取消禁止按键
			{ 0x1b, 0x2d, 0x02, 0x1c, 0x2d, 0x02 }, //设置下划线
			{ 0x1b, 0x2d, 0x00, 0x1c, 0x2d, 0x00 }, //取消下划线
			{ 0x1f, 0x11, 0x03 }, //打印机进入16进制模式
	};
/***************************条                          码***************************************************************/
	final String[] codebar = { "UPC_A", "UPC_E", "JAN13(EAN13)", "JAN8(EAN8)", 
							   "CODE39", "ITF", "CODABAR", "CODE93", "CODE128", "QR Code" };
	final byte[][] byteCodebar = { 
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
			{ 0x1b, 0x40 },// 复位打印机
	};
/******************************************************************************************************/
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// Set up the custom title
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_title);
		mTitle = (TextView) findViewById(R.id.title_right_text);

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		
		// If Bluetooth is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the session
		} else {
			if (mService == null)
				KeyListenerInit();//监听
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		
		if (mService != null) {
			
			if (mService.getState() == BluetoothService.STATE_NONE) {
				// Start the Bluetooth services
				mService.start();
			}
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (DEBUG)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (DEBUG)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth services
		if (mService != null)
			mService.stop();
		if (DEBUG)
			Log.e(TAG, "--- ON DESTROY ---");
	}

/*****************************************************************************************************/	
	private void KeyListenerInit() {
		
		editText = (EditText) findViewById(R.id.edit_text_out);

		sendButton = (Button) findViewById(R.id.Send_Button);
		sendButton.setOnClickListener(this);
		
		testButton = (Button) findViewById(R.id.btn_test);
		testButton.setOnClickListener(this);

		printbmpButton = (Button) findViewById(R.id.btn_printpicture);
		printbmpButton.setOnClickListener(this);

		btnScanButton = (Button)findViewById(R.id.button_scan);
		btnScanButton.setOnClickListener(this);
		
		hexBox = (CheckBox)findViewById(R.id.checkBoxHEX);
		hexBox.setOnClickListener(this);
		
		width_58mm = (RadioButton)findViewById(R.id.width_58mm);
		width_58mm.setOnClickListener(this);
		
		width_80 = (RadioButton)findViewById(R.id.width_80mm);
		width_80.setOnClickListener(this);
		
		imageViewPicture = (ImageView) findViewById(R.id.imageViewPictureUSB);
		imageViewPicture.setOnClickListener(this);
	
		btnClose = (Button)findViewById(R.id.btn_close);
		btnClose.setOnClickListener(this);
		
		btn_BMP = (Button)findViewById(R.id.btn_prtbmp);
		btn_BMP.setOnClickListener(this);
		
		btn_ChoseCommand = (Button)findViewById(R.id.btn_prtcommand);
		btn_ChoseCommand.setOnClickListener(this);
		
		btn_prtsma = (Button)findViewById(R.id.btn_prtsma);
		btn_prtsma.setOnClickListener(this);
		
		btn_prttableButton = (Button)findViewById(R.id.btn_prttable);
		btn_prttableButton.setOnClickListener(this);
		
		btn_prtcodeButton = (Button)findViewById(R.id.btn_prtbarcode);
		btn_prtcodeButton.setOnClickListener(this);
		
		btn_camer = (Button)findViewById(R.id.btn_dyca);
		btn_camer.setOnClickListener(this);
		
		btn_scqrcode = (Button)findViewById(R.id.btn_scqr);
		btn_scqrcode.setOnClickListener(this);
		
		Simplified = (RadioButton)findViewById(R.id.gbk12);
		Simplified.setOnClickListener(this);
		big5 = (RadioButton)findViewById(R.id.big5);
		big5.setOnClickListener(this);
		thai = (RadioButton)findViewById(R.id.thai);
		thai.setOnClickListener(this);
		Korean = (RadioButton)findViewById(R.id.kor);
		Korean.setOnClickListener(this);
		
		Bitmap bm = getImageFromAssetsFile("demo.bmp");
		if (null != bm) {
			imageViewPicture.setImageBitmap(bm);
		}
		
		editText.setEnabled(false);
		imageViewPicture.setEnabled(false);
		width_58mm.setEnabled(false);
		width_80.setEnabled(false);
		hexBox.setEnabled(false);
		sendButton.setEnabled(false);
		testButton.setEnabled(false);
		printbmpButton.setEnabled(false);
		btnClose.setEnabled(false);	
		btn_BMP.setEnabled(false);
		btn_ChoseCommand.setEnabled(false);
		btn_prtcodeButton.setEnabled(false);
		btn_prtsma.setEnabled(false);
		btn_prttableButton.setEnabled(false);
		btn_camer.setEnabled(false);
		btn_scqrcode.setEnabled(false);
		Simplified.setEnabled(false);
		Korean.setEnabled(false);
		big5.setEnabled(false);
		thai.setEnabled(false);
		
		mService = new BluetoothService(this, mHandler);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button_scan:{
			Intent serverIntent = new Intent(Main_Activity.this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			break;
		}
		case R.id.btn_close:{
			mService.stop();
			editText.setEnabled(false);
			imageViewPicture.setEnabled(false);
			width_58mm.setEnabled(false);
			width_80.setEnabled(false);
			hexBox.setEnabled(false);
			sendButton.setEnabled(false);
			testButton.setEnabled(false);
			printbmpButton.setEnabled(false);
			btnClose.setEnabled(false);
			btn_BMP.setEnabled(false);
			btn_ChoseCommand.setEnabled(false);
			btn_prtcodeButton.setEnabled(false);
			btn_prtsma.setEnabled(false);
			btn_prttableButton.setEnabled(false);
			btn_camer.setEnabled(false);
			btn_scqrcode.setEnabled(false);
			btnScanButton.setEnabled(true);
			Simplified.setEnabled(false);
			Korean.setEnabled(false);
			big5.setEnabled(false);
			thai.setEnabled(false);
			btnScanButton.setText(getText(R.string.connect));
			break;
		}
		case R.id.btn_test:{
			BluetoothPrintTest();;
			break;
		}
		case R.id.Send_Button:{
			if (hexBox.isChecked()) {
				String str = editText.getText().toString().trim();//去掉头尾空白
				if(str.length() > 0){
					str = Other.RemoveChar(str, ' ').toString();
					if (str.length() <= 0)
						return;
					if ((str.length() % 2) != 0) {
						Toast.makeText(getApplicationContext(), getString(R.string.msg_state),
								Toast.LENGTH_SHORT).show();
						return;
					}
					byte[] buf = Other.HexStringToBytes(str);
					SendDataByte(buf);
				}else{
					Toast.makeText(Main_Activity.this, getText(R.string.empty), Toast.LENGTH_SHORT).show();
				}
			} else {
				String msg = editText.getText().toString();
				if(msg.length()>0){
					if(thai.isChecked()){
						SendDataByte(PrinterCommand.POS_Print_Text(msg, THAI, 255, 0, 0, 0));
						SendDataByte(Command.LF);
					}else if(big5.isChecked()){
						SendDataByte(PrinterCommand.POS_Print_Text(msg, BIG5, 0, 0, 0, 0));
						SendDataByte(Command.LF);
					}else if(Korean.isChecked()){
						SendDataByte(PrinterCommand.POS_Print_Text(msg, KOREAN, 0, 0, 0, 0));
						SendDataByte(Command.LF);
					}else if(Simplified.isChecked()){
						SendDataByte(PrinterCommand.POS_Print_Text(msg, CHINESE, 0, 0, 0, 0));
						SendDataByte(Command.LF);
					}
				}else{
					Toast.makeText(Main_Activity.this, getText(R.string.empty), Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
		case R.id.width_58mm:
		case R.id.width_80mm:{
			is58mm = v == width_58mm;
			width_58mm.setChecked(is58mm);
			width_80.setChecked(!is58mm);
			break;
		}
		case R.id.btn_printpicture:{
			GraphicalPrint();
			break;
			}
		case R.id.imageViewPictureUSB:{
			Intent loadpicture = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(loadpicture, REQUEST_CHOSE_BMP);
			break;
		}
		case R.id.btn_prtbmp:{
			Print_BMP();
			break;
		}
		case R.id.btn_prtcommand:{
			CommandTest();
			break;
		}
		case R.id.btn_prtsma:{
			SendDataByte(Command.ESC_Init);
			SendDataByte(Command.LF);
			Print_Ex();
			break;
		}
		case R.id.btn_prttable:{
			SendDataByte(Command.ESC_Init);
			SendDataByte(Command.LF);
			PrintTable();
			break;
		}
		case R.id.btn_prtbarcode:{
			printBarCode();
			break;
		}
		case R.id.btn_scqr:{
			createImage();
			break;
		}
		case R.id.btn_dyca:{
			dispatchTakePictureIntent(REQUEST_CAMER);
			break;
		}
		default:
			break;
		}
	}
		
/*****************************************************************************************************/
	/*
	 * SendDataString
	 */
	private void SendDataString(String data) {
		
		if (mService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (data.length() > 0) {				
			try {
				mService.write(data.getBytes("GBK"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/*
	 *SendDataByte 
	 */
	private void SendDataByte(byte[] data) {
		
		if (mService.getState() != BluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}			
		mService.write(data);
	}

	/****************************************************************************************************/
	@SuppressLint("HandlerLeak") 
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (DEBUG)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					btnScanButton.setText(getText(R.string.Connecting));
					Print_Test();//
					btnScanButton.setEnabled(false);
					editText.setEnabled(true);
					imageViewPicture.setEnabled(true);
					width_58mm.setEnabled(true);
					width_80.setEnabled(true);
					hexBox.setEnabled(true);
					sendButton.setEnabled(true);
					testButton.setEnabled(true);
					printbmpButton.setEnabled(true);
					btnClose.setEnabled(true);
					btn_BMP.setEnabled(true);
					btn_ChoseCommand.setEnabled(true);
					btn_prtcodeButton.setEnabled(true);
					btn_prtsma.setEnabled(true);
					btn_prttableButton.setEnabled(true);
					btn_camer.setEnabled(true);
					btn_scqrcode.setEnabled(true);
					Simplified.setEnabled(true);
					Korean.setEnabled(true);
					big5.setEnabled(true);
					thai.setEnabled(true);
					break;
				case BluetoothService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothService.STATE_LISTEN:
				case BluetoothService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				
				break;
			case MESSAGE_READ:
				
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_CONNECTION_LOST:    //蓝牙已断开连接
                Toast.makeText(getApplicationContext(), "Device connection was lost",
                               Toast.LENGTH_SHORT).show();
                editText.setEnabled(false);
				imageViewPicture.setEnabled(false);
				width_58mm.setEnabled(false);
				width_80.setEnabled(false);
				hexBox.setEnabled(false);
				sendButton.setEnabled(false);
				testButton.setEnabled(false);
				printbmpButton.setEnabled(false);
				btnClose.setEnabled(false);
				btn_BMP.setEnabled(false);
				btn_ChoseCommand.setEnabled(false);
				btn_prtcodeButton.setEnabled(false);
				btn_prtsma.setEnabled(false);
				btn_prttableButton.setEnabled(false);
				btn_camer.setEnabled(false);
				btn_scqrcode.setEnabled(false);
				Simplified.setEnabled(false);
				Korean.setEnabled(false);
				big5.setEnabled(false);
				thai.setEnabled(false);
                break;
            case MESSAGE_UNABLE_CONNECT:     //无法连接设备
            	Toast.makeText(getApplicationContext(), "Unable to connect device",
                        Toast.LENGTH_SHORT).show();
            	break;
			}
		}
	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (DEBUG)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:{
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK) {
					// Get the device MAC address
					String address = data.getExtras().getString(
							DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					// Get the BLuetoothDevice object
					if (BluetoothAdapter.checkBluetoothAddress(address)) {
						BluetoothDevice device = mBluetoothAdapter
								.getRemoteDevice(address);
						// Attempt to connect to the device
						mService.connect(device);
					}
				}
				break;
			}
			case REQUEST_ENABLE_BT:{
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a session
					KeyListenerInit();
				} else {
					// User did not enable Bluetooth or an error occured
					Log.d(TAG, "BT not enabled");
					Toast.makeText(this, R.string.bt_not_enabled_leaving,
							Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
			}
			case REQUEST_CHOSE_BMP:{
	        	if (resultCode == Activity.RESULT_OK){
					Uri selectedImage = data.getData();
					String[] filePathColumn = { MediaColumns.DATA };
		
					Cursor cursor = getContentResolver().query(selectedImage,
							filePathColumn, null, null, null);
					cursor.moveToFirst();
		
					int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
					String picturePath = cursor.getString(columnIndex);
					cursor.close();
		
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inJustDecodeBounds = true;
					BitmapFactory.decodeFile(picturePath, opts);
					opts.inJustDecodeBounds = false;
					if (opts.outWidth > 1200) {
						opts.inSampleSize = opts.outWidth / 1200;
					}
					Bitmap bitmap = BitmapFactory.decodeFile(picturePath, opts);
					if (null != bitmap) {
						imageViewPicture.setImageBitmap(bitmap);
					}
	        	}else{
					Toast.makeText(this, getString(R.string.msg_statev1), Toast.LENGTH_SHORT).show();
				}
				break;
			}
			case REQUEST_CAMER:{
	        	if (resultCode == Activity.RESULT_OK){
	        		handleSmallCameraPhoto(data);
	        	}else{
	        		Toast.makeText(this, getText(R.string.camer), Toast.LENGTH_SHORT).show();
	        	}
	        	break;
	        }
		}
	}

/****************************************************************************************************/
	/**
	 * 连接成功后打印测试页
	 */
	private void Print_Test(){
		String lang = getString(R.string.strLang);
  		if((lang.compareTo("en")) == 0){
			String msg = "Congratulations!\n\n";
			String data = "You have sucessfully created communications between your device and our bluetooth printer.\n"
	                +"  the company is a high-tech enterprise which specializes" +
	                " in R&D,manufacturing,marketing of thermal printers and barcode scanners.\n\n";
			SendDataByte(PrinterCommand.POS_Print_Text(msg, CHINESE, 0, 1, 1, 0));
			SendDataByte(PrinterCommand.POS_Print_Text(data, CHINESE, 0, 0, 0, 0));
			SendDataByte(PrinterCommand.POS_Set_Cut(1));
			SendDataByte(PrinterCommand.POS_Set_PrtInit());
		}else if((lang.compareTo("cn")) == 0){
			String msg = "恭喜您!\n\n";
			String data = "您已经成功的连接上了我们的便携式蓝牙打印机！\n我们公司是一家专业从事研发，生产，销售商用票据打印机和条码扫描设备于一体的高科技企业.\n\n\n\n\n\n\n";
			SendDataByte(PrinterCommand.POS_Print_Text(msg, CHINESE, 0, 1, 1, 0));
			SendDataByte(PrinterCommand.POS_Print_Text(data, CHINESE, 0, 0, 0, 0));
			SendDataByte(PrinterCommand.POS_Set_Cut(1));
			SendDataByte(PrinterCommand.POS_Set_PrtInit());
		}else if((lang.compareTo("hk")) == 0 ){
			String msg = "恭喜您!\n";
			String data = "您已經成功的連接上了我們的便攜式藍牙打印機！ \n我們公司是一家專業從事研發，生產，銷售商用票據打印機和條碼掃描設備於一體的高科技企業.\n\n\n\n\n\n\n";
			SendDataByte(PrinterCommand.POS_Print_Text(msg, BIG5, 0, 1, 1, 0));
			SendDataByte(PrinterCommand.POS_Print_Text(data, BIG5, 0, 0, 0, 0));
			SendDataByte(PrinterCommand.POS_Set_Cut(1));
			SendDataByte(PrinterCommand.POS_Set_PrtInit());
		}else if((lang.compareTo("kor")) == 0){
			String msg = "축하 해요!\n";
			String data = "성공적으로 우리의 휴대용 블루투스 프린터에 연결 한! \n우리는 하이테크 기업 중 하나에서 개발, 생산 및 상업 영수증 프린터와 바코드 스캐닝 장비 판매 전문 회사입니다.\n\n\n\n\n\n\n";
			SendDataByte(PrinterCommand.POS_Print_Text(msg, KOREAN, 0, 1, 1, 0));
			SendDataByte(PrinterCommand.POS_Print_Text(data, KOREAN, 0, 0, 0, 0));
			SendDataByte(PrinterCommand.POS_Set_Cut(1));
			SendDataByte(PrinterCommand.POS_Set_PrtInit());
		}else if((lang.compareTo("thai")) == 0){
			String msg = "ขอแสดงความยินดี!\n";
			String data = "คุณได้เชื่อมต่อกับบลูทู ธ เครื่องพิมพ์แบบพกพาของเรา! \n เราเป็น บริษัท ที่มีความเชี่ยวชาญในการพัฒนา, การผลิตและการขายของเครื่องพิมพ์ใบเสร็จรับเงินและการสแกนบาร์โค้ดอุปกรณ์เชิงพาณิชย์ในหนึ่งในองค์กรที่มีเทคโนโลยีสูง.\n\n\n\n\n\n\n";
			SendDataByte(PrinterCommand.POS_Print_Text(msg, THAI, 255, 1, 1, 0));
			SendDataByte(PrinterCommand.POS_Print_Text(data, THAI, 255, 0, 0, 0));
			SendDataByte(PrinterCommand.POS_Set_Cut(1));
			SendDataByte(PrinterCommand.POS_Set_PrtInit());
		}
  	}
	
	/**
	 * 打印测试页
	 * @param mPrinter
	 */
	private void BluetoothPrintTest() {
		String msg = "";
		String lang = getString(R.string.strLang);
		if((lang.compareTo("en")) == 0 ){
			msg = "Division I is a research and development, production and services in one high-tech research and development, production-oriented enterprises, specializing in POS terminals finance, retail, restaurants, bars, songs and other areas, computer terminals, self-service terminal peripheral equipment R & D, manufacturing and sales! \n company's organizational structure concise and practical, pragmatic style of rigorous, efficient operation. Integrity, dedication, unity, and efficient is the company's corporate philosophy, and constantly strive for today, vibrant, the company will be strong scientific and technological strength, eternal spirit of entrepreneurship, the pioneering and innovative attitude, confidence towards the international information industry, with friends to create brilliant information industry !!! \n\n\n";
			SendDataString(msg);
		}else if((lang.compareTo("cn")) == 0){
			msg = "我司是一家集科研开发、生产经营和服务于一体的高技术研发、生产型企业，专业从事金融、商业零售、餐饮、酒吧、歌吧等领域的POS终端、计算机终端、自助终端周边配套设备的研发、制造及销售！\n公司的组织机构简练实用，作风务实严谨，运行高效。诚信、敬业、团结、高效是公司的企业理念和不断追求今天，朝气蓬勃，公司将以雄厚的科技力量，永恒的创业精神，不断开拓创新的姿态，充满信心的朝着国际化信息产业领域，与朋友们携手共创信息产业的辉煌!!!\n\n\n";
			SendDataString(msg);
		}else if((lang.compareTo("hk")) == 0){
			msg = "我司是一家集科研開發、生產經營和服務於一體的高技術研發、生產型企業，專業從事金融、商業零售、餐飲、酒吧、歌吧等領域的POS終端、計算機終端、自助終端周邊配套設備的研發、製造及銷售！ \n公司的組織機構簡練實用，作風務實嚴謹，運行高效。誠信、敬業、團結、高效是公司的企業理念和不斷追求今天，朝氣蓬勃，公司將以雄厚的科技力量，永恆的創業精神，不斷開拓創新的姿態，充滿信心的朝著國際化信息產業領域，與朋友們攜手共創信息產業的輝煌!!!\n\n\n";
			SendDataByte(PrinterCommand.POS_Print_Text(msg, BIG5, 0, 0, 0, 0));
		}else if((lang.compareTo("kor")) == 0){
			msg = "부문 I는 금융, 소매, 레스토랑, 바, 노래 및 기타 분야, 컴퓨터 단말기, 셀프 서비스 터미널 주변 장치 POS 터미널을 전문으로 한 첨단 기술 연구 및 개발, 생산 지향적 인 기업의 연구 및 개발, 생산 및 서비스입니다 R & D, 제조 및 판매! \n 회사의 조직 구조의 간결하고 엄격한, 효율적인 운영의 실제, 실용적인 스타일. 무결성, 헌신, 단결, 효율적인 회사의 기업 철학이며, 지속적으로, 활기찬,이 회사는 강력한 과학 기술 강도, 기업가 정신의 영원한 정신이 될 것입니다 오늘을 위해 노력, 개척과 혁신적인 태도, 국제 정보 산업을 향해 자신감, 친구와 함께 화려한 정보 산업을 만들 수 있습니다!!!\n\n\n";
			SendDataByte(PrinterCommand.POS_Print_Text(msg, KOREAN, 0, 0, 0, 0));
		}else if((lang.compareTo("thai")) == 0){
			msg = "ส่วนฉันคือการวิจัยและการพัฒนาการผลิตและการบริการในการวิจัยหนึ่งที่มีเทคโนโลยีสูงและการพัฒนาสถานประกอบการผลิตที่มุ่งเน้นความเชี่ยวชาญในขั้ว POS การเงิน, ค้าปลีก, ร้านอาหาร, บาร์, เพลงและพื้นที่อื่น ๆ , เครื่องคอมพิวเตอร์, บริการตนเองขั้วอุปกรณ์ต่อพ่วง R & D, การผลิตและยอดขาย! \n กระชับโครงสร้าง บริษัท  ขององค์กรและการปฏิบัติในทางปฏิบัติของสไตล์อย่างเข้มงวดดำเนินงานมีประสิทธิภาพ ความซื่อสัตย์ทุ่มเทความสามัคคีและมีประสิทธิภาพคือปรัชญาขององค์กรของ บริษัท อย่างต่อเนื่องและมุ่งมั่นเพื่อวันนี้ที่สดใสของ บริษัท จะมีกำลังแรงขึ้นทางวิทยาศาสตร์และเทคโนโลยีที่แข็งแกร่งจิตวิญญาณนิรันดร์ของผู้ประกอบการที่มีทัศนคติที่เป็นผู้บุกเบิกและนวัตกรรมความเชื่อมั่นที่มีต่ออุตสาหกรรมข้อมูลระหว่างประเทศ กับเพื่อน ๆ ในการสร้างอุตสาหกรรมข้อมูลที่ยอดเยี่ยม!!!\n\n\n";
			SendDataByte(PrinterCommand.POS_Print_Text(msg, THAI, 255, 0, 0, 0));
		}
	}
	
	/*
	 * 打印图片
	 */
	private void Print_BMP(){

	//	byte[] buffer = PrinterCommand.POS_Set_PrtInit();
		Bitmap mBitmap = ((BitmapDrawable) imageViewPicture.getDrawable())
				.getBitmap();
		int nMode = 0;
		int nPaperWidth = 384;
		if(width_58mm.isChecked())
			nPaperWidth = 384;
		else if (width_80.isChecked())
			nPaperWidth = 576;
		if(mBitmap != null)
		{
			/**
			 * Parameters:
			 * mBitmap  要打印的图片
			 * nWidth   打印宽度（58和80）
			 * nMode    打印模式
			 * Returns: byte[]
			 */
			byte[] data = PrintPicture.POS_PrintBMP(mBitmap, nPaperWidth, nMode);
		//	SendDataByte(buffer);
			SendDataByte(Command.ESC_Init);
			SendDataByte(Command.LF);
			SendDataByte(data);
			SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(30));
			SendDataByte(PrinterCommand.POS_Set_Cut(1));
			SendDataByte(PrinterCommand.POS_Set_PrtInit());
		}		
	}

	/**
	 * 打印自定义表格
	 */
	@SuppressLint("SimpleDateFormat") 
	private void PrintTable(){

		String lang = getString(R.string.strLang);
		if((lang.compareTo("cn")) == 0){
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String str = formatter.format(curDate);
		String date = str + "\n\n\n\n\n\n";	
		if(is58mm){

			Command.ESC_Align[2] = 0x02;
			byte[][] allbuf;
			try {
				allbuf = new byte[][]{

						Command.ESC_Init, Command.ESC_Three,
						String.format("┏━━┳━━━┳━━┳━━━━┓\n").getBytes("GBK"),
						String.format("┃发站┃%-4s┃到站┃%-6s┃\n","深圳","成都").getBytes("GBK"),
						String.format("┣━━╋━━━╋━━╋━━━━┫\n").getBytes("GBK"),
						String.format("┃件数┃%2d/%-3d┃单号┃%-8d┃\n",1,222,555).getBytes("GBK"),
						String.format("┣━━┻┳━━┻━━┻━━━━┫\n").getBytes("GBK"),
						String.format("┃收件人┃%-12s┃\n","【送】测试/测试人").getBytes("GBK"),
						String.format("┣━━━╋━━┳━━┳━━━━┫\n").getBytes("GBK"),
						String.format("┃业务员┃%-2s┃名称┃%-6s┃\n","测试","深圳").getBytes("GBK"),
						String.format("┗━━━┻━━┻━━┻━━━━┛\n").getBytes("GBK"),
						Command.ESC_Align, "\n".getBytes("GBK")
				};
				byte[] buf = Other.byteArraysToBytes(allbuf);
				SendDataByte(buf);
				SendDataString(date);
				SendDataByte(Command.GS_V_m_n);
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}else {

			Command.ESC_Align[2] = 0x02;
			byte[][] allbuf;
			try {
				allbuf = new byte[][]{

						Command.ESC_Init, Command.ESC_Three,
						String.format("┏━━┳━━━━━━━┳━━┳━━━━━━━━┓\n").getBytes("GBK"),
						String.format("┃发站┃%-12s┃到站┃%-14s┃\n", "深圳", "成都").getBytes("GBK"),
						String.format("┣━━╋━━━━━━━╋━━╋━━━━━━━━┫\n").getBytes("GBK"),
						String.format("┃件数┃%6d/%-7d┃单号┃%-16d┃\n", 1, 222, 55555555).getBytes("GBK"),
						String.format("┣━━┻┳━━━━━━┻━━┻━━━━━━━━┫\n").getBytes("GBK"),
						String.format("┃收件人┃%-28s┃\n", "【送】测试/测试人").getBytes("GBK"),
						String.format("┣━━━╋━━━━━━┳━━┳━━━━━━━━┫\n").getBytes("GBK"),
						String.format("┃业务员┃%-10s┃名称┃%-14s┃\n", "测试", "深圳").getBytes("GBK"),
						String.format("┗━━━┻━━━━━━┻━━┻━━━━━━━━┛\n").getBytes("GBK"),
						Command.ESC_Align, "\n".getBytes("GBK")
				};
				byte[] buf = Other.byteArraysToBytes(allbuf);
				SendDataByte(buf);
				SendDataString(date);
				SendDataByte(Command.GS_V_m_n);
			} catch (UnsupportedEncodingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		}
		}else if((lang.compareTo("en")) == 0){
			SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MM/dd/ HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间
			String str = formatter.format(curDate);
			String date = str + "\n\n\n\n\n\n";	
			if(is58mm){

				Command.ESC_Align[2] = 0x02;
				byte[][] allbuf;
				try {
					allbuf = new byte[][]{

							Command.ESC_Init, Command.ESC_Three,
							String.format("┏━━┳━━━┳━━┳━━━━┓\n").getBytes("GBK"),
							String.format("┃XXXX┃%-6s┃XXXX┃%-8s┃\n","XXXX","XXXX").getBytes("GBK"),
							String.format("┣━━╋━━━╋━━╋━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXX┃%2d/%-3d┃XXXX┃%-8d┃\n",1,222,555).getBytes("GBK"),
							String.format("┣━━┻┳━━┻━━┻━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXXXX┃%-18s┃\n","【XX】XXXX/XXXXXX").getBytes("GBK"),
							String.format("┣━━━╋━━┳━━┳━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXXXX┃%-2s┃XXXX┃%-8s┃\n","XXXX","XXXX").getBytes("GBK"),
							String.format("┗━━━┻━━┻━━┻━━━━┛\n").getBytes("GBK"),
							Command.ESC_Align, "\n".getBytes("GBK")
					};
					byte[] buf = Other.byteArraysToBytes(allbuf);
					SendDataByte(buf);
					SendDataString(date);
					SendDataByte(Command.GS_V_m_n);
				} catch (UnsupportedEncodingException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}else {

				Command.ESC_Align[2] = 0x02;
				byte[][] allbuf;
				try {
					allbuf = new byte[][]{

							Command.ESC_Init, Command.ESC_Three,
							String.format("┏━━┳━━━━━━━┳━━┳━━━━━━━━┓\n").getBytes("GBK"),
							String.format("┃XXXX┃%-14s┃XXXX┃%-16s┃\n", "XXXX", "XXXX").getBytes("GBK"),
							String.format("┣━━╋━━━━━━━╋━━╋━━━━━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXX┃%6d/%-7d┃XXXX┃%-16d┃\n", 1, 222, 55555555).getBytes("GBK"),
							String.format("┣━━┻┳━━━━━━┻━━┻━━━━━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXXXX┃%-34s┃\n", "【XX】XXXX/XXXXXX").getBytes("GBK"),
							String.format("┣━━━╋━━━━━━┳━━┳━━━━━━━━┫\n").getBytes("GBK"),
							String.format("┃XXXXXX┃%-12s┃XXXX┃%-16s┃\n", "XXXX", "XXXX").getBytes("GBK"),
							String.format("┗━━━┻━━━━━━┻━━┻━━━━━━━━┛\n").getBytes("GBK"),
							Command.ESC_Align, "\n".getBytes("GBK")
					};
					byte[] buf = Other.byteArraysToBytes(allbuf);
					SendDataByte(buf);
					SendDataString(date);
					SendDataByte(Command.GS_V_m_n);
				} catch (UnsupportedEncodingException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
			}
	}

	/**
	 * 打印自定义小票
	 */
	@SuppressLint("SimpleDateFormat") 
	private void Print_Ex(){

		String lang = getString(R.string.strLang);
		if((lang.compareTo("cn")) == 0){
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy年MM月dd日 HH:mm:ss ");
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间
		String str = formatter.format(curDate);
		String date = str + "\n\n\n\n\n\n";	
		if (is58mm) {

			try {
				byte[] qrcode = PrinterCommand.getBarCommand("热敏打印机!", 0, 3, 6);//
				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				SendDataByte(qrcode);

				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x11;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("NIKE专卖店\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x00;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x00;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("门店号: 888888\n单据  S00003333\n收银员：1001\n单据日期：xxxx-xx-xx\n打印时间：xxxx-xx-xx  xx:xx:xx\n".getBytes("GBK"));
				SendDataByte("品名       数量    单价    金额\nNIKE跑鞋   10.00   899     8990\nNIKE篮球鞋 10.00   1599    15990\n".getBytes("GBK"));
				SendDataByte("数量：                20.00\n总计：                16889.00\n付款：                17000.00\n找零：                111.00\n".getBytes("GBK"));
				SendDataByte("公司名称：NIKE\n公司网址：www.xxx.xxx\n地址：深圳市xx区xx号\n电话：0755-11111111\n服务专线：400-xxx-xxxx\n================================\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x11;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("谢谢惠顾,欢迎再次光临!\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x00;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x00;
				SendDataByte(Command.GS_ExclamationMark);
				
				SendDataByte("(以上信息为测试模板,如有苟同，纯属巧合!)\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x02;
				SendDataByte(Command.ESC_Align);
				SendDataString(date);
				SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
				SendDataByte(Command.GS_V_m_n);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				byte[] qrcode = PrinterCommand.getBarCommand("热敏打印机!", 0, 3, 6);
				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				SendDataByte(qrcode);

				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x11;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("NIKE专卖店\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x00;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x00;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("门店号: 888888\n单据  S00003333\n收银员：1001\n单据日期：xxxx-xx-xx\n打印时间：xxxx-xx-xx  xx:xx:xx\n".getBytes("GBK"));
				SendDataByte("品名            数量    单价    金额\nNIKE跑鞋        10.00   899     8990\nNIKE篮球鞋      10.00   1599    15990\n".getBytes("GBK"));
				SendDataByte("数量：                20.00\n总计：                16889.00\n付款：                17000.00\n找零：                111.00\n".getBytes("GBK"));
				SendDataByte("公司名称：NIKE\n公司网址：www.xxx.xxx\n地址：深圳市xx区xx号\n电话：0755-11111111\n服务专线：400-xxx-xxxx\n===========================================\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x01;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x11;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("谢谢惠顾,欢迎再次光临!\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x00;
				SendDataByte(Command.ESC_Align);
				Command.GS_ExclamationMark[2] = 0x00;
				SendDataByte(Command.GS_ExclamationMark);
				SendDataByte("(以上信息为测试模板,如有苟同，纯属巧合!)\n".getBytes("GBK"));
				Command.ESC_Align[2] = 0x02;
				SendDataByte(Command.ESC_Align);
				SendDataString(date);
				SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
				SendDataByte(Command.GS_V_m_n);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}else if((lang.compareTo("en")) == 0){
			SimpleDateFormat formatter = new SimpleDateFormat ("yyyy/MM/dd/ HH:mm:ss ");
			Date curDate = new Date(System.currentTimeMillis());//获取当前时间
			String str = formatter.format(curDate);
			String date = str + "\n\n\n\n\n\n";	
			if (is58mm) {

				try {
					byte[] qrcode = PrinterCommand.getBarCommand("Zijiang Electronic Thermal Receipt Printer!", 0, 3, 6);//
					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					SendDataByte(qrcode);

					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x11;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("NIKE Shop\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x00;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x00;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("Number:  888888\nReceipt  S00003333\nCashier：1001\nDate：xxxx-xx-xx\nPrint Time：xxxx-xx-xx  xx:xx:xx\n".getBytes("GBK"));
					SendDataByte("Name    Quantity    price  Money\nShoes   10.00       899     8990\nBall    10.00       1599    15990\n".getBytes("GBK"));
					SendDataByte("Quantity：             20.00\ntotal：                16889.00\npayment：              17000.00\nKeep the change：      111.00\n".getBytes("GBK"));
					SendDataByte("company name：NIKE\nSite：www.xxx.xxx\naddress：ShenzhenxxAreaxxnumber\nphone number：0755-11111111\nHelpline：400-xxx-xxxx\n================================\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x11;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("Welcome again!\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x00;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x00;
					SendDataByte(Command.GS_ExclamationMark);
					
					SendDataByte("(The above information is for testing template, if agree, is purely coincidental!)\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x02;
					SendDataByte(Command.ESC_Align);
					SendDataString(date);
					SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
					SendDataByte(Command.GS_V_m_n);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				try {
					byte[] qrcode = PrinterCommand.getBarCommand("Zijiang Electronic Thermal Receipt Printer!", 0, 3, 8);
					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					SendDataByte(qrcode);

					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x11;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("NIKE Shop\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x00;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x00;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("Number: 888888\nReceipt  S00003333\nCashier：1001\nDate：xxxx-xx-xx\nPrint Time：xxxx-xx-xx  xx:xx:xx\n".getBytes("GBK"));
					SendDataByte("Name                    Quantity price  Money\nNIKErunning shoes        10.00   899     8990\nNIKEBasketball Shoes     10.00   1599    15990\n".getBytes("GBK"));
					SendDataByte("Quantity：               20.00\ntotal：                  16889.00\npayment：                17000.00\nKeep the change：                111.00\n".getBytes("GBK"));
					SendDataByte("company name：NIKE\nSite：www.xxx.xxx\naddress：shenzhenxxAreaxxnumber\nphone number：0755-11111111\nHelpline：400-xxx-xxxx\n================================================\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x01;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x11;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("Welcome again!\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x00;
					SendDataByte(Command.ESC_Align);
					Command.GS_ExclamationMark[2] = 0x00;
					SendDataByte(Command.GS_ExclamationMark);
					SendDataByte("(The above information is for testing template, if agree, is purely coincidental!)\n".getBytes("GBK"));
					Command.ESC_Align[2] = 0x02;
					SendDataByte(Command.ESC_Align);
					SendDataString(date);
					SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
					SendDataByte(Command.GS_V_m_n);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 打印条码、二维码
	 */
	private void printBarCode() {
		
		new AlertDialog.Builder(Main_Activity.this).setTitle(getText(R.string.btn_prtcode))
		.setItems(codebar, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SendDataByte(byteCodebar[which]);
				String str = editText.getText().toString();
				if(which == 0)
				{
					if(str.length() == 11 || str.length() == 12)
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 65, 3, 168, 0, 2);
						SendDataByte(new byte[]{0x1b, 0x61, 0x00});
						SendDataString("UPC_A\n");
						SendDataByte(code);
					}else {
						Toast.makeText(Main_Activity.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(which == 1)
				{
					if(str.length() == 6 || str.length() == 7)
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 66, 3, 168, 0, 2);
						SendDataByte(new byte[]{0x1b, 0x61, 0x00});
						SendDataString("UPC_E\n");
						SendDataByte(code);
					}else {
						Toast.makeText(Main_Activity.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(which == 2)
				{
					if(str.length() == 12 || str.length() == 13)
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 67, 3, 168, 0, 2);
						SendDataByte(new byte[]{0x1b, 0x61, 0x00});
						SendDataString("JAN13(EAN13)\n");
						SendDataByte(code);
					}else {
						Toast.makeText(Main_Activity.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(which == 3)
				{
					if(str.length() >0 )
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 68, 3, 168, 0, 2);
						SendDataByte(new byte[]{0x1b, 0x61, 0x00});
						SendDataString("JAN8(EAN8)\n");
						SendDataByte(code);
					}else {
						Toast.makeText(Main_Activity.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				else if(which == 4)
				{
					if(str.length() == 0)
					{
						Toast.makeText(Main_Activity.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 69, 3, 168, 1, 2);
						SendDataString("CODE39\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 5)
				{
					if(str.length() == 0)
					{
						Toast.makeText(Main_Activity.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 70, 3, 168, 1, 2);
						SendDataString("ITF\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 6)
				{
					if(str.length() == 0)
					{
						Toast.makeText(Main_Activity.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 71, 3, 168, 1, 2);
						SendDataString("CODABAR\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 7)
				{
					if(str.length() == 0)
					{
						Toast.makeText(Main_Activity.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 72, 3, 168, 1, 2);
						SendDataString("CODE93\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 8)
				{
					if(str.length() == 0)
					{
						Toast.makeText(Main_Activity.this, getText(R.string.msg_error), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getCodeBarCommand(str, 73, 3, 168, 1, 2);
						SendDataString("CODE128\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
				else if(which == 9)
				{
					if(str.length() == 0)
					{
						Toast.makeText(Main_Activity.this, getText(R.string.empty1), Toast.LENGTH_SHORT).show();
						return;
					}
					else
					{
						byte[] code = PrinterCommand.getBarCommand(str, 1, 3, 8);
						SendDataString("QR Code\n");
						SendDataByte(new byte[]{0x1b, 0x61, 0x00 });
						SendDataByte(code);
					}
				}
			}
		}).create().show();	
	}

	/**
	 * public static Bitmap createAppIconText(Bitmap icon, String txt, boolean is58mm, int hight)
	 * Bitmap  icon     源图
	 * String txt       要转换的字符串  
	 * boolean is58mm   打印宽度(58和80)
	 * int hight        转换后的图片高度
	 */
	private void GraphicalPrint(){

		String txt_msg = editText.getText().toString(); 
		if(txt_msg.length() == 0){
			Toast.makeText(Main_Activity.this, getText(R.string.empty1), Toast.LENGTH_SHORT).show();
			return;
		}else{
			Bitmap bm1 = getImageFromAssetsFile("demo.jpg");
			if(width_58mm.isChecked()){
				
				Bitmap bmp = Other.createAppIconText(bm1,txt_msg,25,is58mm,200);
				int nMode = 0;
				int nPaperWidth = 384;
				
				if(bmp != null)
				{
					byte[] data = PrintPicture.POS_PrintBMP(bmp, nPaperWidth, nMode);
					SendDataByte(Command.ESC_Init);
					SendDataByte(Command.LF);
					SendDataByte(data);
					SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(30));
					SendDataByte(PrinterCommand.POS_Set_Cut(1));
					SendDataByte(PrinterCommand.POS_Set_PrtInit());
				}
			}
			else if (width_80.isChecked()){
				Bitmap bmp = Other.createAppIconText(bm1,txt_msg,25,false,200);
				int nMode = 0;
				
				int nPaperWidth = 576;
				if(bmp != null)
				{
					byte[] data = PrintPicture.POS_PrintBMP(bmp, nPaperWidth, nMode);
					SendDataByte(Command.ESC_Init);
					SendDataByte(Command.LF);
					SendDataByte(data);
					SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(30));
					SendDataByte(PrinterCommand.POS_Set_Cut(1));
					SendDataByte(PrinterCommand.POS_Set_PrtInit());
				}
			}
		}
	}
	
	/**
	 * 打印指令测试
	 */
	private void CommandTest(){

		String lang = getString(R.string.strLang);
		if((lang.compareTo("cn")) == 0){
		new AlertDialog.Builder(Main_Activity.this).setTitle(getText(R.string.chosecommand))
		.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				SendDataByte(byteCommands[which]);
				try {
					if(which == 16 || which == 17 || which == 18 || which == 19 || which == 22
					|| which == 23 || which == 24|| which == 0 || which == 1 || which == 27){
						return ;
					}else {
						SendDataByte("热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n热敏票据打印机ABCDEFGabcdefg123456,.;'/[{}]!\n".getBytes("GBK"));
					}
					
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).create().show();	
		}else if((lang.compareTo("en")) == 0){
			new AlertDialog.Builder(Main_Activity.this).setTitle(getText(R.string.chosecommand))
			.setItems(itemsen, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SendDataByte(byteCommands[which]);
					try {
						if(which == 16 || which == 17 || which == 18 || which == 19 || which == 22
						|| which == 23 || which == 24|| which == 0 || which == 1 || which == 27){
							return ;
						}else {
							SendDataByte("Thermal Receipt Printer ABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\nThermal Receipt PrinterABCDEFGabcdefg123456,.;'/[{}]!\n".getBytes("GBK"));
						}
						
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}).create().show();	
			}
	}
/************************************************************************************************/	
	/* 
	 * 生成QR图 
	 */
    private void createImage() {
	        try {
	            // 需要引入zxing包
	            QRCodeWriter writer = new QRCodeWriter();

	            String text = editText.getText().toString();

	            Log.i(TAG, "生成的文本：" + text);
	            if (text == null || "".equals(text) || text.length() < 1) {
	            	Toast.makeText(this, getText(R.string.empty), Toast.LENGTH_SHORT).show();
	            	return;
	            }

	            // 把输入的文本转为二维码
	            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
	                    QR_WIDTH, QR_HEIGHT);

	            System.out.println("w:" + martix.getWidth() + "h:"
	                    + martix.getHeight());

	            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
	            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
	            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
	                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
	            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
	            for (int y = 0; y < QR_HEIGHT; y++) {
	                for (int x = 0; x < QR_WIDTH; x++) {
	                    if (bitMatrix.get(x, y)) {
	                        pixels[y * QR_WIDTH + x] = 0xff000000;
	                    } else {
	                        pixels[y * QR_WIDTH + x] = 0xffffffff;
	                    }

	                }
	            }

	            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
	                    Bitmap.Config.ARGB_8888);

	            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
	            
	            byte[] data = PrintPicture.POS_PrintBMP(bitmap, 384, 0);
	            SendDataByte(data);
	            SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(30));
				SendDataByte(PrinterCommand.POS_Set_Cut(1));
				SendDataByte(PrinterCommand.POS_Set_PrtInit());
	        } catch (WriterException e) {
	            e.printStackTrace();
	        }
	    }
//************************************************************************************************//
  	/*
  	 * 调用系统相机
  	 */
  	private void dispatchTakePictureIntent(int actionCode) {
  	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  	    startActivityForResult(takePictureIntent, actionCode);
  	}
  	
  	private void handleSmallCameraPhoto(Intent intent) {
  	    Bundle extras = intent.getExtras();
  	    Bitmap mImageBitmap = (Bitmap) extras.get("data");
  	    imageViewPicture.setImageBitmap(mImageBitmap);
  	}
/****************************************************************************************************/
	 /**
	 * 加载assets文件资源
	 */
	private Bitmap getImageFromAssetsFile(String fileName) {
			Bitmap image = null;
			AssetManager am = getResources().getAssets();
			try {
				InputStream is = am.open(fileName);
				image = BitmapFactory.decodeStream(is);
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return image;

		}

}