import ch.framedev.metarapp.util.Direction;
import ch.framedev.metarapp.util.Variables;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class WindTest {


    @Test
    public void checkHashMapDirection() {
        System.out.println("Check Hash Map Direction started");
        int wind = 14;
        int runway = 12;

        int actualWind = wind-runway;
        System.out.println(Direction.getDirectionHasMap(actualWind));
        Assert.assertNotNull(Direction.getDirectionHasMap(actualWind));
        System.out.println("Test Direction HashMap Completed");
    }

    @SuppressWarnings("unused")
    @Test
    public void checkDirection() {
        System.out.println("Checking Direction Test started");
        int wind = 14;
        int runway = 12;

        int actualWind = wind - runway;
        System.out.println("Calculated wind direction: " + actualWind + " degrees");
        System.out.println("Direction: " + Direction.getDirection(actualWind));
        Assert.assertEquals(Direction.NORTH, Direction.getDirection(actualWind));

        try {
            BufferedImage bufferedImage = Variables.toBufferedImage(Variables.getWeatherImage());
            BufferedImage rotatedImage = Variables.rotate(bufferedImage, actualWind);
            System.out.println("Displaying rotated image with angle: " + actualWind + " degrees");
            // JOptionPane.showMessageDialog(null, new ImageIcon(rotatedImage), "Rotated Image", JOptionPane.PLAIN_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        System.out.println("Test Direction Completed");
    }

    @Test
    public void checkSymbol() {
        System.out.println("Check Symbol started");
        int wind = 10;
        int runway = 12;

        int actualWind = wind-runway;
        System.out.println(Direction.getDirectionSymbol(actualWind));
        System.out.println(Direction.getDirectionCompassSymbol(Direction.getDirection(actualWind)));
        Assert.assertEquals(Direction.getDirectionSymbol(actualWind), Direction.NORTH.getWindSymbol());
        System.out.println("Test Direction Symbol Completed");
    }
}
