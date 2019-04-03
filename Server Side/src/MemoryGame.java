import com.sun.org.apache.regexp.internal.RESyntaxException;
import jdk.internal.org.objectweb.asm.commons.TryCatchBlockSorter;
import org.json.JSONObject;
import sun.util.locale.provider.FallbackLocaleProviderAdapter;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.util.Collection;
import java.util.Random;
import java.util.Vector;
import java.util.Collections;

/*
    todo  need to check the winner
    todo check the other actions coming from the client
    // TODO: 31/03/19  check the players names
    // TODO: 31/03/19 test with multiple games
    // TODO: 02/04/19 take care of the exceptions
    todo points and names and end game

 */
public class MemoryGame {
    private ServerClientThread Player1 = null;
    private ServerClientThread Player2 = null;
    private Vector Grid = null;
    private JSONObject jObj = null; ///object for the grid status sent to players in the starting of the game
    private int Player1_score = 0;
    private int Player2_score = 0 ;
    MemoryGame(ServerClientThread p1 , ServerClientThread p2 ){
        Player1 = p1;
        Player2 = p2;

        Start();
        shuffleGRID(4,4);/// width and height of the game
        playing();
    }
    MemoryGame(){/// todo

    }
    private void Start(){
        Player1.start();
        Player2.start();
    }
    void playing(){
        boolean Finished = false;   /// todo
        Player1.SendToclient(jObj.toString());  /// SENDING THE GRID TO BOTH
        Player2.SendToclient(jObj.toString());
        JSONObject TRUE = new JSONObject();
        JSONObject FALSE = new JSONObject();
        TRUE.put("TURN",true);
        TRUE.put("ACTION","SET_TURN");



        FALSE.put("TURN",false);
        FALSE.put("ACTION","SET_TURN");


        int cnt = 0 ;
        while(!Finished){
            cnt+=1;

//            if (cnt == 2){  /// TODO TO BE REMOVED
//                break;

            int position1 = 0 , position2 = 0 ;
            if(cnt%2 == 0){ /// first player playing
                Player1.SendToclient(TRUE.toString());
                Player2.SendToclient(FALSE.toString());

                 position1 = GetRespondAndForward(true);
//                Player1.SendToclient(TRUE.toString());
//                Player2.SendToclient(FALSE.toString());
                 position2 = GetRespondAndForward(true);
                 CheckTwoCells(position1 , position2 , true);
                 if(!checkGRID()){
                     Finished = true;
                     System.out.println("FINISHED GAME");
                     JSONObject FinishGame = new JSONObject();
                     FinishGame.put("ACTION","END_GAME");
                     Player1.SendToclient(FinishGame.toString());
                     Player2.SendToclient(FinishGame.toString());

                 }
//                String ComingMsg = Player1.getFromClient();
//                JSONObject Respond =  new JSONObject(ComingMsg);
//                String action= Respond.getString("ACITON");
//                System.out.println(action);



            }
            else { /// second player playing
                Player1.SendToclient(FALSE.toString());
                Player2.SendToclient(TRUE.toString());
                position1 = GetRespondAndForward(false);
                position2 = GetRespondAndForward(false);    /// geting the action from the user and forwaring it back to the other player
                CheckTwoCells(position1 , position2, false);
                if(!checkGRID()){
                    System.out.println("FINISHED GAME");
                    Finished = true;
                    JSONObject FinishGame = new JSONObject();
                    FinishGame.put("ACTION","END_GAME");
                    Player1.SendToclient(FinishGame.toString());
                    Player2.SendToclient(FinishGame.toString());

                }

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

    public int GetRespondAndForward(boolean  PlayerTurn){
        String msg = null;
        JSONObject forward = null;
        int pos = 0 ;
        if(PlayerTurn == true){
            msg = Player1.getFromClient();
//            System.out.println("msg from client 1" +msg);
            forward = getjObj(msg);
            pos = forward.getInt("POSITION"); /// todo check if the action is show_tile
            Player2.SendToclient(forward.toString());

        }
        else {

            msg = Player2.getFromClient();
//            System.out.println("msg from client 1" +msg);
            forward = getjObj(msg);
            pos = forward.getInt("POSITION");
            Player1.SendToclient(forward.toString());

        }
        return  pos;

    }
    public void CheckTwoCells(int pos1 , int pos2 , boolean player1_turn){
        if(Grid.get(pos1) == Grid.get(pos2) ){
            Grid.set(pos1 , -1);
            Grid.set(pos2 , -1);
            JSONObject FoundMatch = new JSONObject();
            FoundMatch.put("ACTION" , "REMOVE_TILES");
            FoundMatch.put("POSITION_1",pos1);
            FoundMatch.put("POSITION_2",pos2);
            JSONObject UpdatingScore = new JSONObject();
            if(player1_turn){
                Player1_score +=1;
                UpdatingScore.put("ACTION","UPDATE_POINTS");
                UpdatingScore.put("PLAYER_1", Player1_score);
                UpdatingScore.put("PLAYER_2", Player2_score);

            }
            else {
                Player2_score +=1;

                UpdatingScore.put("ACTION","UPDATE_POINTS");
                UpdatingScore.put("PLAYER_1", Player2_score);
                UpdatingScore.put("PLAYER_2", Player1_score);

            }
            Player1.SendToclient(FoundMatch.toString());
            Player2.SendToclient(FoundMatch.toString());
            Player1.SendToclient(UpdatingScore.toString());
            Player2.SendToclient(UpdatingScore.toString());

        }
    }
    public JSONObject getjObj(String obj) {///todo take care of the rest of the cases

        JSONObject respond = null;
        String action = null;
        JSONObject forward = null;
        forward = new JSONObject();
        respond = new JSONObject(obj);
        action = respond.getString("ACTION");
        switch (action){
            case "SHOW_TILE":
                int pos = 0;
                pos = respond.getInt("POSITION");
                forward.put("POSITION" , pos);
                forward.put("ACTION","RESPOND_PLAYER");
                break;
            default:
                    break;



        }
        return forward;

    }

}
