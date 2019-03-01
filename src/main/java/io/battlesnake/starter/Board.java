package io.battlesnake.starter;

import com.fasterxml.jackson.databind.JsonNode;

public class Board {
  public int[][] grid;
  public final int width;
  public final int height;

  public Board(int[][] grid, int width, int height) {
    this.grid = grid;
    this.width = width;
    this.height = height;
  }

  public void populateBoard(JsonNode food, JsonNode snakes, JsonNode self) {
    populateBoard(food, 1);
    // snakes include self, but difference between other snakes and self might be important later on
    //populateBoard(self, 2);
    for (JsonNode snake : snakes) {
      populateBoard(snake.get("body"), 2);
    }

  }

  private void populateBoard(JsonNode arrOfStuff, int val) {
    grid = new int[height][width];
    for (JsonNode coordinate : arrOfStuff) {
      int x = coordinate.get("x").asInt();
      int y = coordinate.get("y").asInt();
      grid[y][x] = val;
    }
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
