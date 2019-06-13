package org.eclipse.jetty.embedded;

import java.io.File;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.resource.Resource;

import hu.rxd.kevin.alexa.KodiSkillServlet;

/**
 * A {@link ContextHandlerCollection} handler may be used to direct a request to
 * a specific Context. The URI path prefix and optional virtual host is used to
 * select the context.
 */
public class SplitFileServer
{
    public static void main( String[] args ) throws Exception
    {
        // Create the Server object and a corresponding ServerConnector and then
        // set the port for the connector. In this example the server will
        // listen on port 8090. If you set this to port 0 then when the server
        // has been started you can called connector.getLocalPort() to
        // programmatically get the port the server started on.
        Server server = new Server();
        ServerConnector connector = new ServerConnector(server);
    connector.setPort(8090);
        server.setConnectors(new Connector[] { connector });

        
        ServletHandler sh = new ServletHandler();
    sh.addServletWithMapping(KodiSkillServlet.class, "/*");
        
        // Create a Context Handler and ResourceHandler. The ContextHandler is
        // getting set to "/" path but this could be anything you like for
        // builing out your url. Note how we are setting the ResourceBase using
        // our jetty maven testing utilities to get the proper resource
        // directory, you needn't use these, you simply need to supply the paths
        // you are looking to serve content from.
        ResourceHandler rh0 = new ResourceHandler();

        ContextHandler context0 = new ContextHandler();
        context0.setContextPath("/");
        File dir0 = new File("ce967f71-a7a1-47ac-b08c-658b4c03ebe8-tcpdemeter1883");
        context0.setBaseResource(Resource.newResource(dir0));
        context0.setHandler(rh0);

    // Rinse and repeat the previous item, only specifying a different
    // resource base.
    ResourceHandler rh1 = new ResourceHandler();

 
    //    contex
    
    ContextHandler context1 = new ContextHandler();
    context1.setContextPath("/asd");
    File dir1 = new File("1a3e1b87-b69f-4c87-b598-4cfcb806e1dc-tcpdemeter1883");
    context1.setBaseResource(Resource.newResource(dir1));
    context1.setHandler(rh1);

        // Create a ContextHandlerCollection and set the context handlers to it.
        // This will let jetty process urls against the declared contexts in
        // order to match up content.
        ContextHandlerCollection contexts = new ContextHandlerCollection();
    contexts.setHandlers(new Handler[] { context0, context1 });
    //    contexts.setHandlers(new Handler[] { sh });

    server.setHandler(sh);

        // Start things up!
        server.start();


        // Dump the server state
        System.out.println(server.dump());

        // The use of server.join() the will make the current thread join and
        // wait until the server is done executing.
        // See http://docs.oracle.com/javase/7/docs/api/java/lang/Thread.html#join()
    //        server.join();
    }
}