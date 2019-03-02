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
    if (board.foods.isEmpty()) return lastResortMove(position, board);
    int maxMoves = Math.min(board.height/3, 4);
    List<PositionNode> closeFoods = getCloseFoods(board.foods, position, maxMoves);
    if (closeFoods.isEmpty()) return lastResortMove(position, board);
    Position bestFood = safestFood(closeFoods, board, maxMoves);
    String move = bfs(position, bestFood, board);
    String lastResortMove = lastResortMove(position, board);
    if (move != null && isSuicide(position, Move.valueOf(move.toUpperCase()), board)) {
      move = lastResortMove;
    }
    return move == null ? lastResortMove : move ;
  }

  public static String lastResortMove(Position position, Board board) {
    Move lastResortMove;

    lastResortMove = getMoveIntoDirectionWithSpace(position,board,7);
    if(lastResortMove == null){
      //were dead
      return "left";
    }

    return lastResortMove.toString();
  }

  public static Move getMoveIntoDirectionWithSpace(Position position, Board board, int goodEnoughAmountOfSpace){
    Move bestCase = null;
    int bestAmountOfSpace = 0;

    boolean[][] visitedBoard = new boolean[board.height][board.width];
    Queue<Position> toVisit = new LinkedList<>();

    for (Move move : Move.values()) {
      if (MoveHelper.isMoveValid(position, move, board) && !isSuicide(position,move,board)) {
        int amountOfSpace = 1;
        toVisit.add(position.move(move));

        while(amountOfSpace < goodEnoughAmountOfSpace && !toVisit.isEmpty()) {
          Position currPosition = toVisit.remove();
          visitedBoard[currPosition.y][currPosition.x] = true;

          for (Move nextMove : Move.values()) {
            Position nextPosition = currPosition.move(nextMove);
            if (MoveHelper.isMoveValid(currPosition, nextMove, board) && !visitedBoard[nextPosition.y][nextPosition.x]) {
              amountOfSpace++;
              toVisit.add(nextPosition);
            }
          }
        }
        if(amountOfSpace >= goodEnoughAmountOfSpace){
          if(SnakeApplication.doLogging){LOG.info("Moving into direction: "+move.toString()+" because amount of spaces found matched good enough: "+ amountOfSpace);}
          return move;
        }
        else if(amountOfSpace > bestAmountOfSpace) {
            bestAmountOfSpace = amountOfSpace;
            bestCase = move;
        }
      }
    }
    if(SnakeApplication.doLogging){LOG.info("Moving into direction: "+bestCase.toString()+" because amount of spaces found was not good enough: "+ bestAmountOfSpace);}
    return bestCase;
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
    if(SnakeApplication.doLogging){LOG.info("returning closest move instead of safest move");}
    return closestFoodMove;



  }
  /* returns the position of food from foods which is furthest away from enemy snake
   */
  public static Position safestFood(List<PositionNode> foods, Board board, int maxAmountOfMoves) {
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
        if (currPos.distance > maxAmountOfMoves) {
          closestSnake = currPos.distance + 1;
          if(SnakeApplication.doLogging){LOG.info("Optimization triggered");}
          break;
        }
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
        if (greatestDist > maxAmountOfMoves){
          if(SnakeApplication.doLogging){LOG.info("Optimization 2 triggered");}
          break;
        }
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
    if(SnakeApplication.doLogging && move == null){LOG.info("Using last resort during follow tail");}
    return move == null ? lastResortMove(position, board) : move;
  }

  public static boolean isSuicide(Position position, Move move, Board board){
    if(SnakeApplication.Handler.youAreAlpha(board)){
      return false;
    }
    Position proposedPosition = position.move(move);
    for(int index = 0; index < board.enemyHeads.size(); index++){
      if(board.enemyLengths.get(index) > board.ourLength && Move.isPositionOneMoveAway(board.enemyHeads.get(index), proposedPosition)){
        if(SnakeApplication.doLogging){LOG.info("Move is suicide , use something else");}
        return true;
      }
    }
    return false;
  }

  public static Move returnKillMove(Position position, Board board){
    if(!SnakeApplication.Handler.youAreAlpha(board)){
      return null;
    }
    for (Move move : Move.values()) {
      if (MoveHelper.isMoveValid(position, move, board)) {
        Position proposedPosition = position.move(move);
        for(Position enemyHead : board.enemyHeads){
          if(Move.isPositionOneMoveAway(proposedPosition,enemyHead)){
            if(SnakeApplication.doLogging){LOG.info("KILL MOVE FOUND");}
            return move;
          }
        }
      }
    }
    return null;
  }
}
