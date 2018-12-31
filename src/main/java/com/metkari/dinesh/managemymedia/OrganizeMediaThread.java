package com.metkari.dinesh.managemymedia;

public class OrganizeMediaThread extends Thread {
	private int maxCount;
	private int i;
	static Thread current;

	public OrganizeMediaThread() {

	}

	public OrganizeMediaThread(int i) {
		this.i = i;
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

	private int startCount;
	private int recordsUploaded;

	public OrganizeMediaThread(int startCount, int maxCount, int recordsUploaded) {
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
		// new OrganizeMediaThread(0, 2, 0).start();
		// new OrganizeMediaThread(3, 5, 0).start();
		//
	/*	new OrganizeMediaThread(0, 100, 0).start();
		new OrganizeMediaThread(200, 300, 0).start();
		new OrganizeMediaThread(300, 400, 0).start();
		new OrganizeMediaThread(400, 500, 0).start();
		new OrganizeMediaThread(500, 600, 0).start();
		new OrganizeMediaThread(600, 700, 0).start();
		new OrganizeMediaThread(700, 800, 0).start();
		new OrganizeMediaThread(800, 900, 0).start();
		new OrganizeMediaThread(900, 1000, 0).start();*/
		
		OrganizeMediaThread om = new OrganizeMediaThread();
		for (int m = 0; m < 12; m++) {
		
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
		int argSize = 5;
		OrganizeMediaThread[] myThreads; // New array of threads
		myThreads = new OrganizeMediaThread[argSize]; // Same size as our int array
		int incrementcount=100;
		int startCount=0;
		int maxCount=startCount+incrementcount;
		int recordsUploaded=0;
		
		for (int i = 0; i < argSize; i++) {
			//OrganizeMediaThread wt = new OrganizeMediaThread(startCount,maxCount,recordsUploaded);
			// you don't need the second loop.
			myThreads[i] = new OrganizeMediaThread(startCount,maxCount,recordsUploaded);
			myThreads[i].start(); // Spins up a new thread and runs your code
			startCount=startCount+incrementcount;
			maxCount=startCount+incrementcount;
			recordsUploaded=0;
			current=myThreads[i];
			
		}

		/*for (int i = 0; i < argSize; i++) {
			current = myThreads[i];
			try {
				current.join();
				System.out.println("current.join() success");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("current.join() failure"+e.getMessage());
			}
		}*/
	}

}