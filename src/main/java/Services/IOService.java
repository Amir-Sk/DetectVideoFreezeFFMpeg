package Services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;

public class IOService {

    public static int downloadFiles(HashSet<String> paths){
        int CONNECT_TIMEOUT = 10000;
        int READ_TIMEOUT = 10000;
        String downloadsFolderPath = "src/main/java/downloads/";
        AtomicInteger downloadedFilesNum = new AtomicInteger(0);
        paths.parallelStream().forEach(path -> {
            try {
                URL url = new URL(path);
                String fileNameFromUrl = returnFileNameFromUrl(url);
                if (!Files.exists(Paths.get(downloadsFolderPath + fileNameFromUrl)))
                    FileUtils.copyURLToFile(url, new File(downloadsFolderPath + fileNameFromUrl), CONNECT_TIMEOUT, READ_TIMEOUT);
                downloadedFilesNum.getAndAdd(1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return downloadedFilesNum.get();
    }

    public static String returnFileNameFromUrl(@NonNull URL url) {
        String[] uriSegments = url.getPath().split("/");
        return uriSegments[uriSegments.length-1];
    }

    public static String returnFileNameFromUrl(@NonNull String url) {
        String[] uriSegments = url.split("/");
        return uriSegments[uriSegments.length-1];
    }

//    /* 2nd option using NIO, if you don't wish to see any common packages usage. */
//    private void downloadFiles(HashSet<String> paths){
//        paths.parallelStream().forEach(path -> {
//            try {
//                URL url = new URL(path);
//                String[] uriSegments = url.getPath().split("/");
//                // The Java NIO package offers the possibility to transfer bytes between
//                // two Channels without buffering them into allocated application heap memory.
//                ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
//                FileOutputStream fileOutputStream = new FileOutputStream(
//                    "../downloads/".concat(uriSegments[uriSegments.length-1])
//                );
//                FileChannel fileChannel = fileOutputStream.getChannel();
//                fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
//                fileOutputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

}

