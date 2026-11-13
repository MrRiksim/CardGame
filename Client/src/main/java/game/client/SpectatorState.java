package game.client;

import java.util.List;

/**
 * One SPECTATE_STATE message from the server: the whole board from neither
 * player's side. A hand or trap row the server hid comes through as null
 * (as opposed to an empty list) - with the server's default proxy both
 * hands and both trap rows are always hidden, and only their sizes and
 * occupied zones are known.
 */
public record SpectatorState(
        int matchId,
        int turnPlayer,
        int player1Health,
        int player2Health,
        int player1HandCount,
        int player2HandCount,
        List<ClientCard> player1Hand,
        List<ClientCard> player2Hand,
        List<ClientUnit> player1Front,
        List<ClientUnit> player2Front,
        List<ClientTrap> player1Back,
        List<ClientTrap> player2Back,
        List<Integer> player1BackZones,
        List<Integer> player2BackZones,
        Integer player1SecondsLeft,
        Integer player2SecondsLeft,
        ClientCard player1GraveyardTop,
        ClientCard player2GraveyardTop) {
}
