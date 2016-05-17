package com.intendia.gwt.example;

import com.google.gwt.dev.DevMode;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class Main {
    public static void main(String[] args) throws Exception {
        Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[] {
                new File("src/main/java").toURI().toURL(),
                new File("target/generated-sources/annotations").toURI().toURL() },
                Thread.currentThread().getContextClassLoader()));
        DevMode devMode = new DevMode() {
            {
                setHeadless(true);
                new DevMode.ArgProcessor(options).processArgs(
                        "-superDevMode",
                        "-bindAddress", "0.0.0.0",
                        "-war", "target/war",
                        "-startupUrl", "example/index.html",
                        "com.intendia.gwt.example.Example");
            }
        };
        devMode.run();
        System.exit(0);
    }
}
