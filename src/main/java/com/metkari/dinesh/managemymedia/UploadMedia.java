/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.metkari.dinesh.managemymedia;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifDirectory;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
// import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
// import com.flickr4java.flickr.photos.Size;
import com.flickr4java.flickr.photosets.Photoset;
import com.flickr4java.flickr.photosets.Photosets;
import com.flickr4java.flickr.photosets.PhotosetsInterface;
// import com.flickr4java.flickr.util.IOUtilities;
import com.flickr4java.flickr.prefs.PrefsInterface;
// import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.flickr4java.flickr.uploader.Uploader;
import com.flickr4java.flickr.util.AuthStore;
import com.flickr4java.flickr.util.FileAuthStore;


import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
// import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
// import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
// import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
// import java.util.Map;
import java.util.Scanner;
// import java.io.ByteArrayOutputStream;
//import java.io.File;
// import java.io.FileInputStream;
// import java.io.IOException;
// import java.io.InputStream;
import java.util.Set;

// import com.flickr4java.flickr.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple program to upload photos to a set. It checks for files already uploaded assuming the title is not changed so that it can be rerun if partial upload
 * is done. It uses the tag field to store the filename as OrigFileName to be used while downloading if the title has been changed. If setup.properties is not
 * available, pass the apiKey and secret as arguments to the program.
 * 
 * This sample also uses the AuthStore interface, so users will only be asked to authorize on the first run.
 * 
 * Please NOTE that this needs Java 7 to work. Java 7 was released on July 28, 2011 and soon Java 6 may not be supported anymore ( Jul 2014).
 * 
 * @author Dinesh Metkari
 */

public class UploadMedia {

    //private static final Logger logger = Logger.getLogger(UploadPhoto.class);
    private static final Logger logger = LoggerFactory.getLogger(UploadMedia.class);

    private String nsid;

    private String username;

    // private final String sharedSecret;

    private final Flickr flickr;

    private AuthStore authStore;

    public boolean flickrDebug = true;

    private boolean setOrigFilenameTag = true;

    private boolean replaceSpaces = false;

    private int privacy = -1;

    HashMap<String, Photoset> allSetsMap = new HashMap<String, Photoset>();

    HashMap<String, ArrayList<String>> setNameToId = new HashMap<String, ArrayList<String>>();

    public static final SimpleDateFormat smp = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss a");
    public static int uploadcount=0;
    public static int recordsUploaded=0;
    
    public static int startCount=0;
    public static int maxCount=0;
    public static Properties prop;
    public static FileInputStream input;
    public static FileOutputStream output;
    
    public static String fileName="uploadCounter.txt";
    
    public UploadMedia(String apiKey, String nsid, String sharedSecret, File authsDir, String username) throws FlickrException {
        flickr = new Flickr(apiKey, sharedSecret, new REST());
        this.username = username;
        this.nsid = nsid;
        // this.sharedSecret = sharedSecret;

        if (authsDir != null) {
            this.authStore = new FileAuthStore(authsDir);
        }

        // If one of them is not filled in, find and populate it.
        if (username == null || username.equals(""))
            setUserName();
        if (nsid == null || nsid.equals(""))
            setNsid();

    }

    private void setUserName() throws FlickrException {
        if (nsid != null && !nsid.equals("")) {
            Auth auth = null;
            if (authStore != null) {
                auth = authStore.retrieve(nsid);
                if (auth != null) {
                    username = auth.getUser().getUsername();
                }
            }
            // For this to work: REST.java or PeopleInterface needs to change to pass apiKey
            // as the parameter to the call which is not authenticated.

            if (auth == null) {
                // Get nsid using flickr.people.findByUsername
                PeopleInterface peopleInterf = flickr.getPeopleInterface();
                User u = peopleInterf.getInfo(nsid);
                if (u != null) {
                    username = u.getUsername();
                }
            }
        }
    }

    /**
     * Check local saved copy first ??. If Auth by username is available, then we will not need to make the API call.
     * 
     * @throws FlickrException
     */

    private void setNsid() throws FlickrException {

        if (username != null && !username.equals("")) {
            Auth auth = null;
            if (authStore != null) {
                auth = authStore.retrieve(username); // assuming FileAuthStore is enhanced else need to
                // keep in user-level files.

                if (auth != null) {
                    nsid = auth.getUser().getId();
                }
            }
            if (auth != null)
                return;

            Auth[] allAuths = authStore.retrieveAll();
            for (int i = 0; i < allAuths.length; i++) {
                if (username.equals(allAuths[i].getUser().getUsername())) {
                    nsid = allAuths[i].getUser().getId();
                    return;
                }
            }

            // For this to work: REST.java or PeopleInterface needs to change to pass apiKey
            // as the parameter to the call which is not authenticated.

            // Get nsid using flickr.people.findByUsername
            PeopleInterface peopleInterf = flickr.getPeopleInterface();
            User u = peopleInterf.findByUsername(username);
            if (u != null) {
                nsid = u.getId();
            }
        }
    }

