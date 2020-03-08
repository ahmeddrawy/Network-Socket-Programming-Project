

import org.json.JSONObject;

import javax.print.attribute.standard.MediaSize;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ServerClientThread extends Thread{
    Socket ClientSocket = null;
    int ClientNumber = 0 ;
    boolean connected = true ;
    public String NameOfClient = null;
    ServerClientThread(Socket Client , int cnt ){
        ClientSocket =  Client;
        ClientNumber = cnt;
    }
    public  void run(){ /// overriding from Thread class

    }
    public void  print(){
        System.out.println("hello "+ClientNumber);
    }
    public  String getFromClient(){
        String ret = "haven't got anything";
        try {
            DataInputStream Inserver =
                    new DataInputStream(ClientSocket.getInputStream());
            ret = Inserver.readUTF();

            System.out.println(ret);
//            Inserver.close();
            return ret;
        }
        catch (IOException i){
            System.out.println("Error getting msg from the client number"+ClientNumber);
            connected= false;

        }
        return ret;
    }
    public void SendToclient(String MessageFromGameServer){

        try {
            DataOutputStream OutServer = new DataOutputStream(ClientSocket.getOutputStream());
            OutServer.writeUTF(MessageFromGameServer);
        }
        catch (IOException i ){
            System.out.println("error "+ i + " in sending to client "+ClientNumber);
            connected = false;
        }


    }
    void setClientName(String Name){
        NameOfClient = Name;
    }



}
