package io.battlesnake.starter;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;

public class Board {
  public int[][] grid;
  public final int width;
  public final int height;

  public ArrayList<PositionNode> foods;
  public ArrayList<PositionNode> enemyHeads;
  public ArrayList<Integer> enemyLengths;
  public int ourLength;
  public int maxEnemySnakeLength;

  public Board(int[][] grid, int width, int height) {
    this.grid = grid;
    this.width = width;
    this.height = height;
    this.enemyHeads = new ArrayList<>();
    this.enemyLengths = new ArrayList<>();
    this.foods = new ArrayList<>();
    this.ourLength = 0;
    this.maxEnemySnakeLength = 0;
  }


  public void populateBoard(JsonNode food, JsonNode snakes, JsonNode self) {
    clearBoard();
    populateBoard(food, 1);

    for (JsonNode snake : snakes) {
      if(
          self.get(0).get("x").asInt() == snake.get("body").get(0).get("x").asInt()
              && self.get(0).get("y").asInt() == snake.get("body").get(0).get("y").asInt()
      ){
        populateBoard(snake.get("body"), 4);
      }
      else{
        populateBoard(snake.get("body"), 2);
      }
    }
  }

  //0 is free block
  //1 is food
  //2 is enemy snake
  //3 is enemy snake head
  //4 is our snake
  //5 is our snake head

  private void populateBoard(JsonNode arrOfStuff, int val) {
    boolean headFound = false;

    if (val == 2) {
      int enemyLength = arrOfStuff.size();
      enemyLengths.add(enemyLength);
      if(enemyLength > maxEnemySnakeLength){
        maxEnemySnakeLength = enemyLength;
      }
    }
    else if(val == 4){
      ourLength = arrOfStuff.size();
    }

    for (JsonNode coordinate : arrOfStuff) {
      int x = coordinate.get("x").asInt();
      int y = coordinate.get("y").asInt();
      if(val == 2 && !headFound){
        headFound = true;
        enemyHeads.add(new PositionNode(new Position(x,y),0));
        grid[y][x] = 3;
        continue;
      }
      if(val == 4 && !headFound){
        headFound = true;
        grid[y][x] = 5;
        continue;
      }
      else if(val == 1){
        foods.add(new PositionNode(new Position(x,y),0));
      }
      grid[y][x] = val;
    }
  }

  private void clearBoard(){
    grid = new int[height][width];
    foods.clear();
    enemyHeads.clear();
    enemyLengths.clear();
    ourLength = 0;
    maxEnemySnakeLength = 0;
  }

  @Override
  public String toString() {
    StringBuilder boardString = new StringBuilder();
    boardString.append('\n');
    for (int row = 0; row < height; row++) {
      for (int col = 0; col < width; col++) {
        boardString.append(grid[row][col]);
        boardString.append(' ');
      }
      boardString.append('\n');
    }
    return boardString.toString();
  }
}
