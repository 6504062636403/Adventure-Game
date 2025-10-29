  package mygame.main;

import javax.swing.*;

import mygame.view.GamePanel;

public class Game {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame window = new JFrame("Adventure");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setResizable(false);
            GamePanel panel = new GamePanel();
            window.setContentPane(panel);
            window.pack();
            window.setLocationRelativeTo(null);
            window.setVisible(true);
            panel.requestFocusInWindow();
        });
    }
}
