package bookshop.api;

public class SearchResult extends SuccessResponse {
    public final String title;
    public final double price;

    public SearchResult(String title, double price) {
        this.title = title;
        this.price = price;
    }
}
