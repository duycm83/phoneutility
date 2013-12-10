package jp.spidernet.myphone.tools;

import java.io.File;
import java.util.ArrayList;

import jp.spidernet.myphone.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class DeleteFileAsyncTask extends AsyncTask<ArrayList<File>, Integer, Boolean> {
	Context mContext = null;
	ProgressDialog dialog = null;
	
	public DeleteFileAsyncTask(Context context) {
		mContext = context;
	}
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		dialog = new ProgressDialog(mContext);
		dialog.setTitle(R.string.deleting);
		dialog.show();
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
	}
	
	@Override
	protected Boolean doInBackground(ArrayList<File>... arrayfiles) {
		if (arrayfiles != null) {
			ArrayList<File> files = arrayfiles[0];
			for (File file : files) {
				if (file.isDirectory()) {
					File[] subFiles = file.listFiles();
					if (files != null) {
						
					}
				}
				file.delete();
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}
}
