package com.pathfinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication

public class PathfinderApplication implements CommandLineRunner {

    @Value("${pathfinder.encode}")
    private Boolean printSomeEncodedPasswords;

    @Value("${pathfinder.passwords-count}")
    private Integer passwordsCount;

    private final PasswordEncoder encoder;

    @Autowired
    public PathfinderApplication(PasswordEncoder encoder) {

        this.encoder = encoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(PathfinderApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if(printSomeEncodedPasswords) {
            for (int i = 0; i < passwordsCount; i++) {
                System.out.println(encoder.encode("1234"));
            }
        }
    }

}
