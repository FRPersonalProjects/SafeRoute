package com.saferoute.config_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class Startup {

	public static void main(String[] args) {
		SpringApplication.run(Startup.class, args);
	}

}