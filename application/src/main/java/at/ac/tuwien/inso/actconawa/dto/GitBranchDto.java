package at.ac.tuwien.inso.actconawa.dto;

public class GitBranchDto {
    private Long id;
    private String name;
    private Long headCommitId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getHeadCommitId() {
        return headCommitId;
    }

    public void setHeadCommitId(Long headCommitId) {
        this.headCommitId = headCommitId;
    }
}
