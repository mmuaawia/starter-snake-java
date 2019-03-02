package io.battlesnake.starter;

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
    if (board.foods.isEmpty()) return shitMove(position, board);
    List<PositionNode> closeFoods = getCloseFoods(board.foods, position, board.height*7 / 10);
    if (closeFoods.isEmpty()) return shitMove(position, board);
    Position bestFood = safestFood(closeFoods, board);
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

  /* Returns move to dest position , unless none found, it returns move to closest food,
   * If no closest food, returns null
   */
  public static String bfs(Position src, Position dest, Board board) {
    boolean[][] visited = new boolean[board.height][board.width];
    Queue<PositionNode> q = new LinkedList<>();
    PositionNode srcNode = new PositionNode(src, 0);
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
          PositionNode nextPos = currPos.move(move);
          nextPos.initMove = currPos.initMove == null ? move : currPos.initMove;
          if (!visited[nextPos.y][nextPos.x])
            q.add(nextPos);
        }
      }
    }
    return closestFoodMove;



  }
  /* returns the position of food from foods which is furthest away from enemy snake
   */
  public static Position safestFood(List<PositionNode> foods, Board board) {
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
              PositionNode nextPos = currPos.move(move);
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

  public static ArrayList<PositionNode> getCloseFoods(ArrayList<PositionNode> foods, Position position, int maxAmountOfMoves){
    ArrayList<PositionNode> closerFoods = new ArrayList<>();
    for(PositionNode food : foods){
      int distance = Math.abs(food.y - position.y) + Math.abs(food.x - position.x);
      if(distance <= maxAmountOfMoves){
        closerFoods.add(food);
      }
    }  //
    return closerFoods;
  }

  public static String followTail(Position position, Position tailPos, Board board) {
      String move = bfs(position, tailPos, board);
      return move == null ? shitMove(position, board) : move;
  }

  public static boolean isSuicide(Position position, Move move, Board board){
    if(board.ourLength > board.maxEnemySnakeLength){
      return false;
    }
    Position proposedPosition = position.move(move);
    for(int index = 0; index < board.enemyHeads.size(); index++){
      if(board.enemyLengths.get(index) > board.ourLength && Move.isPositionOneMoveAway(board.enemyHeads.get(0), proposedPosition)){
        return true;
      }
    }
    return false;
  }
}
