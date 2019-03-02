package io.battlesnake.starter;

public class PositionNode extends Position {
  public int distance;
  public Move initMove = Move.LEFT;

  PositionNode(Position position, int distance) {
    super(position.x, position.y);
    this.distance = distance;

  }

  public PositionNode move(Move move) {
    int nextX = x + move.xChange;
    int nextY = y + move.yChange;

    return new PositionNode(new Position(nextX, nextY),0);
  }
}
