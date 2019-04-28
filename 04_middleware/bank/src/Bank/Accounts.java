package Bank;

import java.util.HashMap;
import java.util.Map;

public class Accounts {

    private final static Currency BASE_CURRENCY = Currency.PLN;
    private final static MoneyAmount PREMIUM_THRESHOLD =
            new MoneyAmount(100000, BASE_CURRENCY);

    Map<String, Account> standardAccounts = new HashMap<>();
    Map<String, Account> premiumAccounts = new HashMap<>();


    Account createAccount(String firstName, String lastName, String PESEL,
                          MoneyAmount monthlyIncome) {
        if (monthlyIncome.currency != BASE_CURRENCY) {
            throw new IllegalArgumentException("Monthly income must be given in the base currency");
        }
        if (monthlyIncome.minorUnitAmount < 0) {
            throw new IllegalArgumentException("Monthly income must be non negative");
        }

        final String name = firstName + " " + lastName;
        MoneyAmount balance = new MoneyAmount(
                monthlyIncome.minorUnitAmount * 10, monthlyIncome.currency);

        if (monthlyIncome.minorUnitAmount >= PREMIUM_THRESHOLD.minorUnitAmount) {
            return new PremiumAccountI(PESEL, name, balance);
        } else {
            return new AccountI(PESEL, name, balance);
        }
    }

}
