package jp.spidernet.myphone;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends ArrayAdapter<File> {
	private ArrayList<File> mFiles = null;
	private MainActivity mActivity;
	private HashMap<String, Boolean> mCheckedMap = null;
	private ArrayList<CompoundButton> mCheckedView = null;
	public FileListAdapter(MainActivity activity, int layoutId,
			ArrayList<File> files) {
		super(activity, layoutId, files);
		mFiles = files;
		mActivity = activity;
		mCheckedMap = new HashMap<String, Boolean>();
		mCheckedView = new ArrayList<CompoundButton>();
		if (activity.isCut() == false) {
			activity.setCheckedFilesList(new ArrayList<File>());
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View listItem = null;
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) mActivity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			listItem = layoutInflater.inflate(R.layout.listitem, null);
		} else {
			listItem = convertView;
		}
		File file = mFiles.get(position);
		String title = file.getName();
		long fileSize = file.length();
		long lastModified = file.lastModified();
		TextView tvTitle = (TextView) listItem.findViewById(R.id.tvTitle);
		TextView tvFileInfo = (TextView) listItem.findViewById(R.id.tvFileInfo);
		ImageView ivIcon = (ImageView) listItem.findViewById(R.id.iv_icon);
		CheckBox checkBox = (CheckBox) listItem.findViewById(R.id.checkBox);
		tvTitle.setText(title);
		String fileName = file.getName();
		String info = "";
		
		if (file.isDirectory()) {
			File[] childrenLists = file.listFiles();
			int childFiles = 0;
			int childFolders = 0;
			if (childrenLists != null) {
				int length = childrenLists.length;
				ivIcon.setImageResource(R.drawable.ic_folder_not_empty);
				for (int i = 0; i < length; i++) {
					File file2 = childrenLists[i];
					if (file2.isDirectory())
						childFolders++;
					else if (file2.isFile())
						childFiles++;
				}
			} else {
				ivIcon.setImageResource(R.drawable.ic_empty_folder);
			}
			info = mActivity.getString(R.string.numfiles_numfolders, childFiles, childFolders);
			tvFileInfo.setText(info);
		} else {
			info = mActivity.getString(R.string.file_details, Utility.reportTraffic(fileSize),
					SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
							.format(new Date(lastModified)));
			tvFileInfo.setText(info);
			int id = Utility.getFileExtensionId(fileName);
			if (id == R.drawable.ic_jpeg || id == R.drawable.ic_png
					|| id == R.drawable.ic_gif || id == R.drawable.ic_bmp) {
				Bitmap bmp = Utility.getPreview(file);
				if (bmp != null)
					ivIcon.setImageBitmap(bmp);
				else
					ivIcon.setImageResource(id);
			} else if (id == R.drawable.ic_3gp || id == R.drawable.ic_mp4
					|| id == R.drawable.ic_mpeg || id == R.drawable.ic_avi) {
				Bitmap thumb = ThumbnailUtils.createVideoThumbnail(
						file.getAbsolutePath(),
						MediaStore.Images.Thumbnails.MINI_KIND);
				if (thumb != null)
					ivIcon.setImageBitmap(thumb);
				else
					ivIcon.setImageResource(id);
			} else if (id == R.drawable.ic_apk) {
				Bitmap bmp = Utility.getIconInApkFile(file, mActivity);
				if (bmp != null)
					ivIcon.setImageBitmap(bmp);
				else
					ivIcon.setImageResource(id);
			}
			else
				ivIcon.setImageResource(id);
		}
		/*******/
		checkBox.setTag(checkBox.getId(), file);
		checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		Boolean isChecked = (Boolean) mCheckedMap.get(fileName);
		if (isChecked != null) {
			checkBox.setChecked(isChecked);

		} else {
			checkBox.setChecked(false);

		}
		return listItem;
	}

	OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			File file = (File) buttonView.getTag(buttonView.getId());
			mCheckedMap.put(file.getName(), isChecked);
			
			if (isChecked) {
				mActivity.addToCheckedFilesList(file);
				mCheckedView.add(buttonView);
			} else {
				mActivity.removeFromCheckedFilesList(file);
				mCheckedView.remove(buttonView);
			}
		}
	};
	
	public void clearCheckedView() {
		int size = mCheckedView.size();
		for (int i = 0; i < size; i++) {
			//onCheckedChanged縺ｧ繝ｪ繧ｹ繝医い繧､繝�Β縺悟炎髯､縺輔ｌ縺ｦ縺�ｋ縺溘ａ縲√��0縲阪°繧峨メ繧ｧ繝�け繧呈ｶ医☆縲�
			mCheckedView.get(0).setChecked(false);
		}
	}
}