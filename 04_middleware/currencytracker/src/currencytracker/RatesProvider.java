package currencytracker;

import javax.swing.plaf.ColorUIResource;
import java.util.*;
import java.util.function.Consumer;

public class RatesProvider {
    public static class Change {
        public final Currency currency;
        public final Double newRate;

        public Change(Currency currency, Double newRate) {
            this.currency = currency;
            this.newRate = newRate;
        }
    }

    public static final Currency REFERENCE_CURRENCY = Currency.PLN;

    private static RatesProvider singletonInstance = new RatesProvider();

    /**
     * Exchange rate for given currency relative to the reference currency.
     */
    private HashMap<Currency, Double> ratesDb = new HashMap<>();

    private Set<Consumer<Change>> listeners = new HashSet<>();


    public static RatesProvider getInstance() {
        return singletonInstance;
    }

    private RatesProvider() {
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

    public void addChangeListener(Consumer<Change> listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(Consumer<Change> listener) {
        listeners.remove(listener);
    }


    /**
     * Generates random currency rate change
     */
    public void changeRate() {
        final Random random = new Random();
        final Currency[] currencies = Currency.values();
        final Currency toChange = randomCurrency(currencies);

        final Double previousRate = ratesDb.get(toChange);
        // randomize change between 75% and 125%
        final Double change = 0.75 + random.nextDouble() * 0.5;
        final Double newRate = previousRate * change;

        System.out.println(String.format("Changing rate of %s by %f%%. New rate: %f",
                toChange, change * 100, newRate));

        ratesDb.put(toChange, newRate);
        listeners.forEach(l -> l.accept(new Change(toChange, newRate)));
    }

    private static Currency randomCurrency(Currency[] array) {
        final Random random = new Random();
        Currency randomCurrency;
        do {
            randomCurrency = array[random.nextInt(array.length)];
        } while (randomCurrency == Currency.UNRECOGNIZED);
        return randomCurrency;
    }
}
