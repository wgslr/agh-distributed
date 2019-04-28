package Bank;
import com.zeroc.Ice.Current;

public class AccountI implements Account {
    private static final long serialVersionUID = -2448962912780867770L;
    @Override
    public MoneyAmount getBalance(Current current) {
        MoneyAmount ma = new MoneyAmount();
        ma.currency = Currency.PLN;
        ma.minorUnitAmount = 3300;
        return ma;
    }
}
