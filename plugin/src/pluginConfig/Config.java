package pluginConfig;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONTokener;
import org.json.JSONObject;

public class Config {
    String project;
    int semester;
    int positionInSemester;
    int id;
    ArrayList<String> tags = new ArrayList<String>();
    String title;
    Date date;
    String link;
    JSONObject configJSON;

    public Config(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }

        String fileAsString = sb.toString();
        this.configJSON = new JSONObject(new JSONTokener((fileAsString)));
        this.project = configJSON.getString("project");
        this.semester = configJSON.getInt("semester");
        this.positionInSemester = configJSON.getInt("position_in_semester");
        this.id = configJSON.getInt("id");
    }

    public JSONObject getConfigJSON() {
        return this.configJSON;
    }
}