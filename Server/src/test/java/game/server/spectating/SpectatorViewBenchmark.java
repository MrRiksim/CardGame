package game.server.spectating;

import game.server.Match;
import game.server.MatchSettingsDirector;
import game.server.Player;
import game.server.PlayerConnection;
import game.server.cards.Card;
import game.server.cards.spells.HealthBoostSpell;
import game.server.cards.spells.LightningStrike;
import game.server.cards.spells.ShieldSpell;
import game.server.cards.traps.BearTrap;
import game.server.cards.units.Jumpy;
import game.server.cards.units.Knight;
import game.server.cards.units.Skelly;
import game.server.cards.units.Tombstone;
import org.java_websocket.WebSocket;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Speed and memory of the spectator proxies against the real subject they
 * stand in for. Run from Server/ (see CLAUDE.md for the exact command):
 *
 * <pre>
 * mvn test-compile exec:exec -Dexec.executable=java -Dexec.classpathScope=test
 *     "-Dexec.args=-cp %classpath org.openjdk.jmh.Main SpectatorViewBenchmark -prof gc"
 * </pre>
 *
 * Time is "Score" (ns per call); memory is "gc.alloc.rate.norm" from the gc
 * profiler (bytes allocated per call - for the create* benchmarks, roughly
 * what one view keeps alive for as long as its match lasts).
 *
 * <p>Two groups:
 * <ul>
 *   <li><b>snapshot*</b> - one spectator update through each view. The
 *       caching proxy is measured twice: when nothing changed since the last
 *       call (a cache hit, like the once-a-second clock re-broadcast between
 *       ticks) and right after a change (a miss).</li>
 *   <li><b>create*</b> - giving one new match its view, which the server
 *       does for every match whether anybody watches it or not. This is
 *       where the delayed-creation proxy should win: it doesn't create the
 *       real subject (and its first full snapshot) at all.</li>
 * </ul>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class SpectatorViewBenchmark {

    private Match match;
    private Match changingMatch;

    private MatchStateView real;
    private MatchStateView protection;
    private MatchStateView caching;
    private MatchStateView cachingAfterChange;
    private MatchStateView delayedCreation;

    @Setup
    public void setUp() {
        match = midGameMatch();
        changingMatch = midGameMatch();

        real = new LiveMatchState(match);
        protection = new HiddenInformationProxy(new LiveMatchState(match));
        caching = new CachingStateProxy(new LiveMatchState(match), match);
        cachingAfterChange = new CachingStateProxy(new LiveMatchState(changingMatch), changingMatch);
        delayedCreation = new LazyStateProxy(() -> new LiveMatchState(match));

        // Past their first call, so these measure steady state, not setup.
        caching.snapshot();
        delayedCreation.snapshot();
    }

    @Benchmark
    public MatchSnapshot snapshotReal() {
        return real.snapshot();
    }

    @Benchmark
    public MatchSnapshot snapshotProtection() {
        return protection.snapshot();
    }

    @Benchmark
    public MatchSnapshot snapshotCachingUnchanged() {
        return caching.snapshot();
    }

    /* endMatch is the cheapest action that moves the state version - a cache miss every call. */
    @Benchmark
    public MatchSnapshot snapshotCachingAfterChange() {
        changingMatch.endMatch();
        return cachingAfterChange.snapshot();
    }

    @Benchmark
    public MatchSnapshot snapshotDelayedCreation() {
        return delayedCreation.snapshot();
    }

    @Benchmark
    public MatchStateView createReal() {
        return new LiveMatchState(match);
    }

    @Benchmark
    public MatchStateView createProtection() {
        return new HiddenInformationProxy(new LiveMatchState(match));
    }

    @Benchmark
    public MatchStateView createCaching() {
        return new CachingStateProxy(new LiveMatchState(match), match);
    }

    @Benchmark
    public MatchStateView createDelayedCreation() {
        return new LazyStateProxy(() -> new LiveMatchState(match));
    }

    /*
     * A Speed match (so the clocks are part of every snapshot) partway
     * through: fixed hands, a few units including a buffed one and one
     * carrying riders, and some face-down traps - the same for every run.
     */
    private static Match midGameMatch() {
        Match match = new Match(1, fakeConnection(), fakeConnection(),
                new MatchSettingsDirector().buildSpeedMatch());

        for (Player player : List.of(match.getPlayer1(), match.getPlayer2())) {
            player.getHand().clear();
            for (Card card : List.of(Knight.create(), LightningStrike.create(), ShieldSpell.create(),
                    BearTrap.create(), Jumpy.create())) {
                player.getHand().add(card);
            }

            player.getFrontZones()[0] = Knight.create();
            player.getFrontZones()[1] = Tombstone.create();
            player.getFrontZones()[2] = Skelly.create().attach(Jumpy.create()).attach(Jumpy.create());
            player.getFrontZones()[3] = new game.server.cards.units.buffs.HealthBoostBuff(
                    Knight.create(), (game.server.cards.Spell) HealthBoostSpell.create());
            player.getBackZones()[0] = BearTrap.create();
            player.getBackZones()[3] = BearTrap.create();
            player.getGraveyard().add(Knight.create());
        }
        return match;
    }

    /* A socket that is never open, so the match's own broadcasts go nowhere. */
    private static PlayerConnection fakeConnection() {
        WebSocket socket = (WebSocket) java.lang.reflect.Proxy.newProxyInstance(
                WebSocket.class.getClassLoader(), new Class<?>[]{WebSocket.class},
                (proxy, method, args) -> method.getReturnType() == boolean.class ? false : null);
        return new PlayerConnection(socket);
    }
}
