package at.ac.tuwien.inso.actconawa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Commit not found")
public class CommitNotFoundException extends RuntimeException {

}