    private void authorize() throws IOException, SAXException, FlickrException {
        AuthInterface authInterface = flickr.getAuthInterface();
        Token accessToken = authInterface.getRequestToken();

        // Try with DELETE permission. At least need write permission for upload and add-to-set.
        String url = authInterface.getAuthorizationUrl(accessToken, Permission.DELETE);
        logger.info("Follow this URL to authorise yourself on Flickr");
        logger.debug("Follow this URL to authorise yourself on Flickr");
        logger.info(url);
        logger.debug("Paste in the token it gives you:");
        logger.info("Paste in the token it gives you:");
        
        System.out.println("Follow this URL to authorise yourself on Flickr");

        System.out.println(url);
        System.out.println("Paste in the token it gives you:");
     
        System.out.print(">>");

        Scanner scanner = new Scanner(System.in);
        String tokenKey = scanner.nextLine();
        
        
        /*String tokenKey = "";
        
        while(scanner.hasNextLine()){
        	tokenKey=scanner.nextLine();
        }
         */
        
        
        

        Token requestToken = authInterface.getAccessToken(accessToken, new Verifier(tokenKey));

        Auth auth = authInterface.checkToken(requestToken);
        RequestContext.getRequestContext().setAuth(auth);
        this.authStore.store(auth);
        scanner.close();
        logger.info("Thanks.  You probably will not have to do this every time. Auth saved for user: " + auth.getUser().getUsername() + " nsid is: "
                + auth.getUser().getId());
        logger.debug(" AuthToken: " + auth.getToken() + " tokenSecret: " + auth.getTokenSecret());
        logger.info(" AuthToken: " + auth.getToken() + " tokenSecret: " + auth.getTokenSecret());
        System.out.println("Thanks.  You probably will not have to do this every time. Auth saved for user: " + auth.getUser().getUsername() + " nsid is: "
                + auth.getUser().getId());
        System.out.println(" AuthToken: " + auth.getToken() + " tokenSecret: " + auth.getTokenSecret());
        System.out.println(" AuthToken: " + auth.getToken() + " tokenSecret: " + auth.getTokenSecret());

    }

    /**
     * If the Authtoken was already created in a separate program but not saved to file.
     * 
     * @param authToken
     * @param tokenSecret
     * @param username
     * @return
     * @throws IOException
     */
    private Auth constructAuth(String authToken, String tokenSecret, String username) throws IOException {

        Auth auth = new Auth();
        auth.setToken(authToken);
        auth.setTokenSecret(tokenSecret);

        // Prompt to ask what permission is needed: read, update or delete.
        auth.setPermission(Permission.fromString("delete"));

        User user = new User();
        // Later change the following 3. Either ask user to pass on command line or read
        // from saved file.
        user.setId(nsid);
        user.setUsername((username));
        user.setRealName("");
        auth.setUser(user);
        this.authStore.store(auth);
        return auth;
    }

    public void setAuth(String authToken, String username, String tokenSecret) throws IOException, SAXException, FlickrException {
        RequestContext rc = RequestContext.getRequestContext();
        Auth auth = null;

        if (authToken != null && !authToken.equals("") && tokenSecret != null && !tokenSecret.equals("")) {
            auth = constructAuth(authToken, tokenSecret, username);
            rc.setAuth(auth);
        } else {
            if (this.authStore != null) {
                auth = this.authStore.retrieve(this.nsid);
                if (auth == null) {
                    this.authorize();
                } else {
                    rc.setAuth(auth);
                }
            }
        }
    }

    public int getPrivacy() throws Exception {

        PrefsInterface prefi = flickr.getPrefsInterface();
        privacy = prefi.getPrivacy();

        return (privacy);
    }

    private String makeSafeFilename(String input) {
    	String input1=input.replaceAll("\\s+", "_");
        byte[] fname = input1.trim().getBytes();
        byte[] bad = new byte[] { '\\', '/', '"', '*' };
        byte replace = '_';
        for (int i = 0; i < fname.length; i++) {
            for (byte element : bad) {
                if (fname[i] == element) {
                    fname[i] = replace;
                }
            }
            if (replaceSpaces && fname[i] == ' ')
                fname[i] = '_';
        }
       
		logger.info("FileName: Original:"+ input + " Length:" + input.length()+ " Modified:" + new String(fname) +" Length:" + new String(fname).length()) ;
        return new String(fname);
    }

    public String uploadfile(String filename, String inpTitle) throws Exception {
        String photoId=null;

        RequestContext rc = RequestContext.getRequestContext();

        if (this.authStore != null) {
            Auth auth = this.authStore.retrieve(this.nsid);
            if (auth == null) {
                this.authorize();
            } else {
                rc.setAuth(auth);
            }
        }

        // PhotosetsInterface pi = flickr.getPhotosetsInterface();
        // PhotosInterface photoInt = flickr.getPhotosInterface();
        // Map<String, Collection> allPhotos = new HashMap<String, Collection>();
        /**
         * 1 : Public 2 : Friends only 3 : Family only 4 : Friends and Family 5 : Private
         **/
        
        //TODO: Default privacy: 1
        //logger.info("Privacy: 5 private");
        privacy=5;
        if (privacy == -1)
            getPrivacy();

        UploadMetaData metaData = new UploadMetaData();

        if (privacy == 1)
            metaData.setPublicFlag(true);
        if (privacy == 2 || privacy == 4)
            metaData.setFriendFlag(true);
        if (privacy == 3 || privacy == 4)
            metaData.setFamilyFlag(true);

        if (basefilename == null || basefilename.equals(""))
            basefilename = filename; // "image.jpg";

        String title = basefilename;
        boolean setMimeType = true; // change during testing. Doesn't seem to be supported at this time in flickr.
        if (setMimeType) {
            if (basefilename.lastIndexOf('.') > 0) {
                title = basefilename.substring(0, basefilename.lastIndexOf('.'));
                String suffix = basefilename.substring(basefilename.lastIndexOf('.') + 1);
                // Set Mime Type if known.

                // Later use a mime-type properties file or a hash table of all known photo and video types
                // allowed by flickr.

                if (suffix.equalsIgnoreCase("jpg")) {
                    metaData.setFilemimetype("image/jpg");
                } else if (suffix.equalsIgnoreCase("png")) {
                    metaData.setFilemimetype("image/png");
                } else if (suffix.equalsIgnoreCase("mpg") || suffix.equalsIgnoreCase("mpeg")) {
                    metaData.setFilemimetype("video/mpeg");
                } else if (suffix.equalsIgnoreCase("mov")) {
                    metaData.setFilemimetype("video/quicktime");
                }
            }
        }
        //logger.debug(" File : " + filename);
        //logger.info(" File : " + filename);
        //logger.debug(" basefilename : " + basefilename);
        //logger.info(" basefilename : " + basefilename);

        if (inpTitle != null && !inpTitle.equals("")) {
            title = inpTitle.trim();
            //logger.debug(" title : " + inpTitle);
            logger.info(" title : " + inpTitle);
            metaData.setTitle(title.trim());
        } // flickr defaults the title field from file name.

        // UploadMeta is using String not Tag class.

        // Tags are getting mangled by yahoo stripping off the = , '.' and many other punctuation characters
        // and converting to lower case: use the raw tag field to find the real value for checking and
        // for download.
        if (setOrigFilenameTag) {
            List<String> tags = new ArrayList<String>();
            String tmp = basefilename;
            basefilename = makeSafeFilename(basefilename);
            tags.add("OrigFileName='" + basefilename + "'");
            metaData.setTags(tags);
            //metaData.setTitle(basefilename);

            if (!tmp.equals(basefilename)) {
                logger.info(" File : " + basefilename + " contains special characters.  stored as " + basefilename + " in tag field");
                logger.debug(" File : " + basefilename + " contains special characters.  stored as " + basefilename + " in tag field");
            }
        }

        // File imageFile = new File(filename);
        // InputStream in = null;
        Uploader uploader = flickr.getUploader();

        // ByteArrayOutputStream out = null;
        try {
            // in = new FileInputStream(imageFile);
            // out = new ByteArrayOutputStream();

            // int b = -1;
            /**
             * while ((b = in.read()) != -1) { out.write((byte) b); }
             **/

            /**
             * byte[] buf = new byte[1024]; while ((b = in.read(buf)) != -1) { // fos.write(read); out.write(buf, 0, b); }
             **/

        	basefilename.trim().replaceAll("\\u0020", "%20");
            metaData.setFilename(basefilename);
            // check correct handling of escaped value

            filename.trim().replaceAll("\\u0020", "%20");
            
            File f = new File(filename);
            //logger.info("Before uploader.upload filename:"+filename);
            photoId = uploader.upload(f, metaData);
            //logger.info("After uploader.upload photoId:" + photoId);
            //logger.debug(" File : " + filename + " uploaded: photoId = " + photoId);
        }catch (Exception ex){
        	logger.info("Upload Exception:" + ex.getMessage());
        
        }finally {

        }

        return (photoId);
    }

