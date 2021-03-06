#ifndef BANK_ICE
#define BANK_ICE

module bank
{
    enum Currency { PLN, EUR, USD, GBP, HRK }

    exception AuthenticationException {}

    exception AccountExistsException {}

    // Indicates that a bank does not handle
    // operations in given currency
    exception UnsupportedCurrencyException { 
        Currency currency;
    }

    struct MoneyAmount {
        // i.e. for 100 GBP this field contains number of groszs (10000)
        // to avoid floating point precision issues
        int minorUnitAmount;
        Currency currency;
    }

    struct LoanOffer {
        MoneyAmount foreignCost;
        MoneyAmount convertedCost;
    };


    interface Account
    {
        idempotent MoneyAmount getBalance() throws AuthenticationException;
    }

    interface PremiumAccount extends Account
    {
        idempotent LoanOffer requestLoan(MoneyAmount value, int durationMonths)
            throws AuthenticationException, UnsupportedCurrencyException;
    }

    struct AccountCreationResult {
        Account* account;
        bool isPremium;
        string key;
    }

    interface AccountFactory {
        AccountCreationResult createAccount(
            string firstName, string lastName,
            string PESEL, MoneyAmount monthlyIncome
        ) throws AccountExistsException;
    }

};

#endif
