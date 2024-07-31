package algorithm.visualizer;

import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class AlgorithmVisualizer {

    
    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Visualizing Algorithms");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);

        JTabbedPane tabbedPaneKnight = new JTabbedPane();
        tabbedPaneKnight.addTab("Knight's Tour", new KnightsTour());
        frame.add(tabbedPaneKnight);
        
        JTabbedPane tabbedPaneQueen = new JTabbedPane();
        tabbedPaneKnight.addTab("Queen's Problem", new NQueensProblem());
        frame.add(tabbedPaneKnight);
        frame.setVisible(true);
        System.out.println("GUI initialized");
        });
    }
}

