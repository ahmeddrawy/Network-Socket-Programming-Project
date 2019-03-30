import jdk.internal.org.objectweb.asm.commons.TryCatchBlockSorter;
import org.json.JSONObject;
import sun.util.locale.provider.FallbackLocaleProviderAdapter;

import javax.swing.plaf.BorderUIResource;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;
import java.util.Collections;


public class MemoryGame {
    private ServerClientThread Player1 = null;
    private ServerClientThread Player2 = null;
    private Vector Grid = null;
    private JSONObject jObj = null;
    MemoryGame(ServerClientThread p1 , ServerClientThread p2 ){
        Player1 = p1;
        Player2 = p2;

        Start();
        shuffleGRID(5,6);
        playing();
    }
    MemoryGame(){

    }
    private void Start(){
        Player1.start();
        Player2.start();
    }
    void playing(){
        boolean Finished = false;
        Player1.SendToclient(jObj.toString());
        Player2.SendToclient(jObj.toString());
        int cnt = 0 ;
        while(!Finished){
            cnt+=1;
            JSONObject TRUE = new JSONObject();
            JSONObject FALSE = new JSONObject();
            TRUE.put("TURN",true);
            TRUE.put("ACTION","SET_TURN");
            FALSE.put("TURN",false);
            FALSE.put("ACTION","SET_TURN");
            if (cnt == 2){
                break;
            }
            if(cnt%1 == 0){ /// first player playing
                Player1.SendToclient(TRUE.toString());
                Player2.SendToclient(FALSE.toString());
                String ComingMsg = Player1.getFromClient();


            }
            else { /// second player playing

                Player1.SendToclient(FALSE.toString());
                Player2.SendToclient(TRUE.toString());

            }
        }


    }
    public void shuffleGRID(int w , int h){
        int sz = h*w;

        Grid = new Vector();

        Random rand = new Random();
        for(int i = 0; i < (sz)/2 ; ++i){
            int x = rand.nextInt(27 );
            x+=1;
            Grid.add( x);
            Grid.add( x ) ;

        }
        Collections.shuffle(Grid);
        jObj = new JSONObject();
        jObj.put("WIDTH", w );
        jObj.put("HEIGHT", h );
        jObj.put("GRID" , Grid);
        jObj.put("ACTION","SETUP_GAME");


    }
    public boolean checkGRID(){
        for(int i = 0 ; i < Grid.size() ; ++i){
            if(!Grid.get(i).equals(-1) ){
                return true;
            }
        }
        return false;
    }


}
