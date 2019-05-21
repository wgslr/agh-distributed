package bookshop.api;

public class OrderResult extends SuccessResponse {
    public final String title;

    public OrderResult(String title) {
        this.title = title;
    }
}
