# Geriausias projektas

mes laimesime

## Prerequisites:
Download  IntelliJ:\
https://www.jetbrains.com/idea/download/

## How to run:
- Turn on IntelliJ, go to `File > Open` and click to open the pom.xml file in either server or client directory (they have to be opened separately). If you get a prompt saying "JDK is needed" then click install JDK and select at least version 17. After it is installed you SHOULD (hopefully) be able to run `GameServer.java` for a server instance or `GameClient.java` for client instances.\
- To run multiple client instances, with the client project opened go to `Run > Edit Configurations`, create a new application configuration, choose main class to be `GameClient of game.client`, hit `Modify options` right above it and select `Allow multiple instances`.

## Iterator spell cards

Three spells in the random card pool demonstrate custom `java.util.Iterator<Unit>` implementations
over three collections: the enemy front-zone array, the friendly front-zone array, and the graveyard list.

| Card | Cost | Iterator | Effect |
| --- | --- | --- | --- |
| Chain Lightning | 2 | `CircularTargetIterator` | Start at the selected enemy, move right and wrap once, skipping empty/dead zones. Deal 1 damage to each of up to 3 enemies. |
| Healing Wave | 2 | `WeakestFirstIterator` | Heal up to 3 distinct injured, unbuffed allies by 1 each, lowest current health first; zone order breaks ties. |
| Raise the Fallen | 3 | `RecentCardsIterator` | Return up to 2 unit cards from the graveyard to the hand, newest first, skipping spells/traps and respecting the hand limit. |

Drag Chain Lightning onto an enemy unit. Drop Healing Wave or Raise the Fallen onto your graveyard/deck utility area;
they select their recipients automatically. Casts with no eligible recipients are rejected.
The new cards use `chain_lightning.png`, `healing_wave.png`, and `raise_the_fallen.png` in `Client/assets/spells/`.

The iterators select and order recipients; the spells apply effects. Traversals snapshot collection membership
so removing a killed unit or recovering a graveyard card does not invalidate iteration. Circular and weakest-first
traversals also skip recipients that become ineligible before their visit. Healing follows the existing State rules:
buffed units cannot be healed. Each recipient is visited at most once per cast.

Chain Lightning supports spell undo through Memento. Match saves both players' hands, graveyards, energy and front rows;
each unit saves its own private state, recursively including buff layers, shield use, health bonuses, riders and adapted health.
Restoring the original objects preserves card IDs and shallow-copy links. The circular Iterator still controls targeting order.
Healing Wave and Raise the Fallen retain their existing non-undoable behavior and clear earlier spell undo history.

Run `game.server.cards.iterators.IteratorPatternSelfTest` from the server test sources to verify traversal order,
exhaustion, multi-target damage/death/shield handling, healing priority, recovery, hand capacity, and undo boundaries.
Run `game.server.cards.ChainLightningUndoSelfTest` for Chain Lightning undo regressions, including repeated casts,
stacked buffs, returned riders, linked copies, adapted units and turn boundaries.

## Match chat

During a match, the right sidebar shows chat below card details. Type a message and press Enter or click Send.
Both players can chat on either turn. Messages are sent only to the two players in that match; spectators cannot
read or post in player chat. Chat is disabled when the match ends or the connection closes.

Messages support up to 300 characters and the panel keeps the latest 200 messages locally. History is cleared
when a new match starts. The server assigns sender identity and relays `CHAT|playerNumber|text` independently
of gameplay state and spell undo. No chat history is stored on the server.

Run `game.server.ChatRoutingSelfTest` in the server project and `game.client.ChatPanelSelfTest` in the client
project to check match isolation, message handling and the chat composer.

## Interpreter chat commands

Type these commands in match chat:

```text
/give Chain Lightning
/give "Bear Trap"
/give health_boost
/cards
/help
```

`/give <card name>` gives one new card to the sender, without spending energy, during either player's turn.
Names ignore case and accept underscores instead of spaces; quotation marks are optional. `/cards` lists all
29 named cards, including elemental cards, Phantom Mirror and Essence Replication. Use the displayed card names.
The existing hand limit applies: if your hand is full, play a card first. In Standard, the opening hand already
contains 7 cards. Invalid names and syntax produce private feedback without changing game state.

Successful grants refresh player and spectator state. They clear earlier spell undo history so an older snapshot
cannot remove the granted card. Spells cast after a grant can still be undone normally. Commands and command
feedback are private to their sender; ordinary messages still go to both players.

The Interpreter implementation is in `Server/src/main/java/game/server/interpreter/`:

| Pattern role | Class | Responsibility |
| --- | --- | --- |
| Abstract expression | `Expression<T>` | Defines `interpret(context)` for expression nodes. |
| Terminal expression | `CardNameExpression` | Resolves a card-name literal through the card catalog. |
| Nonterminal expression | `GiveExpression` | Interprets its card-name child, then grants that card. |
| Terminal queries/feedback | `CardListExpression`, `FeedbackExpression` | List cards or report help and syntax errors. |
| Context | `InterpreterContext` | Supplies the authenticated player, match and card lookup. |
| Parser | `CommandParser` | Builds an expression tree from the command text. |

Grammar: `command := '/give' cardName | '/cards' | '/help'`, where `cardName` is an unquoted or quoted name.
`MatchmakingService` interprets the parsed expression; mutations go through `MatchFacade` into `Match`.
`CardFactory` creates named cards with its existing creators and preserves the random pool's weighting.

Run `game.server.interpreter.InterpreterSelfTest` to verify the expression tree, all card names, command grammar,
hand limits, private routing, state refreshes and interaction with spell undo.