    public void getPhotosetsInfo() {
    	allSetsMap.clear();
    	setNameToId.clear();
        PhotosetsInterface pi = flickr.getPhotosetsInterface();
        try {
            int setsPage = 1;
            while (true) {
                Photosets photosets = pi.getList(nsid, 500, setsPage, null);
                Collection<Photoset> setsColl = photosets.getPhotosets();
                Iterator<Photoset> setsIter = setsColl.iterator();
                while (setsIter.hasNext()) {
                    Photoset set = setsIter.next();
                    allSetsMap.put(set.getId(), set);
                    
                    //logger.info("Album Titles:"+set.getTitle());
                    // 2 or more sets can in theory have the same name. !!!
                    ArrayList<String> setIdarr = setNameToId.get(set.getTitle());
                    if (setIdarr == null) {
                        setIdarr = new ArrayList<String>();
                        setIdarr.add(new String(set.getId()));
                        setNameToId.put(set.getTitle(), setIdarr);
                    } else {
                        setIdarr.add(new String(set.getId()));
                    }
                }

                if (setsColl.size() < 500) {
                    break;
                }
                setsPage++;
            }
            logger.debug(" Albus retrieved: " + allSetsMap.size());
            logger.info(" Sets retrieved: " + allSetsMap.size());
            // all_sets_retrieved = true;
            // Print dups if any.

            Set<String> keys = setNameToId.keySet();
            Iterator<String> iter = keys.iterator();
            while (iter.hasNext()) {
                String name = iter.next();
                //logger.info("Album Name:"+name);
                ArrayList<String> setIdarr = setNameToId.get(name);
                /*
                if (setIdarr != null && setIdarr.size() > 1) {
                	logger.debug("There is more than 1 albums with this name : " + setNameToId.get(name));
                	logger.info("There is more than 1 albums with this name : " + setNameToId.get(name));
                    for (int j = 0; j < setIdarr.size(); j++) {
                    	logger.debug("           id: " + setIdarr.get(j));
						logger.info("           id: " + setIdarr.get(j));
						
                    }
                }
                */
            }

        } catch (FlickrException e) {
            e.printStackTrace();
        }
    }

    private String setid = null;

    private String basefilename = null;

    private final PhotoList<Photo> photos = new PhotoList<Photo>();

    private final HashMap<String, Photo> filePhotos = new HashMap<String, Photo>();

    private static void Usage() {
        logger.info("Usage: java " + UploadMedia.class.getName() + "  [ -n nsid | -u username ] -s setName { File../Directories}");
        logger.info("	Must pass either -u username or -n nsid ");
        logger.info("	Must pass  -s followed by set-name(albums)  followed by file/directories.");
        System.out
                .println("apiKey and shared secret must be available as apiKey and secret via setup.properties or passed as -apiKey key -secret shared-secret");
        System.exit(1);
    }

    /**
     * @return the setOrigFilenameTag
     */
    public boolean isSetorigfilenametag() {
        return setOrigFilenameTag;
    }

    /**
     * @param setOrigFilenameTag
     *            the setOrigFilenameTag to set
     */
    public void setSetorigfilenametag(boolean setOrigFilenameTag) {
        this.setOrigFilenameTag = setOrigFilenameTag;
    }

