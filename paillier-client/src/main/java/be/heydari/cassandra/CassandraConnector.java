package be.heydari.cassandra;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;

import static be.heydari.configs.Configs.CASSANDRA_NODE;
import static be.heydari.configs.Configs.CASSANDRA_PORT;

public class CassandraConnector {

    private Cluster cluster;

    private Session session;

    public void connect(String node, Integer port) {
        SocketOptions options = new SocketOptions();
        options.setConnectTimeoutMillis(30000);
        options.setReadTimeoutMillis(300000);
        options.setTcpNoDelay(true);

        Cluster.Builder b = Cluster.builder().addContactPoint(node);
        if (port != null) {
            b.withPort(port);
        }

        b.withSocketOptions(options);

        cluster = b.build();

        session = cluster.connect();
    }

    public void connect() {
        connect(CASSANDRA_NODE, CASSANDRA_PORT);
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
        cluster.close();
    }
}
