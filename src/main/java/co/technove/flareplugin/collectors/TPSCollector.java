package co.technove.flareplugin.collectors;

import co.technove.flare.live.CollectorData;
import co.technove.flare.live.LiveCollector;
import co.technove.flare.live.formatter.SuffixFormatter;
import co.technove.flareplugin.CustomCategories;
import co.technove.flareplugin.utils.NMSHelper;

import java.time.Duration;
import java.util.Arrays;

public class TPSCollector extends LiveCollector {
    private static final CollectorData TPS = new CollectorData("airplane:tps", "TPS", "Ticks per second, or how fast the server updates. For a smooth server this should be a constant 20TPS.", SuffixFormatter.of("TPS"), CustomCategories.MC_PERF);
    private static final CollectorData MSPT = new CollectorData("airplane:mspt", "MSPT", "Milliseconds per tick, which can show how well your server is performing. This value should always be under 50mspt.", SuffixFormatter.of("mspt"), CustomCategories.MC_PERF);

    public TPSCollector() {
        super(TPS, MSPT);

        this.interval = Duration.ofSeconds(5);
    }

    @Override
    public void run() {
        final long[] times = NMSHelper.getTickTimes5s();
        final double[] tps = NMSHelper.getTps();

        if (times.length == 0 && tps.length == 0) {
            return;
        }

        final double mspt = ((double) Arrays.stream(times).sum() / (double) times.length) * 1.0E-6D;

        this.report(TPS, Math.min(20D, Math.round(tps[0] * 100d) / 100d));
        this.report(MSPT, (double) Math.round(mspt * 100d) / 100d);
    }
}
