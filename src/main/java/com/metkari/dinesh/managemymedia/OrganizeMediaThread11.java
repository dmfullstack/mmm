package com.metkari.dinesh.managemymedia;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OrganizeMediaThread11 extends Thread {


	static Thread current;

	public static Properties prop;
	public static FileInputStream input;
	public static FileOutputStream output;
	public static String fileName = "uploadCounter.txt";
	public static int startCount = 0;
	public static int threadRepeatations=0;
	public static int maxCount = 0;
	public static int incrementcount = 0;
	public static int numberofthreads=0;

	public OrganizeMediaThread11() {

	}



	public int getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxCount = maxCount;
	}

	public int getStartCount() {
		return startCount;
	}

	public void setStartCount(int startCount) {
		this.startCount = startCount;
	}

	public int getRecordsUploaded() {
		return recordsUploaded;
	}

	public void setRecordsUploaded(int recordsUploaded) {
		this.recordsUploaded = recordsUploaded;
	}

	private int recordsUploaded;

	public OrganizeMediaThread11(int startCount, int maxCount, int recordsUploaded) {
		this.startCount = startCount;
		this.maxCount = maxCount;
		this.recordsUploaded = recordsUploaded;
	}

	@Override
	public void run() {

		try {
			new OrganizeMedia().getPhotoList(this.startCount, this.maxCount, this.recordsUploaded);
			;

		} catch (Exception e) {
			System.out.println("Exception:" + e.getMessage());
		}

	}

	public static void main(String[] args) throws Exception {
		// new OrganizeMediaThread11(0, 2, 0).start();
		// new OrganizeMediaThread11(3, 5, 0).start();
		//
		/*
		 * new OrganizeMediaThread11(0, 100, 0).start(); new OrganizeMediaThread11(200, 300,
		 * 0).start(); new OrganizeMediaThread11(300, 400, 0).start(); new
		 * OrganizeMediaThread11(400, 500, 0).start(); new OrganizeMediaThread11(500, 600,
		 * 0).start(); new OrganizeMediaThread11(600, 700, 0).start(); new
		 * OrganizeMediaThread11(700, 800, 0).start(); new OrganizeMediaThread11(800, 900,
		 * 0).start(); new OrganizeMediaThread11(900, 1000, 0).start();
		 */

		try {
			// logger.info("Before loadProperties: " + fileName);
			// recordsUploaded = getUploadCount(fileName);

			prop = loadProperties(fileName);

			if (prop != null) {
				if (prop.getProperty("ThreadRepeatationsOrganize").trim().length() != 0) {
					threadRepeatations = Integer.parseInt(prop.getProperty("ThreadRepeatationsOrganize").trim());
				} else {
					threadRepeatations = 5;
				}
				if (prop.getProperty("StartCountOrganize").trim().length() != 0) {
					startCount = Integer.parseInt(prop.getProperty("StartCountOrganize").trim());
				} else {
					startCount = 0;
				}
				
				if (prop.getProperty("IncrementCountOrganize").trim().length() != 0) {
					incrementcount = Integer.parseInt(prop.getProperty("IncrementCountOrganize").trim());
				} else {
					incrementcount = 100;
				}
				if (prop.getProperty("NumberOfThreadsOrganize").trim().length() != 0) {
					numberofthreads = Integer.parseInt(prop.getProperty("NumberOfThreadsOrganize").trim());
				} else {
					numberofthreads = 5;
				}
				
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		OrganizeMediaThread11 om = new OrganizeMediaThread11();
		for (int m = 0; m < threadRepeatations; m++) {

			om.startThreads();
			while (current.isAlive()) {
				System.out.println("Current thread current.isAlive(): TRUE:  Cycle:" + m);
				Thread.sleep(60000);
			}
			System.out.println("Current thread current.isAlive(): FALSE:  Cycle:" + m);
			System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@:");
		}

	}

	public void startThreads() {

		OrganizeMediaThread11[] myThreads; // New array of threads
		myThreads = new OrganizeMediaThread11[numberofthreads]; // Same size as our int array

		maxCount = startCount + incrementcount;
		int recordsUploaded = 0;

		for (int i = 0; i < numberofthreads; i++) {
			// OrganizeMediaThread11 wt = new
			// OrganizeMediaThread11(startCount,maxCount,recordsUploaded);
			// you don't need the second loop.
			myThreads[i] = new OrganizeMediaThread11(startCount, maxCount, recordsUploaded);
			myThreads[i].start(); // Spins up a new thread and runs your code
			startCount = startCount + incrementcount;
			maxCount = startCount + incrementcount;
			recordsUploaded = 0;
			current = myThreads[i];

		}

		/*
		 * for (int i = 0; i < argSize; i++) { current = myThreads[i]; try {
		 * current.join(); System.out.println("current.join() success"); } catch
		 * (InterruptedException e) { // TODO Auto-generated catch block
		 * System.out.println("current.join() failure"+e.getMessage()); } }
		 */
	}

	public static Properties loadProperties(String fileName) {

		Properties prop = null;
		InputStream input = null;

		try {
			// logger.info("Inside loadProperties:" + fileName);
			prop = new Properties();
			input = new FileInputStream(fileName.trim());
			// logger.info("Completed FileInputStream");
			// load a properties file
			prop.load(input);
			// logger.info("Completed prop.load");

		} catch (IOException ex) {
			// ex.printStackTrace();
			System.out.println(ex.getStackTrace().toString());
		} finally {
			if (input != null) {
				try {
					input.close();

				} catch (IOException e) {
					// e.printStackTrace();
					System.out.println(e.getStackTrace().toString());
				}
			}
		}
		return prop;

	}

}