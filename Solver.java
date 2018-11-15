
package sudokusolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Solver {
    private final int n;                 // Block size of sudoku (standar sudoku has n = 3)
    private final int n2;                // Side length of sudoku, equal to n^2;
    private int[][] board;               // The sudoku board. A -1 is interpreted as an empty entry
    private Set<Integer>[] row;          // The set of known entries in each row
    private Set<Integer>[] col;          // The set of known entries in each column
    private Set<Integer>[][] block;      // The set of known entries in each block
    private Set<Integer>[][] candidates; // A set of candidates for each entry in Sudoku
    private Set<Integer>[][] rowPoss;      // Entry i,k stores the set of possible columns which might contain number k in row i
    private Set<Integer>[][] colPoss;      // Entry j,k stores the set of possible rows which might contain number k in column j
    private Set<Integer>[][][] blockPossRow; // Entry a,b,k stores the set of rows within block (a,b) which might contain number k + 1
    private Set<Integer>[][][] blockPossCol; // Entry a,b,k stores the set of columns within block (a,b) which might contain number k + 1
    private int totalPoss;                  // Sum of sizes of all candidate sets and possibility sets (row/colPoss, blockPossRow/Col)
    private int unknowns;                   // Number of unknown entries in board
    
    public Solver(int[][] board) {
        n2 = board.length;
        n = (int) Math.sqrt(n2);
        
        this.board = board;
        
        row = (Set<Integer>[]) new Set[n2];
        col = (Set<Integer>[]) new Set[n2];
        block = (Set<Integer>[][]) new Set[n][n];       
        candidates = (Set<Integer>[][]) new Set[n2][n2];
        rowPoss = (Set<Integer>[][]) new Set[n2][n2];
        colPoss = (Set<Integer>[][]) new Set[n2][n2];
        blockPossRow = (Set<Integer>[][][]) new Set[n][n][n2];
        blockPossCol = (Set<Integer>[][][]) new Set[n][n][n2];        
        
        for( int i = 0; i < n2; i++ ) {
            row[i] = new HashSet<>();
            col[i] = new HashSet<>();
            block[i % n][i / n] = new HashSet<>();
            
            for( int j = 0; j < n2; j++ ) {
                
                candidates[i][j] = new HashSet<>();
                if( board[i][j] == -1 ) {            // -1 interpreted as empty entry
                    for( int k = 1; k <= n2; k++ )   // Add numbers 1,...,n2
                            candidates[i][j].add(k);
                } else
                    candidates[i][j].add( board[i][j] );
                    
                rowPoss[i][j] = new HashSet<>();
                colPoss[i][j] = new HashSet<>();
                blockPossRow[i % n][i / n][j] = new HashSet<>();
                blockPossCol[i % n][i / n][j] = new HashSet<>();
            }
        }
        totalPoss = 3*n2*n2*n2 + 2*n*n*n2*n; // Largest totalPoss could be
        unknowns = n2 * n2;                  // Largest unknowns could be
    }
    
    // Solves sudoku if possible. Returns true if board was solved, false if board was invalid.
    public boolean solve() {
        int state = updateAllWhilePoss();

        if( state == 1 )
            state = tryPossibilities( this );
               
        return state == 2;
    }
    
    // Recursively try all possibilities for location given by minCandidatesLoc().
    // Return 0 if board is invalid in all possibilites, 2 if board is solved.
    private int tryPossibilities( Solver previousSolver ) {
        int[] minLoc = previousSolver.minCandidatesLoc();    // Find location of smallest candidate set in previousSolver
        int x = minLoc[0];
        int y = minLoc[1];
        
        for( int k : previousSolver.candidates[x][y] ) {
            Solver currentSolver = new Solver( previousSolver.copyBoard() ); // Create a new solver whose board is that of previousSolver
            currentSolver.board[x][y] = k;                                   // but with k in the (x,y) location.
            
            int state = currentSolver.updateAllWhilePoss();
            
            if( state == 1 ) {
                state = tryPossibilities( currentSolver );    // If board not solved and not invalid, recursively apply tryPossibilities
            }  
            
            if( state == 2 ) {                                // If board solved, copy solved board to previousSolver
                previousSolver.board = currentSolver.board;
                return 2;
            }     
        }
        
        return 0;
    }
    
    // Run updateAll() as long as the board is valid, unknowns > 0, and progress can be made without
    // trial and error. Returns 0 if board becomes invalid, 1 if no progress can be made but unknowns > 0,
    // and 2 if board has been solved.
    private int updateAllWhilePoss() {
        int lastTotalPoss = totalPoss + 1;

        while ( lastTotalPoss > totalPoss && unknowns > 0 ) {
            lastTotalPoss = totalPoss;
            if( !updateAll() )
                return 0;
        }

        if( unknowns > 0 )
            return 1; 
    
        return 2;
    }
    
    // Run all update methods in proper order. Returns false if board becomes invalid.
    private boolean updateAll() {
        if( !updateKnowns() )
            return false;
        updateCandidatesViaKnownsAndBoard();

        updateRowColPossViaCandidates();
        updateRowColPossViaSelf();
        updateCandidatesViaRowColPoss();

        updateBlockPossViaCandidates();
        updateBlockPossViaSelf();
        updateCandidatesViaBlockPoss();

        updateBoardViaCandidates();
        updateBoardViaRowColPoss();
        updateBoardViaBlockPoss();

        updateTotalPoss();
        updateUnknowns();
        
        return true;
    }
    
    private void updateTotalPoss() {
        totalPoss = 0;
        
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {             
                totalPoss += candidates[i][j].size();
                totalPoss += rowPoss[i][j].size();
                totalPoss += colPoss[i][j].size();
                totalPoss += blockPossRow[i / n][i % n][j].size();
                totalPoss += blockPossCol[i / n][i % n][j].size();
            }
        }
    }
    
    private void updateUnknowns() {
        unknowns = 0;
        
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {             
                if( board[i][j] == -1 )
                    unknowns++;
            }
        }        
    }
    
    // Update board using candidate sets
    private void updateBoardViaCandidates() {
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {             
                if( candidates[i][j].size() == 1 ) {
                    board[i][j] = candidates[i][j].iterator().next();
                }
            }
        }
    }
    
    // Update board using row and column possibilites
    private void updateBoardViaRowColPoss() {
        for( int i = 0; i < n2; i++ ) {
            for( int k = 0; k < n2; k++ ) {  
                if( rowPoss[i][k].size() == 1 ) {
                    int s = rowPoss[i][k].iterator().next();
                    board[i][s] = k + 1;
                }
                if( colPoss[i][k].size() == 1 ) {
                    int r = colPoss[i][k].iterator().next();
                    board[r][i] = k + 1;
                }
            }
        }
    }
    
    // Update board using block possibilities
    private void updateBoardViaBlockPoss() {
        for( int i = 0; i < n; i++ ) {
            for( int j = 0; j < n; j++ ) {  
                for( int k = 0; k < n2; k++ ) {
                    if( blockPossRow[i][j][k].size() == 1 
                            && blockPossCol[i][j][k].size() == 1 ) {
                        
                        int r = blockPossRow[i][j][k].iterator().next();
                        int s = blockPossCol[i][j][k].iterator().next();
                        board[r][s] = k + 1;
                    }
                }
            }
        }
    }
    
    // Update knowns (row, col, block) sets using board. Returns false if 
    // board is not valid Sudoku
    private boolean updateKnowns() {
        for( int i = 0; i < n2; i++ ) {
            row[i].clear();
            col[i].clear();
            block[i / n][i % n].clear();
        }
        
        boolean b = true;
        
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {
                int entry = board[i][j];
                if( entry == -1 )        // If entry is -1, interpret as blank.
                    continue;
                if( !row[i].add( entry ) | !col[j].add(entry) 
                        | !block[i / n][j / n].add(entry) )  // Try to add entry to various sets,
                    b = false;                               // record false for b if fails.
            }                             
        }
   
        return b;
    }
    
    // Update the candidate sets using knowns and current board state
    private void updateCandidatesViaKnownsAndBoard() {        
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {
                
                if( board[i][j] == -1 ) {
                    candidates[i][j].removeAll( row[i] );
                    candidates[i][j].removeAll( col[j] );
                    candidates[i][j].removeAll( block[i / n][j / n] );
                } else {
                    candidates[i][j].clear();
                    candidates[i][j].add( board[i][j] );
                }            
            }
        }
    }
    
    // Update the candidate sets using row and column possibilities
    private void updateCandidatesViaRowColPoss() {
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {
                for( int k = 0; k < n2; k++ ) {
                    
                    if( !rowPoss[i][k].contains(j)
                            || !colPoss[j][k].contains(i) )
                        candidates[i][j].remove(k + 1);
                }
            }
        }
    }

    // Update candidate sets using block possibilities
    private void updateCandidatesViaBlockPoss() {
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {
                for( int k = 0; k < n2; k++ ) {
                    
                    if( !blockPossRow[i / n][j / n][k].contains(i)
                            || !blockPossCol[i / n][j / n][k].contains(j) )
                        candidates[i][j].remove(k + 1);
                }
            }
        }
    }
    
    // Return location (in the form of an array) of smallest candidates set which
    // is not a singleton. Returns [-1, -1] if no such.
    private int[] minCandidatesLoc() {
        int[] minLoc = {-1, -1};
        int minSize = n2 + 1;
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {
                int size = candidates[i][j].size();
                if( size > 1 && size < minSize  ) {
                    minSize = size;
                    minLoc[0] = i;
                    minLoc[1] = j;
                }
            }
        }
        return minLoc;
    }
    
    // Update rowPoss and colPoss sets using candidate sets
    private void updateRowColPossViaCandidates() {
        for( int i = 0; i < n2; i++ ) {
            for( int k = 0; k < n2; k++ ) {
                rowPoss[i][k].clear();
                colPoss[i][k].clear(); 
            }
        }
        
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {
                for( int k : candidates[i][j] ) {
                    rowPoss[i][k - 1].add(j);
                    colPoss[j][k - 1].add(i);
                }
            }
        }
    } 
    
    // Update rowPoss and colPoss using the same sets
    private void updateRowColPossViaSelf() {
        
        for( int k = 0; k < n2; k++ ) {    // Loop through possible values in rows or columns
            
            Map<Set<Integer>, Set<Integer>> rowSets = new HashMap<>(); // Map which takes a set of integers A to the set of indices i
            Map<Set<Integer>, Set<Integer>> colSets = new HashMap<>(); // such that rowPoss[i][k] is a subset of A. Similarly for colPoss.
            
            for( int i = 0; i < n2; i++ ) {    // Create maps rowSets and colSets
                
                Set<Integer> rowSet = rowPoss[i][k];
                
                
                for( Set<Integer> set : rowSets.keySet() ) {
                    if( set.containsAll(rowSet) )
                        rowSets.get( set ).add( i );
                }
                if( !rowSets.keySet().contains(rowSet) ) {
                    Set<Integer> rowSetCopy = new HashSet<>();
                    rowSetCopy.addAll(rowSet);
                    Set<Integer> indexSet = new HashSet<>();
                    indexSet.add(i);
                    rowSets.put( rowSetCopy, indexSet );
                }

                Set<Integer> colSet = colPoss[i][k];
                
                for( Set<Integer> set : colSets.keySet() ) {
                    if( set.containsAll(colSet) )
                        colSets.get( set ).add( i );
                }
                if( !colSets.keySet().contains (colSet) ) {
                    Set<Integer> colSetCopy = new HashSet<>();
                    colSetCopy.addAll(colSet);
                    Set<Integer> indexSet = new HashSet<>();
                    indexSet.add(i);
                    colSets.put( colSetCopy, indexSet );
                }    
            }  
            
            for( Set<Integer> set : rowSets.keySet() ) {
                if( set.size() == rowSets.get( set ).size() ) {    // In this case the possibilities in set are used up by rows corresponding to
                    for( int i = 0; i < n2; i++ ) {                // to indices in rowSets.get( set ), so we may remove set from all other 
                        if( !rowSets.get( set ).contains(i) )      // rowPoss sets.
                            rowPoss[i][k].removeAll( set );
                    }
                }      
            }
            
            for( Set<Integer> set : colSets.keySet() ) {
                if( set.size() == colSets.get( set ).size() ) {    // Same for colSets
                    for( int i = 0; i < n2; i++ ) {
                        if( !colSets.get( set ).contains(i) )
                            colPoss[i][k].removeAll( set );
                    }
                }      
            }
        }
    }
    
    // Update blockPoss sets using candidate sets
    private void updateBlockPossViaCandidates() {
        for( int i = 0; i < n2; i++ ) {
            for( int k = 0; k < n2; k++ ) {
                blockPossRow[i % n][i / n][k].clear();
                blockPossCol[i % n][i / n][k].clear(); 
            }
        }
        
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {
                for( int k : candidates[i][j] ) {
                    blockPossRow[i / n][j / n][k - 1].add(i);
                    blockPossCol[i / n][j / n][k - 1].add(j);
                }
            }
        }
    }
    
    // Update blockPoss sets using the same sets
    private void updateBlockPossViaSelf() {
        for( int k = 0; k < n2; k++ ) {         // Loop through possible values in blocks
            for( int i = 0; i < n; i++ ) {      // Loop blocks
                Map<Set<Integer>, Set<Integer>> rowSets = new HashMap<>(); // Map which takes a set of integers A to the set of indices j such that
                Map<Set<Integer>, Set<Integer>> colSets = new HashMap<>(); // blockPossRow[i][j][k] is a subset of A. Similarly for blockPossCol[j][i][k]
                for( int j = 0; j < n; j++ ) {  // Create rowSets and colsets maps
                    
                    Set<Integer> rowSet = blockPossRow[i][j][k];
                    
                    for( Set<Integer> set : rowSets.keySet() ) {
                        if( set.containsAll(rowSet) )
                            rowSets.get( set ).add(j);
                    }
                    if( !rowSets.keySet().contains(rowSet) ) {
                        Set<Integer> rowSetCopy = new HashSet<>();
                        rowSetCopy.addAll(rowSet);
                        Set<Integer> indexSet = new HashSet<>();
                        indexSet.add(j);
                        rowSets.put( rowSetCopy, indexSet );
                    }
                    
                    Set<Integer> colSet = blockPossCol[j][i][k];
                    
                    for( Set<Integer> set : colSets.keySet() ) {
                        if( set.containsAll(colSet) )
                            colSets.get( set ).add(j);
                    }
                    if( !colSets.keySet().contains(colSet) ) {
                        Set<Integer> colSetCopy = new HashSet<>();
                        colSetCopy.addAll(colSet);
                        Set<Integer> indexSet = new HashSet<>();
                        indexSet.add(j);
                        colSets.put( colSetCopy, indexSet );
                    }
                }
                
                for( Set<Integer> set : rowSets.keySet() ) {
                    if( set.size() == rowSets.get( set ).size() ) {   // In this case the possibilities in set are used up by blocks corresponding to
                        for( int j = 0; j < n; j++ ) {                // to indices in rowSets.get( set ), so we may remove set from all other 
                            if( !rowSets.get( set ).contains(j) )     // blockPossRow sets.
                                blockPossRow[i][j][k].removeAll( set );
                        }
                    }      
                }
                for( Set<Integer> set : colSets.keySet() ) {
                    if( set.size() == colSets.get( set ).size() ) {    // Similarly for blockPossCol sets
                        for( int j = 0; j < n; j++ ) { 
                            if( !colSets.get( set ).contains(j) )
                                blockPossCol[j][i][k].removeAll( set );
                        }
                    }      
                }
            }
        }
    }
    
    // Returns a copy of board
    private int[][] copyBoard() {
        int[][] copy = new int[n2][n2];
        for( int i = 0; i < n2; i++ )
            for( int j = 0; j < n2; j++ )
                copy[i][j] = board[i][j];
        return copy;
    }
    
    // Prints sudoku nicely when n < 4
    public void printBoard() {
        for( int i = 0; i < n2; i++ ) {
            for( int j = 0; j < n2; j++ ) {
                System.out.print( board[i][j] + " " );
                if( (j + 1) % n == 0 && j != n2 - 1 )
                    System.out.print("| ");
            }
            System.out.println("");
            if( (i + 1) % n == 0 && i != n2 - 1 ) {
                for( int k = 0; k < 2*n2 + 2*(n-1) - 1; k++ )
                    System.out.print("-");
                System.out.println("");
            }
        }
        System.out.println("");
    }

    public int[][] getBoard() {
        return board;
    }

}
