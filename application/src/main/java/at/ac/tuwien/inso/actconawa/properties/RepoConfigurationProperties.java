package at.ac.tuwien.inso.actconawa.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * {@link ConfigurationProperties} regarding the repository configuration of Actconawa.
 *
 * @param gitDir        Path to the gitDir (Pattern: *.git) containing the repository metadata
 * @param defaultBranch The default branch. Must not be blank.
 */
@Validated
@ConfigurationProperties(prefix = "actconawa.repo")
public record RepoConfigurationProperties(
        @Pattern(regexp = ".*\\.git")
        String gitDir,
        @NotBlank
        String defaultBranch
) {

}
