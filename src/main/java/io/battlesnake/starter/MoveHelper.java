package io.battlesnake.starter;

import java.util.*;

public class MoveHelper {


  public static boolean isMoveValid(Position position, Move move, Board board) {
    Position newPosition = position.move(move);
    return (isPositionInBounds(newPosition, board.width, board.height) && board.grid[newPosition.y][newPosition.x] < 2);
  }

  public static boolean isMoveValidForEnemy(Position position, Move move, Board board) {
    Position newPosition = position.move(move);
    return isPositionInBounds(newPosition, board.width, board.height)
        && board.grid[newPosition.y][newPosition.x] < 2;
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

  public static Position bestFood(Position[] foods, Board board) {
    int[][] grid = board.grid;
    int bestPos = 0;
    int bestDist = Integer.MAX_VALUE;
    boolean visited[][];

    for (Position foodPos : foods) {
      visited = new boolean[board.height][board.width];
      int closestSnake = Integer.MAX_VALUE;

      Queue<Position> q = new LinkedList<>();
      q.add(foodPos);
      while (!q.isEmpty()) {
        Position currPos = q.poll();
        if (grid[currPos.y][currPos.x] == 3) {
            closestSnake = Math.min(closestSnake, currPos.dist);
        }


      }
    }


    return foods[bestPos];

  }

  public static Move getMoveToClosestFood(int x, int y, Board board) {
    return null;
  }

}
