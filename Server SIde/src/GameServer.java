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
    private ServerClientThread WaitingClient[]= new ServerClientThread[6];
    /// CLIENT LEVEL 3

    public  GameServer(int port){
        System.out.println("Server Started");
        try {
            int cnt = 0 ;
            mServer = new ServerSocket(port); /// to listen on
            while(true){
                cnt++;
                Socket mSocket = mServer.accept();
                ServerClientThread ComingClient = new ServerClientThread(mSocket , cnt);
                System.out.println("Client "+cnt + " connected");
                String msg = ComingClient.getFromClient();
                JSONObject NewPlayerJson  = new JSONObject(msg);
                int LevelOfGame = 0 ; ///todo
                if(NewPlayerJson.getString("ACTION").equals("NEW_PLAYER")) {
                    LevelOfGame = NewPlayerJson.getInt("MODE");
                }

                if(WaitingClient[LevelOfGame] == null){
                    JSONObject WaitingReply =new JSONObject();
                    WaitingReply.put("ACTION" , "WAITING");
                    ComingClient.SendToclient(WaitingReply.toString());
                    ComingClient.setClientName(NewPlayerJson.getString("NAME"));
                    WaitingClient[LevelOfGame] = ComingClient;
                }
                else {
                    ComingClient.setClientName(NewPlayerJson.getString("NAME"));
                    JSONObject MatchFoundJSON =new JSONObject();
                    MatchFoundJSON.put("ACTION" , "MATCH_FOUND");


                    WaitingClient[LevelOfGame].SendToclient(MatchFoundJSON.toString());
                    ComingClient.SendToclient(MatchFoundJSON.toString());
                    MemoryGame G = new MemoryGame(WaitingClient[LevelOfGame] , ComingClient , LevelOfGame);
                    WaitingClient[LevelOfGame] = null;
                }

            }
        }
        catch (IOException i ){
            System.out.println(i+"error");
        }

    }

}

