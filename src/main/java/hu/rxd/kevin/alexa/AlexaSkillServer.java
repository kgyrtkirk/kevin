package hu.rxd.kevin.alexa;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import hu.rxd.kevin.alexa.kodi.KodiSkillServlet;
import hu.rxd.kevin.alexa.mira.MiraSkillServlet;

public class AlexaSkillServer {
  public static void main(String[] args) throws Exception {
    Server server = new Server(new QueuedThreadPool());
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(8090);
    server.setConnectors(new Connector[] { connector });

    ServletHandler sh = new ServletHandler();
    sh.addServletWithMapping(KodiSkillServlet.class, "/kodi/*");
    sh.addServletWithMapping(MiraSkillServlet.class, "/mira/*");

    server.setHandler(sh);

    server.start();

    // Dump the server state
    System.out.println(server.dump());

    //    Runtime.getRuntime().addShutdownHook(new Thread() {
    //
    //      @Override
    //      public void run() {
    //        try {
    //          System.out.println("ssh!");
    //          server.stop();
    //        } catch (Exception e) {
    //          e.printStackTrace();
    //        }
    //      }
    //    });

    //        server.join();
  }
}