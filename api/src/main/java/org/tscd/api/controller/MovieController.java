package org.tscd.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tscd.api.lambda.LambdaQueryClient;

@RestController
@RequestMapping("/api")
public class MovieController {

    private final LambdaQueryClient lambda;

    public MovieController(LambdaQueryClient lambda) {
        this.lambda = lambda;
    }

    @GetMapping("/test")
    public String test() {
        return "El servidor funciona correctamente";
    }

    @GetMapping("/movie/{title}")
    public Object getMovie(@PathVariable String title) {
        return lambda.call("getMovie", title);
    }

    @GetMapping("/actor/{name}")
    public Object getActor(@PathVariable String name) {
        return lambda.call("getActor", name);
    }

    @GetMapping("/director/{name}")
    public Object getDirector(@PathVariable String name) {
        return lambda.call("getDirector", name);
    }
}
