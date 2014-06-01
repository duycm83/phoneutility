package jp.spidernet.myphone;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class SearchResultActivity extends MainActivity implements
		SearchView.OnQueryTextListener {
	private static final String TAG = "SearchResultActivity";
	private SearchView mSearchView;
	private TextView mStatusView;
	private RadioGroup mRadioGroup = null;
	private MyFilenameFilter.TYPE mSearchType = MyFilenameFilter.TYPE.NAME;
	public static boolean isFirstLoad = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		mStatusView = (TextView) findViewById(R.id.status_text);
		mListView = (ListView) findViewById(R.id.listView);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isFirstLoad) {
			updateNewDir(null);
			isFirstLoad = false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_result, menu);
		MenuItem searchItem = menu.findItem(R.id.search);
		mSearchView = (SearchView) searchItem.getActionView();
		setupSearchView(searchItem);
		// mSearchView.onActionViewExpanded();
		return true;
	}

	private void updateSearchResult() {
		Utility.sortFilesList(mListFiles);

		mFilesListAdapter = new FileListAdapter(SearchResultActivity.this,
				R.layout.listitem, mListFiles);

		mListView.setAdapter(mFilesListAdapter);
	}

	private void setupSearchView(MenuItem searchItem) {
		if (isAlwaysExpanded()) {
			mSearchView.setIconifiedByDefault(false);
		} else {
			searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
					| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		}

		// SearchManager searchManager = (SearchManager)
		// getSystemService(Context.SEARCH_SERVICE);
		// if (searchManager != null) {
		// List<SearchableInfo> searchables = searchManager
		// .getSearchablesInGlobalSearch();
		//
		// SearchableInfo info = searchManager
		// .getSearchableInfo(getComponentName());
		// for (SearchableInfo inf : searchables) {
		// if (inf.getSuggestAuthority() != null
		// && inf.getSuggestAuthority().startsWith("applications")) {
		// info = inf;
		// }
		// }
		// mSearchView.setSearchableInfo(info);
		// }

		mSearchView.setOnQueryTextListener(this);
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		String searchText = getString(R.string.action_search, newText);
		mStatusView.setText(searchText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		mListFiles.clear();
		if (mSearchType != MyFilenameFilter.TYPE.SIZE_GREATER
				&& mSearchType != MyFilenameFilter.TYPE.SIZE_SMALLER) {
			searchAction(SDDIR, mListFiles, mSearchType, query);
		} else {
			try {
				int searchSize = Integer.parseInt(query);
				searchActionSize(SDDIR, mListFiles, mSearchType, searchSize);
			} catch (Exception e) {
				Toast.makeText(this, "A number not is inputed",
						Toast.LENGTH_SHORT).show();
			}
		}
		int resultSize = mListFiles.size();
		String searchText = getString(R.string.action_search_result, resultSize);
		if (mListFiles.size() > 0) {
			updateSearchResult();
		} else {
			Log.v(TAG, "not found");
		}
		mStatusView.setText(searchText);
		return true;
	}

	public boolean onClose() {
		mStatusView.setText("Closed!");
		return false;
	}

	public boolean isAlwaysExpanded() {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (R.id.search_options == id) {
			showDialog(0);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.search_option, null);
		mRadioGroup = (RadioGroup) view.findViewById(R.id.groupRadioButton);
		builder.setView(view);
		// Add the buttons
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
						int checkedId = mRadioGroup.getCheckedRadioButtonId();
						switch (checkedId) {
						case R.id.search_name:
							mSearchType = MyFilenameFilter.TYPE.NAME;
							break;
						case R.id.search_ext:
							mSearchType = MyFilenameFilter.TYPE.EXT;
							break;
						case R.id.search_sizelarger:
							mSearchType = MyFilenameFilter.TYPE.SIZE_GREATER;
							break;
						case R.id.search_sizesmallter:
							mSearchType = MyFilenameFilter.TYPE.SIZE_SMALLER;
							break;

						default:
							break;
						}
					}
				});
		// builder.setNegativeButton(R.string.cancel, new
		// DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int id) {
		// // User cancelled the dialog
		// }
		// });

		Dialog dialog = builder.create();
		dialog.setTitle("Search options");
		return dialog;
	}

	private void searchAction(File target, ArrayList<File> result,
			MyFilenameFilter.TYPE type, String keyword) {
		File[] nextFolders = target.listFiles(new MyFilenameFilter(
				MyFilenameFilter.TYPE.FOLDER, null));
		File[] matchedFiles = target.listFiles(new MyFilenameFilter(type,
				keyword));
		if (matchedFiles != null) {
			for (File file : matchedFiles) {
				result.add(file);
			}
		} else {
			Log.v(TAG, "file not matchs");
		}

		if (nextFolders != null) {
			for (File file : nextFolders) {
				searchAction(file, result, type, keyword);
			}
		} else {
			Log.v(TAG, "Next folders not found");
		}
	}

	private void searchActionSize(File target, ArrayList<File> result,
			MyFilenameFilter.TYPE type, int size) {
		File[] nextFolders = target.listFiles(new MyFilenameFilter(
				MyFilenameFilter.TYPE.FOLDER, null));
		File[] matchedFiles = target
				.listFiles(new MyFilenameFilter(type, size));
		for (File file : matchedFiles) {
			result.add(file);
		}
		for (File file : nextFolders) {
			searchActionSize(file, result, type, size);
		}
	}

	@Override
	protected void onDestroy() {
		isFirstLoad = true;
		super.onDestroy();
	}
}
