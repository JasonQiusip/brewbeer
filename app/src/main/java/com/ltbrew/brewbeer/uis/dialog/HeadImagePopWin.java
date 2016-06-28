//package com.ltbrew.brewbeer.uis.dialog;
//
//import java.io.File;
//import java.io.IOException;
//
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.drawable.ColorDrawable;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.view.Gravity;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.ImageView;
//import android.widget.PopupWindow;
//
//import com.ltbrew.brewbeer.R;
//
//@SuppressLint("InflateParams")
//public class HeadImagePopWin {
//
//	private Activity activity;
//	private Uri fromFile;
//	private ImageView showview;
//	private Bitmap bitmap;
//	private View view;
//	private Uri outPutUri;
//	public static final int REQUEST_CODE_CAMERA = 11;
//	public static final int REQUEST_CODE_CROP = 22;
//	public static final int REQUEST_CODE_CHOOSE_PHOTO_KITKAT_LESS = 33;
//	public static final int REQUEST_CODE_CHOOSE_PHOTO_KITKAT_ABOVE = 44;
//	private static final int FOR_SELECT_PHOTO = 55;
//
//	public HeadImagePopWin(Activity activity) {
//		this.activity = activity;
//
//		File file = new File(Environment.getExternalStorageDirectory()+"/brewbeer/avatar", "temp.jpg");
//		if (!file.getParentFile().exists()) {
//			file.getParentFile().mkdirs();
//		}
//		if (!file.exists()) {
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		outPutUri = Uri.fromFile(file);
//
//	}
//
//	public void setShowLocation(View view) {
//		this.view = view;
//	}
//
//	public void setShowView(ImageView showview) {
//		this.showview = showview;
//	}
//
//	public Bitmap getBitmap() {
//		return bitmap;
//	}
//
//	public void setLayout() {
//
//		View popView = activity.getLayoutInflater().inflate(R.layout.layout_choose_head, null);
//		final PopupWindow popupWindow = new PopupWindow(popView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		popupWindow.setBackgroundDrawable(new ColorDrawable(0));
//		popupWindow.setAnimationStyle(R.style.PopMenuAnimation);
//		popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
//		popupWindow.setFocusable(true);
//		popupWindow.setOutsideTouchable(true);
//		popupWindow.update();
//
//		fromFile = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "temp.jpg"));
//		popView.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				popupWindow.dismiss();
//			}
//		});
//
//		popView.findViewById(R.id.babyhead_btn_choosephoto).setOnClickListener(new OnClickListener() {
//			@SuppressLint("InlinedApi")
//			@Override
//			public void onClick(View v) {
//				try {
//					selectPhoto();
//				} catch (Exception e) {
//					Intent intent = new Intent();
//					intent.setType("image/*");
//					if (Build.VERSION.SDK_INT < 19) {
//						intent.setAction(Intent.ACTION_GET_CONTENT);
//						activity.startActivityForResult(intent, REQUEST_CODE_CHOOSE_PHOTO_KITKAT_LESS);
//					} else {
//						intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//						activity.startActivityForResult(intent, REQUEST_CODE_CHOOSE_PHOTO_KITKAT_ABOVE);
//					}
//				}
//				popupWindow.dismiss();
//			}
//		});
//		popView.findViewById(R.id.babyhead_btn_camera).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//				intent.putExtra(MediaStore.EXTRA_OUTPUT, fromFile);
//				activity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
//				popupWindow.dismiss();
//			}
//		});
//		popView.findViewById(R.id.babyhead_btn_back).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				popupWindow.dismiss();
//			}
//		});
//	}
//
//	private void selectPhoto() {
//		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//		activity.startActivityForResult(intent, FOR_SELECT_PHOTO);
//	}
//
//	public void onImageResult(int requestCode, Intent data) {
//
//		if (requestCode == REQUEST_CODE_CAMERA) {
//			if (fromFile != null) {
//				startPhotoZoom(fromFile, outPutUri);
//			}
//		} else if (requestCode == REQUEST_CODE_CHOOSE_PHOTO_KITKAT_LESS) {
//			startPhotoZoom(data.getData(), outPutUri);
//		} else if (requestCode == REQUEST_CODE_CHOOSE_PHOTO_KITKAT_ABOVE) {
//			Uri uri = data.getData();
//			String thePath = PathUtils.getPath(activity, uri);
//			if (thePath != null)
//				uri = Uri.fromFile(new File(thePath));
//			startPhotoZoom(uri, outPutUri);
//		} else if (requestCode == REQUEST_CODE_CROP) {
//			bitmap = BitmapUtil.getBitmapFromUri(activity, outPutUri);
//			if (bitmap != null) {
//				showview.setImageBitmap(bitmap);
//			}
//		} else if (requestCode == FOR_SELECT_PHOTO) {
//			Uri originalUri = data.getData();
//			startPhotoZoom(originalUri, outPutUri);
//		}
//	}
//
//	public void startPhotoZoom(Uri uri, Uri outPutUri) {
//		Intent intent = new Intent("com.android.camera.action.CROP");
//		intent.setDataAndType(uri, "image/*");
//		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
//		intent.putExtra("crop", "true");
//		// aspectX aspectY 是宽高的比例
//		intent.putExtra("aspectX", 1);
//		intent.putExtra("aspectY", 1);
//		// outputX outputY 是裁剪图片宽高
//		intent.putExtra("outputX", 400);
//		intent.putExtra("outputY", 400);
//		intent.putExtra("return-data", false);
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, outPutUri);
//		intent.putExtra("scale", true);
//		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
//		activity.startActivityForResult(intent, REQUEST_CODE_CROP);
//	}
//
//}
