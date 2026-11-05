package restaurant.game;

/**
 * Compatibility entry point for existing IDE run configurations.
 * The console implementation lives in restaurant.RestaurantRush; delegate to it
 * so both launch names use the same current game interface and read-only views.
 */
public final class RestaurantRush {
    private RestaurantRush() { }

    public static void main(String[] args) {
        restaurant.RestaurantRush.main(args);
    }
}
