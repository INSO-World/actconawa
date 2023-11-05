package at.ac.tuwien.inso.actconawa.config;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GitRepoConfig {


    @Value("${actconawa.repo}")
    private String repo;

    @Bean()
    public Git gitApi() throws IOException {
        return new Git(repository());
    }

    @Bean
    public Repository repository() throws IOException {
        return new FileRepository(repo);
    }

}
