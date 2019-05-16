package bookshop.api;

import java.io.Serializable;

public class SearchRequest implements Serializable {
    public final String title;

    public SearchRequest(String title) {
        this.title = title;
    }
}