    static String setNameGlobal = null;
    public static void main(String[] args) throws Exception {

        String apiKey = null; // args[0];
        String sharedSecret = null; // args[1];

        Properties properties = new Properties();
        InputStream in = null;
        try {
            in = UploadMedia.class.getResourceAsStream("/setup.properties");
            if (in != null) {
                properties.load(in);
                apiKey = properties.getProperty("api_key");
                sharedSecret = properties.getProperty("secret");
                if (apiKey != null && sharedSecret != null)
                    logger.debug("Found setup.properties in classpath and set apiKey and shared secret");
                //logger.info("Found setup.properties in classpath and set apiKey and shared secret");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                in.close();
        }

        if (args.length < 5) {
            Usage();
            System.exit(1);
        }

        ArrayList<String> uploadfileArgs = new ArrayList<String>();
        ArrayList<String> optionArgs = new ArrayList<String>();

        // Flickr.debugRequest = true; // keep it false else entire file will be in stdout.

        // Flickr.debugStream = true;

        String authsDirStr = System.getProperty("user.home") + File.separatorChar + ".flickrAuth";

        String nsid = null;
        String username = null;
        String accessToken = null; // Optional entry.
        String tokenSecret = null; // Optional entry.
        String setName = null;

        boolean settagname = true; // Default to true to add tag while uploading.

        int i = 0;
        /***
         * for(i = 0; i < args.length; i++) { logger.info("args[" + i + "] " + args[i]); }
         **/

        for (i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "-n":
                if (i < args.length)
                    nsid = args[++i];
                break;
            case "-u":
                if (i < args.length)
                    username = args[++i];
                break;
            case "-apiKey":
                if (i < args.length)
                    apiKey = args[++i];
                break;

            case "-secret":
                if (i < args.length)
                    sharedSecret = args[++i];
                break;
            case "-notags":
                if (i < args.length)
                    settagname = false;
                break;

            case "-a":
                if (i < args.length)
                    accessToken = args[++i];
                break;
            case "-t":
                if (i < args.length)
                    tokenSecret = args[++i];
                break;
            case "-s":
                if (i < args.length){
                    setName = args[++i];
                    setNameGlobal=setName;
                break;
                }
            case "-option":
                if (i < args.length)
                    optionArgs.add(args[++i]);
                break;
            default:
                if (setName != null)
                {
                	String filePathWithSpace="";
                	if (args[i].toString().startsWith("'")){
                		if (!args[i].toString().endsWith("'") ) {
                			
                		
                		filePathWithSpace= filePathWithSpace+" " +args[i];
                		i+=1;
                		while(!args[i].toString().endsWith("'")){
                			filePathWithSpace= filePathWithSpace+" " +args[i];
                			i+=1;            		
                		}
                		filePathWithSpace= filePathWithSpace+" " +args[i];
                		
                		//String fullpath=args[i].substring(1, args[i].length());
                		//fullpath= fullpath + args[i+1].substring(0, args[i+1].length()-1);
                		
                		
                		filePathWithSpace=filePathWithSpace.trim();
                		//Remove ' first and last
                		filePathWithSpace=filePathWithSpace.substring(1, filePathWithSpace.length()-1);
                		
                		uploadfileArgs.add(filePathWithSpace);
                		} else {
                			uploadfileArgs.add(args[i].substring(1, args[i].length()-1).trim());
                		}
                	} else {
                		uploadfileArgs.add(args[i]);
                	}
                	
                }
                else {
                    Usage();
                    System.exit(1);
                }
            }
        }

        if (apiKey == null || sharedSecret == null || (username == null && nsid == null) || (setName == null) || (uploadfileArgs.size() == 0)) {
            Usage();
            System.exit(1);
        }

        UploadMedia bf = new UploadMedia(apiKey, nsid, sharedSecret, new File(authsDirStr), username);
        for (i = 0; i < optionArgs.size(); i++) {
            bf.addOption(optionArgs.get(i));
        }
		//logger.info("settagname:"+settagname);
        bf.setSetorigfilenametag(settagname);
        bf.setAuth(accessToken, username, tokenSecret);

        if (!bf.canUpload())
            System.exit(1);

      //TODO
        bf.getPrivacy();

        //bf.getPhotosetsInfo();

		
       // if (setName != null && !setName.equals("")) {

      //      bf.getSetPhotos(setName);
       // }

        // String photoid;

        scanFiles(bf, uploadfileArgs.get(0),setName);
        
//        for (i = 0; i < uploadfileArgs.size(); i++) {
//            String filename = uploadfileArgs.get(i);
//
//            File f = new File(filename);
//            if (f.isDirectory()) {
//                String[] filelist = f.list(new UploadFilenameFilter());
//                logger.debug("Processing directory  : " + uploadfileArgs.get(i));
//                for (int j = 0; j < filelist.length; j++) {
//                    bf.processFileArg(uploadfileArgs.get(i) + File.separatorChar + filelist[j], setName);
//                }
//            } else {
//                bf.processFileArg(filename, setName);
//            }
//        }
    }

    private static final String[] photoSuffixes = { "jpg", "jpeg", "png", "gif", "bmp", "tif", "tiff" };

    private static final String[] videoSuffixes = { "3gp", "3gp", "avi", "mov", "mp4", "mpg", "mpeg", "wmv", "ogg", "ogv", "m2v" };

    static class UploadFilenameFilter implements FilenameFilter {

        // Following suffixes from flickr upload page. An App should have this configurable,
        // for videos and photos separately.

        @Override
        public boolean accept(File dir, String name) {
            if (isValidSuffix(name))
                return true;
            else
                return false;
        }

    }

    // Driver Method
    public static void scanFiles(UploadMedia bf, String maindirpath, String setName) throws Exception
    {
        // Provide full path for directory(change accordingly)  
        //maindirpath = "D:\\Dinesh\\upload";
      
        // File object
    	maindirpath.trim().replaceAll("\\u0020", "%20");	
       File maindir = new File(maindirpath);
//        File maindir = null;
//        try {
//            URI u = new URI(maindirpath.trim().replaceAll("\\u0020", "%20"));
//            maindir = new File(u.getPath());
//        } catch (URISyntaxException ex) {
//            logger.info("File Path Exception:" +ex.getMessage());
//            logger.info("File Path Exception:" +ex.getMessage());
//        }
        
       //logger.info("Before scanFiles || maindir.exists() && maindir.isDirectory()");
        if(maindir.exists() && maindir.isDirectory())
        {
            // array for files and sub-directories 
            // of directory pointed by maindir
            File arr[] = maindir.listFiles();
            //logger.info("Inside sts() && maindir.isDirectory(): File Size:" + arr.length);
  
            
            logger.info("**********************************************");
            logger.info("Files from main directory : " + maindir);
            logger.info("**********************************************");
             
            // Calling recursive method
          
				RecursivePrint(bf,arr,0,0, setName);
				
				logger.info("All records updated successfully, Exiting Program:"+uploadcount);
	        	logger.debug("All records updated successfully, Exiting Program:"+uploadcount);
			
       } 
    }
    

