package com.metkari.dinesh.managemymedia.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.xml.sax.SAXException;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;

public class DownloadPhotos {
	private final Flickr flickr;
	private static String baseDirectory = null;
	private static PrintWriter logFile = null;

	public DownloadPhotos() throws FlickrException, IOException, SAXException {
		AuthManager am = new AuthManager();
		flickr = am.getFlickr();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = args[0];
		if (args.length > 1)
			baseDirectory = args[1];
		DownloadPhotos mp;
		try {
			logFile = new PrintWriter("DownloadPics"+configFile+".log", "UTF-8");
			mp = new DownloadPhotos();
			mp.Start(configFile);
		} catch (FlickrException e) {
			e.printStackTrace(logFile);
		} catch (IOException e) {
			e.printStackTrace(logFile);
		} catch (SAXException e) {
			e.printStackTrace(logFile);
		} finally {
			if (logFile != null)
			{
				logFile.println("------ END --------");
				logFile.flush();
				logFile.close();
			}
		}
	}

	private void Start(String configFile) throws FileNotFoundException, IOException, FlickrException {
		File f = new File(configFile);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Cannot open:" + configFile);
			return;
		}

		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				logFile.println("Processing:" + line);
				String[] command = line.split(":");
				if (command.length != 5) {
					System.out.println("Line should contain 5 fields: " + line);
					return;
				}
				String setId = command[0];
				String albumName = command[1];
				String photoId = command[2];
				String photoName = command[3];

				DownloadPhoto(photoId, photoName, albumName);
				logFile.flush();
			}
		}
	}

	private void DownloadPhoto(String photoId, String photoName, String albumName) throws FlickrException, IOException {
		String directoryName = new String(baseDirectory+"\\" + albumName).replace('\\', '/');
		File setDirectory = new File(directoryName);
		if (!setDirectory.exists()) {
			setDirectory.mkdirs();
		}
//
		PhotosInterface photoInt = flickr.getPhotosInterface();
		Photo p = photoInt.getPhoto(photoId);
		String filename = photoName;
		filename = filename.substring(filename.lastIndexOf("/") + 1, filename.length());
		logFile.println("Now writing " + filename + " to " + setDirectory.getAbsolutePath());
		BufferedInputStream inStream = new BufferedInputStream(photoInt.getImageAsStream(p, Size.THUMB));
		//InputStream inStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
		File newFile = new File(setDirectory, filename);

		if(!newFile.exists())
		{
			FileOutputStream fos = new FileOutputStream(newFile);
			int read;
			while ((read = inStream.read()) != -1) {
				fos.write(read);
			}
			fos.flush();
			fos.close();
		}
		inStream.close();
	}
}