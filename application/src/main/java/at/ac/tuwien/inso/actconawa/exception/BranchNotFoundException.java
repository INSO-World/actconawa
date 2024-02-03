package at.ac.tuwien.inso.actconawa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Branch not found")
public class BranchNotFoundException extends RuntimeException {

}
