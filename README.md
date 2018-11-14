# sudoku-solver
The original problem was to solve a given sudoku (of the standard 9x9 size). I wanted to solve this problem with as little trial-filling (i.e., try filling an entry of the board randomly and seeing if it leads to an invalid board) as possible. To make this worthwhile (and more interesting) I decided write the program so that it would handle an n^2 x n^2 sudoku for n. 

The program includes a GUI that allows a user to enter an initial state of the sudoku, and contains two buttons for solving and resetting the board.

What follows is a summary of the class Solver, which contains the logic for solving the sudoku:

Instance variables:

int n; the side length of a block of the sudoku (standard sudoku have n = 3).

int n2; the side length of the whole sudoku board. Therefore, n2 = n^2 (standard sudoku have n2 = 9).

int[][] board; an n2 x n2 matrix storing the entries of the sudoku board (an empty entry is recorded as -1).

Set\<Integer\>[] row; row[i] stores the set of integers currently appearing as entries in row i.
  
Set\<Integer\>[] col; similar to row, but for columns.
  
Set\<Integer\>[][] block; block[i][j] stores the set of integers currently appearing as entries in block (i, j) (we view the blocks as forming an n x n matrix).
  
Set\<Integer\>[][] candidates; candidates[i][j] stores the set of possible values for board[i][j].
  
Set\<Integer\>[][] rowPoss; rowPoss[i][k] stores the possible locations of k + 1 in row i. More precisely, it stores those j such that k + 1 is a possible value for board[i][j].
  
Set\<Integer\>[][] colPoss; colPoss[j][k] stores those i such that k + 1 is a possible value for board[i][j].
  
Set\<Integer\>[][][] blockPossRow; blockPossRow[i][j][k] stores those r such that k + 1 might appear in the portion of row r contained in block (i, j).
  
Set\<Integer\>[][][] blockPossCol; similarly for columns
  
int totalPoss; stores the sum of the sizes of all the sets in the matrices candidates, rowPoss, colPoss, blockPossRow, blockPossCol.

int unknowns; stores the number of currently empty entries on the board.

Important methods:

solve() : The primary method, attempts to solve the sudoku. Returns true if sudoku was solved, false if sudoku was found to be invalid. It is based on the methods updateAllWhilePoss() and tryPossibilities(Solver solver).

updateAllWhilePoss() : Repeatedly updates instance variables until either board is completely filled in (so sudoku is solved), or totalPoss stops changing, meaning that further updates will have no effect. 

tryPossibilities(Solver previousSolver) : Recursively tries filling in an unknown entry of board and running updateAllWhilePoss() until board is either solved or is found to be invalid. 

updateBoardViaCandidates() : If candidates[i][j] has been reduced to a singleton {k}, sets board[i][j] = k.

updateBoardViaRowColPoss() : If rowPoss[i][k] has been reduced to a singleton {j}, sets board[i][j] = k + 1. Similarly for colPoss.

updateBoardViaBlockPoss() : If blockPossRow[i][j][k] = {r} and blockPossCol[i][j][k] = {s}, sets board[i][j] = k + 1.

updateCandidatesViaRowColPoss() : If the location (i, j) fails to be given as possibility for k + 1 by either rowPoss[i][k] or colPoss[j][k], removes k + 1 from candidates[i][j].

updateCandidatesViaBlockPoss() : If location (i, j) fails to be given as possibility for k + 1 by either of the corresponding blockPossRow or blockPossCol sets, removes k + 1 from candidates[i][j]. 

