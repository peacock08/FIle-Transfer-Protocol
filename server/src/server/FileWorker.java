
package server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileWorker {
    public static String URL_FOLDER = "D:\\Tai lieu\\Lap trinh mang\\storefile";

	String[] getAllFileName() {
            File file = new File(URL_FOLDER);
            String[] files = file.list();
            return files;
	}

    String[] searchFile(String keyword) {
        File file = new File(URL_FOLDER);
	String[] files = file.list();
	ArrayList<String> fileSearches = new ArrayList<String>();
	for (String fileItem : files)
            if (fileItem.contains(keyword))
		fileSearches.add(fileItem);
        for (int i = 0; i < fileSearches.size(); i++)
            System.out.println("File searches : " + fileSearches.get(i));
//      if (fileSearches.isEmpty())
//          return null;

	String[] result = new String[fileSearches.size()];
	result = fileSearches.toArray(result);
        return result;
    }

    public boolean checkFile(String fileNameReceived) {
	File file = new File(URL_FOLDER);
	String[] files = file.list();
	for (String fileItem : files)
            if (fileItem.equals(fileNameReceived))
		return false;
        return true;
    }

    public String getFileName(String str) {
	String result = "";
	int len = str.length();
	for (int i = len - 1; i > 0; i--)
            if (str.charAt(i) == '\\')
		return (new StringBuilder(result)).reverse().toString();
	else
		result += str.charAt(i);
        
	return null;
    }
}
