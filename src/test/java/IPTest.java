/*
 * PACKAGE_NAME
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 08.08.2024 15:11
 */

import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.handlers.ConnectionsHandler;
import ch.framedev.metarapp.util.LoggerUtils;
import org.junit.Test;

public class IPTest {

    @Test
    public void testGetIP() {
        Main.connectionTokenHandler = new ConnectionsHandler();
        // Test if the IP can be retrieved correctly
        new LoggerUtils().sendLogsFromIP();
    }
}
