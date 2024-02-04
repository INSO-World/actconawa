package at.ac.tuwien.inso.actconawa.config;

import at.ac.tuwien.inso.actconawa.properties.RepoConfigurationProperties;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
public class GitRepoConfig {

    private final RepoConfigurationProperties repoConfigurationProperties;

    public GitRepoConfig(RepoConfigurationProperties repoConfigurationProperties) {
        this.repoConfigurationProperties = repoConfigurationProperties;
    }

    @Bean
    public Git gitApi() throws IOException {
        return new Git(repository());
    }

    @Bean
    public Repository repository() throws IOException {
        // FileRepository is threadsafe.
        var potentialRepo = new File(repoConfigurationProperties.gitDir());
        if (!potentialRepo.exists() && !potentialRepo.isDirectory()) {
            throw new FileNotFoundException(potentialRepo.getAbsolutePath());
        }
        return new FileRepository(potentialRepo);
    }

}
