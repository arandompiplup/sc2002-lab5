package restaurant.game;

/** A turn is a protocol: callers cannot reset staff or rearrange its steps. */
public enum TurnPhase { BETWEEN_TURNS, WAITER, CHEF, END_OF_TURN, FINISHED }
