package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .log(); // db or a remote service call
    }

    public Mono<String> nameMono() {
        return Mono.just("alex")
                .log();
    }

    public Mono<List<String>> namesMono_flatMap(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(this::splitStringMono)
                .log();
    }

    public Flux<String> namesMono_flatMapMany(int stringLength) {
        return Mono.just("alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMapMany(this::splitString)
                .log();
    }

    private Mono<List<String>> splitStringMono(String s) {
        var charArray = s.split("");
        var charList = List.of(charArray);
        return Mono.just(charList);
    }

    public Flux<String> namesFlux_map(int stringLength) {
        //filter the string whose length is greater than 3
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
//                .map(str -> str.toUpperCase())
                .filter(s -> s.length() > stringLength)
                .map(s -> s.length() + "-" + s) // 4-ALEX, 5-CHLOE
                .log(); // db or a remote service call
    }

    public Flux<String> namesFlux_immutability() {
        var namesFlux = Flux.fromIterable(List.of("alex", "ben", "chloe"));
        namesFlux.map(String::toUpperCase);
        return namesFlux;
    }

    public Flux<String> namesFlux_flatmap(int stringLength) {
        //filter the string whose length is greater than 3
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
//                .map(str -> str.toUpperCase())
                .filter(s -> s.length() > stringLength)
                // ALEX, BE, CHLOE -> A, L, E, X, B, E, C, H, L, O, E
                .flatMap(s -> splitString(s))
                .log(); // db or a remote service call
    }

    public Flux<String> namesFlux_flatmap_async(int stringLength) {
        //filter the string whose length is greater than 3
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
//                .map(str -> str.toUpperCase())
                .filter(s -> s.length() > stringLength)
                // ALEX, BE, CHLOE -> A, L, E, X, B, E, C, H, L, O, E
                .flatMap(s -> splitString_withDelay(s))
                .log(); // db or a remote service call
    }

    public Flux<String> namesFlux_concatmap(int stringLength) {
        //filter the string whose length is greater than 3
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .map(String::toUpperCase)
//                .map(str -> str.toUpperCase())
                .filter(s -> s.length() > stringLength)
                // ALEX, BE, CHLOE -> A, L, E, X, B, E, C, H, L, O, E
                .concatMap(s -> splitString_withDelay(s))
                .log(); // db or a remote service call
    }

    public Flux<String> namesFlux_transform(int stringLength) {
        //filter the string whose length is greater than 3

        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength);

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filterMap)
                .flatMap(s -> splitString(s)) // ALEX, CHLOE -> A, L, E, X, C, H, L, O, E
                .defaultIfEmpty("default")
                .log(); // db or a remote service call
    }

    public Flux<String> namesFlux_transform_switchifEmpty(int stringLength) {
        //filter the string whose length is greater than 3

        Function<Flux<String>, Flux<String>> filterMap = name -> name.map(String::toUpperCase)
                .filter(s -> s.length() > stringLength)
                .flatMap(s -> splitString(s)); // ALEX, CHLOE -> A, L, E, X, C, H, L, O, E

        Flux<String> defaultFlux = Flux.just("default")
                .transform(filterMap); // "D", "E", "F", "A", "U", "L", "T"

        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filterMap)
                .switchIfEmpty(defaultFlux)
                .log(); // db or a remote service call
    }

    public Flux<String> explore_concat() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");

        return Flux.concat(abcFlux, defFlux).log();
    }

    public Flux<String> explore_concatwith() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");
        Flux<String> defFlux = Flux.just("D", "E", "F");

        return abcFlux.concatWith(defFlux).log();
    }

    public Flux<String> explore_concatwith_mono() {
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");

        return aMono.concatWith(bMono).log(); // "A",  "B"
    }

    public Flux<String> explore_merge() {
        Flux<String> abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        Flux<String> defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.merge(abcFlux, defFlux).log();
    }

    public Flux<String> explore_mergeWith() {
        Flux<String> abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        Flux<String> defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return abcFlux.mergeWith(defFlux).log();
    }

    public Flux<String> explore_mergeWithMono() {
        Mono<String> aMono = Mono.just("A");

        Mono<String> bMono = Mono.just("B");

        return aMono.mergeWith(bMono).log();
    }

    public Flux<String> explore_mergeSequential() {
        Flux<String> abcFlux = Flux.just("A", "B", "C")
                .delayElements(Duration.ofMillis(100));

        Flux<String> defFlux = Flux.just("D", "E", "F")
                .delayElements(Duration.ofMillis(125));

        return Flux.mergeSequential(abcFlux, defFlux);
    }

    public Flux<String> explore_zip() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");

        Flux<String> defFlux = Flux.just("D", "E", "F");

        return Flux.zip(abcFlux, defFlux, (first, second) -> first+second).log(); // AD, BE, CF
    }

    public Flux<String> explore_zip_1() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");

        Flux<String> defFlux = Flux.just("D", "E", "F");

        Flux<String> _123Flux = Flux.just("1", "2", "3");

        Flux<String> _456Flux = Flux.just("4", "5", "6");

        return Flux.zip(abcFlux, defFlux, _123Flux, _456Flux)
                .map(t -> t.getT1() + t.getT2() + t.getT3() + t.getT4()).log(); // AD14, BE25, CF36
    }

    public Flux<String> explore_zipWith() {
        Flux<String> abcFlux = Flux.just("A", "B", "C");

        Flux<String> defFlux = Flux.just("D", "E", "F");

        return abcFlux.zipWith(defFlux, (first, second) -> first+second).log(); // AD, BE, CF
    }

    public Mono<String> explore_zipwith_mono() {
        Mono<String> aMono = Mono.just("A");
        Mono<String> bMono = Mono.just("B");

        return aMono.zipWith(bMono)
                .map(t -> t.getT1() + t.getT2()) // "AB"
                .log();
    }

    //ALEX -> Flux(A,L,E,X)
    public Flux<String> splitString(String name) {
        var charArray = name.split("");
        return Flux.fromArray(charArray);
    }


    public Flux<String> splitString_withDelay(String name) {
        var charArray = name.split("");
//        var delay = new Random().nextInt(1000);
        var delay = 1000;
        return Flux.fromArray(charArray)
                .delayElements(Duration.ofMillis(delay));
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

        fluxAndMonoGeneratorService.namesFlux()
                .subscribe(name -> {
                    System.out.println("Name is : " + name);
                });

        fluxAndMonoGeneratorService.nameMono()
                .subscribe(name -> {
                    System.out.println("Mono name is : " + name);
                });
    }
}
