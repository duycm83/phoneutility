package jp.spidernet.myphone;

import java.util.List;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchResultActivity extends Activity implements
		SearchView.OnQueryTextListener {
	private SearchView mSearchView;
	private TextView mStatusView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);
		mStatusView = (TextView) findViewById(R.id.status_text);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search_result, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		mSearchView = (SearchView) searchItem.getActionView();

		setupSearchView(searchItem);
		// mSearchView.onActionViewExpanded();
		return true;
	}

	private void setupSearchView(MenuItem searchItem) {

		if (isAlwaysExpanded()) {
			mSearchView.setIconifiedByDefault(false);
		} else {
			searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM
					| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		}

		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		if (searchManager != null) {
			List<SearchableInfo> searchables = searchManager
					.getSearchablesInGlobalSearch();

			SearchableInfo info = searchManager
					.getSearchableInfo(getComponentName());
			for (SearchableInfo inf : searchables) {
				if (inf.getSuggestAuthority() != null
						&& inf.getSuggestAuthority().startsWith("applications")) {
					info = inf;
				}
			}
			mSearchView.setSearchableInfo(info);
		}

		mSearchView.setOnQueryTextListener(this);
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		mStatusView.setText("Query = " + newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		mStatusView.setText("Query = " + query + " : submitted");
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
		return super.onOptionsItemSelected(item);
	}

}
