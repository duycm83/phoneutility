package jp.spidernet.myphone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import jp.spidernet.myphone.tools.CommonDialogFactory;
import jp.spidernet.myphone.tools.FileUtils;
import jp.spidernet.myphone.tools.ISimpleListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MyPhoneActivity extends Activity {
	private static final String TAG = MyPhoneActivity.class.getSimpleName();
	private static final int DIALOG_ABOUT_ID = 1;
	private ListView mListView = null;
	private File SDDIR = Environment.getExternalStorageDirectory();
	private File mCurrentDir = SDDIR;
	private ArrayList<File> mListFiles;
	private ArrayList<File> mCheckedFilesList = new ArrayList<File>();
	private TextView mTvCurrentDir = null;
	private Stack<Integer> mSelectedPosStack = new Stack<Integer>();
	private FileListAdapter mFilesListAdapter = null;
	private ArrayList<File> mCutFilesList =  new ArrayList<File>();
	private ArrayList<File> mCopyFilesList =  new ArrayList<File>();
	private Menu mMenu = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Utility.getSensorInfo(getBaseContext());
		Utility.getLocationInfo(getBaseContext());
		// accessRoot();
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
		IntentFilter intentfilter = new IntentFilter();
		intentfilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		intentfilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(deviceAtatchReceiver, intentfilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
//		 Intent myStarterIntent = new Intent(this, StartUpService.class);
//		 startService(myStarterIntent);
//		 AdlantisUtil.startAtlantis(this);

		Intent intent = getIntent();
		Log.d(TAG, "intent: " + intent);
		String action = intent.getAction();

		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
			Log.v(TAG, action);
			Toast.makeText(getBaseContext(), action, Toast.LENGTH_SHORT).show();
		}
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
		menuInflater.inflate(R.menu.main_menu, menu);
		this.mMenu = menu;
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
//		if (mCheckedFilesList.size() > 0) {
//			if (mCutFilesList != null && mCutFilesList.size() > 0) {
//				menu.findItem(R.id.itemCut).setTitle(R.string.paste);
//				menu.findItem(R.id.itemCut).setIcon(
//						R.drawable.ic_menu_paste_holo_dark);
//			} else {
//				menu.findItem(R.id.itemCut).setTitle(R.string.cut);
//				menu.findItem(R.id.itemCut).setIcon(R.drawable.ic_menu_cut);
//				menu.findItem(R.id.itemCopy).setTitle(R.string.copy);
//				menu.findItem(R.id.itemCopy).setIcon(R.drawable.ic_menu_copy);
//			}
//			menu.findItem(R.id.itemDelete).setVisible(true);
//			menu.findItem(R.id.itemCut).setVisible(true);
//			menu.findItem(R.id.itemCopy).setVisible(true);
//			menu.findItem(R.id.itemCancel).setVisible(true);
//		} else {
//			menu.findItem(R.id.itemDelete).setVisible(false);
//			menu.findItem(R.id.itemCut).setVisible(false);
//			menu.findItem(R.id.itemCopy).setVisible(false);
//			menu.findItem(R.id.itemCancel).setVisible(false);
//		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.itemDelete:
			if (mCheckedFilesList != null) {
				for (File file : mCheckedFilesList) {
					if (file.isDirectory()) {
						try {
							FileUtils.deleteRecursive(file);
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					} else {
						file.delete();
					}
					mListFiles.remove(file);
					mFilesListAdapter.notifyDataSetChanged();
				}
			}
			break;
		case R.id.itemCut:
			mCutFilesList = (ArrayList<File>) mCheckedFilesList.clone();
			changeToMenuCut();
			break;
		case R.id.itemCopy:
			mCopyFilesList = (ArrayList<File>) mCheckedFilesList.clone();
			changeToMenuCopy();
			break;
		case R.id.itemMove:
			if (mCutFilesList != null && mCutFilesList.size() > 0) {
				Utility.move(mCutFilesList, mCurrentDir);
				updateNewDir(mCurrentDir);
				mCutFilesList.clear();
				mCheckedFilesList.clear();
			}
			changeToMenuMain();
			break;
		case R.id.itemPaste:
			if (mCopyFilesList != null && mCopyFilesList.size() > 0) {
				Utility.copy(mCopyFilesList, mCurrentDir);
				updateNewDir(mCurrentDir);
				mCopyFilesList.clear();
				mCheckedFilesList.clear();
			}
			changeToMenuMain();
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
			onMenuEditMode(null);
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void clearCheckedItem() {
		mCheckedFilesList.clear();
		if (mCutFilesList != null) {
			mCutFilesList.clear();
		}
		if (mCopyFilesList != null) {
			mCopyFilesList.clear();
		}
		if (mFilesListAdapter != null) {
			mFilesListAdapter.clearCheckedView();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		Log.v(TAG, "onCreateContextMenu was called");
		int id = v.getId();
		if (id == R.id.listView) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle(mListFiles.get(info.position).getName());
			// String[] menuItems = getResources().getStringArray(
			// R.array.context_menu);
			// for (int i = 0; i < menuItems.length; i++) {
			// menu.add(info.position, i, i, menuItems[i]);
			// }
			menu.add(info.position, 0, 0, R.string.rename);
			if (mCutFilesList == null)
				menu.add(info.position, 1, 1, R.string.cut);
			else
				menu.add(info.position, 1, 1, R.string.paste);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemIndex = item.getItemId();
		if (menuItemIndex == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final EditText editText = new EditText(MyPhoneActivity.this);
			editText.setText(mListFiles.get(info.position).getName());
			builder.setMessage("Are you sure you want to change file?")
					.setView(editText)
					.setCancelable(false)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									String newName = mCurrentDir.getPath()
											+ "/"
											+ editText.getText().toString();
									boolean result = Utility.rename(
											mListFiles.get(info.position),
											newName);
									if (result) {
										mListFiles.remove(info.position);
										mListFiles.add(info.position, new File(
												newName));
										mFilesListAdapter
												.notifyDataSetChanged();
									} else {
										Toast.makeText(
												getBaseContext(),
												getString(R.string.cant_rename),
												Toast.LENGTH_SHORT).show();
									}
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		} else if (menuItemIndex == 1) {
			if (mCutFilesList == null) { // cut
				mCutFilesList = new ArrayList<File>();
				mCutFilesList.add(mListFiles.get(info.position));
			} else { // paste
				Utility.move(mCutFilesList, mListFiles.get(info.position));
				mCutFilesList = null;
			}
		}
		return true;
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
		mFilesListAdapter = new FileListAdapter(MyPhoneActivity.this,
				R.layout.listitem, mListFiles);
		mListView.setAdapter(mFilesListAdapter);
		mTvCurrentDir.setText(mCurrentDir.getAbsolutePath());
		onMenuEditMode(null);
	}

	@SuppressWarnings("unused")
	@Deprecated
	private ArrayList<File> getCheckedFilesList() {
		return mCheckedFilesList;
	}

	public void addToCheckedFilesList(File file) {
		mCheckedFilesList.add(file);
	}

	public void removeFromCheckedFilesList(File file) {
		if (mCutFilesList != null) {
			mCutFilesList.remove(file);
		}
		if (mCheckedFilesList != null) {
			mCheckedFilesList.remove(file);
		}
	}

	public void setCheckedFilesList(ArrayList<File> checkedFilesList) {
		this.mCheckedFilesList = checkedFilesList;
	}

	public boolean isCut() {
		return (mCutFilesList != null && mCutFilesList.size() > 0);
	}

	BroadcastReceiver deviceAtatchReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "ACTION=" + intent.getAction());
			Toast.makeText(context, intent.getAction(), Toast.LENGTH_SHORT)
					.show();
			UsbManager manager = (UsbManager) getSystemService(USB_SERVICE);
			HashMap<String, UsbDevice> devicemap = manager.getDeviceList();
			for (Iterator<String> iterator = devicemap.keySet().iterator(); iterator
					.hasNext();) {
				String type = (String) iterator.next();
				UsbDevice device = devicemap.get(type);
				Log.v(TAG, device.getDeviceName());
				Log.v(TAG, "productId=" + device.getProductId());
				Log.v(TAG, "vendorId=" + device.getVendorId());
			}
			updateNewDir(mCurrentDir);
		}

	};

	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(deviceAtatchReceiver);
	};
	
	public void onMenuEditMode(View view) {
		if (this.mMenu == null) {
			return;
		} else {
			if (mCheckedFilesList.size() > 0) {
				this.mMenu.clear();
				MenuInflater menuInflater = getMenuInflater();
				menuInflater.inflate(R.menu.edit_select_menu, this.mMenu);
			} else if (mCopyFilesList.size() > 0 || mCutFilesList.size() > 0) {
				//何もしない
			} else {
				this.mMenu.clear();
				MenuInflater menuInflater = getMenuInflater();
				menuInflater.inflate(R.menu.main_menu, this.mMenu);
			}
		}
	}
	
	private void changeToMenuCopy() {
		this.mMenu.clear();
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.edit_copy_menu, this.mMenu);
	}
	
	private void changeToMenuCut() {
		this.mMenu.clear();
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.edit_move_menu, this.mMenu);
	}
	
	private void changeToMenuMain() {
		this.mMenu.clear();
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, this.mMenu);
	}
}