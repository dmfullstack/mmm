# Flickr-Folder-Uploader
Flickr Folder Uploader - by DInesh Metkari

How to Get the flickr id:
Open:
https://www.webfx.com/tools/idgettr/
Paste your flickr url:

http://www.flickr.com/photos/dineshmetkari/  -> FIND

id: 144025267@N08 
Use this id as -n parameter below, while running the app.

java -Xss6144k -cp managemymedia-1.0-SNAPSHOT-jar-with-dependencies.jar  com.metkari.dinesh.managemymedia.UploadMedia -n 144025267@N08 -s myalbums  '/home/dineshmetkari/Downloads/Record'

java -Xss4096k -cp managemymedia-1.0-SNAPSHOT-jar-with-dependencies.jar  com.metkari.dinesh.managemymedia.OrganizeMedia


/home/dineshmetkari/.flickrAuth
find the filename: 
144025267@N08.auth 
which is ID


flickruploadall.bat
start cmd /k D:\flickeruploader\1\flickupload1.bat
start cmd /k D:\flickeruploader\2\flickupload2.bat


flickrupload1.bat
d:
cd flickeruploader
cd 1
java -Xss4096k -cp practicaSD2-1.0-SNAPSHOT-jar-with-dependencies.jar  es.gruposistemasdistribuidos.practicasd2.src.UploadPhoto -n 155239840@N02 -s myalbums 'D:\Dinesh\ShareIT'



uploadcounter.txt
MaxCount=25000
StartCount=0
RecordsUploaded=0