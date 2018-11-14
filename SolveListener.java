
package sudokusolver;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import static java.lang.Integer.parseInt;
import javax.swing.*;


public class SolveListener implements ActionListener {
    
    private JTextField[][] board;
    private JLabel output;
    private JButton solve;
    private JButton reset;
    private boolean[][] filled; // filled[i][j] stores true if board[i][j] was filled initially.
                                // Used to show which fields where filled initially when solved board is displayed
    private final int n;    // Block size of sudoku (standard sudoku has n = 3)
    private final int n2;   // Side length of sudoku, equal to n^2;
    
    public SolveListener( JTextField[][] board, JLabel output, JButton solve, JButton reset ) {
        this.board = board;
        this.output = output;
        this.solve = solve;
        this.reset = reset;
        n2 = board.length;
        n = (int) Math.sqrt(n2);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if( e.getSource().equals(reset) ) {     // If reset was clicked, set all text fields and label to ""
            for( int i = 0; i < n2; i++ ) {
                for ( int j = 0; j < n2; j++ ) {
                    board[i][j].setText("");
                    output.setText("");
                    enableAll();
                }
            }
        } else {                                // If solve was clicked, store board and run solver
            filled = new boolean[n2][n2];
            int[][] intBoard = new int[n2][n2];
            for( int i = 0; i < n2; i++ ) {
                for ( int j = 0; j < n2; j++ ) {
                    String entry = board[i][j].getText();
                    if( entry.isEmpty() )
                        intBoard[i][j] = -1;
                    else {
                        int num = validEntry(entry);
                        if ( num != -1 ) {   // Check if string is one of "1",...,"n2"
                            intBoard[i][j] = num;
                            filled[i][j] = true;
                        }
                        else {
                            output.setText("Invalid board");
                            disableAll();
                            return;
                        }
                             
                    }
                }
            }
            Solver solver = new Solver( intBoard );
            boolean b = solver.solve();                 // Attempt to solve board
            if( b ) {                                   // If solved, write solved board in text fields
                for( int i = 0; i < n2; i++ )
                    for ( int j = 0; j < n2; j++ ) 
                        board[i][j].setText( "" + solver.getBoard()[i][j] );
                
                output.setText("Solved!");
            } else {                                    // If board invalid, say so
                output.setText("Invalid board");
            }
            disableAll();
        }
    }
    
    // If string represents a base 10 integer between 1 and n2, returns that integer;
    // else returns -1.
    private int validEntry(String entry) {
        try{
            int num = Integer.parseInt(entry);
            if ( 1 <= num && num <= n2 )
                return num;
            else
                return -1;
        } catch(Exception ex) {
            return -1;
        }
    }
    
    private void disableAll() {
        solve.setEnabled(false);
        for( int i = 0; i < n2; i++ ) { 
            for ( int j = 0; j < n2; j++ ) {
                if( filled[i][j] )
                    board[i][j].setEnabled(false);
                else
                    board[i][j].setEditable(false);
            }
        }
    }
    
    private void enableAll() {
        solve.setEnabled(true);
        for( int i = 0; i < n2; i++ ) { 
            for ( int j = 0; j < n2; j++ ) {
                board[i][j].setEditable(true);
                board[i][j].setEnabled(true);
            }
        }
    }
}
