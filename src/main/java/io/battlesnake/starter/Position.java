package io.battlesnake.starter;

public class Position {

  public final int x;
  public final int y;

  Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Position move(Move move) {
    int nextX = x + move.xChange;
    int nextY = y + move.yChange;

    return new Position(nextX, nextY);
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }
}
