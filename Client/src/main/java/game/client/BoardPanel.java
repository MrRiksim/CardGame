package game.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoardPanel extends JPanel {

    /*
     * Overall board.
     */
    private static final int BOARD_WIDTH = 760;
    private static final int BOARD_HEIGHT = 900;

    /*
     * Portrait-oriented zones.
     */
    private static final int ZONE_WIDTH = 82;
    private static final int ZONE_HEIGHT = 112;

    private static final int ZONE_GAP = 10;

    /*
     * Portrait-oriented cards.
     */
    private static final int CARD_WIDTH = 64;
    private static final int CARD_HEIGHT = 96;

    /*
     * Five playable columns.
     */
    private static final int FIELD_LEFT = 140;

    /*
     * Opponent:
     *
     * Brown row + graveyard
     * Blue row + deck
     */
    private static final int OPPONENT_BLUE_Y = 150;
    private static final int OPPONENT_BROWN_Y = 275;

    /*
     * Player:
     *
     * Blue row + deck
     * Brown row + graveyard
     */
    private static final int PLAYER_BROWN_Y = 490;
    private static final int PLAYER_BLUE_Y = 615;
    /*
     * Hidden opponent hand.
     */
    private static final int OPPONENT_HAND_Y = 25;

    /*
     * Own hand.
     */
    private static final int PLAYER_HAND_Y = 790;

    /*
     * Colors.
     */
    private static final Color LIGHT_BROWN =
            new Color(215, 190, 145);

    private static final Color LIGHT_BLUE =
            new Color(175, 210, 235);

    private static final Color DARK_BROWN =
            new Color(95, 55, 30);

    private static final Color GRAVEYARD_GRAY =
            new Color(155, 155, 155);

    private final List<ClientCard> ownHand =
            new ArrayList<>();

    private final List<ClientCard> ownPlayed =
            new ArrayList<>();

    private final List<ClientCard> opponentPlayed =
            new ArrayList<>();

    private int opponentHandCount = 0;

    private boolean yourTurn = false;

    private boolean gameOver = false;

    /*
     * Card currently being dragged.
     *
     * IMPORTANT:
     * This is only ever a card from the hand.
     */
    private String draggedCardId;

    private ClientCard draggedCard;

    private int mouseX;
    private int mouseY;

    private final Consumer<CardMove> moveListener;

    public BoardPanel(
            Consumer<CardMove> moveListener) {

        this.moveListener = moveListener;

        setPreferredSize(
                new Dimension(
                        BOARD_WIDTH,
                        BOARD_HEIGHT
                )
        );

        setBackground(
                new Color(235, 235, 235)
        );

        setBorder(
                BorderFactory.createLineBorder(
                        Color.BLACK,
                        2
                )
        );

        MouseAdapter mouseAdapter =
                new MouseAdapter() {

                    @Override
                    public void mousePressed(
                            MouseEvent e) {

                        startDragging(
                                e.getX(),
                                e.getY()
                        );
                    }

                    @Override
                    public void mouseDragged(
                            MouseEvent e) {

                        if (draggedCard == null) {
                            return;
                        }

                        mouseX = e.getX();
                        mouseY = e.getY();

                        repaint();
                    }

                    @Override
                    public void mouseReleased(
                            MouseEvent e) {

                        finishDragging(
                                e.getX(),
                                e.getY()
                        );
                    }
                };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    public void updateState(
            List<ClientCard> ownHand,
            int opponentHandCount,
            List<ClientCard> ownPlayed,
            List<ClientCard> opponentPlayed,
            boolean yourTurn) {

        this.ownHand.clear();
        this.ownHand.addAll(ownHand);

        this.opponentHandCount =
                opponentHandCount;

        this.ownPlayed.clear();
        this.ownPlayed.addAll(ownPlayed);

        this.opponentPlayed.clear();
        this.opponentPlayed.addAll(opponentPlayed);

        this.yourTurn = yourTurn;

        /*
         * The server state is authoritative.
         */
        draggedCard = null;
        draggedCardId = null;

        repaint();
    }

    public void resetGame() {

        ownHand.clear();
        ownPlayed.clear();
        opponentPlayed.clear();

        opponentHandCount = 0;

        yourTurn = false;
        gameOver = false;

        draggedCard = null;
        draggedCardId = null;

        repaint();
    }

    public void setGameOver() {

        gameOver = true;
        yourTurn = false;

        draggedCard = null;
        draggedCardId = null;

        repaint();
    }

    /*
     * Immediately move a card visually when the player
     * successfully drops it on a valid zone.
     *
     * The server will then send the authoritative state.
     */
    public void applyLocalMove(
            String cardId,
            CardType type,
            int zoneIndex) {

        ClientCard movingCard = null;

        /*
         * Card was in our hand.
         */
        for (ClientCard card : ownHand) {

            if (card.id().equals(cardId)) {

                movingCard = card;
                break;
            }
        }

        if (movingCard != null) {

            ownHand.remove(movingCard);

            ownPlayed.add(
                    ClientCard.playedCard(
                            cardId,
                            type,
                            zoneIndex
                    )
            );

            repaint();
        }
    }

    /*
     * ONLY HAND CARDS ARE SEARCHED HERE.
     *
     * This is what prevents an already-played card
     * from being draggable.
     */
    private void startDragging(
            int x,
            int y) {

        if (!yourTurn || gameOver) {
            return;
        }

        for (int i = 0;
             i < ownHand.size();
             i++) {

            ClientCard card =
                    ownHand.get(i);

            Rectangle rectangle =
                    getHandCardRectangle(
                            i,
                            ownHand.size()
                    );

            if (rectangle.contains(x, y)) {

                draggedCard = card;
                draggedCardId = card.id();

                mouseX = x;
                mouseY = y;

                repaint();

                return;
            }
        }
    }

    private void finishDragging(
            int x,
            int y) {

        if (draggedCard == null) {
            return;
        }

        DropTarget target =
                findOwnDropTarget(
                        x,
                        y
                );

        /*
         * Invalid drop:
         *
         * Don't change the model.
         * Therefore the card snaps back.
         */
        if (target == null) {

            draggedCard = null;
            draggedCardId = null;

            repaint();

            return;
        }

        /*
         * Make sure brown cards only go to brown
         * zones and blue cards only go to blue zones.
         */
        if (!isCorrectType(
                draggedCard,
                target.type()
        )) {

            draggedCard = null;
            draggedCardId = null;

            repaint();

            return;
        }

        /*
         * Don't allow an occupied zone.
         */
        if (isOwnZoneOccupied(
                target.type(),
                target.zoneIndex()
        )) {

            draggedCard = null;
            draggedCardId = null;

            repaint();

            return;
        }

        /*
         * Visually snap the card into the zone.
         */
        applyLocalMove(
                draggedCard.id(),
                draggedCard.type(),
                target.zoneIndex()
        );

        /*
         * Send the logical operation to the server.
         */
        moveListener.accept(
                new CardMove(
                        draggedCard.id(),
                        target.type(),
                        target.zoneIndex()
                )
        );

        draggedCard = null;
        draggedCardId = null;

        repaint();
    }

    private boolean isCorrectType(
            ClientCard card,
            DropZoneType targetType) {

        if (targetType ==
                DropZoneType.BROWN) {

            return card.type() ==
                    CardType.LIGHT_BROWN;
        }

        return card.type() ==
                CardType.LIGHT_BLUE;
    }

    private boolean isOwnZoneOccupied(
            DropZoneType type,
            int zoneIndex) {

        for (ClientCard card :
                ownPlayed) {

            if (card.zoneIndex() != zoneIndex) {
                continue;
            }

            if (type ==
                    DropZoneType.BROWN &&
                    card.type() ==
                            CardType.LIGHT_BROWN) {

                return true;
            }

            if (type ==
                    DropZoneType.BLUE &&
                    card.type() ==
                            CardType.LIGHT_BLUE) {

                return true;
            }
        }

        return false;
    }

    /*
     * IMPORTANT:
     * This only checks the player's own two rows.
     *
     * The opponent's rows cannot be drop targets.
     */
    private DropTarget findOwnDropTarget(
            int x,
            int y) {

        /*
         * Player's blue row.
         */
        for (int zone = 0;
             zone < 5;
             zone++) {

            Rectangle rectangle =
                    getOwnZoneRectangle(
                            zone,
                            PLAYER_BLUE_Y
                    );

            if (rectangle.contains(x, y)) {

                return new DropTarget(
                        DropZoneType.BLUE,
                        zone
                );
            }
        }

        /*
         * Player's brown row.
         */
        for (int zone = 0;
             zone < 5;
             zone++) {

            Rectangle rectangle =
                    getOwnZoneRectangle(
                            zone,
                            PLAYER_BROWN_Y
                    );

            if (rectangle.contains(x, y)) {

                return new DropTarget(
                        DropZoneType.BROWN,
                        zone
                );
            }
        }

        return null;
    }

    @Override
    protected void paintComponent(
            Graphics g) {

        super.paintComponent(g);

        Graphics2D graphics =
                (Graphics2D) g.create();

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        drawBoard(graphics);
        drawOpponentHand(graphics);
        drawOwnHand(graphics);
        drawPlayedCards(graphics);

        if (draggedCard != null) {
            drawDraggedCard(graphics);
        }

        graphics.dispose();
    }

    private void drawBoard(
            Graphics2D graphics) {

        /*
         * ==========================
         * OPPONENT SIDE
         * ==========================
         */

        /*
         * Opponent brown row.
         */
        for (int zone = 0;
             zone < 5;
             zone++) {

            drawZone(
                    graphics,
                    zone,
                    OPPONENT_BROWN_Y,
                    LIGHT_BROWN,
                    true
            );
        }

        /*
         * Opponent blue row.
         */
        for (int zone = 0;
             zone < 5;
             zone++) {

            drawZone(
                    graphics,
                    zone,
                    OPPONENT_BLUE_Y,
                    LIGHT_BLUE,
                    true
            );
        }

        /*
         * Opponent graveyard on LEFT.
         */
        Rectangle opponentGraveyard =
                getOpponentUtilityRectangle(
                        OPPONENT_BROWN_Y
                );

        drawUtilityZone(
                graphics,
                opponentGraveyard,
                GRAVEYARD_GRAY,
                "GRAVEYARD",
                Color.BLACK
        );

        /*
         * Opponent deck on LEFT.
         */
        Rectangle opponentDeck =
                getOpponentUtilityRectangle(
                        OPPONENT_BLUE_Y
                );

        drawUtilityZone(
                graphics,
                opponentDeck,
                DARK_BROWN,
                "DECK",
                Color.WHITE
        );

        /*
         * ==========================
         * PLAYER SIDE
         * ==========================
         */

        /*
         * Player blue row.
         */
        for (int zone = 0;
             zone < 5;
             zone++) {

            drawZone(
                    graphics,
                    zone,
                    PLAYER_BLUE_Y,
                    LIGHT_BLUE,
                    false
            );
        }

        /*
         * Player brown row.
         */
        for (int zone = 0;
             zone < 5;
             zone++) {

            drawZone(
                    graphics,
                    zone,
                    PLAYER_BROWN_Y,
                    LIGHT_BROWN,
                    false
            );
        }

        /*
         * Player graveyard on RIGHT.
         */
        Rectangle playerGraveyard =
                getPlayerUtilityRectangle(
                        PLAYER_BROWN_Y
                );

        drawUtilityZone(
                graphics,
                playerGraveyard,
                GRAVEYARD_GRAY,
                "GRAVEYARD",
                Color.BLACK
        );

        /*
         * Player deck on RIGHT.
         */
        Rectangle playerDeck =
                getPlayerUtilityRectangle(
                        PLAYER_BLUE_Y
                );

        drawUtilityZone(
                graphics,
                playerDeck,
                DARK_BROWN,
                "DECK",
                Color.WHITE
        );

        /*
         * Center divider.
         */
        graphics.setColor(Color.BLACK);

        graphics.setStroke(
                new BasicStroke(2)
        );

        graphics.drawLine(
                40,
                435,
                BOARD_WIDTH - 40,
                435
        );

        /*
         * Side labels.
         */
        graphics.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        14
                )
        );

        graphics.drawString(
                "OPPONENT",
                10,
                120
        );

        graphics.drawString(
                "YOU",
                10,
                780
        );
    }

    private void drawZone(
            Graphics2D graphics,
            int zone,
            int y,
            Color color,
            boolean opponent) {

        Rectangle rectangle;

        if (opponent) {

            rectangle =
                    getOpponentZoneRectangle(
                            zone,
                            y
                    );

        } else {

            rectangle =
                    getOwnZoneRectangle(
                            zone,
                            y
                    );
        }

        graphics.setColor(color);

        graphics.fill(rectangle);

        graphics.setColor(Color.BLACK);

        graphics.draw(rectangle);
    }

    private void drawUtilityZone(
            Graphics2D graphics,
            Rectangle rectangle,
            Color background,
            String text,
            Color textColor) {

        graphics.setColor(background);

        graphics.fill(rectangle);

        graphics.setColor(Color.BLACK);

        graphics.draw(rectangle);

        graphics.setColor(textColor);

        graphics.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        12
                )
        );

        drawCenteredText(
                graphics,
                text,
                rectangle
        );
    }

    private void drawOpponentHand(
            Graphics2D graphics) {

        /*
         * Every opponent hand card is hidden.
         */
        int totalWidth =
                opponentHandCount
                        * CARD_WIDTH
                        + Math.max(
                        0,
                        opponentHandCount - 1
                ) * 8;

        int startX =
                (BOARD_WIDTH - totalWidth)
                        / 2;

        for (int i = 0;
             i < opponentHandCount;
             i++) {

            int x =
                    startX
                            + i
                            * (CARD_WIDTH + 8);

            Rectangle rectangle =
                    new Rectangle(
                            x,
                            OPPONENT_HAND_Y,
                            CARD_WIDTH,
                            CARD_HEIGHT
                    );

            graphics.setColor(
                    DARK_BROWN
            );

            graphics.fill(rectangle);

            graphics.setColor(Color.BLACK);

            graphics.draw(rectangle);

            graphics.setColor(Color.WHITE);

            drawCenteredText(
                    graphics,
                    "CARD",
                    rectangle
            );
        }
    }

    private void drawOwnHand(
            Graphics2D graphics) {

        for (int i = 0;
             i < ownHand.size();
             i++) {

            ClientCard card =
                    ownHand.get(i);

            /*
             * Don't draw the card twice while dragging.
             */
            if (card.id().equals(
                    draggedCardId)) {

                continue;
            }

            Rectangle rectangle =
                    getHandCardRectangle(
                            i,
                            ownHand.size()
                    );

            drawCard(
                    graphics,
                    card,
                    rectangle
            );
        }
    }

    private void drawPlayedCards(
            Graphics2D graphics) {

        /*
         * Own played cards.
         */
        for (ClientCard card :
                ownPlayed) {

            if (card.id().equals(
                    draggedCardId)) {

                continue;
            }

            Rectangle rectangle =
                    getPlayedCardRectangle(
                            card,
                            false
                    );

            drawCard(
                    graphics,
                    card,
                    rectangle
            );
        }

        /*
         * Opponent played cards are revealed.
         */
        for (ClientCard card :
                opponentPlayed) {

            Rectangle rectangle =
                    getPlayedCardRectangle(
                            card,
                            true
                    );

            drawCard(
                    graphics,
                    card,
                    rectangle
            );
        }
    }

    private void drawDraggedCard(
            Graphics2D graphics) {

        Rectangle rectangle =
                new Rectangle(
                        mouseX - CARD_WIDTH / 2,
                        mouseY - CARD_HEIGHT / 2,
                        CARD_WIDTH,
                        CARD_HEIGHT
                );

        drawCard(
                graphics,
                draggedCard,
                rectangle
        );
    }

    private void drawCard(
            Graphics2D graphics,
            ClientCard card,
            Rectangle rectangle) {

        if (card.type() ==
                CardType.LIGHT_BROWN) {

            graphics.setColor(
                    LIGHT_BROWN
            );

        } else {

            graphics.setColor(
                    LIGHT_BLUE
            );
        }

        graphics.fill(rectangle);

        graphics.setColor(Color.BLACK);

        graphics.draw(rectangle);

        graphics.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        11
                )
        );

        String text;

        if (card.type() ==
                CardType.LIGHT_BROWN) {

            text = "BROWN";

        } else {

            text = "BLUE";
        }

        drawCenteredText(
                graphics,
                text,
                rectangle
        );
    }

    private Rectangle getOwnZoneRectangle(
            int zone,
            int y) {

        /*
         * Zone 0 is closest to YOUR graveyard.
         *
         * Your graveyard is on the right, therefore:
         *
         * zone 0 -> rightmost
         * zone 4 -> leftmost
         */
        int x =
                FIELD_LEFT
                        + (4 - zone)
                        * (ZONE_WIDTH + ZONE_GAP);

        return new Rectangle(
                x,
                y,
                ZONE_WIDTH,
                ZONE_HEIGHT
        );
    }

    private Rectangle getOpponentZoneRectangle(
            int zone,
            int y) {

        /*
         * Zone 0 is closest to the OPPONENT'S graveyard.
         *
         * Their graveyard is on the left from our perspective,
         * therefore:
         *
         * zone 0 -> leftmost
         * zone 4 -> rightmost
         */
        int x =
                FIELD_LEFT
                        + zone
                        * (ZONE_WIDTH + ZONE_GAP);

        return new Rectangle(
                x,
                y,
                ZONE_WIDTH,
                ZONE_HEIGHT
        );
    }

    /*
     * Player's utility zones are on the RIGHT.
     */
    private Rectangle getPlayerUtilityRectangle(
            int y) {

        int x =
                FIELD_LEFT
                        + 5
                        * (ZONE_WIDTH + ZONE_GAP)
                        + 8;

        return new Rectangle(
                x,
                y,
                ZONE_WIDTH,
                ZONE_HEIGHT
        );
    }

    /*
     * Opponent's utility zones are on the LEFT.
     */
    private Rectangle getOpponentUtilityRectangle(
            int y) {

        int x =
                FIELD_LEFT
                        - ZONE_WIDTH
                        - 8;

        return new Rectangle(
                x,
                y,
                ZONE_WIDTH,
                ZONE_HEIGHT
        );
    }

    private Rectangle getHandCardRectangle(
            int index,
            int cardCount) {

        int totalWidth =
                cardCount * CARD_WIDTH
                        + Math.max(
                        0,
                        cardCount - 1
                ) * 8;

        int startX =
                (BOARD_WIDTH - totalWidth)
                        / 2;

        return new Rectangle(
                startX
                        + index
                        * (CARD_WIDTH + 8),
                PLAYER_HAND_Y,
                CARD_WIDTH,
                CARD_HEIGHT
        );
    }

    private Rectangle getPlayedCardRectangle(
            ClientCard card,
            boolean opponent) {

        int y;

        if (opponent) {

            /*
             * Opponent:
             *
             * Brown = closer to center
             * Blue = farther away
             */
            if (card.type() ==
                    CardType.LIGHT_BROWN) {

                y = OPPONENT_BROWN_Y;

            } else {

                y = OPPONENT_BLUE_Y;
            }

        } else {

            /*
             * Player:
             *
             * Brown = closer to center
             * Blue = farther away
             */
            if (card.type() ==
                    CardType.LIGHT_BROWN) {

                y = PLAYER_BROWN_Y;

            } else {

                y = PLAYER_BLUE_Y;
            }
        }

        Rectangle zone;

        if (opponent) {

            zone =
                    getOpponentZoneRectangle(
                            card.zoneIndex(),
                            y
                    );

        } else {

            zone =
                    getOwnZoneRectangle(
                            card.zoneIndex(),
                            y
                    );
        }

        int x =
                zone.x
                        + (
                        ZONE_WIDTH
                                - CARD_WIDTH
                ) / 2;

        int cardY =
                zone.y
                        + (
                        ZONE_HEIGHT
                                - CARD_HEIGHT
                ) / 2;

        return new Rectangle(
                x,
                cardY,
                CARD_WIDTH,
                CARD_HEIGHT
        );
    }

    private void drawCenteredText(
            Graphics2D graphics,
            String text,
            Rectangle rectangle) {

        FontMetrics metrics =
                graphics.getFontMetrics();

        int x =
                rectangle.x
                        + (
                        rectangle.width
                                - metrics.stringWidth(text)
                ) / 2;

        int y =
                rectangle.y
                        + (
                        rectangle.height
                                - metrics.getHeight()
                ) / 2
                        + metrics.getAscent();

        graphics.drawString(
                text,
                x,
                y
        );
    }

    public enum DropZoneType {
        BROWN,
        BLUE
    }

    public record DropTarget(
            DropZoneType type,
            int zoneIndex) {
    }

    public record CardMove(
            String cardId,
            DropZoneType targetType,
            int targetZone) {
    }
}