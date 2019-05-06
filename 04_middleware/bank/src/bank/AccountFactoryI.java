package bank;

import com.zeroc.Ice.*;

public class AccountFactoryI implements AccountFactory {

    private final static MoneyAmount PREMIUM_THRESHOLD =
            new MoneyAmount(100000, BankServer.BASE_CURRENCY);


    private final CurrencyTrackerClient currencyTrackerClient;

    public AccountFactoryI(CurrencyTrackerClient currencyTrackerClient) {
        this.currencyTrackerClient = currencyTrackerClient;
    }

    @Override
    public AccountCreationResult createAccount(String firstName, String lastName, String PESEL,
                                               MoneyAmount monthlyIncome, Current current) throws AccountExistsException {
        if (monthlyIncome.currency != BankServer.BASE_CURRENCY) {
            throw new IllegalArgumentException("Monthly income must be given in the base currency");
        }
        if (monthlyIncome.minorUnitAmount < 0) {
            throw new IllegalArgumentException("Monthly income must be non negative");
        }

        final String name = firstName + " " + lastName;
        MoneyAmount balance = new MoneyAmount(
                monthlyIncome.minorUnitAmount * 10, monthlyIncome.currency);

        AccountI newAccount = shouldBePremium(monthlyIncome) ?
                new PremiumAccountI(PESEL, name, balance, currencyTrackerClient) :
                new AccountI(PESEL, name, balance);

        String category = newAccount.isPremium() ? "premium" : "standard";

        Identity id = new Identity(PESEL, category);
        if (current.adapter.find(id) != null) {
            throw new AccountExistsException();
        }

        ObjectPrx proxy = current.adapter.add(newAccount, id);

        return new AccountCreationResult(AccountPrx.uncheckedCast(proxy),
                newAccount.isPremium(), newAccount.key);
    }

    boolean shouldBePremium(MoneyAmount monthlyIncome) {
        return monthlyIncome.minorUnitAmount >= PREMIUM_THRESHOLD.minorUnitAmount;
    }

}
