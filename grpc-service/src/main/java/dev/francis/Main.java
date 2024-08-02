package dev.francis;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class Main {
        public static void main(String[] args) throws IOException, InterruptedException {
            Server server = ServerBuilder
                    .forPort(9090)
                    .addService(new BookAuthorServerService()).build();

            server.start();
            System.out.println("server started");
            server.awaitTermination();
            System.out.println("server terminating");

            SpringApplication.run(Main.class, args);
        }


}