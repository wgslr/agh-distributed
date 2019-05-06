package bank;


public class CurrencyTranslator {

    public static currencytracker.Currency iceToGrpc(Currency currency)  {
        switch (currency) {
            case PLN:
                return currencytracker.Currency.PLN;
            case USD:
                return currencytracker.Currency.USD;
            case GBP:
                return currencytracker.Currency.GBP;
            case HRK:
                return currencytracker.Currency.HRK;
        }
        throw new IllegalArgumentException();
    }

    public static bank.Currency grpcToIce(currencytracker.Currency currency) {
        switch (currency) {
            case PLN:
                return bank.Currency.PLN;
            case USD:
                return bank.Currency.USD;
            case GBP:
                return bank.Currency.GBP;
            case HRK:
                return bank.Currency.HRK;
        }
        throw new IllegalArgumentException();
    }
}
