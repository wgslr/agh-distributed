package bank;

public class PremiumAccountI extends AccountI {
    public PremiumAccountI(String ownerPesel, String ownerName, MoneyAmount balance) {
        super(ownerPesel, ownerName, balance);
    }

    @Override
    public boolean isPremium() {
        return false;
    }
}
