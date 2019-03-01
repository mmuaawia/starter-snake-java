package io.battlesnake.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MoveHelper {


  public static boolean isMoveValid(Position position, Move move, Board board) {
    Position newPosition = position.move(move);
    return (isPositionInBounds(newPosition, board.width, board.height) && board.grid[newPosition.y][newPosition.x] != 2);
  }

  private static boolean isPositionInBounds(Position position, int width, int height) {
    return (position.x > -1 && position.x < width && position.y > -1 && position.y < height);
  }

  public static String getMove(Position position, Board board) {
    List<Move> validMoves = new ArrayList<>();
    for (Move move : Move.values()) {
      if (MoveHelper.isMoveValid(position, move, board)) {
        validMoves.add(move);
      }
    }
    Random random = new Random();
    //will cause exception if no valid moves at the moment
    int index = random.nextInt(validMoves.size());
    return validMoves.get(index).toString();
  }

  public static Move getMoveToClosestFood(int x, int y, Board board) {
    return null;
  }

}