    static void RecursivePrint(UploadMedia bf, File[] arr,int index,int level, String setName) throws Exception 
    {
   	
        // terminate condition
        if(index == arr.length)
            return;
         
        // tabs for internal levels
        //for (int i = 0; i < level; i++)
           //System.out.print("\t");
         
        String albumName="";
        // for files
  
        //logger.info("RecordsUploaded: " + recordsUploaded + "||" + "UploadCount:" + uploadcount);
        //logger.debug("RecordsUploaded: " + recordsUploaded + "||" + "UploadCount:" + uploadcount);
        //logger.info("Inside RecursivePrint || Index:" + index + " Level:" + level+ " file Path: "+arr[index].getPath());
        if(arr[index].isFile()){
        
        	 //logger.info("Inside if(arr[index].isFile()) || Index:" + index + " Level:" + level+ " file Path: "+arr[index].getPath());
        
        	try {
        		//logger.info("Before loadProperties: " + fileName);
        		//recordsUploaded = getUploadCount(fileName);
        	
        		prop =loadProperties(fileName);
        		
        		if (prop != null){
        		if (prop.getProperty("StartCount").trim().length()!=0){
        			startCount=Integer.parseInt(prop.getProperty("StartCount").trim());
        		} else {
        			startCount=0;
        		}
        		//logger.info("startCount:" + startCount);
        		if (prop.getProperty("MaxCount").trim().length()!=0){
        			maxCount=Integer.parseInt(prop.getProperty("MaxCount").trim());
        		} else {
        			maxCount=1000;
        		}
        		//logger.info("maxCount:" + maxCount);
        		if (prop.getProperty("RecordsUploaded").trim().length()!=0){
        			recordsUploaded=Integer.parseInt(prop.getProperty("RecordsUploaded").trim());
        			//logger.info("recordsUploaded:" + recordsUploaded);
        		} else {
        			recordsUploaded=startCount;
        			//logger.info(" Else recordsUploaded:" + recordsUploaded);
        		}
        		} else {
        			logger.info("Property file not loaded");
        		}
        		//logger.info("After loadProperties");
        		/*
        		startCount=Integer.parseInt(prop.getProperty("StartCount"));
        		maxCount=Integer.parseInt(prop.getProperty("MaxCount"));
        		if (prop.getProperty("RecordsUploaded").length()==0){
        			recordsUploaded=0;
        		} else {
        			recordsUploaded=Integer.parseInt(prop.getProperty("RecordsUploaded"));
        		}
        		*/
        		
        		 if (uploadcount == maxCount){
        	        	logger.info("All records updated successfully, Exiting Program:"+uploadcount);
        	        	//logger.debug("All records updated successfully, Exiting Program:"+uploadcount);
        	        	System.exit(0);
        	        }
        		
        		 //logger.info("Before catch");
        		
        	} catch (Exception e){
        		logger.info(e.getMessage());
        		e.getMessage();
        	logger.info(e.getStackTrace().toString());
        		//updateUploadCount(fileName, 1);
        	saveProperties(fileName, startCount, maxCount, 1);
        		//saveProperties(fileName, startCount);
        	}
        	
        	if (recordsUploaded > uploadcount) {
        		//logger.info(" inside if (recordsUploaded > uploadcount) || Index:" + index + " Level:" + level + " recordsUploaded:" + recordsUploaded+ " uploadcount:"+uploadcount+ " file Path: "+arr[index].getPath());
        		uploadcount +=1; 
        	} else {
        		
        		//logger.info(" inside  Else if (recordsUploaded > uploadcount) || Index:" + index + " Level:" + level + " recordsUploaded:" + recordsUploaded+ " uploadcount:"+uploadcount+ " file Path: "+arr[index].getPath());
	        	 uploadcount +=1; 
	            //logger.info(uploadcount +"||"+ arr[index].getPath());
	            //logger.debug(uploadcount +"||"+ arr[index].getPath());
	          
	            //updateUploadCount(fileName, uploadcount);
	        	 saveProperties(fileName, startCount, maxCount, uploadcount);
	            logger.info("\n\nProcessing file: "+arr[index].getPath());
	            setName=getPhotoTakenMetadata(new File(arr[index].getPath()));
	            
	            if (setName.trim().equalsIgnoreCase("")){
	            	setName=getPhotoClickTimeFromFileName(arr[index].getPath());
	            }
	            if (setName.trim().equalsIgnoreCase("")){
	            
	            	setName = getCreationTime(new File(arr[index].getPath()));
	        	}
	            
	            if (setName.trim().equalsIgnoreCase("")){
		            
	            	setName = setNameGlobal.trim();
		        	}
	            //logger.info(albumName);
	            bf.getPrivacy();
	            bf.getPhotosetsInfo();
	            if (setName != null && !setName.trim().equalsIgnoreCase("")) {

	                bf.getSetPhotos(setName);
	            }
	            
	             bf.processFileArg(arr[index].getPath(), setName);
	            
	            logger.info("RecordsUploaded: " + recordsUploaded + "||" + "UploadCount:" + uploadcount+"\n\n\n");
	            logger.info("#############################################################################");
	            //logger.debug("RecordsUploaded: " + recordsUploaded + "||" + "UploadCount:" + uploadcount);
	            
           
        }
            
        // for sub-directories
        }else if(arr[index].isDirectory())
        {
           //logger.info(" Inside else if(arr[index].isDirectory()) || Index:" + index + " Level:" + level + " file Path: "+arr[index].getPath());
            RecursivePrint(bf, arr[index].listFiles(), 0, level + 1,setName);
        }
          
        //logger.info(" Before Last RecursivePrint(bf, arr,++index, level, setName) || Index:" + index + " Level:" + level+ " file Path: "+arr[index].getPath());
        // recursion for main directory
        //savePropertiesSingleton(fileName, startCount, maxCount, uploadcount);
        RecursivePrint(bf, arr,++index, level, setName);
        
    
   }

