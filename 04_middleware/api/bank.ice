#ifndef BANK_ICE
#define BANK_ICE

module Bank
{
    enum Currency { PLN, EUR, USD, GBP, HRK }

    struct MoneyAmount {
        // i.e. for 100 PLN this field contains number of Grosz's (10000)
        // to avoid floating point precision issues
        int minorUnitAmount;
        Currency currency;
    }

    exception AuthenticationException {}

    interface Account
    {
        idempotent MoneyAmount getBalance() throws AuthenticationException;
    }

    interface PremiumAccount extends Account
    {
        // TODO loan request
    }

    struct AccountCreationResult {
        Account* account;
        bool isPremium;
        string key;
    }

    interface AccountFactory {
        AccountCreationResult createAccount(string firstName, string lastName,
                string PESEL, MoneyAmount monthlyIncome);
    }

};

#endif
