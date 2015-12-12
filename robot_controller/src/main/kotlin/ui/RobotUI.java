package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author plorio
 */
public class RobotUI extends JFrame {
    private JPanel root;
    private JButton dropButton;
    private JLabel angles;
    private JLabel state;
    private JLabel video1;
    private JLabel video2;

    public RobotUI(BatClicked clicked) {
        super("Robot");
        setContentPane(root);
        root.setSize(900, 500);
        pack();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);

        dropButton.addActionListener(e -> clicked.bat());
    }

    public void setState(String text) {
        state.setText(text);
    }

    public void setAngles(String text) {
        angles.setText(text);
        angles.setBounds(0, 0, 1000, 100);
    }

    public void setVideo1(BufferedImage img) {
        video1.setIcon(new ImageIcon(img));
        video1.setText("");
    }

    public void setVideo2(BufferedImage img) {
        video2.setIcon(new ImageIcon(img));
        video2.setText("");
    }

    public void enableDrop(boolean enable) {
        dropButton.setEnabled(enable);
    }

    public void draw() {
        root.repaint();
    }

    interface BatClicked {
        void bat();
    }
}
