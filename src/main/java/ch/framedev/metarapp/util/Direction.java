package ch.framedev.metarapp.util;

import javax.swing.*;
import java.util.HashMap;

public enum Direction {


    NORTH(338, 22, "↓","↑"),
    NORTHEAST(22, 68, "↙", "↗"),
    EAST(68, 112, "←", "→"),
    SOUTHEAST(112, 158, "↖", "↘"),
    SOUTH(158, 202, "↑", "↓"),
    SOUTHWEST(202, 248, "↗","↙"),
    WEST(248, 292, "→","←"),
    NORTHWEST(292, 338, "↘", "↖");

    final String windSymbol, compassSymbol;
    final int min, max;
    int degrees;

    Direction(int min, int max, String windSymbol, String compassSymbol) {
        this.min = min;
        this.max = max;
        this.windSymbol = windSymbol;
        this.compassSymbol = compassSymbol;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public String getWindSymbol() {
        return windSymbol;
    }

    public String getCompassSymbol() {
        return compassSymbol;
    }

    public void setDegrees(int degrees) {
        this.degrees = degrees;
    }

    public ImageIcon getImageIcon() {
        return new ImageIcon(Variables.rotate(Variables.toBufferedImage(Variables.getWeatherImage()), degrees));
    }

    public static HashMap<Direction, String> getDirectionHasMap(int degree) {
        HashMap<Direction, String> output = new HashMap<>();
        if (isPositive(degree)) {
            if (degree > NORTH.min && degree < NORTH.max || degree > 0 && degree < NORTH.getMax()
                    || degree < 360 && degree > NORTH.min || degree == NORTH.min || degree == NORTH.max) {
                output.put(NORTH, NORTH.windSymbol);
            } else if (degree > NORTHEAST.min && degree < NORTHEAST.max || degree == NORTHEAST.min || degree == NORTHEAST.max) {
                output.put(NORTHEAST, NORTHEAST.windSymbol);
            } else if (degree > EAST.min && degree < EAST.max || degree == EAST.min || degree == EAST.max) {
                output.put(EAST, EAST.windSymbol);
            } else if (degree > SOUTHEAST.min && degree < SOUTHEAST.max || degree == SOUTHEAST.min || degree == SOUTHEAST.max) {
                output.put(SOUTHEAST, SOUTHEAST.windSymbol);
            } else if (degree > SOUTH.min && degree < SOUTH.max || degree == SOUTH.min || degree == SOUTH.max) {
                output.put(SOUTH, SOUTH.windSymbol);
            } else if (degree > SOUTHWEST.min && degree < SOUTHWEST.max || degree == SOUTHWEST.min || degree == SOUTHWEST.max) {
                output.put(SOUTHWEST, SOUTHWEST.windSymbol);
            } else if (degree > WEST.min && degree < WEST.max || degree == WEST.min || degree == WEST.max) {
                output.put(WEST, WEST.windSymbol);
            } else if (degree > NORTHWEST.min && degree < NORTHWEST.max || degree == NORTHWEST.min || degree == NORTHWEST.max) {
                output.put(NORTHWEST, NORTHWEST.windSymbol);
            }
        } else {
            int actual = degree + 360;
            if (actual > NORTH.min && actual < NORTH.max || actual > 0 && actual < NORTH.getMax()
                    || actual < 360 && actual > NORTH.min || actual == NORTH.min || actual == NORTH.max) {
                output.put(NORTH, NORTH.windSymbol);
            } else if (actual > NORTHEAST.min && actual < NORTHEAST.max || actual == NORTHEAST.min || actual == NORTHEAST.max) {
                output.put(NORTHEAST, NORTHEAST.windSymbol);
            } else if (actual > EAST.min && actual < EAST.max || actual == EAST.min || actual == EAST.max) {
                output.put(EAST, EAST.windSymbol);
            } else if (actual > SOUTHEAST.min && actual < SOUTHEAST.max || actual == SOUTHEAST.min || actual == SOUTHEAST.max) {
                output.put(SOUTHEAST, SOUTHEAST.windSymbol);
            } else if (actual > SOUTH.min && actual < SOUTH.max || actual == SOUTH.min || actual == SOUTH.max) {
                output.put(SOUTH, SOUTH.windSymbol);
            } else if (actual > SOUTHWEST.min && actual < SOUTHWEST.max || actual == SOUTHWEST.min || actual == SOUTHWEST.max) {
                output.put(SOUTHWEST, SOUTHWEST.windSymbol);
            } else if (actual > WEST.min && actual < WEST.max || actual == WEST.min || actual == WEST.max) {
                output.put(WEST, WEST.windSymbol);
            } else if (actual > NORTHWEST.min && actual < NORTHWEST.max || actual == NORTHWEST.min || actual == NORTHWEST.max) {
                output.put(NORTHWEST, NORTHWEST.windSymbol);
            }
        }

        return output;
    }

    public static Direction getDirection(int degree) {
        return (Direction) getDirectionHasMap(degree).keySet().toArray()[0];
    }

    public static String getDirectionCompassSymbol(Direction direction) {
        return direction.compassSymbol;
    }

    public static String getDirectionSymbol(int degree) {
        if (isPositive(degree)) {
            if (degree > NORTH.min && degree < NORTH.max || degree > 0 && degree < NORTH.getMax()
                    || degree < 360 && degree > NORTH.min || degree == NORTH.min || degree == NORTH.max) {
                return NORTH.windSymbol;
            } else if (degree > NORTHEAST.min && degree < NORTHEAST.max || degree == NORTHEAST.min || degree == NORTHEAST.max) {
                return NORTHEAST.windSymbol;
            } else if (degree > EAST.min && degree < EAST.max || degree == EAST.min || degree == EAST.max) {
                return EAST.windSymbol;
            } else if (degree > SOUTHEAST.min && degree < SOUTHEAST.max || degree == SOUTHEAST.min || degree == SOUTHEAST.max) {
                return SOUTHEAST.windSymbol;
            } else if (degree > SOUTH.min && degree < SOUTH.max || degree == SOUTH.min || degree == SOUTH.max) {
                return SOUTH.windSymbol;
            } else if (degree > SOUTHWEST.min && degree < SOUTHWEST.max || degree == SOUTHWEST.min || degree == SOUTHWEST.max) {
                return SOUTHWEST.windSymbol;
            } else if (degree > WEST.min && degree < WEST.max || degree == WEST.min || degree == WEST.max) {
                return WEST.windSymbol;
            } else if (degree > NORTHWEST.min && degree < NORTHWEST.max || degree == NORTHWEST.min || degree == NORTHWEST.max) {
                return NORTHWEST.windSymbol;
            }
        } else {
            int actual = degree + 360;
            if (actual > NORTH.min && actual < NORTH.max || actual > 0 && actual < NORTH.getMax()
                    || actual < 360 && actual > NORTH.min || actual == NORTH.min || actual == NORTH.max) {
                return NORTH.windSymbol;
            } else if (actual > NORTHEAST.min && actual < NORTHEAST.max || actual == NORTHEAST.min || actual == NORTHEAST.max) {
                return NORTHEAST.windSymbol;
            } else if (actual > EAST.min && actual < EAST.max || actual == EAST.min || actual == EAST.max) {
                return EAST.windSymbol;
            } else if (actual > SOUTHEAST.min && actual < SOUTHEAST.max || actual == SOUTHEAST.min || actual == SOUTHEAST.max) {
                return SOUTHEAST.windSymbol;
            } else if (actual > SOUTH.min && actual < SOUTH.max || actual == SOUTH.min || actual == SOUTH.max) {
                return SOUTH.windSymbol;
            } else if (actual > SOUTHWEST.min && actual < SOUTHWEST.max || actual == SOUTHWEST.min || actual == SOUTHWEST.max) {
                return SOUTHWEST.windSymbol;
            } else if (actual > WEST.min && actual < WEST.max || actual == WEST.min || actual == WEST.max) {
                return WEST.windSymbol;
            } else if (actual > NORTHWEST.min && actual < NORTHWEST.max || actual == NORTHWEST.min || actual == NORTHWEST.max) {
                return NORTHWEST.windSymbol;
            }
        }
        return null;
    }

    private static boolean isPositive(int degree) {
        return degree >= 0;
    }
}
