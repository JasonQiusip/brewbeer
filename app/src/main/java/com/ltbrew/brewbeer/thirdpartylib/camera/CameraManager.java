package com.ltbrew.brewbeer.thirdpartylib.camera;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public final class CameraManager {

	private static final String TAG = CameraManager.class.getSimpleName();
	private static int MIN_FRAME_WIDTH;
	private static int MIN_FRAME_HEIGHT;
	private static int MAX_FRAME_WIDTH;
	private static int MAX_FRAME_HEIGHT;

	private static CameraManager cameraManager;

	static final int SDK_INT;
	static {
		int sdkInt;
		try {
			sdkInt = Integer.parseInt(Build.VERSION.SDK);
		} catch (NumberFormatException nfe) {

			sdkInt = 10000;
		}
		SDK_INT = sdkInt;
	}

	public static void setFrameSize(int width, int height) {

		MIN_FRAME_WIDTH = (int) (width * 0.09);
		MIN_FRAME_HEIGHT = MAX_FRAME_WIDTH;
		MAX_FRAME_WIDTH = (int) (height * 0.36);
		MAX_FRAME_HEIGHT = MAX_FRAME_WIDTH;
	}

	private final Context context;
	private final CameraConfigurationManager configManager;
	private Camera mCamera;
	private Rect framingRect;
	private Rect framingRectInPreview;
	private boolean initialized;
	private boolean previewing;
	private final boolean useOneShotPreviewCallback;

	private final PreviewCallback previewCallback;
	private final AutoFocusCallback autoFocusCallback;

	public static void init(Context context) {
		if (cameraManager == null) {
			cameraManager = new CameraManager(context);
		}
	}

	public static CameraManager get() {
		return cameraManager;
	}

	private CameraManager(Context context) {

		this.context = context;
		this.configManager = new CameraConfigurationManager(context);

		useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > 3;

		previewCallback = new PreviewCallback(configManager, useOneShotPreviewCallback);
		autoFocusCallback = new AutoFocusCallback();
	}

	public void openDriver(SurfaceHolder holder) throws IOException {
		if (mCamera == null) {
			mCamera = Camera.open();
			if (mCamera == null) {
				throw new IOException();
			}
			mCamera.setPreviewDisplay(holder);

			if (!initialized) {
				initialized = true;
				configManager.initFromCameraParameters(mCamera);
			}
			configManager.setDesiredCameraParameters(mCamera);

			FlashlightManager.enableFlashlight();
		}
	}

	public void closeDriver() {
		if (mCamera != null) {
			FlashlightManager.disableFlashlight();
			mCamera.release();
			mCamera = null;
		}
	}

	public void startPreview() {
		if (mCamera != null && !previewing) {
			mCamera.startPreview();
			previewing = true;
		}
	}

	public void stopPreview() {
		if (mCamera != null && previewing) {
			if (!useOneShotPreviewCallback) {
				mCamera.setPreviewCallback(null);
			}
			mCamera.stopPreview();
			previewCallback.setHandler(null, 0);
			autoFocusCallback.setHandler(null, 0);
			previewing = false;
		}
	}

	public void requestPreviewFrame(Handler handler, int message) {
		if (mCamera != null && previewing) {
			previewCallback.setHandler(handler, message);
			if (useOneShotPreviewCallback) {
				mCamera.setOneShotPreviewCallback(previewCallback);
			} else {
				mCamera.setPreviewCallback(previewCallback);
			}
		}
	}

	public void requestAutoFocus(Handler handler, int message) {
		if (mCamera != null && previewing) {
			autoFocusCallback.setHandler(handler, message);
			// Log.d(TAG, "Requesting auto-focus callback");
			mCamera.autoFocus(autoFocusCallback);
		}
	}

	public Rect getFramingRect() {
		Point screenResolution = configManager.getScreenResolution();
		if (framingRect == null) {
			if (mCamera == null) {
				return null;
			}
			int width = screenResolution.x * 3 / 4;
			if (width < MIN_FRAME_WIDTH) {
				width = MIN_FRAME_WIDTH;
			} else if (width > MAX_FRAME_WIDTH) {
				width = MAX_FRAME_WIDTH;
			}
			int height = screenResolution.y * 3 / 4;
			if (height < MIN_FRAME_HEIGHT) {
				height = MIN_FRAME_HEIGHT;
			} else if (height > MAX_FRAME_HEIGHT) {
				height = MAX_FRAME_HEIGHT;
			}
			int leftOffset = (screenResolution.x - width) / 2;
			int topOffset = (int) ((screenResolution.y - height) / 2.5);
			framingRect = new Rect(leftOffset, topOffset, leftOffset + width, topOffset + height);
			Log.d(TAG, "Calculated framing rect: " + framingRect);
		}
		return framingRect;
	}

	public Rect getFramingRectInPreview() {
		if (framingRectInPreview == null) {
			Rect rect = new Rect(getFramingRect());
			Point cameraResolution = configManager.getCameraResolution();
			Point screenResolution = configManager.getScreenResolution();
			rect.left = rect.left * cameraResolution.y / screenResolution.x;
			rect.right = rect.right * cameraResolution.y / screenResolution.x;
			rect.top = rect.top * cameraResolution.x / screenResolution.y;
			rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
			framingRectInPreview = rect;
		}
		return framingRectInPreview;
	}

	/**
	 * 工厂方法构建适当的LuminanceSource对象基于预览缓冲区的格式*,像Camera.Parameters所描述的那样.
	 * 
	 * @param data
	 *            A preview frame.
	 * @param width
	 *            The width of the image.
	 * @param height
	 *            The height of the image.
	 * @return A PlanarYUVLuminanceSource instance.
	 */
	public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
		Rect rect = getFramingRectInPreview();
		int previewFormat = configManager.getPreviewFormat();
		String previewFormatString = configManager.getPreviewFormatString();
		switch (previewFormat) {

		case PixelFormat.YCbCr_420_SP:

		case PixelFormat.YCbCr_422_SP:
			return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height());
		default:

			if ("yuv420p".equals(previewFormatString)) {
				return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height());
			}
		}
		throw new IllegalArgumentException("Unsupported picture format: " + previewFormat + '/' + previewFormatString);
	}

	public Context getContext() {
		return context;
	}

	public void openF() {
		if (mCamera == null) {
			Toast.makeText(context, "Camera not found", Toast.LENGTH_LONG).show();
			return;
		}
		Parameters parameters = mCamera.getParameters();
		if (parameters == null) {
			return;
		}
		List<String> flashModes = parameters.getSupportedFlashModes();
		if (flashModes == null) {
			return;
		}
		String flashMode = parameters.getFlashMode();
		if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
			if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
				parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
				mCamera.setParameters(parameters);
			} else {
				Toast.makeText(context, "Flash mode (torch) not supported", Toast.LENGTH_LONG).show();
			}
		}
	}

	public void stopF() {

		if (mCamera == null) {
			System.out.println("ssss");
			return;
		}
		System.out.println("parameters");
		Parameters parameters = mCamera.getParameters();
		if (parameters == null) {
			System.out.println("parameters == null");
			return;
		}
		List<String> flashModes = parameters.getSupportedFlashModes();
		String flashMode = parameters.getFlashMode();
		// 检查是否存在相机闪光灯
		if (flashModes == null) {
			return;
		}
		Log.i(TAG, "Flash mode: " + flashMode);
		Log.i(TAG, "Flash modes: " + flashModes);
		if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {
			// 关掉闪光
			if (flashModes.contains(Parameters.FLASH_MODE_OFF)) {
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(parameters);
			} else {
				Log.e(TAG, "FLASH_MODE_OFF not supported");
			}
		}
	}
}
