import static Services.IOService.returnFileNameFromUrl;

import Model.Point;
import Model.VideoStream;
import Services.IOService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class AnalyzeStreams {

    public static JSONObject runAnalysis(HashSet<String> urls) {
        int downloadedFiles = IOService.downloadFiles(urls);
        if (downloadedFiles == urls.size()) {
            HashSet<VideoStream> videoStreams = analyzeFiles(urls);
            return createJSONForVideos(videoStreams); }
        else {
            System.out.println("Some files were not downloaded");
            return null;
        }
    }

    private static JSONObject createJSONForVideos(HashSet<VideoStream> videoStreams) {
        // TODO: Missing all_videos_freeze_frame_synced field.
        JSONObject jsonObject = new JSONObject();
        JSONArray list = new JSONArray();
        videoStreams.stream()
            .forEach(videoStream -> {
                list.add(
                    new JSONObject().put("longest_valid_period",
                        String.valueOf(videoStream.getLongestValidTimeframe()))
                );
                list.add(
                    new JSONObject().put("valid_video_percentage",
                        String.valueOf((videoStream.getLongestValidTimeframe() / videoStream.getVideoDuration())))
                );
                JSONArray validPoints = new JSONArray();
                videoStream.getValidPoints().stream()
                    .forEach(validPoint -> {
                        JSONArray pointsArray = new JSONArray();
                        pointsArray.add(validPoint.getStart());
                        pointsArray.add(validPoint.getEnd());
                        validPoints.add(pointsArray);
                    });
                list.add(
                    new JSONObject().put("valid period", validPoints)
                );
            });
        jsonObject.put("videos", list);
        return jsonObject;
    }

    private static HashSet<VideoStream> parseLogsIntoObjects(String logsFolder,
        HashSet<VideoStream> videoStreams) throws IOException {
        try (Stream<Path> stream = Files.walk(Paths.get(logsFolder))){
            stream.filter(Files::isRegularFile)
                .forEach( file -> {
                    VideoStream videoStream = new VideoStream();
                    try (LineIterator iterator = FileUtils.lineIterator(new File(logsFolder + "/" + file.getFileName()))){
                        // Using line iterator (streaming) and not Files.readAllLines in case downloaded file
                        // has a lot of data and can saturate our heap space.
                        while (iterator.hasNext()) {
                            analyzeLine(iterator.nextLine(), videoStream);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    videoStreams.add(videoStream);
                });
        }
        return videoStreams;
    }

    private static void analyzeLine(String line, VideoStream videoStream) {
        Point prevPoint = videoStream.getCurrentPoint();
        if (line.contains("=")) {
            float timeValue = Float.parseFloat(line.split("=")[1]);
            if (line.contains("start")){
                prevPoint.setEnd(timeValue);
                float latestValidTimeFrame = prevPoint.getEnd() - prevPoint.getStart();
                if (latestValidTimeFrame > videoStream.getLongestValidTimeframe()) {
                    videoStream.setLongestValidTimeframe(latestValidTimeFrame);
                }
                videoStream.getValidPoints().add(videoStream.getCurrentPoint());
            }
            else if (line.contains("end")){
                videoStream.setCurrentPoint(new Point(timeValue, 0));
            }
            else if (line.contains("duration")){
                videoStream.setTotalInvalidFreezeDuration(timeValue + videoStream.getTotalInvalidFreezeDuration());
            }
        }
        else if (!line.contains("pts_time")){
            try {
                // There are 2 lines of code in order to differ the assigning
                // from the String conversion that might fail.
                Float videoDuration = Float.parseFloat(line);
                videoStream.setVideoDuration(videoDuration);
            }
            catch (Exception error) {
                System.out.println("Error detected while trying to convert unknown pattern line in file: \n"
                    + error.getMessage());
            }
        }
    }

    private static String buildFFmpegCommand(String filename) {
        return "ffmpeg -i ../" + filename + " -vf \"freezedetect=n=0.003,metadata=mode=print:file=" + filename.split("\\.")[0] + ".txt\" -map 0:v:0 -f null -" ;
    }

    private static String buildFFprobCommand(String filename) {
        return "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 ../" + filename;
    }

    private static HashSet<VideoStream> analyzeFiles(HashSet<String> urls) {
        File logsDir = new File("src/main/java/downloads/logs/");
        AtomicReference<HashSet<VideoStream>> videoStreams = new AtomicReference<>(new HashSet<>());
        urls.stream()
            .forEach(url -> {
                String filename = returnFileNameFromUrl(url);
                detectFreezeInStream(filename, logsDir);
                try {
                    addVideoDuration(filename, logsDir);
                    videoStreams.getAndSet(parseLogsIntoObjects(logsDir.getPath(), videoStreams.get()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        return videoStreams.get();
    }

    private static void detectFreezeInStream(String filename, File logsDir) {
        String FFmpegCommand = buildFFmpegCommand(filename);
        try {
            createProcessByCommandAndDir(FFmpegCommand, logsDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addVideoDuration(String filename, File logsDir) {
        String ffProbeCommand = buildFFprobCommand(filename);
        try {
            Process process = createProcessByCommandAndDir(ffProbeCommand, logsDir);
            appendOutputToLogFile(process, filename, logsDir);
        } catch (IOException | InterruptedException e) {
            System.out.println("IOException/InterruptedException in addVideoDuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Process createProcessByCommandAndDir(String command, File logsDir)
        throws IOException {
        return Runtime.getRuntime().exec(command, null, logsDir);
    }

    private static void appendOutputToLogFile(Process process, String filename, File logsDir)
        throws IOException, InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String currentLine;
        while((currentLine = reader.readLine()) != null){
            builder.append(currentLine);
        }
        File logFile = new File(logsDir.getCanonicalPath()+ "/" +filename.split("\\.")[0] + ".txt");
        FileUtils.writeStringToFile(
            logFile, builder.toString()+"\r\n", StandardCharsets.UTF_8, true);
        int exitVal = process.waitFor();
        if (exitVal != 0) throw new InterruptedException("Invalid exit status received for process waitFor.");
    }
}
