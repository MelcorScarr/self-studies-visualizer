package pluginDatabase;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import pluginEvents.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;

public class DatabaseConnector {
    private Connection connection = null;

    public void connect() {
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:data.sqlite");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);  // set timeout to 30 sec.

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS user(user TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ideEvents(id INTEGER PRIMARY KEY AUTOINCREMENT, time DATE, type TEXT, message TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS projectEvents(id INTEGER PRIMARY KEY AUTOINCREMENT, time DATE, type TEXT, message TEXT, project TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS editorEvents(id INTEGER PRIMARY KEY AUTOINCREMENT, time DATE, type TEXT, message TEXT, filename TEXT, project TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS codeEvents(id INTEGER PRIMARY KEY AUTOINCREMENT, time DATE, type TEXT, message TEXT, filename TEXT, project TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS compileEvents(id INTEGER PRIMARY KEY AUTOINCREMENT, time DATE, type TEXT, message TEXT, project TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS fileList(id INTEGER PRIMARY KEY AUTOINCREMENT, filename TEXT, project TEXT, size INT)");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    String getID() {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT user FROM user");
            if (rs.isClosed()) {
                String uuid = UUID.randomUUID().toString();
                String insertUUID = "INSERT INTO user (user) VALUES ('" + uuid + "')";
                statement.executeUpdate(insertUUID);
                return uuid;
            } else {
                System.err.println("user = " + rs.getString("user"));
                return rs.getString("user");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return "none";
    }

    void writeValuesIntoDatabase(String table, ArrayList<String> fieldsList, ArrayList<String> valuesList) {
        String[] fields = fieldsList.toArray(new String[fieldsList.size()]);
        String[] values = valuesList.toArray(new String[valuesList.size()]);

        String sql = "INSERT INTO %s(%s) VALUES (%s)";
        String fieldsString = String.join(",", fields);
        String valuesString = "";
        for (String value : values) {
            valuesString += "?,";
        }
        valuesString = valuesString.substring(0, valuesString.length() - 1);
        sql = String.format(sql, table, fieldsString, valuesString);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int a = 0; a < values.length; a++) {
                pstmt.setString(a + 1, values[a]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("I tried to write:");
            System.out.println(sql);
            System.err.println(e.getMessage());
        }
    }

    JSONArray getTimelineMain() {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT strftime('%Y-%m-%dT%H:00:00.000', time) as time, sum(type) AS type FROM codeEvents GROUP BY strftime('%Y-%m-%dT%H:00:00.000', time);");
            return ResultSetConverter.convert(rs);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    JSONArray getTimelineBounds() {
        try {
            JSONArray jsonMax;
            JSONArray jsonMin;
            Statement statement = connection.createStatement();
            ResultSet rsMax = statement.executeQuery("SELECT max(time) as maxTime FROM codeEvents;");
            jsonMax = ResultSetConverter.convert(rsMax);
            ResultSet rsMin = statement.executeQuery("SELECT min(time) as minTime FROM codeEvents;");
            jsonMin = ResultSetConverter.convert(rsMin);
            jsonMin.put(jsonMax.getJSONObject(0));
            return jsonMin;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    void logIdeEvent(IDEEvent event) {
        ArrayList<String> standardFieldsArrayList = getStandardFieldNames();
        ArrayList<String> standardValuesArrayList = getStandardValues(event);
        writeValuesIntoDatabase("ideEvents", standardFieldsArrayList, standardValuesArrayList);
    }

    void logProjectEvent(ProjectEvent event) {
        ArrayList<String> standardFieldsArrayList = getStandardFieldNames();
        standardFieldsArrayList.add("project");
        ArrayList<String> standardValuesArrayList = getStandardValues(event);
        standardValuesArrayList.add(event.getName());
        writeValuesIntoDatabase("projectEvents", standardFieldsArrayList, standardValuesArrayList);
    }

    void logEditorEvent(EditorEvent event) {
        ArrayList<String> standardFieldsArrayList = getStandardFieldNames();
        standardFieldsArrayList.add("project");
        standardFieldsArrayList.add("filename");
        ArrayList<String> standardValuesArrayList = getStandardValues(event);
        standardValuesArrayList.add(event.getName());
        standardValuesArrayList.add(event.getFileName());
        writeValuesIntoDatabase("editorEvents", standardFieldsArrayList, standardValuesArrayList);
    }

    void logCompileEvent(CompileEvent event) {
        ArrayList<String> standardFieldsArrayList = getStandardFieldNames();
        standardFieldsArrayList.add("project");
        ArrayList<String> standardValuesArrayList = getStandardValues(event);
        standardValuesArrayList.add(event.getName());
        writeValuesIntoDatabase("compileEvents", standardFieldsArrayList, standardValuesArrayList);
    }

    void logCodeEvent(CodeEvent event) { //TODO: Find coding event error?
        String updateFile = "UPDATE fileList SET size = ? WHERE id = ?";
        String insertChange = "INSERT INTO codeEvents(time, filename, project, type) VALUES(?,?,?,?)";
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM fileList WHERE project='" + event.getName() + "'AND filename='" + event.getFilename() + "'");
            if (rs.isClosed()) {
                System.out.println("File not found yet. Adding.");
                logNewFile(event.getName(), event.getFilename(), Integer.parseInt(event.getMessage()));
            } else {
                System.out.println("File was found. Editing.");
                int id = rs.getInt("id");
                int previousSize = Integer.parseInt(rs.getString("size"));
                try (PreparedStatement pstmt = connection.prepareStatement(updateFile)) {
                    pstmt.setString(1, event.getMessage());
                    pstmt.setInt(2, id);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
                try (PreparedStatement pstmt = connection.prepareStatement(insertChange)) {
                    int newSize = Integer.parseInt(event.getMessage()) - previousSize;
                    pstmt.setString(1, event.getTimestamp().toString());
                    pstmt.setString(2, event.getFilename());
                    pstmt.setString(3, event.getName());
                    pstmt.setString(4, Integer.toString(newSize));
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println(e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    ArrayList<String> getStandardFieldNames() {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("time");
        arrayList.add("type");
        arrayList.add("message");
        return arrayList;
    }

    ArrayList<String> getStandardValues(Event event) {
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add(event.getTimestamp().toString());
        arrayList.add(event.getEventType().toString());
        arrayList.add(event.getMessage());
        return arrayList;
    }

    void logNewFile(String project, String name, int size) {
        String updateFileListSQL = "INSERT INTO fileList(project, name, size) VALUES(?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(updateFileListSQL)) {
            pstmt.setString(1, project);
            pstmt.setString(2, name);
            pstmt.setInt(3, size);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void logEvent(Event event) {
        switch (event.getEventType()) {
            case IDE:
                IDEEvent ideEvent = (IDEEvent) event;
                logIdeEvent(ideEvent);
                break;
            case PROJECT:
                ProjectEvent projectEvent = (ProjectEvent) event;
                logProjectEvent(projectEvent);
                break;
            case EDITOR:
                EditorEvent editorEvent = (EditorEvent) event;
                logEditorEvent(editorEvent);
                break;
            case COMPILE:
                CompileEvent compileEvent = (CompileEvent) event;
                logCompileEvent(compileEvent);
                break;
            case CODE:
                System.out.println("CASE: CODE");
                CodeEvent codeEvent = (CodeEvent) event;
                logCodeEvent(codeEvent);
                break;

        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