    public static boolean isNumber(String string) {
  	  for (int i = 0; i < string.length(); i++) {
  	    if (!Character.isDigit(string.charAt(i))) {
  	      return false;
  	    }
  	  }

  	  return true;
  	}
    
   
    
    private static boolean isValidSuffix(String basefilename) {
        if (basefilename.lastIndexOf('.') <= 0) {
            return false;
        }
        String suffix = basefilename.substring(basefilename.lastIndexOf('.') + 1).toLowerCase();
        for (int i = 0; i < photoSuffixes.length; i++) {
            if (photoSuffixes[i].equals(suffix))
                return true;
        }
        for (int i = 0; i < videoSuffixes.length; i++) {
            if (videoSuffixes[i].equals(suffix))
                return true;
        }
        logger.debug(basefilename + " does not have a valid suffix, skipped.");
        logger.info(basefilename + " does not have a valid suffix, skipped.");
        return false;
    }

    void processFileArg(String filename, String setName) throws Exception {
        String photoid;
        if (filename.equals("")) {
            logger.info("filename must be entered for uploadfile ");
            return;
        }
        if (filename.lastIndexOf(File.separatorChar) > 0)
            basefilename = filename.substring(filename.lastIndexOf(File.separatorChar) + 1, filename.length());
        else
            basefilename = filename;

        boolean fileUploaded = checkIfLoaded(filename);

        if (!fileUploaded) {
            if (!isValidSuffix(basefilename)) {
                logger.info(" File: " + basefilename + " is not a supported filetype for flickr (invalid suffix)");
                return;
            }

            File f = new File(filename);
            if (!f.exists() || !f.canRead()) {
                logger.info(" File: " + filename + " cannot be processed, does not exist or is unreadable.");
                logger.debug(" File: " + filename + " cannot be processed, does not exist or is unreadable.");
                return;
            }
            logger.debug("Calling uploadfile for filename : " + filename);
            logger.debug("Upload of " + filename + " started at: " + smp.format(new Date()) + "\n");
            logger.info("Upload of " + filename + " started at: " + smp.format(new Date()) + "\n");

            
            photoid = uploadfile(filename, null);
            // Add to Set. Create set if it does not exist.
            if (photoid != null) {
                addPhotoToSet(photoid, setName);
            } else {
            	logger.info("PhotoID is null, Not added in album:" + filename + " PhotoID:" + photoid);
            }
            logger.debug("Upload of " + filename + " finished at: " + smp.format(new Date()) + "\n");
            logger.info("Upload of " + filename + " finished at: " + smp.format(new Date()) + "\n");

        } else {
            logger.debug(" File: " + filename + " has already been uploaded on " + getUploadedTime(filename));
            logger.info(" File: " + filename + " has already been uploaded on " + getUploadedTime(filename));
        }
    }

    void addOption(String opt) {

        switch (opt) {
        case "replaceSpaces":
            replaceSpaces = true;
            break;

        case "notags":
            setSetorigfilenametag(false);
            break;

        default: // Not supported at this time.
            logger.info("Option: " + opt + " is not supported at this time");
            logger.debug("Option: " + opt + " is not supported at this time");
        }
    }

    boolean canUpload() {
        RequestContext rc = RequestContext.getRequestContext();
        Auth auth = null;
        auth = rc.getAuth();
        if (auth == null) {
            logger.info(" Cannot upload, there is no authorization information.");
            logger.debug(" Cannot upload, there is no authorization information.");
            return false;
        }
        Permission perm = auth.getPermission();
        if ((perm.getType() == Permission.WRITE_TYPE) || (perm.getType() == Permission.DELETE_TYPE))
            return true;
        else {
            logger.info(" Cannot upload, You need write or delete permission, you have : " + perm.toString());
            logger.debug(" Cannot upload, You need write or delete permission, you have : " + perm.toString());
            return false;
        }
    }

    /**
     * The assumption here is that for a given set only unique file-names will be loaded and the title field can be used. Later change to use the tags field (
     * OrigFileName) and strip off the suffix.
     * 
     * @param filename
     * @return
     */
    private boolean checkIfLoaded(String filename) {

        String title;
        if (basefilename.lastIndexOf('.') > 0){
            title = basefilename.substring(0, basefilename.lastIndexOf('.'));
        	title=title.replaceAll("\\s+", "_");
        }
        else
            return false;
        //logger.info(filePhotos.size());
     //   for (String key : filePhotos.keySet()) {
        	   //logger.info("key: " + key + " value: " + filePhotos.get(key));
      //  	   logger.debug("key: " + key + " value: " + filePhotos.get(key));
       // 	}


        		
        if (filePhotos.containsKey(title))
            return true;

        return false;
    }

    private String getUploadedTime(String filename) {

        String title = "";
        if (basefilename.lastIndexOf('.') > 0)
            title = basefilename.substring(0, basefilename.lastIndexOf('.'));
        	title=title.replaceAll("\\s+", "_");

        if (filePhotos.containsKey(title)) {
            Photo p = filePhotos.get(title);
            if (p.getDatePosted() != null) {
                return (smp.format(p.getDatePosted()));
            }
        }

        return "";
    }

