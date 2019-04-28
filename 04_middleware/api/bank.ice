#ifndef BANK_ICE
#define BANK_ICE

module Bank
{
    enum Currency { PLN, EUR, USD, GBP }

    struct MoneyAmount {
        // i.e. for 100 PLN this field contains number of Grosz's (10000)
        // to avoid floating point precision issues
        int minorUnitAmount;
        Currency currency;
    }

    interface Account
    {
        idempotent MoneyAmount getBalance();
    }

    interface PremiumAccount extends Account
    {
        // TODO loan request
    }

    interface AccountFactory {
        Account* createAccount(string pesel, MoneyAmount monthlyIncome);
    }
    
};

#endif
