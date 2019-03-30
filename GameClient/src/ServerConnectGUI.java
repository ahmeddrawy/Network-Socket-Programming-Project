import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import org.json.*;

public class ServerConnectGUI extends JFrame {


    ServerConnectGUI(){
        super("Connect to server");
        JPanel frame = new JPanel();
        frame.setLayout(new BoxLayout(frame, BoxLayout.PAGE_AXIS));
        frame.setBorder(new EmptyBorder(3, 3, 3, 3));

        JPanel titleFrame = new JPanel();
        titleFrame.setLayout(new BoxLayout(titleFrame, BoxLayout.LINE_AXIS));

        JLabel greeting = new JLabel("Please enter server ip and click connect!", SwingConstants.LEFT);
        titleFrame.add(greeting);
        frame.add(titleFrame);

        JPanel inputFrame = new JPanel();
        inputFrame.setLayout(new BoxLayout(inputFrame, BoxLayout.LINE_AXIS));

        JTextField server_tf = new JTextField("192.168.1.103");
        inputFrame.add(server_tf);

        JButton connect_btn = new JButton("Connect!");
        connect_btn.addActionListener(new ConnectToServer(this, server_tf, greeting));
        inputFrame.add(connect_btn);

        frame.add(inputFrame);

        this.add(frame);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setLayout(null);
        this.setVisible(true);
    }

    class ConnectToServer implements ActionListener{
        private JTextField server_tf;
        private JFrame frame;
        private JLabel message;
        ConnectToServer(JFrame frame, JTextField server_tf, JLabel message){
            this.server_tf = server_tf;
            this.frame = frame;
            this.message = message;
        }

        public void startGame(Socket socket){
            this.frame.dispose();
            new GameGUI(socket);
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String ip = this.server_tf.getText();
            try {
                Socket socket = new Socket(ip, 5555);
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                DataInputStream input = new DataInputStream(socket.getInputStream());

                // First Message
//                JSONObject request = new JSONObject();
//                request.put("action", "NEW_GAME");
//                output.writeUTF(request.toString());

                JSONObject response;
                String status = "WAITING";
                while(status.equals("WAITING")){
                    JButton caller = (JButton) actionEvent.getSource();
                    caller.setEnabled(false);
                    this.server_tf.setEnabled(false);
                    System.out.println("WAITING FOR MATCH");
                    this.message.setText("Waiting for match");
                    this.message.setForeground(Color.BLUE);
                    // Keep blocking till a match is found
                    String response_txt = input.readUTF();
                    System.out.println(response_txt);
                    response = new JSONObject(response_txt);
                    status = response.getString("ACTION");
                }
                startGame(socket);
                //startGame(null);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}