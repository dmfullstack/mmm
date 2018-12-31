package com.metkari.dinesh.managemymedia.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.xml.sax.SAXException;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoSet;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.upload.UploadInterface;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.flickr4java.flickr.uploader.Uploader;

public class UploadPics {
	private final Flickr flickr;
	private static String baseDir = null;
	private String nsid;
	Photosets photoSets;
	PhotosInterface photoInt;
	PhotosetsInterface photosetInt;

	public UploadPics() throws FlickrException, IOException, SAXException {
		AuthManager am = new AuthManager();
		flickr = am.getFlickr();
		nsid = am.getNsId();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = args[0];
		baseDir = args[0];
		UploadPics mp;
		try {
			mp = new UploadPics();
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

	private void Start(String configFile) throws FileNotFoundException, IOException, FlickrException {
		File f = new File(configFile);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Cannot open:" + configFile);
			return;
		}
		PrintWriter uploadedPics = new PrintWriter("UploadedPics.log", "UTF-8");

		Uploader u = flickr.getUploader();
		photoInt = flickr.getPhotosInterface();
		photosetInt = flickr.getPhotosetsInterface();
		photoSets = photosetInt.getList(this.nsid);

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
				// String photoId = command[2];
				String photoName = command[3];
				// String dateTaken = command[4];
				File uploadFile = new File(baseDir +"/"+ albumName + "/" + photoName);
				uploadedPics.println("Uploading:" + uploadFile.getAbsolutePath());
				uploadedPics.flush();
				if (uploadFile.exists() && uploadFile.isFile()) {
					UploadMetaData metaData = new UploadMetaData();
					metaData.setTitle(photoName);
					metaData.setPublicFlag(false);
					metaData.setAsync(false);
					String photoId = u.upload(uploadFile, metaData);
					uploadedPics.println("Ret:" + photoId);
					AddToAlbum(albumName, photoId);
				} else {
					System.out.println("Unable to load image: " + line);
				}
			}
		} finally {
			uploadedPics.close();
		}
	}

	private void AddToAlbum(String newAlbumName, String photoId) throws FlickrException {

		Photoset ps = null;
		Iterator<Photoset> sets = photoSets.getPhotosets().iterator();
		// Find the destination Album
		while (sets.hasNext()) {
			Photoset set = (Photoset) sets.next();
			if (set.getTitle().equals(newAlbumName)) {
				// Yepee we found it
				ps = set;
				break;
			}
		}

		if (ps == null) {
			// If new set, create the album
			ps = photosetInt.create(newAlbumName, "", photoId);
			photoSets = photosetInt.getList(this.nsid);
		} else {
			photosetInt.addPhoto(ps.getId(), photoId);
		}

	}

}
