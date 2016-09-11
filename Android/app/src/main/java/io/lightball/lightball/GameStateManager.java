package io.lightball.lightball;

import android.util.Log;

import java.util.ArrayList;

import io.lightball.lightball.entities.Player;
import io.lightball.lightball.interfaces.GameStateInterface;

/**
 * Created by Alexander Hoffmann on 10.09.16.
 */
public class GameStateManager{
    private ArrayList<Player> mTeam1;
    private ArrayList<Player> mTeam2;

    private GameStateInterface mCallback;

    boolean mGameEnded = false;
    int mWinningTeam = 0;

    private static GameStateManager mInstance = new GameStateManager();

    public static GameStateManager getInstance() {
        return mInstance;
    }

    private GameStateManager() {
    }

    public void reduceHealth(String playerId){
        Log.d("Debug","Reduce health in GameStateManager triggered");
        int newHealth = 0;
        for(Player p : mTeam1){
            if(p.id != null && p.id.equals(playerId)){
                p.health = p.health - 25;
                newHealth = p.health;
            }
        }

        for(Player p : mTeam2){
            if(p.id != null && p.id.equals(playerId)){
                p.health = p.health - 25;
                newHealth = p.health;
            }
        }

        //Update the UI
        Log.d("Debug","Calling setPlayerHealth in UI with " + newHealth + "HP.");
        mCallback.setPlayerHealth(playerId, newHealth);

        checkIfGameEnds();
        sendHealthToShirt();
    }

    private void sendHealthToShirt() {
        //TODO: Bluetooth Code
    }

    /**
     * Checks if one of the teams has no more alive players
     * @return 0 if game hasn't ended. Otherwise number of the winning team.
     */
    private int checkIfGameEnds() {
        int alivePlayersTeam1 = 0;
        int alivePlayersTeam2 = 0;

        for(Player p : mTeam1){
            if(p.health > 0) alivePlayersTeam1 += 1;
        }

        for(Player p: mTeam2) {
            if (p.health > 0) alivePlayersTeam2 += 1;
        }

        if(alivePlayersTeam1 > 0 && alivePlayersTeam2 > 0){
            return 0;
        }else {
            mGameEnded = true;

            if(alivePlayersTeam1 == 0){
                mWinningTeam = 1;
            }

            if(alivePlayersTeam2 == 0){
                mWinningTeam = 2;
            }
        }
        {mCallback.setGameEnd(mWinningTeam);}
        return mWinningTeam;
    }

    public ArrayList<Player> getTeam1() {
        return mTeam1;
    }

    public void setTeam1(ArrayList<Player> team1) {
        mTeam1 = team1;
    }

    public ArrayList<Player> getTeam2() {
        return mTeam2;
    }

    public void setTeam2(ArrayList<Player> team2) {
        mTeam2 = team2;
    }

    public boolean isGameEnded() {
        return mGameEnded;
    }

    public int getWinningTeam() {
        return mWinningTeam;
    }

    /**
     * Resets the health of all players to 100 and restarts the game
     */
    public void resetGame(){
        for(Player p : mTeam1){
            p.health = 100;
        }

        for(Player p : mTeam2){
            p.health = 100;
        }

        mGameEnded = false;
        mWinningTeam = 0;
    }

    public void setCallback(GameStateInterface callback){
        mCallback = callback;
    }

    /**
     * Calculates the current total lifepoints per team
     * @return int array with scores for team 1 [0] and team 2 [1]
     */
    public int[] getScores() {
        int[] scores = {0,0};
        int score1 = 0;
        int score2 = 0;
        if(mTeam1 != null && mTeam2 != null){
            for(Player p : mTeam1){
                score1 += p.health;
            }
            scores[0] = score1;
            for(Player p : mTeam2){
                score2 += p.health;
            }
            scores[1] = score2;
        }
        return scores;

    }
}
