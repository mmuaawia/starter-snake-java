package io.battlesnake.starter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import java.util.*;

import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.get;

/**
 * SnakeApplication server that deals with requests from the snake engine.
 * Just boiler plate code.  See the readme to get started.
 * It follows the spec here: https://github.com/battlesnakeio/docs/tree/master/apis/snake
 */
public class SnakeApplication {
  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  private static final Handler HANDLER = new Handler();
  private static final Logger LOG = LoggerFactory.getLogger(SnakeApplication.class);
  private static Board board;
  static int health;
  public Position tail;

  /**
   * Main entry point.
   *
   * @param args are ignored.
   */
  public static void main(String[] args) {
    String port = System.getProperty("PORT");
    if (port != null) {
      LOG.info("Found system provided port: {}", port);
    } else {
      LOG.info("Using default port: {}", port);
      port = "8080";
    }
    port(Integer.parseInt(port));
    get("/", (req, res) -> "Battlesnake documentation can be found at " +
    "<a href=\"https://docs.battlesnake.io\">https://docs.battlesnake.io</a>.");
    post("/start", HANDLER::process, JSON_MAPPER::writeValueAsString);
    post("/ping", HANDLER::process, JSON_MAPPER::writeValueAsString);
    post("/move", HANDLER::process, JSON_MAPPER::writeValueAsString);
    post("/end", HANDLER::process, JSON_MAPPER::writeValueAsString);
  }

  /**
   * Handler class for dealing with the routes set up in the main method.
   */
  public static class Handler {

    /**
     * For the ping request
     */
    private static final Map<String, String> EMPTY = new HashMap<>();

    /**
     * Generic processor that prints out the request and response from the methods.
     *
     * @param req
     * @param res
     * @return
     */
    public Map<String, String> process(Request req, Response res) {
      try {
        JsonNode parsedRequest = JSON_MAPPER.readTree(req.body());
        String uri = req.uri();
        LOG.info("{} called with: {}", uri, req.body());
        Map<String, String> snakeResponse;
        if (uri.equals("/start")) {
          snakeResponse = start(parsedRequest);
        } else if (uri.equals("/ping")) {
          snakeResponse = ping();
        } else if (uri.equals("/move")) {
          snakeResponse = move(parsedRequest);
        } else if (uri.equals("/end")) {
          snakeResponse = end(parsedRequest);
        } else {
          throw new IllegalAccessError("Strange call made to the snake: " + uri);
        }
        LOG.info("Responding with: {}", JSON_MAPPER.writeValueAsString(snakeResponse));
        return snakeResponse;
      } catch (Exception e) {
        LOG.warn("Something went wrong!", e);
        return null;
      }
    }

    /**
     * /ping is called by the play application during the tournament or on play.battlesnake.io to make sure your
     * snake is still alive.
     *
     * @return an empty response.
     */
    public Map<String, String> ping() {
      return EMPTY;
    }

    /**
     * /start is called by the engine when a game is first run.
     *
     * @param startRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
     * @return a response back to the engine containing the snake setup values.
     */
    public Map<String, String> start(JsonNode startRequest) {
      Map<String, String> response = new HashMap<>();
      int height = startRequest.get("board").get("height").asInt();
      int width = startRequest.get("board").get("width").asInt();
      response.put("color", "#ff00ff");
      board = new Board(new int[height][width], width, height);
      return response;
    }

    /**
     * /move is called by the engine for each turn the snake has.
     *
     * @param moveRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
     * @return a response back to the engine containing snake movement values.
     */
    public Map<String, String> move(JsonNode moveRequest) {

      Map<String, String> response = new HashMap<>();
      JsonNode food = moveRequest.get("board").get("food");
      JsonNode snakes = moveRequest.get("board").get("snakes");
      JsonNode self = moveRequest.get("you").get("body");
      health = moveRequest.get("you").get("health").asInt();

      JsonNode tailNode = self.get(self.size()-1);
      int tailX = tailNode.get("x").asInt();
      int tailY = tailNode.get("y").asInt();
      LOG.info(tailNode.toString());
      Position tailPos = new Position(tailX, tailY);

      board.populateBoard(food, snakes, self);
      LOG.info(board.toString());

      int currX = self.get(0).get("x").asInt();
      int currY = self.get(0).get("y").asInt();

      Position position = new Position(currX, currY);

      LOG.info("Current Position: " + position.toString());
//      if (health > healthThresh(moveRequest.get("turn").asInt())) {
//        board.grid[tailY][tailX] = 0;
//        response.put("move", MoveHelper.followTail(position, tailPos, board));
//        return response;
//      }
      response.put("move", MoveHelper.getMove(position, board));
      return response;
    }

    /**
     * /end is called by the engine when a game is complete.
     *
     * @param endRequest a map containing the JSON sent to this snake. See the spec for details of what this contains.
     * @return responses back to the engine are ignored.
     */
    public Map<String, String> end(JsonNode endRequest) {
      Map<String, String> response = new HashMap<>();
      return response;
    }

    static int healthThresh(int turn) {
      if (turn < 15) {
        return 100;
      }
      if (turn < 30) {
        return 80;
      }
      else {
        return Math.max(30, (250 - turn)/3);
      }

    }

  }
}
