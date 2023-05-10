package at.ac.tuwien.inso.actconawa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EntityScan
@EnableJpaRepositories
@EnableConfigurationProperties
public class ActConAwaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActConAwaApplication.class, args);
    }

}
