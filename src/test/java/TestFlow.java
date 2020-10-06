import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class TestFlow {

    private HashSet<String> urls;

    @BeforeAll
    void setUp(){
        urls = new HashSet<>();
        addUrlsFromConfig(urls);
    }

    @Test
    public void testPositiveFlow(){
        JSONObject json = AnalyzeStreams.runAnalysis(urls);
        JSONArray videosArray = (JSONArray) json.get("videos");
        assert(videosArray.size() != 3);
//        assert((boolean) json.get("all_videos_freeze_frame_synced") != false);
    }

    private void addUrlsFromConfig(HashSet<String> urls){
        JSONParser parser = new JSONParser();
        try {
            Path path = Paths.get("src/test/java/resources/urls.json");
            String content = Files.readString(path);
            JSONObject jsonObjparser = (JSONObject) parser.parse(content);
            JSONArray urlsArray = (JSONArray) jsonObjparser.get("urls");
            Iterator<JSONObject> iterator = urlsArray.iterator();
            while (iterator.hasNext()) {
                urls.add(String.valueOf(iterator.next()));
            }
        }
        catch (Exception err){
            err.printStackTrace();
        }
    }

}
