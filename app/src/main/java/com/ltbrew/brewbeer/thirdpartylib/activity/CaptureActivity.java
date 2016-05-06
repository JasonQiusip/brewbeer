package com.ltbrew.brewbeer.thirdpartylib.activity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Hashtable;
import java.util.Vector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.ltbrew.brewbeer.R;
import com.ltbrew.brewbeer.thirdpartylib.camera.CameraManager;
import com.ltbrew.brewbeer.thirdpartylib.decoding.CaptureActivityHandler;
import com.ltbrew.brewbeer.thirdpartylib.decoding.InactivityTimer;
import com.ltbrew.brewbeer.thirdpartylib.decoding.RGBLuminanceSource;
import com.ltbrew.brewbeer.thirdpartylib.decoding.Utils;
import com.ltbrew.brewbeer.thirdpartylib.view.ViewfinderView;
import com.ltbrew.brewbeer.uis.activity.BaseActivity;

/**
 * Initial the camera
 * 
 * @author Ryan.Tang
 */
public class CaptureActivity extends BaseActivity implements Callback, OnClickListener {

	private static final int REQUEST_CODE = 234;
	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private boolean isLightOpen;
	private boolean hasFlashlight;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		CameraManager.init(this);
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		findViewById(R.id.tv_back).setOnClickListener(this);
		findViewById(R.id.tv_light).setOnClickListener(this);
		findViewById(R.id.tv_img_document).setOnClickListener(this);
		viewfinderView.setContext(this);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	/**
	 * 判断手机是否支持闪光灯
	 * */
	public boolean hasFlashlight() {
		FeatureInfo[] features = getPackageManager().getSystemAvailableFeatures();
		for (FeatureInfo info : features) {
			if (PackageManager.FEATURE_CAMERA_FLASH.equals(info.name)) {
				return true;
			}
		}
		return false;
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void openPicDocument() {

		try {
			Intent wrapperIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			CaptureActivity.this.startActivityForResult(wrapperIntent, REQUEST_CODE);

		} catch (Exception e) {
			Intent innerIntent = new Intent();
			if (Build.VERSION.SDK_INT < 19) {
				innerIntent.setAction(Intent.ACTION_GET_CONTENT);
			} else {
				innerIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
			}
			innerIntent.setType("image/*");
			Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
			CaptureActivity.this.startActivityForResult(wrapperIntent, REQUEST_CODE);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			/**
			 * SURFACE_TYPE_PUSH_BUFFERS表明该Surface不包含原生数据，Surface用到的数据由其他对象提供，
			 * 在Camera图像预览中就使用该类型的Surface，有Camera负责提供给预览Surface数据，这样图像预览会比较流畅。
			 */
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
	}

	/**
	 * 离开此页按钮
	 * */
	public void cancelScan(View v) {
		CaptureActivity.this.finish();
	}

	/**
	 * 开启闪光灯按钮
	 * */
	public void openLight() {
		hasFlashlight = hasFlashlight();
		if (!hasFlashlight) {
			showSnackBar("您的手机不支持闪光灯");
		} else {
			if (!isLightOpen) {
				try {
					CameraManager.get().openF();
					isLightOpen = true;
				} catch (Exception e) {
					showSnackBar("无法开启闪光灯");
					isLightOpen = false;
				}
			} else {
				CameraManager.get().stopF();
				isLightOpen = false;
			}
		}
	}

	@Override
	protected void onPause() {
		isLightOpen = false;
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * Handler scan result
	 * 
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		// FIXME
		if (resultString.equals("")) {
			Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		} else {
			Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);
		}
		CaptureActivity.this.finish();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};
	private String photo_path;

	/**
	 * 手机实体按键监控
	 * */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 返回按键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			CaptureActivity.this.finish();
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {

			if (requestCode == REQUEST_CODE) {

			}

			String[] proj = { MediaStore.Images.Media.DATA };
			// 获取选中图片的路径
			Cursor cursor = getContentResolver().query(data.getData(), proj, null, null, null);
			if(null != cursor) {
				if (cursor.moveToFirst()) {
					
					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					photo_path = cursor.getString(column_index);
					if (photo_path == null) {
						photo_path = Utils.getPath(getApplicationContext(), data.getData());
					}
				}
				
				cursor.close();
			} else {
				showSnackBar( "无法获取图片，请重试");
				return;
			}

			new Thread(new Runnable() {

				@Override
				public void run() {

					Result result = scanningImage(photo_path);
					// String result = decode(photo_path);
					if (result == null) {
						Looper.prepare();
						showSnackBar( "请选择可解析的二维码图片");
						Looper.loop();
					} else {
						// Log.i("123result", result.getText());
						// 数据返回
						String recode = recode(result.toString());
						Intent data = new Intent();
						data.putExtra("result", recode);
						setResult(Activity.RESULT_OK, data);
						finish();
					}
				}
			}).start();

		}

	}

