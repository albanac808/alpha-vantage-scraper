package com.financialdata;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DependencyTest {
    public static void main(String[] args) {
        // Test HttpClient
        HttpClient client = HttpClients.createDefault();
        System.out.println("HttpClient initialized: " + (client != null));

        // Test Jackson
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("ObjectMapper initialized: " + (mapper != null));

        System.out.println("All dependencies working!");
    }
}