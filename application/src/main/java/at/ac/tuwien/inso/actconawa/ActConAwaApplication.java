package at.ac.tuwien.inso.actconawa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan
@EnableJpaRepositories
@EnableConfigurationProperties
@ConfigurationPropertiesScan("at.ac.tuwien.inso.actconawa.properties")
public class ActConAwaApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActConAwaApplication.class, args);
    }

}
