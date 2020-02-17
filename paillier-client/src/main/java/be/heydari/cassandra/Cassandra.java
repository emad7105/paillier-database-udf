package be.heydari.cassandra;

import com.datastax.driver.core.Session;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Cassandra {
    private CassandraConnector connector;

    public Cassandra() {
        connector = new CassandraConnector();
        connector.connect();
    }

    public void loadCustomKeyspace() throws IOException, URISyntaxException {
        /*System.out.println("\n\n\nDropping Keyspace...\n" +
                "Creating Keyspace...\n" +
                "Creating Table...\n" +
                "Creating User-defined functions...\n\n\n");*/

        String cqlDrop = loadCQL("cassandra/facturis-drop.cql");
        String cqlCreateKS = loadCQL("cassandra/facturis-create-keyspace.cql");
        String cqlCreateTable = loadCQL("cassandra/facturis-create-table.cql");
        String cqlCreateUDF = loadCQL("cassandra/facturis-create-udf.cql");

        System.out.println("\n\n\nDropping Keyspace...\n\n\n");
        connector.getSession().execute(cqlDrop);

        System.out.println("\n\n\n\"Creating Keyspace...\n\n\n");
        connector.getSession().execute(cqlCreateKS);

        System.out.println("\n\n\nCreating Table...\n\n\n");
        connector.getSession().execute(cqlCreateTable);

        System.out.println("\n\n\nCreating User-defined functions...\n\n\n");
        connector.getSession().execute(cqlCreateUDF);
    }


    private static String loadCQL(String cqlFile) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(Cassandra.class.getClassLoader().getResource(cqlFile).toURI())));
    }

    public Session getSession() {
        return connector.getSession();
    }
}
