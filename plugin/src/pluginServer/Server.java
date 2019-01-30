package pluginServer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import pluginData.DataProvider;
import pluginData.ProjectData;

import java.io.IOException;
import java.net.InetSocketAddress;


public class Server extends WebSocketServer {

    private WebSocket connection;
    private DataProvider dataProvider;


    public Server(int port, DataProvider dataProvider) {
        super(new InetSocketAddress(port));
        this.dataProvider = dataProvider;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        this.connection = conn;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        message = message.trim();
        if (message.equals("currentProject")) {
            ProjectData projectData = dataProvider.getCurrentProject();
            String jsonString = projectData.toJson(); //TODO: Check if correct data form
            conn.send(jsonString);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occured on connection ");//+ conn.getRemoteSocketAddress());
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
    }

    public void shutdown() {
        try {
            stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}