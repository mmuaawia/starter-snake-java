package io.battlesnake.starter;

import java.util.*;

public class MoveHelper {


  public static boolean isMoveValid(Position position, Move move, Board board) {
    Position newPosition = position.move(move);
    return (isPositionInBounds(newPosition, board.width, board.height) && board.grid[newPosition.y][newPosition.x] < 2);
  }

  public static boolean isMoveValidForFoodToEnemy(Position position, Move move, Board board) {
    Position newPosition = position.move(move);
    return isPositionInBounds(newPosition, board.width, board.height)
        && (board.grid[newPosition.y][newPosition.x] < 2
        || board.grid[newPosition.y][newPosition.x] == 3);
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

  public static Position bestFood(PositionNode[] foods, Board board) {
    int[][] grid = board.grid;
    int bestFoodIndex = 0;
    int greatestDist = Integer.MIN_VALUE;
    boolean visited[][];

    int i = 0;
    for (PositionNode foodPos : foods) {
      visited = new boolean[board.height][board.width];
      int closestSnake = Integer.MAX_VALUE;

      Queue<PositionNode> q = new LinkedList<>();
      q.add(foodPos);
      while (!q.isEmpty()) {
        PositionNode currPos = q.poll();
        visited[currPos.y][currPos.x] = true;
        if (grid[currPos.y][currPos.x] == 3) {
            closestSnake = currPos.distance;
            break;
        }
        for (Move move : Move.values()) {
          if (isMoveValidForFoodToEnemy(currPos, move, board)) {
              PositionNode nextPos = (PositionNode) currPos.move(move);
              nextPos.distance = currPos.distance + 1;
              if (!visited[nextPos.y][nextPos.x])
                q.add(nextPos);
          }
        }
      }
      if (closestSnake > greatestDist) {
        greatestDist = closestSnake;
        bestFoodIndex = i;
      }
      i++;

    }


    return foods[bestFoodIndex];
  }

  public static Move getMoveToClosestFood(int x, int y, Board board) {
    return null;
  }

}
