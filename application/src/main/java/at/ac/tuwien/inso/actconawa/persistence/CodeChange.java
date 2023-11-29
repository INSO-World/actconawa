package at.ac.tuwien.inso.actconawa.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "code_change")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class CodeChange {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String type;

    @Column(name = "identifier", nullable = false)
    private String identifier;

    @Column(name = "src_line_start", nullable = false)
    private int sourceLineStart;

    @Column(name = "src_line_end", nullable = false)
    private int sourceLineEnd;

    // Flag this this as pseudo change that just provides context for a real change.
    private boolean justContext;

    @ManyToOne
    private CodeChange parent;

    @Lazy
    @OneToMany(mappedBy = "parent")
    private List<CodeChange> children;

    @ManyToOne
    @JoinColumn(name = "diff_hunk_id")
    private GitCommitDiffHunk diffHunk;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getSourceLineStart() {
        return sourceLineStart;
    }

    public void setSourceLineStart(int sourceLineStart) {
        this.sourceLineStart = sourceLineStart;
    }

    public int getSourceLineEnd() {
        return sourceLineEnd;
    }

    public void setSourceLineEnd(int sourceLineEnd) {
        this.sourceLineEnd = sourceLineEnd;
    }

    public boolean isJustContext() {
        return justContext;
    }

    public void setJustContext(boolean justContext) {
        this.justContext = justContext;
    }

    public CodeChange getParent() {
        return parent;
    }

    public void setParent(CodeChange parent) {
        this.parent = parent;
    }

    public List<CodeChange> getChildren() {
        return children;
    }

    public void setChildren(List<CodeChange> children) {
        this.children = children;
    }

    public GitCommitDiffHunk getDiffHunk() {
        return diffHunk;
    }

    public void setDiffHunk(GitCommitDiffHunk diffHunk) {
        this.diffHunk = diffHunk;
    }
}
