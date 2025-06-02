``` java
package your.package;



/*
 * ch.framedev
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 30.05.2025 23:38
 */

import ch.framedev.metarapp.apis.MetarAPPApi;
import ch.framedev.metarapp.events.EventBus;
import ch.framedev.metarapp.util.Plugin;

public class TestPlugin implements Plugin {

    @Override
    public void initialize() {
        System.out.println("TestPlugin initialized!");
    }

    @Override
    public void start() {
        System.out.println("TestPlugin started!");
        EventBus.registerRefreshListener(event -> {
            System.out.println("Plugin received refresh from " + event.getFrom() + " to " + event.getTo());
            if(event.getFrom().equalsIgnoreCase("login"))
                System.out.println(MetarAPPApi.getInstance().getLastSearchList());
        });
        EventBus.registerSendIcaoListeners(event -> {
            System.out.println("Plugin received send ICAO event: " + event.getIcao());
        });
    }

    @Override
    public void stop() {
        System.out.println("TestPlugin stopped!");
    }

    @Override
    public String getName() {
        return "TestPlugin";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public String getDescription() {
        return "Test Plugin for MetarApp";
    }

    @Override
    public String getAuthor() {
        return "FrameDev";
    }
}
```
<br><br>
You need to create a meta-inf
in the Folder META-INF/services/ch.framedev.metarapp.util.Plugin

with the content
``` text
your.package.YourPlugin
```