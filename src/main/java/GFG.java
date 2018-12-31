import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
 
public class GFG 
{
	
	
     static void RecursivePrint(File[] arr,int index,int level) throws IOException 
     {
    	
         // terminate condition
         if(index == arr.length)
             return;
          
         // tabs for internal levels
         for (int i = 0; i < level; i++)
            System.out.print("\t");
          
         String albumName="";
         // for files
         if(arr[index].isFile()){
             System.out.println(arr[index].getPath());
          
             albumName=getPhotoClickTime(arr[index].getPath());
             
             if (albumName.equalsIgnoreCase("")){
             
            	 albumName = getCreationTime(new File(arr[index].getPath()));
         	}
             
             System.out.println(albumName);
             
         // for sub-directories
         }else if(arr[index].isDirectory())
         {
           
             RecursivePrint(arr[index].listFiles(), 0, level + 1);
         }
           
         // recursion for main directory
         RecursivePrint(arr,++index, level);
    }
     
     public static String getPhotoClickTime(String fileName) throws IOException {
    	 fileName=fileName.replace("-", "");
    	 fileName=fileName.replace("_", "");
    	 String albumName="";
    	 int yearIndex =fileName.indexOf("20");
    	 if (yearIndex != -1 && fileName.length() >= yearIndex+6){
    		 
    		 albumName= fileName.substring(yearIndex, yearIndex+6);
    	 }
    	 return albumName;
     }
     
     public static String getCreationTime(File file) throws IOException {
    	    Path p = Paths.get(file.getAbsolutePath());
    	    BasicFileAttributes view
    	        = Files.getFileAttributeView(p, BasicFileAttributeView.class)
    	                    .readAttributes();
    	    String  fileTime=view.creationTime().toString();
    
    	    fileTime=fileTime.replace("-", "");
    	    fileTime=fileTime.replace("_", "");
    	    
    	    //  also available view.lastAccessTine and view.lastModifiedTime
    	    String fileYearMonth = fileTime.substring(0, 6);
    	    return fileYearMonth;
    	  }
     
     // Driver Method
     public static void scanFiles(String maindirpath)
     {
         // Provide full path for directory(change accordingly)  
         maindirpath = "D:\\Dinesh\\upload";
       
         // File object
         File maindir = new File(maindirpath);
           
         if(maindir.exists() && maindir.isDirectory())
         {
             // array for files and sub-directories 
             // of directory pointed by maindir
             File arr[] = maindir.listFiles();
              
             System.out.println("**********************************************");
             System.out.println("Files from main directory : " + maindir);
             System.out.println("**********************************************");
              
             // Calling recursive method
             try {
 				RecursivePrint(arr,0,0);
 			} catch (IOException e) {
 				e.getMessage();
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
        } 
     }
     
    // Driver Method
    public static void main(String[] args)
    {
        // Provide full path for directory(change accordingly)  
        String maindirpath = "D:\\Dinesh\\upload";
      
        // File object
        File maindir = new File(maindirpath);
          
        if(maindir.exists() && maindir.isDirectory())
        {
            // array for files and sub-directories 
            // of directory pointed by maindir
            File arr[] = maindir.listFiles();
             
            System.out.println("**********************************************");
            System.out.println("Files from main directory : " + maindir);
            System.out.println("**********************************************");
             
            // Calling recursive method
            try {
				RecursivePrint(arr,0,0);
			} catch (IOException e) {
				e.getMessage();
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
       } 
    }
}