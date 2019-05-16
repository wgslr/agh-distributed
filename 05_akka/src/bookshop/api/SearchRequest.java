package bookshop.api;

public class SearchRequest extends Request {
    public final String title;

    public SearchRequest(String title) {
        this.title = title;
    }
}
