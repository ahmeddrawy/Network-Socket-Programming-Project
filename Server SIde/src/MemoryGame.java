import org.json.JSONException;
import org.json.JSONObject;
import sun.management.counter.perf.PerfLongArrayCounter;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;
import java.util.Collections;

/*
    todo  need to check the winner
    todo check the other actions coming from the client
    // TODO: 31/03/19 test with multiple games
    // TODO: 02/04/19 take care of the exceptions
    todo names

 */
public class MemoryGame {
    private ServerClientThread Player1 = null;
    private ServerClientThread Player2 = null;
    private Vector Grid = null;
    private JSONObject GridSetupObj = null; ///object for the grid status sent to players in the starting of the game
    private int Player1_score = 0;
    private int Player2_score = 0 ;
    private int GameLEVEL = -1;
    JSONObject TRUE_TURN = new JSONObject();
    JSONObject FALSE_TURN = new JSONObject();
    JSONObject Disconnected = new JSONObject();
    MemoryGame(ServerClientThread p1 , ServerClientThread p2 , int GameLevel ){
        Player1 = p1;
        Player2 = p2;
        GameLEVEL = GameLevel;

        Start();    /// todo check if this removed
        shuffleGRID();
        IntializeJsonObjects();
        playing();
    }
    MemoryGame(){/// todo

    }   /// we can't have a game without players , todo check if we can make a game then assign the level and players
    private void Start(){
        Player1.start();
        Player2.start();
    }
    private void SendGridConfigurationToClients(){
        Player1.SendToclient(GridSetupObj.toString());  /// SENDING THE GRID TO BOTH
        Player2.SendToclient(GridSetupObj.toString());
    }
    void CheckDisconnection(){
        if(!Player1.connected){
            Player2.SendToclient(Disconnected.toString());
        }
        else if(!Player2.connected) {
            Player1.SendToclient(Disconnected.toString());
        }
    }
    void IntializeJsonObjects(){
        TRUE_TURN.put("TURN",true);
        TRUE_TURN.put("ACTION","SET_TURN");
        FALSE_TURN.put("TURN",false);
        FALSE_TURN.put("ACTION","SET_TURN");
        Disconnected.put("ACTION","DISCONNECTED_PLAYER" );
    }
    void playing(){
        boolean Finished = false;   /// todo
        CheckDisconnection();
        SendGridConfigurationToClients();
        int cnt = -1 ;
        while(!Finished ){
            CheckDisconnection();
            cnt+=1;
            int position1 = 0 , position2 = 0 ;
            if(cnt%2 == 0){ /// first player playing
                TRUE_TURN.put("NAME_PLAYER_1" , Player1.NameOfClient);
                TRUE_TURN.put("NAME_PLAYER_2" , Player2.NameOfClient);
                FALSE_TURN.put("NAME_PLAYER_1" , Player2.NameOfClient);
                FALSE_TURN.put("NAME_PLAYER_2" , Player1.NameOfClient);
                Player1.SendToclient(TRUE_TURN.toString());
                Player2.SendToclient(FALSE_TURN.toString());
                 position1 = GetRespondAndForward(true);
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

            }
            else { /// second player playing
                /// any player1 is me and any player2 is the opponent
                TRUE_TURN.put("NAME_PLAYER_1" , Player2.NameOfClient);
                TRUE_TURN.put("NAME_PLAYER_2" , Player1.NameOfClient);
                FALSE_TURN.put("NAME_PLAYER_1" , Player1.NameOfClient);
                FALSE_TURN.put("NAME_PLAYER_2" , Player2.NameOfClient);
                Player1.SendToclient(FALSE_TURN.toString());
                Player2.SendToclient(TRUE_TURN.toString());
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

    public int GetRespondAndForward(boolean  PlayerTurn){
        CheckDisconnection();
        String msg = null;
        JSONObject forward =null;
        int pos = 0 ;
        try {
            if (PlayerTurn == true) {
                msg = Player1.getFromClient();
                forward = SetupGameObjConstruct(msg);
//                System.out.println(forward);
                pos = forward.getInt("POSITION"); /// todo check if the action is show_tile

                Player2.SendToclient(forward.toString());

            } else {
                msg = Player2.getFromClient();
                forward = SetupGameObjConstruct(msg);
                System.out.println(forward);
                pos = forward.getInt("POSITION");
                Player1.SendToclient(forward.toString());

            }

        }
        catch (JSONException J ){
            System.out.println("error in receiving and forwarding from the clients " + J);
            Player1.stop();
            Player2.stop();
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
            JSONObject UpdatingScoreP1 = new JSONObject();
            JSONObject UpdatingScoreP2 = new JSONObject();
            if(player1_turn){
                Player1_score +=1;
                UpdatingScoreP1.put("ACTION","UPDATE_POINTS");
                UpdatingScoreP2.put("ACTION","UPDATE_POINTS");
                UpdatingScoreP1.put("PLAYER_1_POINTS", Player1_score);
                UpdatingScoreP2.put("PLAYER_1_POINTS", Player2_score);
                UpdatingScoreP1.put("PLAYER_2_POINTS", Player2_score);
                UpdatingScoreP2.put("PLAYER_2_POINTS", Player1_score);
                UpdatingScoreP1.put("NAME_PLAYER_1", Player1.NameOfClient);
                UpdatingScoreP2.put("NAME_PLAYER_1", Player2.NameOfClient);
                UpdatingScoreP1.put("NAME_PLAYER_2", Player2.NameOfClient);
                UpdatingScoreP2.put("NAME_PLAYER_2", Player1.NameOfClient);

            }
            else {
                Player2_score +=1;

                UpdatingScoreP1.put("ACTION","UPDATE_POINTS");
                UpdatingScoreP2.put("ACTION","UPDATE_POINTS");
                UpdatingScoreP2.put("PLAYER_1_POINTS", Player2_score);
                UpdatingScoreP1.put("PLAYER_1_POINTS", Player1_score);
                UpdatingScoreP2.put("PLAYER_2_POINTS", Player1_score);
                UpdatingScoreP1.put("PLAYER_2_POINTS", Player2_score);
                UpdatingScoreP2.put("NAME_PLAYER_1", Player2.NameOfClient);
                UpdatingScoreP1.put("NAME_PLAYER_1", Player1.NameOfClient);
                UpdatingScoreP2.put("NAME_PLAYER_2", Player1.NameOfClient);
                UpdatingScoreP1.put("NAME_PLAYER_2", Player2.NameOfClient);

            }
            Player1.SendToclient(FoundMatch.toString());
            Player2.SendToclient(FoundMatch.toString());
            Player1.SendToclient(UpdatingScoreP1.toString());
            Player2.SendToclient(UpdatingScoreP2.toString());

        }
    }
    public JSONObject SetupGameObjConstruct(String obj) {///todo take care of the rest of the cases
        JSONObject respond = null;
        String action = null;
        JSONObject forward = null;
        JSONObject Disconnected = new JSONObject();
        Disconnected.put("ACTION","DISCONNECTED_PLAYER" );
        if(!Player1.connected){
            Player2.SendToclient(Disconnected.toString());
//            return  null;

        }
        else if(!Player2.connected) {
            Player1.SendToclient(Disconnected.toString());
//            return  null;
        }
        try {
            forward = new JSONObject();
            respond = new JSONObject(obj);
            System.out.println("SetupGameObjConstruct");
            System.out.println(respond  );
            action = respond.getString("ACTION");
            if (action.equals("SHOW_TILE")) {
                int pos = 0;
                pos = respond.getInt("POSITION");
                forward.put("POSITION", pos);
                forward.put("ACTION", "RESPOND_PLAYER");
            } else{

            }

        }
        catch (JSONException J){
            System.out.println("Exception from the the JSON object when constructing the Grid"+ J);
            Player1.stop();
            Player2.stop();


        }

            return forward;
    }

    public void shuffleGRID(){
        int w  = 4, h = 4;
        switch (GameLEVEL){     /// todo include this in the shuffle grid
            case 0:
                w= 3 ;  h =2 ;
                break;
            case 1:
                 w= 4 ; h =  4;
                break;
            case 2:
                w = 5 ;
                h = 4;
                break;
            case 3:
                w= 6 ;
                h = 6;
                break;
            case 4:
                w = 7;
                h =6 ;
                break;
            default:
                break;

        }
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
        GridSetupObj = new JSONObject();
        GridSetupObj.put("WIDTH", w );
        GridSetupObj.put("HEIGHT", h );
        GridSetupObj.put("GRID" , Grid);
        GridSetupObj.put("ACTION","SETUP_GAME");


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
