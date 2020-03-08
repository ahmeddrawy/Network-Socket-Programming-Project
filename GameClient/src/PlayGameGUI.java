import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class PlayGameGUI extends JFrame {

    private final static int GRID_SIZE = 500;
    private final static String WINDOW_TITLE = "Game!";
    private final static String DISCONNECT_BTN = "Disconnect!";
    private final static String MY_TURN = "Your Turn";
    private final static String OTHER_TURN = "Other's Turn";

    private static JLabel player1_score;
    private static JLabel player2_score;
    private static JLabel turn_detector;
    private static JPanel cardsPanel;

    private static ArrayList<CardButton> buttons = new ArrayList<>();
    private static int showed[] = new int[2];

    private int buttonSideLength;
    private ImageIcon defaultImage;

    private ServerConnection conn;

    private boolean myTurn = false;

    private static int scores[] = new int[2];

    PlayGameGUI(ServerConnection conn){
        this.conn = conn;
        //Outer Frame
        BackgroundPanel frame = new BackgroundPanel("plaid.jpg", BackgroundPanel.TILED);
        frame.setLayout(new BoxLayout(frame, BoxLayout.PAGE_AXIS));// Vertical Layout
        frame.setBorder(new EmptyBorder(4, 4, 4, 4));// Padding to panel

        // Top Panel (Players, Turn, Disconnect)
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));

        // Players
        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.PAGE_AXIS));
        player1_score = new JLabel();
        player2_score = new JLabel();
        playersPanel.add(player1_score);
        playersPanel.add(player2_score);
        topPanel.add(playersPanel);
        topPanel.add(Box.createHorizontalGlue());

        // Turn
        turn_detector = new JLabel();
        topPanel.add(turn_detector);
        topPanel.add(Box.createHorizontalGlue());

        // Disconnect Button
        topPanel.add(new DisconnectButton());
        frame.add(topPanel);

        // Cards Grid
        cardsPanel = new JPanel();
        cardsPanel.setPreferredSize(new Dimension(GRID_SIZE, GRID_SIZE));
        cardsPanel.setSize(new Dimension(GRID_SIZE, GRID_SIZE));
        frame.add(cardsPanel);

        // GUI Settings
        this.setTitle(WINDOW_TITLE);
        this.setContentPane(frame);
        //this.add(frame);// Add Contents to window
        this.setSize(504, 600);
        //this.pack();// Calculates Window Size\
        this.setResizable(false);// Disable window resizing
        this.setLocationRelativeTo(null);// Centers Window
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);// Show Window

        new Thread(() -> {
            while(true){
                try {
                    if(conn.isConnected()){
                        JSONObject instruction = conn.readObject();
                        SwingUtilities.invokeLater(() -> processGUI(instruction));
                    }else{
                       handleDisconnection();
                        break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }



    class DisconnectButton extends JButton implements ActionListener {

        DisconnectButton(){
            this.setText(DISCONNECT_BTN);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            // TODO
        }
    }

    void handleDisconnection(){
        turn_detector.setText("Client Disconnected");
        disableCards();
    }

    class CardButton extends JButton implements ActionListener{
        private int position;
        private ImageIcon image;
        CardButton(int position, int imageNumber){
            this.position = position;
            this.setIcon(defaultImage);
            this.setDisabledIcon(defaultImage);
            String filename = String.format("%03d", imageNumber) + ".png";
            Image img = new ImageIcon(getClass().getResource(filename)).getImage();
            img = img.getScaledInstance(buttonSideLength, buttonSideLength,  java.awt.Image.SCALE_SMOOTH);
            this.image = new ImageIcon(img);
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(showButton(this.position)){
                // Send showed button to client
                new Thread(() -> {
                    try {
                        JSONObject jObj = new JSONObject();
                        jObj.put("ACTION", "SHOW_TILE");
                        jObj.put("POSITION", this.position);
                        conn.writeObject(jObj);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }

        void showImage(){
            this.setIcon(this.image);
            this.setDisabledIcon(this.image);
            this.setEnabled(false);
        }

        void hideImage(){
            this.setIcon(defaultImage);
            this.setDisabledIcon(defaultImage);
        }

        void removeButton(){
            this.setVisible(false);
        }

    }


    void processGUI(JSONObject request){
        String action = request.getString("ACTION");
        System.out.println(request.toString());
        if(action.equals("SETUP_GAME")){
            int width = request.getInt("WIDTH");
            int height = request.getInt("HEIGHT");
            JSONArray grid = request.getJSONArray("GRID");
            cardsPanel.setLayout(new GridLayout(width, height, 10, 10));
            buttonSideLength = (500 - (10 * (width - 1))) / width;
            // Setup default Image
            Image tmp = new ImageIcon(this.getClass().getResource("star.png")).getImage();
            tmp = tmp.getScaledInstance(buttonSideLength, buttonSideLength,  java.awt.Image.SCALE_SMOOTH);
            defaultImage = new ImageIcon(tmp);
            for(int i = 0; i < width * height; ++i){
                CardButton button = new CardButton(i, grid.getInt(i));
                cardsPanel.add(button);
                buttons.add(button);
            }
            showed[0] = -1;
            showed[1] = -1;
        }else if(action.equals("SET_TURN")){
            String player_1_name = request.getString("NAME_PLAYER_1");
            String player_2_name = request.getString("NAME_PLAYER_2");
            writeScore(player_1_name, player_2_name);
            myTurn = request.getBoolean("TURN");
            if(myTurn){
                turn_detector.setText(MY_TURN);
                enableCards();
            }else{
                turn_detector.setText(OTHER_TURN);
                disableCards();
            }
        }else if(action.equals("RESPOND_PLAYER")){
            int position = request.getInt("POSITION");
            showButton(position);
        }else if(action.equals("REMOVE_TILES")){
            int p1 = request.getInt("POSITION_1");
            int p2 = request.getInt("POSITION_2");
            new Thread(() -> {
                try {
                    buttons.get(p1).setBorder(BorderFactory.createLineBorder(Color.green, 3));
                    buttons.get(p2).setBorder(BorderFactory.createLineBorder(Color.green, 3));
                    Thread.sleep(3000);
                    buttons.get(p1).removeButton();
                    buttons.get(p2).removeButton();
                } catch (InterruptedException e) {
                    e.printStackTrace();buttons.get(p1).removeButton();
                    buttons.get(p2).removeButton();
                }
            }).start();
        }else if(action.equals("UPDATE_POINTS")){
            String player_1_name = request.getString("NAME_PLAYER_1");
            String player_2_name = request.getString("NAME_PLAYER_2");
            scores[0] = request.getInt("PLAYER_1_POINTS");
            scores[1] = request.getInt("PLAYER_2_POINTS");
            System.out.println(scores[0]);
            System.out.println(scores[1]);
            writeScore(player_1_name, player_2_name);
        }else if(action.equals("END_GAME")){
            //this.dispose();
            if(scores[0] > scores[1]){
                turn_detector.setText("You Win!!!!!");
            }else if(scores[0] < scores[1]){
                turn_detector.setText("You Lose :(");
            }else{
                turn_detector.setText("Tie ^o^");
            }
            new ConnectToServerGUI();
        }else if(action.equals("DISCONNECTED_PLAYER")){
           handleDisconnection();
        }
    }

    private boolean showButton(int position){
        System.out.println("Pos: " + position);
        if(showed[0] == -1){
            showed[0] = position;
            buttons.get(position).showImage();
        }else if(showed[1] == -1){
            showed[1] = position;
            buttons.get(position).showImage();
            disableCards();
            startHideTimer();
        }else{
            return false;
        }
        return true;
    }

    void startHideTimer(){
        new Thread(() -> {
            try {
                Thread.sleep(4000);
                SwingUtilities.invokeAndWait(() -> {
                    buttons.get(showed[0]).hideImage();
                    showed[0] = -1;
                    buttons.get(showed[1]).hideImage();
                    showed[1] = -1;
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }).start();
    }

    void enableCards(){
        for(int i = 0; i < buttons.size(); ++i){
            buttons.get(i).setEnabled(true);
        }
    }

    void disableCards(){
        for(int i = 0; i < buttons.size(); ++i){
            buttons.get(i).setEnabled(false);
        }
    }

    void writeScore(String player1, String player2){
        player1_score.setText(player1 + ": " + scores[0]);
        player2_score.setText(player2 + ": " + scores[1]);
    }
}
