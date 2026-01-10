package org.tscd.query;


import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import org.neo4j.driver.*;
import java.util.List;
import java.util.Map;


public class MovieRequestHandler implements RequestHandler<Map<String, String>, String> {

    private final Driver driver;
    public MovieRequestHandler() {
        // Leemos las variables que definiste en Terraform
        String uri = System.getenv("NEO4J_URI");
        String user = System.getenv("NEO4J_USER");
        String password = System.getenv("NEO4J_PASSWORD");

        Config config = Config.builder()
                .withoutEncryption()
                .build();

        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password), config);
    }

    @Override
    public String handleRequest(Map<String, String> input, Context context) {
        String type = input.get("type");
        String actorName = input.get("value");

        if (!"getActor".equals(type)) {
            return "{\"error\": \"Tipo de consulta no soportado\"}";
        }

        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                Result result = tx.run(
                        "MATCH (a:Actor {name: $name})-[:ACTED_IN]->(m:Movie) RETURN m.title AS title",
                        Values.parameters("name", actorName)
                );

                List<String> movies = result.list(record -> record.get("title").asString());

                return "{\"actor\": \"" + actorName + "\", \"movies\": " + movies.toString() + "}";
            });
        } catch (Exception e) {
            return "{\"error\": \"Error conectando a Neo4j: " + e.getMessage() + "\"}";
        }
    }
}