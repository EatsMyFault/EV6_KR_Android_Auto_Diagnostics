package hmgapi.mods.ev6kr;

import hmgapi.hook.LoadedApp;
import hmgapi.hook.MethodHook;
import hmgapi.hook.MethodHook.Param;
import hmgapi.hook.ModContext;
import hmgapi.hook.ModEntry;

public final class AndroidAutoDiagnostics extends ModEntry {
    private static final String GPM = "com.google.android.projection.sink";
    private static final String PREFIX = "AA_DIAG_010 ";

    @Override
    public void appLoaded(ModContext mod, LoadedApp app) {
        if (!GPM.equals(app.packageName)) return;

        Trace trace = new Trace(mod);
        ClassLoader loader = app.classLoader;
        int installed = observe(mod, loader, "gpm.sensor_source.init", trace::sensorInit)
                + observe(mod, loader, "gpm.navigation_status.init", trace::navigationInit)
                + observe(mod, loader, "gpm.navigation_status.status", trace::status)
                + observe(mod, loader, "gpm.navigation_status.next_turn", trace::turn)
                + observe(mod, loader, "gpm.navigation_status.next_turn_distance", trace::distance)
                + observe(mod, loader, "gpm.receiver.destroy", p -> trace.end("receiver_destroy"))
                + observe(mod, loader, "gpm.receiver_service.disconnected", p -> trace.end("service"));
        mod.log(PREFIX + "GPM_READY hooks=" + installed + "/7");
    }

    private interface Callback {
        void record(Param p);
    }

    private static int observe(ModContext mod, ClassLoader loader, String point, Callback callback) {
        try {
            mod.hook(loader, point, new MethodHook() {
                @Override
                protected void afterHookedMethod(Param p) {
                    if (!p.hasThrowable()) callback.record(p);
                }
            });
            return 1;
        } catch (RuntimeException | LinkageError e) {
            mod.error(PREFIX + "GPM_HOOK_FAILED point=" + point
                    + " error=" + e.getClass().getSimpleName(), null);
            return 0;
        }
    }

    private static final class Trace {
        private final ModContext mod;
        private boolean sawEvent;
        private int statusCount;
        private int lastStatus;
        private int turnCount;
        private String lastTurn;
        private int distanceCount;

        Trace(ModContext mod) {
            this.mod = mod;
        }

        synchronized void sensorInit(Param p) {
            log("AA_SENSOR_INIT result=" + p.getResult());
        }

        synchronized void navigationInit(Param p) {
            Object cluster = p.args[3];
            String clusterName = cluster instanceof Enum<?> ? ((Enum<?>) cluster).name() : "unknown";
            log("AA_NAV_INIT cluster=" + clusterName + " result=" + p.getResult());
        }

        synchronized void status(Param p) {
            int code = (Integer) p.args[0];
            if (++statusCount == 1 || code != lastStatus) log("AA_STATUS code=" + code);
            lastStatus = code;
        }

        synchronized void turn(Param p) {
            // The callback also carries a road name and image, which this mod never reads.
            String turn = "side=" + p.args[1] + " event=" + p.args[2]
                    + " angle=" + p.args[4] + " number=" + p.args[5];
            if (sample(++turnCount) || !turn.equals(lastTurn)) {
                log("AA_TURN count=" + turnCount + " " + turn);
            }
            lastTurn = turn;
        }

        synchronized void distance(Param p) {
            if (sample(++distanceCount)) {
                log("AA_DISTANCE count=" + distanceCount + " meters=" + p.args[0]);
            }
        }

        synchronized void end(String source) {
            // A single disconnect can call both teardown methods.
            if (!sawEvent) return;
            mod.log(PREFIX + "AA_DISCONNECTED source=" + source + " statuses=" + statusCount
                    + " turns=" + turnCount + " distances=" + distanceCount);
            sawEvent = false;
            statusCount = 0;
            turnCount = 0;
            lastTurn = null;
            distanceCount = 0;
        }

        private void log(String message) {
            sawEvent = true;
            mod.log(PREFIX + message);
        }

        private static boolean sample(int count) {
            return count <= 3 || count % 20 == 0;
        }
    }
}
