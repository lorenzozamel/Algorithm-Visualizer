package algorithm.visualizer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;


public class KnightsTour extends JPanel {
    private static final int BOARD_SIZE = 8;
    private static final int CELL_SIZE = 60;
    int[] dx = {2, 1, -1, -2, -2, -1, 1, 2}; // origin is at upper left
    int[] dy = {1, 2, 2, 1, -1, -2, -2, -1}; // clockwise. i = 0 means right down

    private CanvasPanel canvas;
    private Image knightImage;
    private int knightX = 2; // initial knight position
    private int knightY = 2;
    private int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
    private boolean dragging = false;
    private Point dragOffset = new Point();
    private int dragKnightX = -1;
    private int dragKnightY = -1;
    private boolean solutionFound = false;
    private int c = 0;
    private int sliderValue  = 250;
    private int dpc = 0;
    private Font stringFont = new Font( "SansSerif", Font.BOLD, 18 );
    private JButton button;
    private JLabel l3;
    private JLabel l4;
    private boolean showLabel = true;


    private java.util.List<Point> path; // Store the path of the knight
    private Timer timer; // Timer for animation
    private int currentStep; // Current step in the path

    public KnightsTour() {
        setLayout(null);  // Use null layout for absolute positioning

        // Load the knight image
        URL knightImageURL = getClass().getResource("knight.png");
        if (knightImageURL != null) {
            knightImage = new ImageIcon(knightImageURL).getImage();
        } else {
            System.err.println("Knight image not found.");
        }

        canvas = new CanvasPanel();
        canvas.setBounds(20, 20, BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE);  // Positioning the chessboard
        canvas.setBorder(new LineBorder(Color.DARK_GRAY, 2));  // Adding a black border around the canvas
        add(canvas);

        // Creating the combo box
        String[] solutions = {"Warnsdorf's rule", "Backtracking", "Random"};
        JComboBox<String> cb = new JComboBox<>(solutions);
        cb.setBounds(600, 100, 250, 30);
        cb.addActionListener(e -> showLabel = false);
        
        add(cb);
        
        // Create the slider
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 100, 2000, 250);
        slider.setBounds(596, 180, 265, 60);
        slider.setMinorTickSpacing(95);
        slider.setMajorTickSpacing(380);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> sliderValue = slider.getValue());
        add(slider);
        
        // Creating labels
        JLabel l1 = new JLabel("Choose an algorithm to solve!");
        l1.setBounds(640, 60, 290, 20);
        add(l1);
        
        JLabel l2 = new JLabel("Set the delay (ms)");
        l2.setBounds(675, 160, 290, 20);
        add(l2);
        
        l3 = new JLabel("Warnsdorf's Rule: The knight always moves to");
        l3.setBounds(585, 240, 400, 90);
        add(l3);
        
        
        l4 = new JLabel("the square with the fewest onward moves available");
        l4.setBounds(575, 265, 400, 90);
        add(l4);


        // Creating the start tour button
        JButton button = new JButton("Start Tour");
        button.setBounds(600, 350, 250, 50);  // Positioning the button
        button.addActionListener(e -> startKnightsTour(knightY, knightX, cb.getSelectedIndex()));
        add(button);
        
        JButton resetButton = new JButton("Reset");
        resetButton.setBounds(645, 420, 160, 40);  // Position it below the "Start Tour" button
        resetButton.addActionListener(e -> resetBoard());
        add(resetButton);

        // Initialize the path
        path = new java.util.ArrayList<>();
    }
    private void resetBoard() {
        // Reset the board state
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = -1;
            }
        }

        // Reset the knight's position
        knightX = 2;
        knightY = 2;

        // Clear the path
        path.clear();

        // Reset other variables
        currentStep = 0;
        solutionFound = false;

        // Stop the timer if it's running
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        // Reset step moves
        stepMoves.clear();

        // Repaint the canvas
        canvas.repaint();
    }
    
    private void updateAlgorithmDescription(String selectedAlgorithm) {
        String description;
        if (selectedAlgorithm.equals("Warnsdorf's rule")) {
            description = "<html><div style='width:200px;'>Warnsdorf's Rule: The knight always moves to the square with the fewest onward moves available.</div></html>";
        } else if (selectedAlgorithm.equals("Backtracking")) {
            description = "<html><div style='width:200px;'>Backtracking: Systematically explores all possible moves, backtracking when it reaches a dead end until a full tour is found.</div></html>";
        } else {
            description = "Select an algorithm to see its description.";
        }
        l3.setText(description);
    }
    private void startKnightsTour(int startCol, int startRow, int index) {
        System.out.printf("knight position (x, y): %d, %d\n", knightX, knightY);
        System.out.printf("method selected (index): %d\n", index);

        path.clear();
        currentStep = 0;

        if (index == 0) {
            // Warnsdorf's rule
            System.out.println("W");
            
            if (solveKnightsTourWarnsdorf(knightY, knightX)) {
                System.out.println("Solution found!");
                printSolution();
                startAnimation();
            } else {
                System.out.println("No solution found.");
            }
        } else if (index == 1) {
            // Backtracking
            System.out.println("B");
            if (solveKnightsTourBacktracking(startCol, startRow)) {
                System.out.printf("Solution found!, c: %d\n", c);
                printSolution();
                startAnimation();
                c = 0;
            } else {
                System.out.println("No solution found.");
            }
        } else if (index == 2) {
            // Random
            System.out.println("R");
        } else {
            System.err.print("Something went wrong");
        }
    }

    private void startAnimation() {
        currentStep = 0;
        timer = new Timer(10, new ActionListener() { // Use a fixed, short interval
            private long lastUpdateTime = System.currentTimeMillis();

            @Override
            public void actionPerformed(ActionEvent e) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime >= sliderValue) {
                    if (currentStep < path.size()) {
                        Point p = path.get(currentStep);
                        knightX = p.y;
                        knightY = p.x;
                        canvas.repaint();
                        currentStep++;
                        lastUpdateTime = currentTime;
                    } else {
                        timer.stop();
                        solutionFound = true;
                        canvas.repaint();
                    }
                }
            }
        });
        timer.start();
    }

    private void printSolution() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                System.out.printf("%2d ", board[i][j]);
            }
            System.out.println();
        }
    }

    private boolean solveKnightsTourBacktracking(int startX, int startY) {
        // Initialize the board with -1 indicating unvisited cells
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = -1;
            }
        }
        // Starting position
        board[startX][startY] = 0;

        path.add(new Point(startX, startY));

        // Start the tour from the initial position
        if (!solveKnightsTourBacktrackingUtil(startX, startY, 1)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean solveKnightsTourBacktrackingUtil(int x, int y, int movei) {
        c++;
        if (movei == BOARD_SIZE * BOARD_SIZE) {
            return true;
        }

        for (int k = 0; k < 8; k++) {
            int nextX = x + dx[k];
            int nextY = y + dy[k];
            if (isSafe(nextX, nextY)) {
                board[nextX][nextY] = movei;
                path.add(new Point(nextX, nextY));
                if (solveKnightsTourBacktrackingUtil(nextX, nextY, movei + 1)) {
                    return true;
                } else {
                    // Backtrack
                    board[nextX][nextY] = -1;
                    path.remove(path.size() - 1);
                }
            }
        }
        return false;
    }

    private boolean isSafe(int x, int y) {
        return limit(x, y) && board[x][y] == -1;
    }

    private boolean solveKnightsTourWarnsdorf(int startX, int startY) {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = -1;
            }
        }
        board[startX][startY] = 0;

        path.add(new Point(startX, startY));

        if (!solveKnightsTourWarnsdorfUtil(startX, startY, 1)) {
            return false;
        } else {
            return true;
        }
    }
    
    private class Move {
        int x, y, degree;
        Color color;

        Move(int x, int y, int degree) {
            this.x = x;
            this.y = y;
            this.degree = degree;
        }

        int getDegree() { return degree; }
        void setColor(Color c) { color = c; }
    }

    private List<List<Move>> stepMoves = new ArrayList<>();

    
    private boolean solveKnightsTourWarnsdorfUtil(int x, int y, int movei) {
        if (movei == BOARD_SIZE * BOARD_SIZE) {
            return true;
        }

        List<Move> possibleMoves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int nextX = x + dx[i];
            int nextY = y + dy[i];
            if (isSafe(nextX, nextY)) {
                possibleMoves.add(new Move(nextX, nextY, getDegree(nextX, nextY)));
            }
        }

        possibleMoves.sort(Comparator.comparingInt(Move::getDegree));

        // Assign colors to all possible moves
        for (int i = 0; i < possibleMoves.size(); i++) {
            possibleMoves.get(i).setColor(getMoveColor(i));
        }

        // Store the possible moves for this step
        stepMoves.add(new ArrayList<>(possibleMoves));

        for (Move move : possibleMoves) {
            board[move.x][move.y] = movei;
            path.add(new Point(move.x, move.y));

            if (solveKnightsTourWarnsdorfUtil(move.x, move.y, movei + 1)) {
                return true;
            } else {
                board[move.x][move.y] = -1;
                path.remove(path.size() - 1);
            }
        }

        stepMoves.remove(stepMoves.size() - 1);
        return false;
    }
    
    private Color getMoveColor(int rank) {
        switch (rank) {
            case 0:
                return Color.GREEN; // Best move
            case 1:
                return Color.YELLOW; // Second best move
            case 2:
                return Color.ORANGE; // Third best move
            default:
                return Color.RED; // All other moves
        }
    }
    
    private int getDegree(int x, int y) {
        int count = 0;
        for (int k = 0; k < 8; k++) {
            int nextX = x + dx[k];
            int nextY = y + dy[k];
            if (isSafe(nextX, nextY)) {
                count++;
            }
        }
        return count;
    }

    private boolean limit(int x, int y) {
        return (x >= 0) && (y >= 0) && (x < BOARD_SIZE) && (y < BOARD_SIZE);
    }

    
    class CanvasPanel extends JPanel {
        CanvasPanel() {
            setPreferredSize(new Dimension(BOARD_SIZE * CELL_SIZE, BOARD_SIZE * CELL_SIZE));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!solutionFound) {
                        int x = e.getX() / CELL_SIZE;
                        int y = e.getY() / CELL_SIZE;
                        if (knightX == x && knightY == y) {
                            dragging = true;
                            dragKnightX = e.getX();
                            dragKnightY = e.getY();
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (dragging) {
                        knightX = e.getX() / CELL_SIZE;
                        knightY = e.getY() / CELL_SIZE; //meter un system out?
                        canvas.repaint();
                        dragging = false;
                        dragKnightX = -1;
                        dragKnightY = -1;
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    dragKnightX = e.getX();
                    dragKnightY = e.getY();
                    repaint();
                }
            }
        });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawBoard(g);
            if (!solutionFound) {
                if (!path.isEmpty()) {
                    drawPartialSolution(g);
                }
                drawKnight(g);
            } else {
                drawSolution(g);
            }
        }
        
        private void drawKnight(Graphics g) {
            if (dragging) {
                // Draw the knight at the dragging position
                g.drawImage(knightImage, dragKnightX - CELL_SIZE / 2, dragKnightY - CELL_SIZE / 2, CELL_SIZE, CELL_SIZE, this);
            } else {
                // Draw the knight at its current position on the board
                g.drawImage(knightImage, knightX * CELL_SIZE, knightY * CELL_SIZE, CELL_SIZE, CELL_SIZE, this);
            }
        }
            
        private void drawPath(Graphics g) {
            if (path.size() < 2) return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLUE);
            g2d.setStroke(new BasicStroke(3)); // Adjust the line thickness as needed

            for (int i = 1; i < currentStep; i++) {
                Point start = path.get(i - 1);
                Point end = path.get(i);

                int startX = start.y * CELL_SIZE + CELL_SIZE / 2;
                int startY = start.x * CELL_SIZE + CELL_SIZE / 2;
                int endX = end.y * CELL_SIZE + CELL_SIZE / 2;
                int endY = end.x * CELL_SIZE + CELL_SIZE / 2;

                g2d.drawLine(startX, startY, endX, endY);
            }
        }
        
        private void drawBoard(Graphics g) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    if ((i + j) % 2 == 0) {
                        g.setColor(Color.WHITE);
                    } else {
                        g.setColor(Color.LIGHT_GRAY);
                    }
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        private void drawSolution(Graphics g) {
            // Draw the board
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    g.setColor((i + j) % 2 == 0 ? Color.WHITE : Color.LIGHT_GRAY);
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }

            // Draw the path
            drawPath(g);

            // Draw the knight at its final position
            g.drawImage(knightImage, knightX * CELL_SIZE, knightY * CELL_SIZE, CELL_SIZE, CELL_SIZE, this);
        }

        private Point getCenteredStringPosition(Graphics g, String str, int cellX, int cellY) {
            FontMetrics fm = g.getFontMetrics();
            int stringWidth = fm.stringWidth(str);
            int stringHeight = fm.getAscent();
            int x = cellX * CELL_SIZE + (CELL_SIZE - stringWidth) / 2;
            int y = cellY * CELL_SIZE + (CELL_SIZE + stringHeight) / 2;
            return new Point(x, y);
        }
        
        
        private void drawPartialSolution(Graphics g) {
            // Draw the path
            drawPath(g);

            // Draw the colored cells for possible moves
            if (currentStep > 0 && currentStep <= stepMoves.size()) {
                for (Move move : stepMoves.get(currentStep - 1)) {
                    g.setColor(move.color);
                    g.fillOval(move.y * CELL_SIZE, move.x * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.BLACK);
                    Point strPos = getCenteredStringPosition(g, String.valueOf(move.degree), move.y, move.x);
                    g.drawString(String.valueOf(move.degree), strPos.x, strPos.y);
                }
            }

            // Draw the path numbers
            for (int i = 0; i < currentStep; i++) {
                Point p = path.get(i);
                g.setColor(Color.BLACK);
                g.setFont(stringFont);
                Point strPos = getCenteredStringPosition(g, String.valueOf(board[p.x][p.y]), p.y, p.x);
                //g.drawString(String.valueOf(board[p.x][p.y]), strPos.x, strPos.y);
            }
        }
        
    }
}
