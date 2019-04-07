import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

public class ConnectToServerGUI extends JFrame {

    private final static String WINDOW_TITLE = "Connect to a Server";

    private final static String TITLE = "Welcome, Enter your details and press connect";
    private final static String NAME_LABEL = "Name: ";
    private final static String IP_LABEL = "Server Address: ";
    private final static String MODE_LABEL = "Mode: ";
    private final static String CONNECT_BTN = "Connect!";

    private final static String CONNECTING_STATUS = "Connecting to Server...";
    private final static String WAITING_STATUS = "Waiting for a match...";
    private final static String FAILED_STATUS = "Connection Failed!";



    private final static String[] GAME_MODES = {"Kiddo", "Easy", "Medium", "Hard", "Extreme"};

    ConnectToServerGUI(){
        // Outer Panel (top -> bottom)
        JPanel frame = new JPanel();
        frame.setLayout(new BoxLayout(frame, BoxLayout.PAGE_AXIS));// Vertical Layout
        frame.setBorder(new EmptyBorder(4, 4, 4, 4));// Padding to panel

        // Top Label
        JLabel topLabel = new JLabel(TITLE);
        frame.add(GUIHelper.leftAlign(topLabel));
        frame.add(GUIHelper.padding(0,10));

        // Name Field
        JLabel nameLabel = new JLabel(NAME_LABEL);
        JTextField nameField = new JTextField();
        frame.add(GUIHelper.leftAlign(nameLabel));
        frame.add(nameField);
        frame.add(GUIHelper.padding(0,5));

        // IP Field
        JLabel ipLabel = new JLabel(IP_LABEL);
        JTextField ipField = new JTextField("192.168.43.168");
        frame.add(GUIHelper.leftAlign(ipLabel));
        frame.add(ipField);
        frame.add(GUIHelper.padding(0,5));

        // Mode
        JLabel modeLabel = new JLabel(MODE_LABEL);
        JComboBox<String> modeCombo = new JComboBox<>(GAME_MODES);
        frame.add(GUIHelper.leftAlign(modeLabel));
        frame.add(modeCombo);
        frame.add(GUIHelper.padding(0,5));

        // Connect Button
        JButton connectBtn = new JButton(CONNECT_BTN);
        frame.add(GUIHelper.rightAlign(connectBtn));

        // Clicking connect button action
        connectBtn.addActionListener(actionEvent -> {
            topLabel.setText(CONNECTING_STATUS);
            topLabel.setForeground(Color.blue);
            connectBtn.setEnabled(false);
            // Start thread to listen on
            String server = ipField.getText();
            String playerName = nameField.getText();
            int gameMode = modeCombo.getSelectedIndex();
            Runnable connection = () -> {
                try {
                    ServerConnection conn = new ServerConnection(server);
                    // Send Client Data
                    JSONObject clientData = new JSONObject();
                    clientData.put("ACTION", "NEW_PLAYER");
                    clientData.put("NAME", playerName);
                    clientData.put("MODE", gameMode);
                    conn.writeObject(clientData);
                    String action = "WAITING";
                    while(action.equals("WAITING")){
                        SwingUtilities.invokeLater(() -> {
                            topLabel.setText(WAITING_STATUS);
                            topLabel.setForeground(Color.green);
                        });
                        JSONObject statusObj = conn.readObject();
                        action = statusObj.getString("ACTION");
                    }
                    startGame(conn);
                } catch (IOException e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        topLabel.setText(FAILED_STATUS);
                        topLabel.setForeground(Color.red);
                        connectBtn.setEnabled(true);
                    });
                }
            };
            Thread networking = new Thread(connection);
            networking.start();
        });

        // GUI Settings
        this.setTitle(WINDOW_TITLE);
        this.add(frame);// Add Contents to window
        this.pack();// Calculates Window Size
        this.setResizable(false);// Disable window resizing
        this.setLocationRelativeTo(null);// Centers Window
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);// Show Window
    }

    private void startGame(ServerConnection conn){
        new PlayGameGUI(conn);
        this.dispose();
    }
}
