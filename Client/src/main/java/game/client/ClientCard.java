package game.client;

public record ClientCard(
        String id,
        CardType type,
        int zoneIndex,
        boolean played) {

    public static ClientCard handCard(
            String id,
            CardType type) {

        return new ClientCard(
                id,
                type,
                -1,
                false
        );
    }

    public static ClientCard playedCard(
            String id,
            CardType type,
            int zoneIndex) {

        return new ClientCard(
                id,
                type,
                zoneIndex,
                true
        );
    }
}