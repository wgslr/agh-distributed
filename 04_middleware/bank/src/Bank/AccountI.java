package Bank;

import com.zeroc.Ice.Current;

public class AccountI implements Account {
    private static final long serialVersionUID = -2448062912780867770L;

    public final String ownerPesel;
    public final String ownerName;
    protected MoneyAmount balance;
    public final String key;

    public AccountI(String ownerPesel, String ownerName, MoneyAmount balance) {
        this.ownerPesel = ownerPesel;
        this.ownerName = ownerName;
        this.balance = balance;
        this.key = generateKey();
    }

    @Override
    public MoneyAmount getBalance(Current current) {
        return balance;
    }


    private static String generateKey() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }

    boolean isPremium() {
        return false;
    }
}
