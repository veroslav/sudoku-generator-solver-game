sudoku-generator-solver-game
============================

#### Contact: sudonkey(at)gmx(dot)com

A sudoku game featuring a puzzle generator and a solver written in Java programing language.

System Requirements: Java Runtime Environment (JRE) 1.7 or later

The following features are currently implemented:

- Generate standard 9x9 Sudoku puzzles of five different difficulty grades (Easy, Moderate, Hard, Expert and Diabolic)
- Solve any puzzle, check whether a puzzle has a unique, multiple or no solution
- Save and load puzzles in following formats: SadMan Sudoku and SudoCue Sudoku (sdk files), and Simple Sudoku (ss files)
- Export current puzzle as an image (PNG, JPEG and GIF) or to a PDF file
- Generate and export multiple puzzles to a PDF file
- Undo and redo
- Reveal clues (next digit) on player's request
- Flag wrong entries
- Pencilmark entries
- Candidate focus (show all or focus on one or more candidates)
- Enter a puzzle manually, verify it (determine difficulty grading), and allow player to use all available aid tools in order to solve the puzzle
- Puzzle symmetry can be selected before creating a new puzzle. Five symmetry types can be chosen: 180 Degree Rotational, Vertical and Horizontal mirroring, Diagonal and Anti-diagonal
- Either digits or letters can be used based on player’s preferences
- Apply colors to any cell (often needed for more difficult puzzles)
- Use keyboard or mouse for input
- Copy a puzzle from and paste to the board
- Resizable game board

The following features are planned for version 1.1:

- [IMPLEMENTED] Always offer to save changes when a puzzle has been modified
- Any combination of difficulty and symmetry settings when generating and exporting
- Show progress when generating and exporting puzzles
- Display legend below puzzles when exporting to PDF
- Export grids with pencilmarks already filled in
- Offer to also print solutions when generating and exporting puzzles
- Show hints for how the next solution step can be revealed
- Show time taken to solve a puzzle
- [IMPLEMENTED] Autofill candidates for a puzzle
- Candidate focus color highlighting
- Recent puzzles list
- Toolbar buttons
- Add about window
- Print puzzles
- User guide
