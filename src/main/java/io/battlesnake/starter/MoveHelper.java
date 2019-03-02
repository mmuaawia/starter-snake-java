package io.battlesnake.starter;

import javafx.geometry.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MoveHelper {
  private static final Logger LOG = LoggerFactory.getLogger(MoveHelper.class);

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
    List<PositionNode> closeFoods = getCloseFoods(board.foods, position, board.height);
    Position bestFood = bestFood(closeFoods, board);
    String move = bfs(position, bestFood, board);
    return move == null ? shitMove(position, board) : move ;


  }

  public static String shitMove (Position position, Board board) {
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

  private static String bfs(Position src, Position dest, Board board) {
    boolean[][] visited = new boolean[board.height][board.width];
    Queue<PositionNode> q = new LinkedList<>();
    PositionNode srcNode = (PositionNode) src;
    srcNode.initMove = null;
    q.add(srcNode);
    String closestFoodMove = null;
    while (!q.isEmpty()) {
      PositionNode currPos = q.poll();
      visited[currPos.y][currPos.x] = true;
      if (board.grid[currPos.y][currPos.x] == 1) {
        closestFoodMove = currPos.initMove.toString();
      }
      if (dest.y == currPos.y && dest.x == currPos.x) {
        return currPos.initMove.toString();
      }

      for (Move move : Move.values()) {
        if (isMoveValid(currPos, move, board)) {
          PositionNode nextPos = (PositionNode) currPos.move(move);
          nextPos.initMove = currPos.initMove == null ? move : currPos.initMove;
          if (!visited[nextPos.y][nextPos.x])
            q.add(nextPos);
        }
      }
    }
    return closestFoodMove;



  }

  public static Position bestFood(List<PositionNode> foods, Board board) {
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
    return foods.get(bestFoodIndex);
  }

  public static ArrayList<PositionNode> getCloseFoods(ArrayList<PositionNode> foods, Position position, int range){
    ArrayList<PositionNode> closerFoods = new ArrayList<>();
    for(PositionNode food : foods){
      if(Math.abs(food.y - position.y) < range && Math.abs(food.x - position.x) < range){
        closerFoods.add(food);
      }
    }
    return closerFoods;
  }


}
