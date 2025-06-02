import ch.framedev.metarapp.requests.ATCRequester;
import ch.framedev.metarapp.main.Main;
import ch.framedev.metarapp.handlers.ConnectionsHandler;
import org.junit.Test;

import java.io.IOException;

public class ATCTest {

    @Test
    public void atcTest() {
        try {
            Main.connectionTokenHandler = new ConnectionsHandler();
            new ATCRequester("lspl").printData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
