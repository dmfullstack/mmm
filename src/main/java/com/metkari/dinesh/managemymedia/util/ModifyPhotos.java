package com.metkari.dinesh.managemymedia.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import org.xml.sax.SAXException;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.PhotosetsInterface;

public class ModifyPhotos {
	private final Flickr flickr;
	private String nsid;
	Photosets photoSets;
	PhotosInterface photoInt;
	PhotosetsInterface photosetInt;

	public ModifyPhotos() throws FlickrException, IOException, SAXException {
		AuthManager am = new AuthManager();
		flickr = am.getFlickr();
		nsid = am.getNsId();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configFile = args[0];
		ModifyPhotos mp;
		try {
			mp = new ModifyPhotos();
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
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void StartModifyAlbumNames(String setId1,String albumName1,String photoId1,String photoName1,String dateTaken1) throws FileNotFoundException, IOException, FlickrException, ParseException {
//		File f = new File(configFile);
//		if (!f.exists() || f.isDirectory()) {
//			System.out.println("Cannot open:" + configFile);
//			return;
//		}
		photoInt = flickr.getPhotosInterface();
		photosetInt = flickr.getPhotosetsInterface();
		photoSets = photosetInt.getList(this.nsid);
		
		//try (BufferedReader br = new BufferedReader(new FileReader(f))) {
			String line;
			//while ((line = br.readLine()) != null) {
				//System.out.println("Processing:" + line);
				////String[] command = line.split(":");
				//if (command.length != 5) {
				//	System.out.println("Line should contain 5 fields: " + line);
				//	return;
				//}
				String setId = setId1;
				String albumName = albumName1;
				String photoId = photoId1;
				String photoName = photoName1;
				String dateTaken = dateTaken1;
				DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date dateTakenInDate = df.parse(dateTaken);

				Photo p = photoInt.getPhoto(photoId);
				Photoset ps = photosetInt.getInfo(setId);
				/// DINESH
				String oldAlbumName = albumName;
				String setName = albumName;
				String fileYearMonth = "";
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");
					String dateTaken2 = sdf.format(dateTakenInDate);
					dateTaken2 = dateTaken2.replaceAll("/", "");
					fileYearMonth = dateTaken2;
					//System.out.println("TAG_DATETIME: Photo taken metadata found:" + fileYearMonth);
				} catch (Exception ex) {
					System.out.println("TAG_DATETIME: Date conversion excepton:" + ex.getMessage());
				}

				// setName=getPhotoTakenMetadata(new File(arr[index].getPath()));

				setName = getPhotoClickTimeFromFileName(photoName);

				if (setName.trim().equalsIgnoreCase("")) {
					setName = fileYearMonth.trim();
				}
				/*
				 * if (setName.trim().equalsIgnoreCase("")){
				 * 
				 * setName = getCreationTime(new File(arr[index].getPath())); }
				 */

				if (setName.trim().equalsIgnoreCase("")) {

					setName = albumName.trim();
				}
				// logger.info(albumName);
				/*
				 * if (setName != null && !setName.trim().equalsIgnoreCase("")) {
				 * 
				 * bf.getSetPhotos(setName); }
				 */

				UpdateAlbum(oldAlbumName, setName, ps, p);
				UpdatePhoto(photoName, dateTakenInDate, p);

			//}
		//}
	}
	
	private void Start(String configFile) throws FileNotFoundException, IOException, FlickrException, ParseException {
		File f = new File(configFile);
		if (!f.exists() || f.isDirectory()) {
			System.out.println("Cannot open:" + configFile);
			return;
		}
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
				String photoId = command[2];
				String photoName = command[3];
				String dateTaken = command[4];
				DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
				Date dateTakenInDate = df.parse(dateTaken);

				Photo p = photoInt.getPhoto(photoId);
				Photoset ps = photosetInt.getInfo(setId);
				/// DINESH
				String oldAlbumName = albumName;
				String setName = albumName;
				String fileYearMonth = "";
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");
					String dateTaken1 = sdf.format(dateTakenInDate);
					dateTaken1 = dateTaken1.replaceAll("/", "");
					fileYearMonth = dateTaken1;
					System.out.println("TAG_DATETIME: Photo taken metadata found:" + fileYearMonth);
				} catch (Exception ex) {
					System.out.println("TAG_DATETIME: Date conversion excepton:" + ex.getMessage());
				}

				// setName=getPhotoTakenMetadata(new File(arr[index].getPath()));

				setName = getPhotoClickTimeFromFileName(photoName);

				if (setName.trim().equalsIgnoreCase("")) {
					setName = fileYearMonth.trim();
				}
				/*
				 * if (setName.trim().equalsIgnoreCase("")){
				 * 
				 * setName = getCreationTime(new File(arr[index].getPath())); }
				 */

				if (setName.trim().equalsIgnoreCase("")) {

					setName = albumName.trim();
				}
				// logger.info(albumName);
				/*
				 * if (setName != null && !setName.trim().equalsIgnoreCase("")) {
				 * 
				 * bf.getSetPhotos(setName); }
				 */

				UpdateAlbum(oldAlbumName, setName, ps, p);
				UpdatePhoto(photoName, dateTakenInDate, p);

			}
		}
	}

	private void UpdatePhoto(String photoName, Date dateTaken, Photo p) throws FlickrException {

		if (!p.getTitle().equals(photoName))
			photoInt.setMeta(p.getId(), photoName, p.getDescription());
		if (!p.getDateTaken().equals(dateTaken))
			photoInt.setDates(p.getId(), null, dateTaken, null);
	}

	private void UpdateAlbum(String oldAlbumName, String newAlbumName, Photoset ps, Photo p) throws FlickrException {
		Photoset oldPhotoSet = ps;
		System.out.print("	Photo URL: " + p.getUrl());
		boolean newAlbumFound = false;

		if (ps == null) {
			// Ideally i should throw an error
			ps = photosetInt.create(newAlbumName, "", p.getId());
			photoSets = photosetInt.getList(this.nsid);
		}

		// Looks like the photo has to be moved
		// photosetInt.removePhoto(ps.getId(), p.getId());
		ps = null;
		Iterator<Photoset> sets = photoSets.getPhotosets().iterator();
		// Find the destination Album
		while (sets.hasNext()) {
			Photoset set = (Photoset) sets.next();
			if (set.getTitle().equals(newAlbumName)) {
				//System.out.println("New album name already found:" + newAlbumName);
				// Yepee we found it
				ps = set;
				newAlbumFound = true;
				break;
			}
		}
		if (!newAlbumFound) {
			ps = photosetInt.create(newAlbumName, "", p.getId());
			photoSets = photosetInt.getList(this.nsid);
			System.out.println("newAlbumName"+newAlbumName);

		} else {
			try {
				photosetInt.addPhoto(ps.getId(), p.getId());
				photoSets = photosetInt.getList(this.nsid);
				System.out.print("	NewAlbumName:"+newAlbumName);
			} catch (Exception e) {
				System.out.print("	"+e.getMessage());
			}
		}
		if (oldAlbumName.trim().equalsIgnoreCase("Auto Upload") || oldAlbumName.trim().equalsIgnoreCase("Auto Upload1")  || oldAlbumName.trim().equalsIgnoreCase("2018121")) {
			try {
				photosetInt.removePhoto(oldPhotoSet.getId(), p.getId());
				System.out.print("	Deleted from Auto Upload");
			} catch (Exception e) {
				System.out.print("	"+e.getMessage());
			}
		}

		/*
		 * //New album is not found if (ps == null) { // Create new album with requested
		 * name ps = photosetInt.create(newAlbumName, "", p.getId()); photoSets =
		 * photosetInt.getList(this.nsid); } else { photosetInt.addPhoto(ps.getId(),
		 * p.getId()); }
		 */

	}

	//////// Dinesh
