package org.tscd.api.controller;

import org.springframework.web.bind.annotation.*;
import org.tscd.api.service.Neo4jService;

@RestController
@RequestMapping("/api")
public class MovieController {

    private final Neo4jService neo4jService;

    public MovieController(Neo4jService neo4jService) {
        this.neo4jService = neo4jService;
    }

    @GetMapping("/test")
    public String test() {
        return "Servidor conectado directamente a Neo4j funcionando";
    }

    // Cambiado de /movie/{title} a /movie?title=...
    @GetMapping("/movie")
    public String getMovie(@RequestParam(name = "title") String title) {
        return neo4jService.executeQuery("getMovie", title);
    }

    // Cambiado de /actor/{name} a /actor?name=...
    @GetMapping("/actor")
    public String getActor(@RequestParam(name = "name") String name) {
        return neo4jService.executeQuery("getActor", name);
    }

    // Cambiado de /director/{name} a /director?name=...
    @GetMapping("/director")
    public String getDirector(@RequestParam(name = "name") String name) {
        return neo4jService.executeQuery("getDirector", name);
    }
}