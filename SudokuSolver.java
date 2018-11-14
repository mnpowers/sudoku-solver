
package sudokusolver;

import java.util.Arrays;
import java.util.Set;
import javax.swing.SwingUtilities;

public class SudokuSolver {

    public static void main(String[] args) {
        SudokuInterface si = new SudokuInterface(4);
        SwingUtilities.invokeLater(si); 
    }
}
