package Bank;

import com.zeroc.Ice.Current;
import com.zeroc.Ice.Object;
import com.zeroc.Ice.ServantLocator;
import com.zeroc.Ice.UserException;

import java.util.HashMap;
import java.util.Map;

public class Accounts implements ServantLocator {

    private final static Currency BASE_CURRENCY = Currency.PLN;
    private final static MoneyAmount PREMIUM_THRESHOLD =
            new MoneyAmount(100000, BASE_CURRENCY);

    private Map<String, Account> standardAccounts = new HashMap<>();
    private Map<String, Account> premiumAccounts = new HashMap<>();


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

        Account newAccount;
        if (monthlyIncome.minorUnitAmount >= PREMIUM_THRESHOLD.minorUnitAmount) {
            newAccount = new PremiumAccountI(PESEL, name, balance);
            premiumAccounts.put(PESEL, newAccount);
        } else {
            newAccount = new AccountI(PESEL, name, balance);
            standardAccounts.put(PESEL, newAccount);
        }
        return newAccount;
    }

    Account findStandardAccount(String PESEL) {
        return standardAccounts.get(PESEL);
    }

    Account findPremiumAccount(String PESEL) {
        return premiumAccounts.get(PESEL);
    }

    @Override
    public LocateResult locate(Current current) throws UserException {
        Map<String, Account> collection;
        if(current.id.category.equals("premium")) {
            collection = premiumAccounts;
        } else if (current.id.category.equals("standard")) {;
            collection = standardAccounts;
        } else {
            // TODO use an Ice or custom exception
            throw new IllegalArgumentException();
        }

        String PESEL = current.id.name;
        return new LocateResult(collection.get(PESEL), null);
    }

    @Override
    public void finished(Current current, Object object, java.lang.Object o) throws UserException {

    }

    @Override
    public void deactivate(String s) {

    }
}
