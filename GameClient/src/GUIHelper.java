import javax.swing.*;
import java.awt.*;

public class GUIHelper {
    static Component padding(int w, int h){
        return Box.createRigidArea(new Dimension(w, h));
    }

    static JPanel leftAlign(Component c){
        JPanel parent = new JPanel();
        parent.setLayout(new BoxLayout(parent, BoxLayout.LINE_AXIS));
        parent.add(c);
        parent.add(Box.createHorizontalGlue());
        return parent;
    }

    static JPanel rightAlign(Component c){
        JPanel parent = new JPanel();
        parent.setLayout(new BoxLayout(parent, BoxLayout.LINE_AXIS));
        parent.add(Box.createHorizontalGlue());
        parent.add(c);
        return parent;
    }
}
