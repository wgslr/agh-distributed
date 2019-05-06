package bank;


import sun.print.CUPSPrinter;

public class CurrencyTranslator {

    public static currencytracker.Currency iceToGrpc(Currency currency) {
        switch (currency) {
            case PLN:
                return currencytracker.Currency.PLN;
            case EUR:
                return currencytracker.Currency.EUR;
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
            case EUR:
                return bank.Currency.EUR;
            case USD:
                return bank.Currency.USD;
            case GBP:
                return bank.Currency.GBP;
            case HRK:
                return bank.Currency.HRK;
        }
        throw new IllegalArgumentException();
    }

    public static bank.Currency stringToIce(String currencyStr) {
        if (currencyStr.toUpperCase().equals("PLN")) {
            return Currency.PLN;
        } else if (currencyStr.toUpperCase().equals("EUR")) {
            return Currency.EUR;
        } else if (currencyStr.toUpperCase().equals("USD")) {
            return Currency.USD;
        } else if (currencyStr.toUpperCase().equals("GBP")) {
            return Currency.GBP;
        } else if (currencyStr.toUpperCase().equals("HRK")) {
            return Currency.HRK;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
