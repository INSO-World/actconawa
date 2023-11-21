package at.ac.tuwien.inso.actconawa.index;

public interface Indexer {

    /**
     * Method to start the specific indexer implementation.
     */
    void index();

    /**
     * Method to provide a {@link String} containing a short description of the indexed content. E.g. "commit and branch
     * information"
     *
     * @return the description of the content indexed by this indexer.
     */
    String getIndexedContentDescription();

}
