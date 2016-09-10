package io.lightball.lightball.interfaces;

/**
 * Created by Alexander Hoffmann on 11.09.16.
 * Triggers events to the UI from the {@link io.lightball.lightball.GameStateManager}
 */
public interface GameStateInterface{
    void setPlayerHealth(String playerId, int health);

    void setGameEnd(int winningTeam);
}