    void getSetPhotos(String setName) throws FlickrException {
        // Check if this is an existing set. If it is get all the photo list to avoid reloading already
        // loaded photos.
        ArrayList<String> setIdarr;
        setIdarr = setNameToId.get(setName);
       if (setIdarr !=null) {
        for (int j=0;j<setIdarr.size();j++){
        	
   
        try {
        if (setIdarr != null) {
            setid = setIdarr.get(j);
            PhotosetsInterface pi = flickr.getPhotosetsInterface();

            Set<String> extras = new HashSet<String>();
            /**
             * A comma-delimited list of extra information to fetch for each returned record. Currently supported fields are: license, date_upload, date_taken,
             * owner_name, icon_server, original_format, last_update, geo, tags, machine_tags, o_dims, views, media, path_alias, url_sq, url_t, url_s, url_m,
             * url_o
             */

            extras.add("date_upload");
            extras.add("original_format");
            extras.add("media");
            // extras.add("url_o");
            extras.add("tags");

            int setPage = 1;
            while (true) {
                PhotoList<Photo> tmpSet = pi.getPhotos(setid, extras, Flickr.PRIVACY_LEVEL_NO_FILTER, 500, setPage);

                int tmpSetSize = tmpSet.size();
                photos.addAll(tmpSet);
                if (tmpSetSize < 500) {
                    break;
                }
                setPage++;
            }
            for (int i = 0; i < photos.size(); i++) {
            	//logger.info("Title:"+photos.get(i).getTitle()+"\t ID: "+photos.get(i));
            	
                filePhotos.put(photos.get(i).getTitle(), photos.get(i));
            }
            if (flickrDebug) {
                logger.debug("Set title: " + setName + "  id:  " + setid + " found");
                logger.debug("   Photos in Album: "+setName+" already uploaded: " + photos.size());
            }
        }
        
        } catch (FlickrException fe){
        	logger.info(fe.getMessage());
        	continue;
        }
        //break;
        }
       }
    }

    public void addPhotoToSet(String photoid, String setName) throws Exception {
    	logger.info("addPhotoToSet: PhotoID:" +photoid + " SetName:" + setName );
        ArrayList<String> setIdarr;

        // all_set_maps.

        PhotosetsInterface psetsInterface = flickr.getPhotosetsInterface();

        Photoset set = null;

        
        //Changed logic to get old setid from name
        setid=null;
        setIdarr = setNameToId.get(setName);
       if (setIdarr != null) {
            setid = setIdarr.get(0);
        }
        
        if (setid == null) {
            // In case it is a new photo-set.
            setIdarr = setNameToId.get(setName);
            if (setIdarr == null) {
                // setIdarr should be null since we checked it getSetPhotos.
                // Create the new set.
                // set the setid .

                String description = "";
                set = psetsInterface.create(setName, description, photoid);
                setid = set.getId();

                setIdarr = new ArrayList<String>();
                setIdarr.add(new String(setid));
                setNameToId.put(setName, setIdarr);

                allSetsMap.put(set.getId(), set);
                logger.info("Created New Set:" + setName +" setID:"+setid + " setIdarr:"+setIdarr.toString());
            }
        } else {
            set = allSetsMap.get(setid);
            psetsInterface.addPhoto(setid, photoid);
        }
        // Add to photos .

        // Add Photo to existing set.
        PhotosInterface photoInt = flickr.getPhotosInterface();
        Photo p = photoInt.getPhoto(photoid);
        if (p != null) {
            photos.add(p);
            String title;
            if (basefilename.lastIndexOf('.') > 0)
                title = basefilename.substring(0, basefilename.lastIndexOf('.')).trim();
            else
                title = p.getTitle().trim();
            filePhotos.put(title, p);
        }
    }
    
	
	public static void updateUploadCount(String fileName, int count) throws IOException{
							
			    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			    writer.write(new Integer(count).toString());
			     
			    writer.close();
			}
	
	public static int getUploadCount(String fileName) throws IOException{
		
	    BufferedReader reader = new BufferedReader(new FileReader(fileName));
	    return new Integer(reader.readLine());
	     
	    
	}
	