/*	public static String getPhotoTakenMetadata(File file) {

		String fileYearMonth = "";
		try {

			Metadata imageMetadata = ImageMetadataReader.readMetadata(file);

			// Read Exif Data
			// Directory directory = imageMetadata.getDirectory( ExifDirectory.class );
			Directory directory = imageMetadata.getFirstDirectoryOfType(ExifDirectory.class);
			if (directory != null) {
				// Read the date
				Date date = directory.getDate(ExifDirectory.TAG_DATETIME);
				// logger.info( "Date Taken: " + date );

				try {
					SimpleDateFormat df = new SimpleDateFormat("yyyy/MM");
					String dateTaken = df.format(date);
					dateTaken = dateTaken.replaceAll("/", "");
					fileYearMonth = dateTaken;
					//System.out.println("TAG_DATETIME: Photo taken metadata found:" + fileYearMonth);
				} catch (Exception ex) {
					System.out.println("TAG_DATETIME: Date conversion excepton:" + ex.getMessage());
				}

			}

			if (fileYearMonth.trim().equalsIgnoreCase("")) {

				if (directory != null) {
					// Read the date
					Date date = directory.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL);
					// logger.info( "Date Taken: " + date );

					try {
						SimpleDateFormat df = new SimpleDateFormat("yyyy/MM");
						String dateTaken = df.format(date);
						dateTaken = dateTaken.replaceAll("/", "");
						fileYearMonth = dateTaken;
						//System.out.println("TAG_DATETIME_ORIGINAL: Photo taken metadata found:" + fileYearMonth);
					} catch (Exception ex) {
						System.out.println("TAG_DATETIME_ORIGINAL: Date conversion excepton:" + ex.getMessage());
					}

				}
			}

		} catch (Exception e) {
			// logger.debug("Photo Taken metadata exception:"+ e.getMessage());
			e.printStackTrace();
		}
		return fileYearMonth.trim();

	}
*/
	public static String getPhotoClickTimeFromFileName(String fileName) throws IOException {
		fileName = fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.length()).trim();
		try {
			fileName = fileName.substring(0, fileName.lastIndexOf(".")).trim();
		} catch (Exception e) {
			//System.out.println("Filename without extension:" + e.getMessage());
		}

		fileName = fileName.replace("-", "");
		fileName = fileName.replace("_", "");
		String albumName = "";
		String dateString = "";
		int yearIndex = fileName.indexOf("20");

		try {

			if (yearIndex != -1 && fileName.length() >= yearIndex + 6) {

				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				int photoYear = new Integer(fileName.substring(yearIndex, yearIndex + 4));
				dateString = fileName.substring(yearIndex, yearIndex + 6);
				int photoMonth = new Integer(dateString.substring(4, dateString.length()));
				if (photoYear <= currentYear && photoMonth <= 12 && photoMonth > 0) {

					if (isNumber(dateString)) {
						albumName = dateString;
						//System.out.println("TAG_getPhotoClickTimeFromFileName: :" + albumName);
					}
				}
			}
		} catch (NumberFormatException nfe) {
			System.out.println("getPhotoClickTimeFromFileName: NumberFormException:" + nfe.getMessage());
		}
		return albumName.trim();
	}

	public static boolean isNumber(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (!Character.isDigit(string.charAt(i))) {
				return false;
			}
		}

		return true;
	}

}
