package bank;

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
    public MoneyAmount getBalance(Current current) throws AuthenticationException {
        checkAuthentication(current);
        return balance;
    }

    public boolean isPremium() {
        return false;
    }

    protected void checkAuthentication(Current current) throws AuthenticationException {
        String key = current.ctx.get("key");
        if (key == null || !key.equals(this.key)) {
            // TODO  descriptive excepion class
            throw new AuthenticationException();
        }

    }

    private static String generateKey() {
        return Long.toHexString(Double.doubleToLongBits(Math.random()));
    }
}