	// TODO: 解析部分图片
	protected Result scanningImage(String path) {
		if (TextUtils.isEmpty(path)) {

			return null;

		}
		// DecodeHintType 和EncodeHintType
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小

		int sampleSize = (int) (options.outHeight / (float) 200);

		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);

		// --------------测试的解析方法---PlanarYUVLuminanceSource-这几行代码对project没作功----------
		if(null == scanBitmap) {
			return null;
		}
		LuminanceSource source1 = new PlanarYUVLuminanceSource(rgb2YUV(scanBitmap), scanBitmap.getWidth(), scanBitmap.getHeight(), 0, 0,
				scanBitmap.getWidth(), scanBitmap.getHeight(), false);
		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source1));
		MultiFormatReader reader1 = new MultiFormatReader();
		Result result1;
		try {
			result1 = reader1.decode(binaryBitmap);
			String content = result1.getText();
		} catch (NotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// ----------------------------

		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {

			return reader.decode(bitmap1, hints);

		} catch (NotFoundException e) {

			e.printStackTrace();

		} catch (ChecksumException e) {

			e.printStackTrace();

		} catch (FormatException e) {

			e.printStackTrace();

		}

		return null;

	}

	/**
	 * 中文乱码
	 * 
	 * 暂时解决大部分的中文乱码 但是还有部分的乱码无法解决 .
	 * 
	 * 如果您有好的解决方式 请联系 2221673069@qq.com
	 * 
	 * 我会很乐意向您请教 谢谢您
	 * 
	 * @return
	 */
	private String recode(String str) {
		String formart = "";

		try {
			boolean ISO = Charset.forName("ISO-8859-1").newEncoder().canEncode(str);
			if (ISO) {
				formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
			} else {
				formart = str;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return formart;
	}

	/**
	 * //TODO: TAOTAO 将bitmap由RGB转换为YUV //TOOD: 研究中
	 * 
	 * @param bitmap
	 *            转换的图形
	 * @return YUV数据
	 */
	public byte[] rgb2YUV(Bitmap bitmap) {
		// 该方法来自QQ空间
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		int len = width * height;
		byte[] yuv = new byte[len * 3 / 2];
		int y, u, v;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int rgb = pixels[i * width + j] & 0x00FFFFFF;

				int r = rgb & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb >> 16) & 0xFF;

				y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
				u = ((-38 * r - 74 * g + 112 * b + 128) >> 8) + 128;
				v = ((112 * r - 94 * g - 18 * b + 128) >> 8) + 128;

				y = y < 16 ? 16 : (y > 255 ? 255 : y);
				u = u < 0 ? 0 : (u > 255 ? 255 : u);
				v = v < 0 ? 0 : (v > 255 ? 255 : v);

				yuv[i * width + j] = (byte) y;
				// yuv[len + (i >> 1) * width + (j & ~1) + 0] = (byte) u;
				// yuv[len + (i >> 1) * width + (j & ~1) + 1] = (byte) v;
			}
		}
		return yuv;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_back:
			finish();
			break;
		case R.id.tv_light:
			openLight();
			break;
		case R.id.tv_img_document:
			openPicDocument();
			break;
		default:
			break;
		}
	}
}