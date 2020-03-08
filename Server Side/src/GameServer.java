import com.sun.istack.internal.localization.NullLocalizable;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.Scanner;

public class GameServer {
//    private Socket mSocket = null   ;
    private  ServerSocket mServer = null;
    private ServerClientThread WaitingClient = null;

    public  GameServer(int port){
        System.out.println("Server Started");
        try {
            int cnt = 0 ;
            mServer = new ServerSocket(port); /// to listen on
            while(true){
                cnt++;
                Socket mSocket = new Socket();
                mSocket  =mServer.accept();
                ServerClientThread ComingClient = new ServerClientThread(mSocket , cnt);
                System.out.println("Client "+cnt + " connected");
                if(WaitingClient == null){
                    JSONObject j =new JSONObject();
                    j.put("ACTION" , "WAITING");
                    ComingClient.SendToclient(j.toString());

                    WaitingClient = ComingClient;

                }
                else {
                    JSONObject j =new JSONObject();
                    j.put("ACTION" , "MATCH_FOUND");
                    WaitingClient.SendToclient(j.toString());
                    ComingClient.SendToclient(j.toString());
                    MemoryGame G = new MemoryGame(WaitingClient , ComingClient);
                    WaitingClient = null;
                }

            }
        }
        catch (IOException i ){
            System.out.println(i+"error");
        }

    }

}

