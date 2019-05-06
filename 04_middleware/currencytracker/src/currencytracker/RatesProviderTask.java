package currencytracker;

public class RatesProviderTask implements Runnable {
    private static int CHANGE_PERIOD_SECONDS = 5;

    @Override
    public void run() {
        final RatesProvider rp = RatesProvider.getInstance();

        while (true) {
            try {
                Thread.sleep(CHANGE_PERIOD_SECONDS * 1000);
                rp.changeRate();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
