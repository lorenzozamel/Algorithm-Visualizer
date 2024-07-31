package algorithm.visualizer;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NQueensProblem extends JPanel {
    private static final int MIN_BOARD_SIZE = 4;
    private static final int MAX_BOARD_SIZE = 8;
    private static final int CELL_SIZE = 60;
    private Image queenImage;

    private CanvasPanel canvas;
    private int boardSize = 8;
    private int[][] board;
    private List<Point> queens;
    private boolean solutionFound = false;
    private Timer timer;
    private JLabel stepCountLabel;
    private JLabel stateLabel;
    private int sliderValue = 250;
    private int currentRow;
    private int currentCol;
    private boolean backtracking;
    private int iterationsCount = 0;

    public NQueensProblem() {
        setLayout(null);  // Use null layout for absolute positioning

        // Load the queen image
        URL queenImageURL = getClass().getResource("myQueen.png");
        if (queenImageURL != null) {
            queenImage = new ImageIcon(queenImageURL).getImage();
        } else {
            System.err.println("Queen image not found.");
        }

        canvas = new CanvasPanel();
        canvas.setBorder(new LineBorder(Color.DARK_GRAY, 2));  // Adding a black border around the canvas
        canvas.setBounds(20, 20, boardSize * CELL_SIZE, boardSize * CELL_SIZE);  // Positioning the chessboard
        add(canvas);
        

        JLabel l1 = new JLabel("Choose board size:");
        l1.setBounds(670, 20, 290, 20);
        add(l1);
        

        JLabel l2 = new JLabel("Set the delay (ms)");
        l2.setBounds(675, 160, 290, 20);
        add(l2);
        

        stepCountLabel = new JLabel("Iterations: 0");
        stepCountLabel.setBounds(600, 300, 250, 30);  // Adjust position as needed
        add(stepCountLabel);
        
        // Creating the Radio box
        JRadioButton r4 = new JRadioButton("A) 4x4");
        JRadioButton r5 = new JRadioButton("B) 5x5");
        JRadioButton r6 = new JRadioButton("C) 6x6");
        JRadioButton r7 = new JRadioButton("D) 7x7");
        JRadioButton r8 = new JRadioButton("E) 8x8");
        r4.setBounds(685, 40, 290, 20);
        r5.setBounds(685, 60, 290, 20);
        r6.setBounds(685, 80, 290, 20);
        r7.setBounds(685, 100, 290, 20);
        r8.setBounds(685, 120, 290, 20);
        ButtonGroup bg = new ButtonGroup();
        bg.add(r4); bg.add(r5); bg.add(r6); bg.add(r7); bg.add(r8);
        add(r4); add(r5); add(r6); add(r7); add(r8);
        r8.setSelected(true);
        ActionListener boardSizeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == r4) { boardSize = 4;
                } else if (e.getSource() == r5) { boardSize = 5;
                } else if (e.getSource() == r6) { boardSize = 6;
                } else if (e.getSource() == r7) { boardSize = 7;
                } else if (e.getSource() == r8) { boardSize = 8; }
                resetBoard();
            }
        };
        r4.addActionListener(boardSizeListener);
        r5.addActionListener(boardSizeListener);
        r6.addActionListener(boardSizeListener);
        r7.addActionListener(boardSizeListener);
        r8.addActionListener(boardSizeListener);

        // Creating the time slider
        JSlider slider1 = new JSlider(JSlider.HORIZONTAL, 10, 1010, 150);
        slider1.setBounds(596, 180, 265, 60);
        slider1.setMinorTickSpacing(50);
        slider1.setMajorTickSpacing(200);
        slider1.setPaintTicks(true);
        slider1.setPaintLabels(true);
        slider1.addChangeListener(e -> sliderValue = slider1.getValue());
        add(slider1);

        JButton startButton = new JButton("Start Solving");
        startButton.setBounds(600, 350, 250, 50);
        startButton.addActionListener(e -> startNQueens());
        add(startButton);

        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(645, 420, 160, 40);
        resetButton.addActionListener(e -> resetBoard());
        add(resetButton);

        resetBoard();
    }

    private void resetBoard() {
        board = new int[boardSize][boardSize];
        queens = new ArrayList<>();
        solutionFound = false;
        currentRow = 0;
        currentCol = 0;
        backtracking = false;
        iterationsCount = 0;
        canvas.setBounds(20, 20, boardSize * CELL_SIZE, boardSize * CELL_SIZE);  // Update canvas size
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        canvas.repaint();
    }

    private void startNQueens() {
        resetBoard();
        currentRow = 0;
        currentCol = 0;
        backtracking = false;

        timer = new Timer(sliderValue, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iterationsCount++;
                //SwingUtilities.invokeLater(() -> stepCountLabel.setText("Steps: " + iterationsCount));
                stepCountLabel.setText("Iterations: " + iterationsCount);
                if (currentCol >= boardSize) {
                    timer.stop();
                    solutionFound = true;
                    canvas.repaint();
                    return;
                }

                if (backtracking) {
                    // Remove the last queen and move back
                    if (!queens.isEmpty()) {
                        Point lastQueen = queens.remove(queens.size() - 1);
                        board[lastQueen.x][lastQueen.y] = 0;
                        currentRow = lastQueen.x + 1;
                        currentCol = lastQueen.y;
                        backtracking = false;
                    } else {
                        timer.stop(); // No solution found
                        return;
                    }
                }

                if (currentRow < boardSize) {
                    // Move to the current cell regardless of safety
                    canvas.repaint();
                    System.out.printf("Col: %d, Row: %d\n", currentCol, currentRow);
                    if (isSafe(currentRow, currentCol)) {
                        // Place the queen
                        board[currentRow][currentCol] = 1;
                        queens.add(new Point(currentRow, currentCol));
                        currentCol++;
                        currentRow = 0;
                    } else {
                        // Try next row in this column
                        currentRow++;
                    }
                } else {
                    // We've tried all rows in this column, need to backtrack
                    backtracking = true;
                }

                canvas.repaint();
            }
        });
        timer.start();
    }

    private boolean isSafe(int row, int col) {
        // Check this row on left side
        for (int i = 0; i < col; i++)
            if (board[row][i] == 1)
                return false;

        // Check upper diagonal on left side
        for (int i = row, j = col; i >= 0 && j >= 0; i--, j--)
            if (board[i][j] == 1)
                return false;

        // Check lower diagonal on left side
        for (int i = row, j = col; j >= 0 && i < boardSize; i++, j--)
            if (board[i][j] == 1)
                return false;

        return true;
    }

    class CanvasPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard(g);
            drawQueens(g);
            drawCurrentQueen(g); // Add this line to draw the current queen position
        }

        private void drawBoard(Graphics g) {
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    if ((i + j) % 2 == 0) {
                        g.setColor(Color.WHITE);
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        private void drawQueens(Graphics g) {
            g.setColor(Color.BLACK);
            for (Point queen : queens) {
                g.drawImage(queenImage, queen.y * CELL_SIZE + 5, queen.x * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10, this);
            }
        }

        private void drawCurrentQueen(Graphics g) {
            g.setColor(Color.RED); // Optional: Change the color to differentiate the current position
            g.drawImage(queenImage, currentCol * CELL_SIZE + 5, currentRow * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10, this);
        }
    }
}
