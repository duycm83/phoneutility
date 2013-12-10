package jp.spidernet.myphone.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.util.Log;

public class FileUtils {
    private static final String TAG = "FileUtils";

	/**
     * By default File#delete fails for non-empty directories, it works like "rm". 
     * We need something a little more brutual - this does the equivalent of "rm -r"
     * @param path Root File Path
     * @return true iff the file and all sub files/directories have been removed
     * @throws FileNotFoundException
     */
    public static boolean deleteRecursive(File path) throws FileNotFoundException{
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && FileUtils.deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }
    
    /**
     * コピー元のパス[srcPath]から、コピー先のパス[destPath]へ
     * ファイルのコピーを行います。
     * コピー処理にはFileChannel#transferToメソッドを利用します。
     * 尚、コピー処理終了後、入力・出力のチャネルをクローズします。
     * @param srcPath    コピー元のパス
     * @param destPath    コピー先のパス
     * @throws IOException    何らかの入出力処理例外が発生した場合
     */
    public static void copyTransfer(String srcPath, String destPath) 
        throws IOException {
    	FileInputStream inputStream = new FileInputStream(srcPath);
        FileChannel srcChannel = inputStream.getChannel();
        FileOutputStream outputStream = new FileOutputStream(destPath);
        FileChannel destChannel = outputStream.getChannel();
        try {
            srcChannel.transferTo(0, srcChannel.size(), destChannel);
        } finally {
            srcChannel.close();
            destChannel.close();
            inputStream.close();
            outputStream.close();
        }

    }

	public static void copyDirectory(File fromFile, File toDir) {
		try {
			copyTransfer(fromFile.getAbsolutePath(), toDir.getAbsolutePath());
		} catch (IOException e) {
			Log.e(TAG, "copyDirectory FAILED", e);
		}
	}
	
	/**
	 * 
	 * @param file  File (or directory) to be moved
	 * @param directoryname Move file to new directory
	 * @return
	 */
	public static boolean move(File fromFile, File toDir) {
		boolean result = fromFile.renameTo(new File(toDir.getPath(), fromFile.getName()));
		return result;
	}

}
