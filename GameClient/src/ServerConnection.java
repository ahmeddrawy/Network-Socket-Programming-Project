import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public class ServerConnection {
    private final static int PORT = 5555;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean connected = false;


    ServerConnection(String ip) throws IOException {
        socket = new Socket(ip, PORT);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        connected = true;
    }

    JSONObject readObject() throws IOException {
        JSONObject jObj = new JSONObject();
        try{
            String message = dis.readUTF();
            jObj = new JSONObject(message);
        }catch (JSONException e){
            e.printStackTrace();
            connected = false;
        }catch(IOException e){
            e.printStackTrace();
            connected = false;
        }
        return jObj;
    }

    public boolean isConnected() {
        return connected;
    }

    void writeObject(JSONObject jObj) throws IOException {
        dos.writeUTF(jObj.toString());
        dos.flush();
    }
}
