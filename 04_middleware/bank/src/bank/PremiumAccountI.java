package bank;

import com.zeroc.Ice.Current;
import sun.rmi.server.UnicastServerRef;

public class PremiumAccountI extends AccountI implements PremiumAccount {
    private static double YEARLY_INTEREST_RATE = 0.1;

    final private CurrencyTrackerClient currencyTrackerClient;

    public PremiumAccountI(String ownerPesel, String ownerName, MoneyAmount balance,
                           CurrencyTrackerClient currencyTrackerClient) {
        super(ownerPesel, ownerName, balance);
        this.currencyTrackerClient = currencyTrackerClient;
    }

    @Override
    public boolean isPremium() {
        return true;
    }

    @Override
    public LoanOffer requestLoan(MoneyAmount value, int durationMonths, Current current) throws AuthenticationException, UnsupportedCurrencyException {
        checkAuthentication(current);

        System.out.println(String.format("Calculating loan in %s for %d months",
                value.currency, durationMonths));

        double interest = value.minorUnitAmount * durationMonths / 12.0 * YEARLY_INTEREST_RATE;
        MoneyAmount totalCost = new MoneyAmount(
                (int) Math.round(value.minorUnitAmount * (1 + interest)),
                value.currency
        );

        double rate = currencyTrackerClient.getRate(value.currency)
                .orElseThrow(() -> new UnsupportedCurrencyException(value.currency));
        System.out.println(String.format("Rate for %s: %f", value.currency, rate));
        MoneyAmount convertedCost = new MoneyAmount(
                (int) Math.round(totalCost.minorUnitAmount * rate),
                BankServer.BASE_CURRENCY
        );


        System.out.println(String.format("Loan cost is %.2f %s (%.2f %s)",
                totalCost.minorUnitAmount / 100.0, totalCost.currency,
                convertedCost.minorUnitAmount / 100.0, convertedCost.currency));

        return new LoanOffer(totalCost, convertedCost);
    }
}
