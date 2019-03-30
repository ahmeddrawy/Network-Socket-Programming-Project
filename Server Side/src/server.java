import com.sun.istack.internal.localization.NullLocalizable;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class server {
    private Socket mSocket = null   ;
    private  ServerSocket mServer = null;

    public  server(int port){
        System.out.println("Server Started");
        try {
            int cnt = 0 ;
//            while(true) {
                mServer = new ServerSocket(port);
                while(true){
                    cnt++;

                    mSocket = mServer.accept();
                    ServerClientThread scr = new ServerClientThread(mSocket , cnt);

                    System.out.println("Client number "+cnt+ " connected");
                    scr.start();


            }
        }
        catch (IOException i ){
            System.out.println(i+"error");
        }

    }

}
