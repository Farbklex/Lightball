package io.lightball.lightball.entities;

/**
 * Created by Alexander Hoffmann on 09.09.16.
 */
public class Player {
    public final String id;
    public final String name;
    public final int health;

    public Player(String id, String content, int health) {
        this.id = id;
        this.name = content;
        this.health = health;
    }

    @Override
        public String toString() {
            return name + " " + health;
        }
}
