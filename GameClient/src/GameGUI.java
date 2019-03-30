import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class GameGUI extends JFrame {
    private Socket socket;
    private Image defaultIcon = new ImageIcon(this.getClass().getResource("star.png")).getImage();
    private JLabel player1 = new JLabel("Player 1: 0");
    private JLabel player2 = new JLabel("Player 2: 0");
    private JLabel turnText = new JLabel("Your Turn");
    private JButton disconnect_btn = new JButton("Disconnect");
    private JPanel gameTilesPanel = new JPanel();
    ArrayList<GameToggleButton> gridButtons = new ArrayList<>();
    boolean myTurn = false;
    JSONObject getServerInstructions(){
        JSONObject obj = new JSONObject();
        try {
            DataInputStream inputStream = new DataInputStream(this.socket.getInputStream());
            String data = inputStream.readUTF();
            System.out.println(data);
            obj = new JSONObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return obj;
    }

    void processGUIInstructions(JSONObject jObj){
        String action = jObj.getString("ACTION");
        if(action.equals("SETUP_GAME")){
            int width = jObj.getInt("WIDTH");
            int height = jObj.getInt("HEIGHT");
            JSONArray grid = jObj.getJSONArray("GRID");
            gameTilesPanel.setPreferredSize(new Dimension(500, 500));
            gameTilesPanel.setLayout(new GridLayout(width, height, 10, 10));
            int tileSide = (500 - (10 * (width - 1))) / width;
            for(int i = 0; i < height*width; ++i){
                GameToggleButton btn = new GameToggleButton(i, grid.getInt(i), tileSide);
                gridButtons.add(btn);
                gameTilesPanel.add(btn);
            }
        }else if(action.equals("SET_TURN")){

            myTurn = jObj.getBoolean("TURN");
            if(myTurn){
                turnText.setText("Your Turn");
                for(int i = 0; i < gridButtons.size(); ++i){
                    gridButtons.get(i).setEnabled(true);
                }
            }else{
                turnText.setText("Other's Turn");
                for(int i = 0; i < gridButtons.size(); ++i){
                    gridButtons.get(i).setEnabled(false);
                }
            }
        }
    }

    GameGUI(Socket socket){
        super("Match Game");
        this.socket = socket;
        JPanel frame = new JPanel();
        frame.setLayout(new BoxLayout(frame, BoxLayout.PAGE_AXIS));
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.PAGE_AXIS));
        playersPanel.add(player1);
        playersPanel.add(player2);
        topPanel.add(playersPanel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(turnText);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(disconnect_btn);
        frame.add(topPanel);
        frame.add(Box.createRigidArea(new Dimension(0, 5)));
        frame.add(gameTilesPanel);

        // Setup game
        JSONObject instruction = getServerInstructions();
        processGUIInstructions(instruction);
        // Turn Starter
        instruction = getServerInstructions();
        processGUIInstructions(instruction);

        this.add(frame);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setVisible(true);
    }



    class GameToggleButton extends JButton implements ActionListener {
        private int pos;
        private int imgNumber;
        private ImageIcon image;
        private int state;

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch(state){
                case 0:{
                    this.setIcon(image);
                    break;
                }
                case 1:{
                    this.setIcon(new ImageIcon(defaultIcon));
                    break;
                }
            }
            state = ++state % 2;
        }

        GameToggleButton(int pos, int imgNumber, int tileSide){
            tileSide -= 25; // Padding
            this.pos = pos;
            this.imgNumber = imgNumber;
            defaultIcon = defaultIcon.getScaledInstance(tileSide, tileSide,  java.awt.Image.SCALE_SMOOTH);
            this.setIcon(new ImageIcon(defaultIcon));
            this.state = 0;
            String filename = String.format("%03d", imgNumber) + ".png";
            Image img = new ImageIcon(getClass().getResource(filename)).getImage();
            img = img.getScaledInstance(tileSide, tileSide,  java.awt.Image.SCALE_SMOOTH);
            this.image = new ImageIcon(img);
            this.addActionListener(this);
        }
    }
}
