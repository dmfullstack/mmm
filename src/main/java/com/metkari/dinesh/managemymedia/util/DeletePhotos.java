package com.metkari.dinesh.managemymedia.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import org.xml.sax.SAXException;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;

public class DeletePhotos {
	private final Flickr flickr;

	public DeletePhotos() throws FlickrException, IOException, SAXException {
		AuthManager am = new AuthManager();
		flickr = am.getFlickr();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = args[0];
		DeletePhotos mp;
		try {
			mp = new DeletePhotos();
			mp.Start(configFile);
		} catch (FlickrException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void Start(String configFile) throws FileNotFoundException,
			IOException, FlickrException {
		File f = new File(configFile);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Cannot open:" + configFile);
			return;
		}
		PhotosInterface photoInt = flickr.getPhotosInterface();
		PhotosetsInterface pi = flickr.getPhotosetsInterface();
		PrintWriter deletedPics = new PrintWriter("DeletedPics.log", "UTF-8"); 

		try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			while ((line = br.readLine()) != null) {
				System.out.println("Processing:" + line);
				String[] command = line.split(":");
				if (command.length != 5) {
					System.out.println("Line should contain 5 fields: " + line);
					return;
				}
				String setId = command[0];
				String albumName = command[1];
				String photoId = command[2];
				String photoName = command[3];
//				String dateTaken = command[4];

				Photo p = photoInt.getPhoto(photoId);

				Photoset ps = pi.getInfo(setId);
				if (ps == null) {
					System.out.println("SKIP: " + line
							+ " Reason: SetId not found");
				} else {
					pi.removePhoto(ps.getId(), p.getId());
					deletedPics.write("Deleted:" + photoName + " from Album:" + albumName);
				}
			}
			deletedPics.close();
		}
	}
}
