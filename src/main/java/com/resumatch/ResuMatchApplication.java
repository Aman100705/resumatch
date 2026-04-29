package com.resumatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "ResuMatch API",
        version = "1.0",
        description = "AI-powered resume ↔ job description matcher. " +
                      "Upload resumes and JDs, get match scores, discover keyword gaps.",
        contact = @Contact(name = "Aman Patel", email = "ap3668@srmist.edu.in")
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class ResuMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResuMatchApplication.class, args);
    }
}
