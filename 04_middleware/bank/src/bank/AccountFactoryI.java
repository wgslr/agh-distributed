package bank;

import com.zeroc.Ice.*;

public class AccountFactoryI implements AccountFactory {

    private final static Currency BASE_CURRENCY = Currency.PLN;
    private final static MoneyAmount PREMIUM_THRESHOLD =
            new MoneyAmount(100000, BASE_CURRENCY);



    @Override
    public AccountCreationResult createAccount(String firstName, String lastName, String PESEL,
                                               MoneyAmount monthlyIncome, Current current) {
        if (monthlyIncome.currency != BASE_CURRENCY) {
            throw new IllegalArgumentException("Monthly income must be given in the base currency");
        }
        if (monthlyIncome.minorUnitAmount < 0) {
            throw new IllegalArgumentException("Monthly income must be non negative");
        }

        final String name = firstName + " " + lastName;
        MoneyAmount balance = new MoneyAmount(
                monthlyIncome.minorUnitAmount * 10, monthlyIncome.currency);

        AccountI newAccount = shouldBePremium(monthlyIncome) ?
                new PremiumAccountI(PESEL, name, balance) :
                new AccountI(PESEL, name, balance);

        String category = newAccount.isPremium() ? "premium" : "standard";

        ObjectPrx proxy = current.adapter.add(newAccount, new Identity(PESEL, category));

        return new AccountCreationResult(AccountPrx.uncheckedCast(proxy),
                newAccount.isPremium(), newAccount.key);
    }

    boolean shouldBePremium(MoneyAmount monthlyIncome) {
        return monthlyIncome.minorUnitAmount >= PREMIUM_THRESHOLD.minorUnitAmount;
    }

}
