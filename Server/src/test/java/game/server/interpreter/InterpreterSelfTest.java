package game.server.interpreter;

import game.server.Match;
import game.server.MatchFacade;
import game.server.MatchSettingsDirector;
import game.server.MatchmakingService;
import game.server.Player;
import game.server.PlayerConnection;
import game.server.cards.Card;
import game.server.cards.CardFactory;
import game.server.cards.Unit;
import game.server.cards.spells.ChainLightningSpell;
import game.server.cards.units.Knight;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class InterpreterSelfTest {
    private static final CommandParser PARSER = new CommandParser();

    public static void main(String[] args) throws Exception {
        catalog();
        expressionTreeAndGrammar();
        handLimitAndUndo();
        routing();
        System.out.println("All Interpreter command checks passed.");
    }

    private static void catalog() {
        check(CardFactory.getCardNames().size() == 29, "All 29 named cards, including elemental and starting cards, must be listed");
        for (String name : CardFactory.getCardNames()) {
            Card first = CardFactory.createByName(name).orElseThrow();
            Card second = CardFactory.createByName(name).orElseThrow();
            check(first.getName().equals(name) && !first.getId().equals(second.getId()),
                    "Named creation must return fresh, correctly named cards: " + name);
        }
        check(CardFactory.createByName(" chain_lightning ").orElseThrow().getName().equals("Chain Lightning"),
                "Names should accept case, whitespace and underscores");
        check(CardFactory.createByName("java.lang.Runtime").isEmpty(), "Only registered card names may be created");
    }

    private static void expressionTreeAndGrammar() {
        Match match = match();
        Player sender = current(match);
        InterpreterContext context = new InterpreterContext(new MatchFacade(match), sender);
        Expression<InterpretationResult> tree = PARSER.parse("/give Chain Lightning");
        check(tree instanceof GiveExpression give && give.cardName() instanceof CardNameExpression,
                "Parser must build a nonterminal Give expression containing a terminal card-name expression");
        check(sender.getHand().isEmpty(), "Parsing must not execute the command");
        check(tree.interpret(context).stateChanged() && sender.getHand().get(0).getName().equals("Chain Lightning"),
                "Interpretation must evaluate the name and grant the resolved card");
        check(PARSER.parse(" /GIVE   \"Bear Trap\" ").interpret(context).stateChanged(), "Quoted names and command case");
        check(PARSER.parse("/give Phantom Mirror").interpret(context).stateChanged(), "Non-random starting cards are available");
        check(PARSER.parse("/give Skeleton Warrior").interpret(context).stateChanged(), "Adapted cards are available");
        long version = match.getStateVersion();
        int size = sender.getHand().size();
        for (String invalid : List.of("/give", "/give No Such Card", "/give \"Knight", "/give Knight\"",
                "/give Knight 100", "/give Knight | END_TURN", "/unknown")) {
            check(!PARSER.parse(invalid).interpret(context).stateChanged(), "Invalid command must not mutate: " + invalid);
        }
        check(!PARSER.parse("/help").interpret(context).stateChanged(), "Help must not change state");
        check(PARSER.parse("/cards").interpret(context).message().contains("Raise the Fallen"), "Catalog command must list new cards");
        check(version == match.getStateVersion() && size == sender.getHand().size(), "Queries and errors must preserve state");
        Player other = match.getOpponent(sender);
        int senderSize = sender.getHand().size();
        check(PARSER.parse("/give Knight").interpret(new InterpreterContext(new MatchFacade(match), other)).stateChanged()
                && other.getHand().size() == 1 && sender.getHand().size() == senderSize,
                "Commands must grant only to their context's player, on either turn");
        match.endMatch();
        check(!PARSER.parse("/give Knight").interpret(context).stateChanged(), "Ended match cannot grant cards");
    }

    private static void handLimitAndUndo() {
        Match match = match();
        Player sender = current(match);
        InterpreterContext context = new InterpreterContext(new MatchFacade(match), sender);
        Unit enemy = Knight.create();
        match.getOpponent(sender).getFrontZones()[0] = enemy;
        sender.restoreEnergy(20);
        ChainLightningSpell spell = ChainLightningSpell.create();
        sender.getHand().add(spell);
        check(match.playSpell(sender, spell.getId(), enemy) && match.canUndoSpell(sender), "Prepare prior undo history");
        while (!sender.isHandFull()) sender.addToHand(Knight.create());
        long version = match.getStateVersion();
        InterpretationResult full = PARSER.parse("/give Knight").interpret(context);
        check(!full.stateChanged() && full.message().contains("hand is full")
                && version == match.getStateVersion() && match.canUndoSpell(sender),
                "Failed grants must preserve capacity, version and prior undo history");
        sender.getHand().remove(sender.getHand().size() - 1);
        int energy = sender.getEnergy();
        check(PARSER.parse("/give Chain Lightning").interpret(context).stateChanged(), "Grant with one free slot");
        check(sender.isHandFull() && sender.getEnergy() == energy && match.getStateVersion() == version + 1,
                "Grant must respect the cap, cost no energy and invalidate cached state");
        check(!match.undoLastSpell(sender), "Earlier spell undo must not erase granted cards");
        Card granted = sender.getHand().get(sender.getHand().size() - 1);
        check(match.playSpell(sender, granted.getId(), enemy) && match.undoLastSpell(sender)
                && sender.getHand().contains(granted), "A later spell must still use normal Memento undo");
    }

    @SuppressWarnings("unchecked")
    private static void routing() throws Exception {
        MatchmakingService service = new MatchmakingService();
        DummyConnection sender = new DummyConnection();
        DummyConnection opponent = new DummyConnection();
        DummyConnection spectator = new DummyConnection();
        service.handleMessage(sender, "JOIN|STANDARD");
        service.handleMessage(opponent, "JOIN|STANDARD");
        service.handleMessage(spectator, "SPECTATE|1");
        // Arrange a free hand slot through the test fixture, then exercise the actual chat handler.
        Field field = MatchmakingService.class.getDeclaredField("playerMatches");
        field.setAccessible(true);
        MatchFacade facade = ((Map<PlayerConnection, MatchFacade>) field.get(service)).get(sender);
        Player player = facade.getPlayerFor(sender);
        player.getHand().clear();
        sender.messages.clear(); opponent.messages.clear(); spectator.messages.clear();
        service.handleMessage(sender, "CHAT|/give \"Chain Lightning\"");
        check(player.getHand().size() == 1 && player.getHand().get(0).getName().equals("Chain Lightning"),
                "Chat command must grant through the Interpreter");
        check(sender.messages.stream().anyMatch(message -> message.startsWith("CHAT_INFO|Added Chain Lightning"))
                && sender.messages.stream().anyMatch(message -> message.startsWith("STATE|")),
                "Sender must receive feedback and the updated hand");
        check(opponent.messages.stream().anyMatch(message -> message.startsWith("STATE|"))
                && spectator.messages.stream().anyMatch(message -> message.startsWith("SPECTATE_STATE|")),
                "Opponent and spectators must get refreshed game state");
        check(opponent.messages.stream().noneMatch(message -> message.startsWith("CHAT"))
                && spectator.messages.stream().noneMatch(message -> message.startsWith("CHAT")),
                "Commands and their feedback must not be broadcast as chat");
        sender.messages.clear(); opponent.messages.clear();
        service.handleMessage(sender, "CHAT|/cards");
        check(sender.messages.size() == 1 && sender.messages.get(0).startsWith("CHAT_INFO|Available cards:")
                && opponent.messages.isEmpty(), "Catalog query must be private and avoid state broadcasts");
        service.handleMessage(sender, "CHAT|normal chat still works");
        check(opponent.messages.equals(List.of("CHAT|1|normal chat still works")), "Ordinary chat must still relay normally");
        service.handleMessage(spectator, "CHAT|/give Knight");
        check(spectator.messages.stream().anyMatch(message -> message.startsWith("CHAT_ERROR|")),
                "Spectators must not execute commands");
    }

    private static Match match() {
        Match match = new Match(1, new DummyConnection(), new DummyConnection(),
                new MatchSettingsDirector().buildStandardMatch());
        match.getPlayer1().getHand().clear();
        match.getPlayer2().getHand().clear();
        return match;
    }

    private static Player current(Match match) {
        return match.getCurrentTurnPlayerNumber() == 1 ? match.getPlayer1() : match.getPlayer2();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class DummyConnection extends PlayerConnection {
        private final List<String> messages = new ArrayList<>();
        DummyConnection() { super(null); }
        @Override public void send(String message) { messages.add(message); }
        @Override public boolean isConnected() { return true; }
    }
}
