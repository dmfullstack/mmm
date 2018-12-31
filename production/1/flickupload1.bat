d:
cd flickeruploader
cd 1
java -Xss4096k -cp practicaSD2-1.0-SNAPSHOT-jar-with-dependencies.jar  es.gruposistemasdistribuidos.practicasd2.src.UploadPhoto -n 155239840@N02 -s myalbums 'D:\Dinesh\ShareIT'

java -Xss4096k -cp practicaSD2-1.0-SNAPSHOT-jar-with-dependencies.jar  es.gruposistemasdistribuidos.practicasd2.src.UploadPhoto -n 155239840@N02 -s myalbums '/home/dineshmetkari/Downloads/Record'

java -Xss4096k -cp managemymedia-1.0-SNAPSHOT-jar-with-dependencies.jar  com.metkari.dinesh.managemymedia.UploadMedia -n 144025267@N08 -s myalbums  '/home/dineshmetkari/Downloads/Record'

java -Xss4096k -cp managemymedia-1.0-SNAPSHOT-jar-with-dependencies.jar  com.metkari.dinesh.managemymedia.OrganizeMedia

java -Xss6144k -cp managemymedia-1.0-SNAPSHOT-jar-with-dependencies.jar  com.metkari.dinesh.managemymedia.OrganizeMediaThread > OrganizeMediaThread.log