	 public static void saveProperties(String fileName, int startCount, int maxCount, int recordsUploaded) {

		Properties prop = new Properties();
		OutputStream output = null;

		try {

			output = new FileOutputStream(fileName);

			// set the properties value
			prop.setProperty("StartCount", Integer.toString(startCount));
			prop.setProperty("MaxCount", Integer.toString(maxCount));
			prop.setProperty("RecordsUploaded", Integer.toString(recordsUploaded));
	
			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	  }
	 
	 public static void savePropertiesSingleton(String fileName, int startCount, int maxCount, int recordsUploaded) {



			try {
				//logger.info("Inside savePropertiesSingleton:" + fileName);
				if (prop ==null){
					prop = new Properties();
					
				}
				if (output ==null){
					
					output = new FileOutputStream(fileName);
				}

				// set the properties value
				prop.setProperty("StartCount", Integer.toString(startCount));
				prop.setProperty("MaxCount", Integer.toString(maxCount));
				prop.setProperty("RecordsUploaded", Integer.toString(recordsUploaded));
		
				// save properties to project root folder
				prop.store(output, null);
				logger.info("Complete savePropertiesSingleton:" + fileName);
			} catch (IOException io) {
				io.printStackTrace();
			} 
		  }
	
	 public static Properties loadPropertiesSingleton(String fileName) {

			try {
				//logger.info("Inside loadProperties:" + fileName);
				if (prop ==null ) {
					prop = new Properties();
					
				}
				
				if(input==null) {
					input = new FileInputStream(fileName.trim());
				
				}
				//logger.info("Completed FileInputStream");
				// load a properties file
				prop.load(input);
				//logger.info("Completed prop.load");
			

			} catch (IOException ex) {
				//ex.printStackTrace();
				logger.info(ex.getStackTrace().toString());
			} 
			return prop;
		 
	 }
	 public static Properties loadProperties(String fileName) {

			Properties prop = null;
			InputStream input = null;

			try {
				//logger.info("Inside loadProperties:" + fileName);
				prop = new Properties();
				input = new FileInputStream(fileName.trim());
				//logger.info("Completed FileInputStream");
				// load a properties file
				prop.load(input);
				//logger.info("Completed prop.load");
			

			} catch (IOException ex) {
				//ex.printStackTrace();
				logger.info(ex.getStackTrace().toString());
			} finally {
				if (input != null) {
					try {
						input.close();
						
					} catch (IOException e) {
						//e.printStackTrace();
						logger.info(e.getStackTrace().toString());
					}
				}
			}
			return prop;

		  }
	 
	  public static String getPhotoTakenMetadata(File file )
	    {
	        
	    	  String fileYearMonth ="";
	        try
	        {
	           
	            Metadata imageMetadata = ImageMetadataReader.readMetadata( file );

	            // Read Exif Data
	            Directory directory = imageMetadata.getDirectory( ExifDirectory.class );
	            if( directory != null )
	            {
	                // Read the date
	                Date date = directory.getDate( ExifDirectory.TAG_DATETIME );
	                //logger.info( "Date Taken: " + date );
	              
	                try {
	         	   	   SimpleDateFormat df = new SimpleDateFormat("yyyy/MM");
	         	       String dateTaken = df.format(date);
	         	      dateTaken=dateTaken.replaceAll("/", "");
	         	       fileYearMonth=dateTaken;
	         	       logger.info("TAG_DATETIME: Photo taken metadata found:"+fileYearMonth);
	            	    }catch(Exception ex){
	            	    	logger.debug("TAG_DATETIME: Date conversion excepton:"+ex.getMessage());
	                }
	            
	            }
	            
	            if (fileYearMonth.trim().equalsIgnoreCase("")){
		           
		            if( directory != null )
		            {
		                // Read the date
		                Date date = directory.getDate( ExifDirectory.TAG_DATETIME_ORIGINAL );
		                //logger.info( "Date Taken: " + date );
		              
		                try {
		         	   	   SimpleDateFormat df = new SimpleDateFormat("yyyy/MM");
		         	       String dateTaken = df.format(date);
		         	      dateTaken=dateTaken.replaceAll("/", "");
		         	       fileYearMonth=dateTaken;
		         	       logger.info("TAG_DATETIME_ORIGINAL: Photo taken metadata found:"+fileYearMonth);
		            	    }catch(Exception ex){
		            	    	logger.debug("TAG_DATETIME_ORIGINAL: Date conversion excepton:"+ex.getMessage());
		                }
		            
		            }
	            }
	            
	            
	        }
	        catch( Exception e )
	        {
	        	logger.debug("Photo Taken metadata exception:"+ e.getMessage());
	            //e.printStackTrace();
	        }
	        return fileYearMonth.trim();
	                
	    }
	  
	  public static String getPhotoClickTimeFromFileName(String fileName) throws IOException {
		  fileName=fileName.substring(fileName.lastIndexOf("\\")+1,fileName.length()).trim();
		  fileName=fileName.substring(0,fileName.lastIndexOf(".")).trim();	
	      	 fileName=fileName.replace("-", "");
	      	 fileName=fileName.replace("_", "");
	      	 String albumName="";
	      	 String dateString="";
	      	 int yearIndex =fileName.indexOf("20");
	    
	      try {
	 
	      	 if (yearIndex != -1 && fileName.length() >= yearIndex+6){
	      		 
	      		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
	      		int photoYear= new Integer(fileName.substring(yearIndex, yearIndex+4));
	      		dateString= fileName.substring(yearIndex, yearIndex+6);
	      		int photoMonth= new Integer(dateString.substring(4,dateString.length()));
	      		if (photoYear <= currentYear && photoMonth <=12 && photoMonth > 0) {
		      		
		      		if (isNumber(dateString)){
		      			albumName=dateString;
		      			logger.info("TAG_getPhotoClickTimeFromFileName: :"+albumName);
		      		}
	      		}
	      	 }
	      } catch (NumberFormatException nfe){
	    	  logger.info("getPhotoClickTimeFromFileName: NumberFormException:" + nfe.getMessage());
	      }
	      	 return albumName.trim();
	       }
	    

	    
	    public static String getCreationTime(File file) throws IOException {
	   	    Path p = Paths.get(file.getAbsolutePath());
	   	    BasicFileAttributes view
	   	        = Files.getFileAttributeView(p, BasicFileAttributeView.class)
	   	                    .readAttributes();
	   	    FileTime date = view.lastModifiedTime();
	   	  
	   	    String fileYearMonth="";
	   	
	   	    try {
		   	   SimpleDateFormat df = new SimpleDateFormat("yyyy/MM");
		       String dateCreated = df.format(date.toMillis());
		       dateCreated=dateCreated.replaceAll("/", "");
		       fileYearMonth=dateCreated;
		       logger.info("TAG_getCreationTime:lastModifiedTime :"+fileYearMonth);
	   	    }catch(Exception ex){
	    	   logger.debug("lastModifiedTime: Date conversion excepton:"+ex.getMessage());
	       }
	   	    
	   	    if (fileYearMonth.trim().equalsIgnoreCase("")){
	   	    	
	   	     date = view.creationTime();
		   	 
		   	
		   	    try {
			   	   SimpleDateFormat df = new SimpleDateFormat("yyyy/MM");
			       String dateCreated = df.format(date.toMillis());
			       dateCreated=dateCreated.replaceAll("/", "");
			       fileYearMonth=dateCreated;
			       logger.info("TAG_getCreationTime:creationTime :"+fileYearMonth);
		   	    }catch(Exception ex){
		    	   logger.debug("creationTime: Date conversion excepton:"+ex.getMessage());
		       }
	   	    	
	   	    	
	   	    }
	   	    return fileYearMonth.trim();
	    }
	   	    
	  
	    
	    
}