import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;

public class FileCounter {

	public static void main(String args[]){
		try {
			getFileCreationTime();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String fileName="uploadeCounter.txt";
		int i=1;
		try {
			updateUploadCount(fileName, i);
			updateUploadCount(fileName, 2);
			updateUploadCount(fileName, 11);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void getFileCreationTime() throws IOException{
		File file =new File("D:\\Dinesh\\tmp\\IMG_20170418_000617.jpg");
		   Path p = Paths.get(file.getAbsolutePath());
	   	    BasicFileAttributes view
	   	        = Files.getFileAttributeView(p, BasicFileAttributeView.class)
	   	                    .readAttributes();
	

		        FileTime date = view.creationTime();
		        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		        String dateCreated = df.format(date.toMillis());
		        System.out.println(dateCreated);
		
	}
	public static void updateUploadCount(String fileName, int count) throws IOException{
				fileName="uploadeCounter.txt";
			
			    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			    writer.write(new Integer(count).toString());
			     
			    writer.close();
			}
}
