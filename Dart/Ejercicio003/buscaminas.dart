import 'dart:io';
import 'dart:math';

const int rows = 6;
const int cols = 10;
const int minesCount = 8;
late List<List<String>> board;
late List<List<bool>> revealed;
late List<List<bool>> flagged;
late List<List<bool>> mineBoard;

void main() {
  initializeGame();
  playGame();
}

void initializeGame() {
  board = List.generate(rows, (i) => List.filled(cols, '·'));
  revealed = List.generate(rows, (i) => List.filled(cols, false));
  flagged = List.generate(rows, (i) => List.filled(cols, false));
  mineBoard = List.generate(rows, (i) => List.filled(cols, false));

  placeMines();
}

void placeMines() {
  Random rand = Random();
  int placedMines = 0;

  while (placedMines < minesCount) {
    int r = rand.nextInt(rows);
    int c = rand.nextInt(cols);

    if (!mineBoard[r][c]) {
      if (checkQuadrantLimits(r, c, placedMines)) {
        mineBoard[r][c] = true;
        placedMines++;
      }
    }
  }
}

bool checkQuadrantLimits(int r, int c, int placedMines) {
  int q1 = 0, q2 = 0, q3 = 0, q4 = 0;
  for (int i = 0; i < rows; i++) {
    for (int j = 0; j < cols; j++) {
      if (mineBoard[i][j]) {
        if (i <= 2 && j <= 4) q1++;
        if (i <= 2 && j > 4) q2++;
        if (i > 2 && j <= 4) q3++;
        if (i > 2 && j > 4) q4++;
      }
    }
  }
  return (q1 < 2 && r <= 2 && c <= 4) ||
      (q2 < 2 && r <= 2 && c > 4) ||
      (q3 < 2 && r > 2 && c <= 4) ||
      (q4 < 2 && r > 2 && c > 4);
}

void playGame() {
  bool gameOver = false;
  int moves = 0;

  while (!gameOver) {
    printBoard();
    stdout.write('Escribe una comanda: ');
    String? input = stdin.readLineSync();
    if (input == null || input.isEmpty) continue;

    if (input.toLowerCase() == 'cheat' || input.toLowerCase() == 'trampes') {
      printMines();
      continue;
    }

    List<String> parts = input.split(' ');
    String command = parts[0];
    bool flagAction = parts.length > 1 && parts[1].toLowerCase() == 'flag';

    if (!RegExp(r'^[A-F][0-9]+\$?').hasMatch(command)) {
      print(
          'Comanda inválida. Usa una letra (A-F) seguida de un número (0-9). Ejemplo: A1');
      continue;
    }

    int r = command.codeUnitAt(0) - 'A'.codeUnitAt(0);
    int? c = int.tryParse(command.substring(1));

    if (c == null || r < 0 || r >= rows || c < 0 || c >= cols) {
      print('Comanda fuera de límites. Usa A-F y 0-9.');
      continue;
    }

    if (flagAction) {
      flagged[r][c] = !flagged[r][c];
    } else {
      if (mineBoard[r][c]) {
        print('Has perdut!');
        gameOver = true;
      } else {
        revealCell(r, c);
        moves++;
      }
    }
  }

  printBoard(revealAll: true);
  print('Número de tirades: $moves');
}

void revealCell(int r, int c) {
  if (r < 0 || r >= rows || c < 0 || c >= cols) return;
  if (revealed[r][c] || flagged[r][c]) return;

  int minesAround = countMinesAround(r, c);
  revealed[r][c] = true;
  board[r][c] = minesAround == 0 ? ' ' : '$minesAround';

  if (minesAround == 0) {
    for (int dr = -1; dr <= 1; dr++) {
      for (int dc = -1; dc <= 1; dc++) {
        if (dr != 0 || dc != 0) {
          revealCell(r + dr, c + dc);
        }
      }
    }
  }
}

int countMinesAround(int r, int c) {
  int count = 0;
  for (int dr = -1; dr <= 1; dr++) {
    for (int dc = -1; dc <= 1; dc++) {
      int nr = r + dr;
      int nc = c + dc;
      if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && mineBoard[nr][nc]) {
        count++;
      }
    }
  }
  return count;
}

void printBoard({bool revealAll = false}) {
  print(' 0123456789');
  for (int i = 0; i < rows; i++) {
    stdout.write(String.fromCharCode('A'.codeUnitAt(0) + i));
    for (int j = 0; j < cols; j++) {
      if (revealAll && mineBoard[i][j]) {
        stdout.write('*');
      } else if (flagged[i][j]) {
        stdout.write('#');
      } else if (revealed[i][j]) {
        stdout.write(board[i][j]);
      } else {
        stdout.write('·');
      }
    }
    print('');
  }
}

void printMines() {
  print(' 0123456789');
  for (int i = 0; i < rows; i++) {
    stdout.write(String.fromCharCode('A'.codeUnitAt(0) + i));
    for (int j = 0; j < cols; j++) {
      stdout.write(mineBoard[i][j] ? '*' : '·');
    }
    print('');
  }
}
