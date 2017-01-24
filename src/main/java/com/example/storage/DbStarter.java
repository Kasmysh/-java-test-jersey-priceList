package com.example.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.h2.tools.Server;
import org.h2.util.StringUtils;

public class DbStarter implements ServletContextListener {

    private Connection conn;
    private Server server;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        try {
            org.h2.Driver.load();

            ServletContext servletContext = servletContextEvent.getServletContext();

            String url = getParameter(servletContext, "db.url", "jdbc:h2:~/test");
            String user = getParameter(servletContext, "db.user", "sa");
            String password = getParameter(servletContext, "db.password", "sa");

            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            // Start the server if configured to do so
            String serverParams = getParameter(servletContext, "db.tcpServer", null);
            if (serverParams != null) {
                String[] params = StringUtils.arraySplit(serverParams, ' ', true);
                server = Server.createTcpServer(params);
                server.start();
            }

            // To access the database in server mode, use the database URL:
            // jdbc:h2:tcp://localhost/~/test

            // conn = DriverManager.getConnection(url, user, password);
            conn = DriverManager.getConnection(url, props);

            servletContext.setAttribute("connection", conn);

            // servletContext.setAttribute("cheking", "it very WorkS!");
        } catch (Exception e) {
           System.out.println( "Init db connection fail.  " + e.getMessage() );
            // e.printStackTrace();
        }
    }

    private static String getParameter(ServletContext servletContext,
            String key, String defaultValue) {
        String value = servletContext.getInitParameter(key);
        return value == null ? defaultValue : value;
    }

    /**
     * Get the connection.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return conn;
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        try {
            Statement stat = conn.createStatement();
            stat.execute("SHUTDOWN");
            stat.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (server != null) {
            server.stop();
            server = null;
        }
    }

}
