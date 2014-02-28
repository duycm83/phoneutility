package jp.spidernet.myphone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jp.spidernet.myphone.tools.CommonDialogFactory;
import jp.spidernet.myphone.tools.ISimpleListener;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class EditActivity extends MainActivity {
	private static final String TAG = EditActivity.class.getSimpleName();
	private static final int DIALOG_ABOUT_ID = 1;
	public static final String CURRENT_DIR = "current_dir";
	private ListView mListView = null;
	private ArrayList<File> mListFiles;
	private ArrayList<File> mCheckedFilesList = new ArrayList<File>();
	private TextView mTvCurrentDir = null;
	private FileListAdapter mFilesListAdapter = null;
	private Menu mMenu = null;
	private int mEditMode = 0;
	private String[] mFilesList = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mEditMode = getIntent().getIntExtra(EXTRA_EDIT_MODE, 0);
		mFilesList = getIntent().getStringArrayExtra(EXTRA_FILE_LIST);
		if (mEditMode == 0 || mFilesList == null) {
			finish();
			return;
		}
		
		mTvCurrentDir = (TextView) findViewById(R.id.tvCurrentDir);
		mTvCurrentDir.setText(mCurrentDir.getAbsolutePath());
		mListView = (ListView) findViewById(R.id.listView);
		registerForContextMenu(mListView);
		updateNewDir(mCurrentDir);
		mListView.setAdapter(mFilesListAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long id) {
				File selectFile = mListFiles.get(pos);
				if (!selectFile.canRead()) {
					Toast.makeText(getBaseContext(),
							getString(R.string.cant_open), Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (selectFile.isFile()) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					String fileName = selectFile.getName();
					String extension = Utility.getFileExtension(fileName);
					String mimeType = null;
					if (Utility.APK.toLowerCase().equals(extension)) {
						mimeType = Utility.MIME_TYPE_APK;
					} else {
						mimeType = MimeTypeMap.getSingleton()
								.getMimeTypeFromExtension(extension);
						if (mimeType == null)
							mimeType = Utility.TEXT_PLAIN;
					}
					intent.setDataAndType(Uri.fromFile(selectFile), mimeType);
					try {
						startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					mSelectedPosStack.push(pos);
					updateNewDir(selectFile);
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		String name = mCurrentDir.getName();
		if (SDDIR.getName().equals(name)) {
			super.onBackPressed();
		} else {
			File newDir = upToParentDir(null);
			if (newDir == null) {
				super.onBackPressed();
			}
		}
		clearCheckedItem();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_ABOUT_ID:
			dialog = new Dialog(this);

			dialog.setContentView(R.layout.about);
			dialog.setTitle(R.string.app_name);
			dialog.findViewById(R.id.close).setOnClickListener(
					new OnClickListener() {
						public void onClick(View arg0) {
							dismissDialog(DIALOG_ABOUT_ID);
						}
					});
			break;

		default:
			dialog = super.onCreateDialog(id);
			break;
		}
		return dialog;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		if (mEditMode == EDIT_MODE_COPY) {
			menuInflater.inflate(R.menu.edit_copy_menu, menu);
		} else if (mEditMode == EDIT_MODE_CUT) {
			menuInflater.inflate(R.menu.edit_move_menu, menu);
		}
		this.mMenu = menu;
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent data = new Intent();
		int id = item.getItemId();
		switch (id) {
		case R.id.itemMove:
			if (mFilesList.length > 0) {
				Utility.move(mFilesList, mCurrentDir);
				updateNewDir(mCurrentDir);
			}
			data.putExtra(CURRENT_DIR, mCurrentDir.getAbsolutePath());
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.itemPaste:
			if (mFilesList.length > 0) {
				Utility.copy(mFilesList, mCurrentDir);
				updateNewDir(mCurrentDir);
			}
			data.putExtra(CURRENT_DIR, mCurrentDir.getAbsolutePath());
			setResult(RESULT_CANCELED);
			finish();
			break;
		case R.id.itemAbout:
			// Intent intent = new Intent(this, AboutActivity.class);
			// startActivity(intent);
			showDialog(DIALOG_ABOUT_ID);
			break;
		case R.id.itemNewFile:
			CommonDialogFactory.make1(this, getString(R.string.new_file),
					new ISimpleListener() {
						public void onClick(String... params) {
							File file = new File(params[0]);
							if (!file.exists()) {
								try {
									boolean result = file.createNewFile();
									if (result) {
										mListFiles.add(file);
										mFilesListAdapter
												.notifyDataSetChanged();
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}, new ISimpleListener() {
						public void onClick(String... params) {
							File file = new File(params[0]);
							if (!file.exists()) {
								boolean result = file.mkdir();
								if (result) {
									mListFiles.add(file);
									mFilesListAdapter.notifyDataSetChanged();
								}
							}
						}
					}, null).show();
			break;
		case R.id.itemCancel:
			clearCheckedItem();
			setResult(RESULT_CANCELED);
			finish();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void clearCheckedItem() {
		mCheckedFilesList.clear();
		if (mFilesListAdapter != null) {
			mFilesListAdapter.clearCheckedView();
		}
	}

	
	public File upToParentDir(MenuItem v) {
		File upDir = mCurrentDir.getParentFile();
		if (upDir != null) {
			updateNewDir(upDir);
			if (!mSelectedPosStack.isEmpty())
				mListView.setSelection(mSelectedPosStack.pop());
		}
		return upDir;
	}

	private void updateNewDir(File newFileDir) {
		mCurrentDir = newFileDir;
		mListFiles = Utility.makeFilesArrayList(mCurrentDir.listFiles());
		Utility.sortFilesList(mListFiles);
		mFilesListAdapter = new FileListAdapter(EditActivity.this,
				R.layout.listitem, mListFiles);
		mListView.setAdapter(mFilesListAdapter);
		mTvCurrentDir.setText(mCurrentDir.getAbsolutePath());
	}

	public void addToCheckedFilesList(File file) {
		mCheckedFilesList.add(file);
	}

	public void removeFromCheckedFilesList(File file) {
		if (mCheckedFilesList != null) {
			mCheckedFilesList.remove(file);
		}
	}

	public void setCheckedFilesList(ArrayList<File> checkedFilesList) {
		this.mCheckedFilesList = checkedFilesList;
	}

	public void onMenuEditMode(View view) {
		if (this.mMenu == null) {
			return;
		} else {
			if (mCheckedFilesList.size() > 0) {
				this.mMenu.clear();
				MenuInflater menuInflater = getMenuInflater();
				menuInflater.inflate(R.menu.edit_select_menu, this.mMenu);
			} else {
				this.mMenu.clear();
				MenuInflater menuInflater = getMenuInflater();
				menuInflater.inflate(R.menu.main_menu, this.mMenu);
			}
		}
	}
}