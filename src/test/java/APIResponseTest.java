/*
 * PACKAGE_NAME
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 19.06.2024 09:37
 */

import ch.framedev.metarapp.apis.MetarAPPApi;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.handlers.ConnectionsHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class APIResponseTest {

    @Before
    public void setUp() throws Exception {
        Main.connectionTokenHandler = new ConnectionsHandler();
    }

    @Test
    public void onRetrieveData() {
        try {
            Assert.assertTrue(MetarAPPApi.getInstance().getMetarData("vidp").hasResults());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
