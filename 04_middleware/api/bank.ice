#ifndef BANK_ICE
#define BANK_ICE

module Bank
{
    enum Currency { PLN, EUR, USD, GBP }

    struct MoneyAmount {
        int minorUnitAmount;
        Currency currency;
    }
    
    interface Account
    {
        MoneyAmount getBalance();
    }
};

#endif
