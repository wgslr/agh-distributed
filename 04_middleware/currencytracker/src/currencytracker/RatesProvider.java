package currencytracker;

import java.lang.management.PlatformLoggingMXBean;
import java.util.HashMap;

public class RatesProvider {
    public static final Currency REFERENCE_CURRENCY = Currency.PLN;

    /**
     * Exchange rate for given currency relative to the reference currency.
     */
    private HashMap<Currency, Double> ratesDb = new HashMap<>();

    public RatesProvider() {
        initializeRates();
    }

    private void initializeRates() {
        ratesDb.put(Currency.PLN, 1.0);
        ratesDb.put(Currency.EUR, 4.0);
        ratesDb.put(Currency.USD, 3.0);
        ratesDb.put(Currency.GBP, 5.0);
        ratesDb.put(Currency.HRK, 0.5);
    }


    public double getRate(Currency base, Currency other) {
        return ratesDb.get(other) / ratesDb.get(base);
    }

}
