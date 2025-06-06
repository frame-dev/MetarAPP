import ch.framedev.metarapp.apis.MetarAPPApi;
import ch.framedev.metarapp.main.Main;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MetarAPITest {
    public static void main(String[] args) {
        MetarAPPApi metarAPPApi = MetarAPPApi.getInstance();
        System.out.println(metarAPPApi.getAllVersions());
        System.out.println(metarAPPApi.getAllPreReleaseVersions());
        System.out.println(metarAPPApi.isVersionAvailable("1.2.8"));
        System.out.println(metarAPPApi.getAllChangelogs());
        System.out.println(metarAPPApi.getAllPreReleaseChangelogs());

        try {
            System.out.println(metarAPPApi.getMetarRequest("LSZH"));
            System.out.println(metarAPPApi.getMetarData("LSZH"));
            System.out.println(metarAPPApi.getAirportData("LSZH"));
            System.out.println(metarAPPApi.getAirportRequest("LSZH"));
            Assert.assertEquals(Main.VERSION, metarAPPApi.getMetarAPPVersion());
            Assert.assertTrue(metarAPPApi.getMetarData("LSZH").hasResults());
            Assert.assertTrue(metarAPPApi.getAirportData("LSZH").hasResults());
        } catch (IOException e) {
            Logger.getLogger(MetarAPITest.class.getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @Test
    public void testMetarAPPApi() {
        MetarAPPApi metarAPPApi = MetarAPPApi.getInstance();
        Assert.assertEquals(Main.VERSION, metarAPPApi.getMetarAPPVersion());
        System.out.println(metarAPPApi.getMetarAPPVersion());
    }
}