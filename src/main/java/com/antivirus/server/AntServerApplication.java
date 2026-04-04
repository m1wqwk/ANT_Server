package com.antivirus.server;

import com.antivirus.server.signature.SignatureProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SignatureProperties.class)
public class AntServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AntServerApplication.class, args);
	}

}
