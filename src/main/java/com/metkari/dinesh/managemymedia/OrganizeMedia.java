package com.metkari.dinesh.managemymedia;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Extras;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
import com.metkari.dinesh.managemymedia.util.AuthManager;
import com.metkari.dinesh.managemymedia.util.ModifyPhotos;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 
 * 
 * @author dineshmetkari
 *
 */

public class OrganizeMedia {
	private final Flickr flickr;
	private static int MAX_COUNT = 1000;
	private String nsid;
	private boolean exitFlag=false;
	public OrganizeMedia() throws FlickrException, IOException, SAXException {
		AuthManager am = new AuthManager();
		flickr = am.getFlickr();
		nsid = am.getNsId();
	}

	public void getPhotoList(int startCount, int maxCount, int recordsUploaded) throws Exception {
		int photocount = 1;
		ModifyPhotos mp = new ModifyPhotos();

		PrintWriter dumpText = new PrintWriter("PictureList.Flickr.txt", "UTF-8");

		PhotosetsInterface pi = flickr.getPhotosetsInterface();

		Iterator<Photoset> sets = pi.getList(this.nsid).getPhotosets().iterator();
		// ExecutorService pool = Executors.newFixedThreadPool(1);

		while (sets.hasNext() && !this.exitFlag) {

			Photoset set = (Photoset) sets.next();
			System.out.println("Processing Album:" + set.getTitle());
			DownloadSetDetails(startCount, maxCount, recordsUploaded, set, pi, dumpText, mp, photocount);
			// pool.submit(() -> {
			// try {
			// DownloadSetDetails(set,pi,dumpText);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// });
		}
		// pool.wait();
		dumpText.close();
		System.out.println("Processing:Done");

	}

	public void getPhotoList() throws Exception {
		int photocount = 1;
		ModifyPhotos mp = new ModifyPhotos();

		PrintWriter dumpText = new PrintWriter("PictureList.Flickr.txt", "UTF-8");

		PhotosetsInterface pi = flickr.getPhotosetsInterface();

		Iterator<Photoset> sets = pi.getList(this.nsid).getPhotosets().iterator();
		// ExecutorService pool = Executors.newFixedThreadPool(1);
		while (sets.hasNext()) {
			Photoset set = (Photoset) sets.next();
			System.out.println("Processing Album:" + set.getTitle());
			DownloadSetDetails(set, pi, dumpText, mp, photocount);
			// pool.submit(() -> {
			// try {
			// DownloadSetDetails(set,pi,dumpText);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// });
		}
		// pool.wait();
		dumpText.close();
		System.out.println("Processing:Done");

	}

	public void DownloadSetDetails(int startCount, int maxCount, int recordsUploaded, Photoset set,
			PhotosetsInterface pi, PrintWriter dumpText, ModifyPhotos mp, int photocount) throws Exception {

		int count = MAX_COUNT;
		int page = 1;
		Set<String> extras = new HashSet<String>();
		extras.add(Extras.DATE_TAKEN);
		PhotosetsInterface piT = flickr.getPhotosetsInterface();
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	
		while (count == MAX_COUNT && !this.exitFlag) {
			PhotoList<Photo> photos = piT.getPhotos(set.getId(), extras, Flickr.PRIVACY_LEVEL_NO_FILTER, MAX_COUNT,
					page);
			count = photos.size();
			page++;
			Iterator<Photo> i = photos.iterator();

			int ii = 0;
			while (i.hasNext() && ii < startCount) {
				i.next();
				System.out.println("Incredmenting count:" + ii);
				ii += 1;
			}

			while (i.hasNext() && startCount <= maxCount) {
				startCount = startCount + 1;
				System.out.println();
				System.out.print("StartIndex:" + startCount +"   "  );
				photocount += 1;
				Photo p = ((Photo) i.next());
				// dumpText.println(set.getId() + ":" + set.getTitle() + ":"
				// + p.getId() + ":" + p.getTitle() + ":"
				// + df.format(p.getDateTaken()) );
				mp.StartModifyAlbumNames(set.getId(), set.getTitle(), p.getId(), p.getTitle(),
						df.format(p.getDateTaken()));
				recordsUploaded = recordsUploaded + 1;
				
			}
			System.out.println("////////////////");
			System.out.println("Completed: StartCount:" + (startCount-1) + " MaxCount:" + maxCount);
			System.out.println("////////////////");
			this.exitFlag=true;
			
	

		}

	}

	public void DownloadSetDetails(Photoset set, PhotosetsInterface pi, PrintWriter dumpText, ModifyPhotos mp,
			int photocount) throws Exception {

		int count = MAX_COUNT;
		int page = 1;
		Set<String> extras = new HashSet<String>();
		extras.add(Extras.DATE_TAKEN);
		PhotosetsInterface piT = flickr.getPhotosetsInterface();
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		while (count == MAX_COUNT) {
			PhotoList<Photo> photos = piT.getPhotos(set.getId(), extras, Flickr.PRIVACY_LEVEL_NO_FILTER, MAX_COUNT,
					page);
			count = photos.size();
			page++;
			Iterator<Photo> i = photos.iterator();

			while (i.hasNext()) {
				System.out.println();
				System.out.print("Count:" + photocount + "   ");
				photocount += 1;
				Photo p = ((Photo) i.next());
				// dumpText.println(set.getId() + ":" + set.getTitle() + ":"
				// + p.getId() + ":" + p.getTitle() + ":"
				// + df.format(p.getDateTaken()) );
				mp.StartModifyAlbumNames(set.getId(), set.getTitle(), p.getId(), p.getTitle(),
						df.format(p.getDateTaken()));
			}

		}

	}

	private String makeSafeFilename(String input) {
		byte[] fname = input.getBytes();
		byte[] bad = new byte[] { '\\', '/', '"' };
		byte replace = '_';
		for (int i = 0; i < fname.length; i++) {
			for (byte element : bad) {
				if (fname[i] == element) {
					fname[i] = replace;
				}
			}
		}
		return new String(fname);
	}

	public static void main(String[] args) throws Exception {
		OrganizeMedia bf = new OrganizeMedia();
		bf.getPhotoList();
	}
}