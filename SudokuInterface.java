
package sudokusolver;

import java.awt.*;
import javax.swing.*;

public class SudokuInterface implements Runnable {

    private JFrame frame;
    private final int n;    // Block size of sudoku (standard sudoku has n = 3)
    private final int n2;   // Side length of sudoku, equal to n^2;
    
    public SudokuInterface( int size ) {
        n = size;
        n2 = n * n;
    }
    
    @Override
    public void run() {
        frame = new JFrame("Sudoku Solver");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        createComponents(frame.getContentPane());
        
        frame.pack();
        frame.setVisible(true);
    }
    
    private void createComponents(Container container) {
        container.setLayout(new GridBagLayout());
        
        JTextField[][] board = new JTextField[n2][n2];
        createBoardPanel( board, container );
        
        JLabel output = new JLabel();
        createOutputPanel( output, container );
        
        JButton solve = new JButton("Solve");
        JButton reset = new JButton("Reset");
        createButtonPanel( solve, reset, container );
        
        SolveListener listener = new SolveListener( board, output, solve, reset );
        solve.addActionListener( listener );
        reset.addActionListener( listener );
    }
    
    private void createBoardPanel( JTextField[][] board, Container container ) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        for( int i = 0; i < n; i++ ) {
            for( int j = 0; j < n; j++ ) {
                JPanel box = new JPanel(new GridLayout(n,n));
                box.setBorder( BorderFactory.createLineBorder(Color.black) );
                box.setPreferredSize(new Dimension(n*30, n*30));
                c.gridx = j;
                c.gridy = i;
                container.add(box, c);
                for ( int k = 0; k < n2; k++ ) {
                    board[n*i + (k / n)][n*j + k % n] = new JTextField();
                    box.add( board[n*i + (k / n)][n*j + k % n] );
                }
            }
        }
    }
    
    private void createOutputPanel( JLabel output, Container container ) {
        output.setPreferredSize(new Dimension(n2*30, 60));
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = n;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = n;
        container.add(output, c);
    }
    
    private void createButtonPanel( JButton solve, JButton reset, Container container ) {
        JPanel buttonPanel = new JPanel(new GridLayout(1,2));
        buttonPanel.setPreferredSize(new Dimension(n2*30, 60));
        buttonPanel.add(solve);
        buttonPanel.add(reset);
        
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = n;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = n + 1;
        container.add(buttonPanel, c);
    }
    
}
