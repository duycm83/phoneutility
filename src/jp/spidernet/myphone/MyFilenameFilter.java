package jp.spidernet.myphone;

import java.io.File;
import java.io.FilenameFilter;


public class MyFilenameFilter implements FilenameFilter {
	public static enum TYPE {
		NAME, EXT, SIZE_SMALLER, SIZE_GREATER, FOLDER
	};

	private String searchName = null;
	private int searchSize = 0;
	private TYPE searchType = TYPE.NAME;

	public MyFilenameFilter(String searchName) {
		this.searchName = searchName;
	}

	public MyFilenameFilter(TYPE type, String searchName) {
		this.searchName = searchName;
		this.searchType = type;
	}
	
	public MyFilenameFilter(TYPE type, int searchSize) {
		this.searchSize = searchSize;
		this.searchType = type;
	}

	@Override
	public boolean accept(File dir, String filename) {
		File file = new File(dir, filename);
		boolean result = false;
		switch (searchType) {
		case NAME:
			if (file.isFile()) {
				result = filename.contains(this.searchName);
			}
			break;
		case EXT:
			if (file.isFile()) {
				result = filename.endsWith(searchName);
			}
			break;
		case SIZE_GREATER:
			if (file.isFile()) {
				result = file.length() >= searchSize;
			}
			file = null;
			break;
		case SIZE_SMALLER:
			if (file.isFile()) {
				result = file.length() <= searchSize;
			}
			break;
		case FOLDER:
			result = file.isDirectory();
			break;
		default:
			break;
		}
		file = null;
		return result;
	}
}
