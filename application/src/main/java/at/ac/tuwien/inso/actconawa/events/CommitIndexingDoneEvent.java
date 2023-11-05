package at.ac.tuwien.inso.actconawa.events;

import org.springframework.context.ApplicationEvent;

public class CommitIndexingDoneEvent extends ApplicationEvent {

    public CommitIndexingDoneEvent(Object source) {
        super(source);
    }
}
