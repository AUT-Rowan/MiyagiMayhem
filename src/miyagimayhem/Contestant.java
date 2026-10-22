package miyagimayhem;

import java.util.Random;

/**
 * Represents a single contestant in Miyagi Mayhem.
 * Stats are randomly generated on creation and drive challenge/elimination outcomes.
 */
public class Contestant {

    private final String name;
    private int strength;
    private int intelligence;
    private int social;
    private int luck;
    private boolean eliminated;
    private boolean hasImmunity;
    private String allyName; // name of allied contestant, null if none

    private static final Random RNG = new Random();

    public Contestant(String name) {
        this.name = name;
        this.strength = 1 + RNG.nextInt(10);
        this.intelligence = 1 + RNG.nextInt(10);
        this.social = 1 + RNG.nextInt(10);
        this.luck = 1 + RNG.nextInt(10);
        this.eliminated = false;
        this.hasImmunity = false;
        this.allyName = null;
    }

    // --- Getters (encapsulation: stats are read-only from outside) ---
    public String getName() {
        return name;
    }

    public int getStrength() {
        return strength;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public int getSocial() {
        return social;
    }

    public int getLuck() {
        return luck;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public boolean hasImmunity() {
        return hasImmunity;
    }

    public String getAllyName() {
        return allyName;
    }

    // --- Controlled mutators ---
    public void eliminate() {
        this.eliminated = true;
    }

    public void grantImmunity() {
        this.hasImmunity = true;
    }

    public void clearImmunity() {
        this.hasImmunity = false;
    }

    public void setAlly(String allyName) {
        this.allyName = allyName;
    }

    public boolean isAlliedWith(String otherName) {
        return allyName != null && allyName.equalsIgnoreCase(otherName);
    }

    /**
     * Combined "challenge score" weighted by a given stat plus a luck factor.
     * Used by Event subclasses to determine challenge winners.
     */
    public int rollScore(String primaryStat) {
        int base;
        switch (primaryStat.toLowerCase()) {
            case "strength":
                base = strength;
                break;
            case "intelligence":
                base = intelligence;
                break;
            case "social":
                base = social;
                break;
            default:
                base = (strength + intelligence + social) / 3;
        }
        int luckFactor = RNG.nextInt(luck + 1); // higher luck stat = bigger possible swing
        return base + luckFactor;
    }

    @Override
    public String toString() {
        return String.format("%s [STR:%d INT:%d SOC:%d LCK:%d]",
                name, strength, intelligence, social, luck);
    }
}
