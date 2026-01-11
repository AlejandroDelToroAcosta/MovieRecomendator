package org.tscd.query;


import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.Context;
import org.neo4j.driver.*;
import java.util.List;
import java.util.Map;


public class MovieRequestHandler implements RequestHandler<Map<String, String>, String> {

    private final Driver driver;
    public MovieRequestHandler() {

        String boltUrl = System.getenv("NEO4J_URI");
        String neo4jUser = System.getenv("NEO4J_USER");
        String neo4jPasswd = System.getenv("NEO4J_PASSWD");


        Config config = Config.builder()
                .withoutEncryption()
                .build();

        // Si esto falla, el log de CloudWatch te dirá exactamente qué falta
        if (boltUrl == null || boltUrl.isEmpty()) {
            throw new RuntimeException("ERROR CRÍTICO: La variable NEO4J_URI es NULL o está vacía.");
        }
        this.driver = GraphDatabase.driver(boltUrl, AuthTokens.basic(neo4jUser, neo4jPasswd), config);
    }

    @Override
    public String handleRequest(Map<String, String> input, Context context) {

        context.getLogger().log("Input recibido: " + input);

        if (input == null) {
            return "{\"error\": \"El input es nulo\"}";
        }

        String type = input.get("type");
        String actorName = input.get("value");

        if (actorName == null || actorName.isEmpty()) {
            return "{\"error\": \"El nombre del actor (value) no puede ser nulo o vacío\"}";
        }

        if (!"getActor".equals(type)) {
            return "{\"error\": \"Tipo de consulta no soportado: " + type + "\"}";
        }

        try (Session session = driver.session()) {
            return session.executeRead(tx -> {
                String query = "MATCH (a:Actor {name: $name})-[:ACTED_IN]->(m:Movie) RETURN m.title AS title";

                Result result = tx.run(query, Values.parameters("name", actorName));

                List<String> movies = result.list(record -> record.get("title").asString());
                String moviesJson = "[\"" + String.join("\", \"", movies) + "\"]";
                return "{\"actor\": \"" + actorName + "\", \"movies\": " + moviesJson + "}";
            });
        } catch (Exception e) {
            context.getLogger().log("Error en Cypher: " + e.getMessage());
            return "{\"error\": \"Error en Neo4j: " + e.getMessage() + "\"}";
        }
    }
}