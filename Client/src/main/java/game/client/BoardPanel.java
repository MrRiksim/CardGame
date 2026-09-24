package game.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BoardPanel extends JPanel {

    private static final int BOARD_WIDTH = 760;
    private static final int BOARD_HEIGHT = 900;

    /*
     * The five playable columns, shared by both players' front (unit) and
     * back (trap) rows.
     */
    private static final int ZONE_WIDTH = 82;
    private static final int ZONE_HEIGHT = 112;
    private static final int ZONE_GAP = 10;
    private static final int FIELD_LEFT = 140;

    /*
     * Opponent: back row (hidden traps, farthest from the divider) then
     * front row (units, closest to the divider). Player is the mirror
     * image, closest to their own hand at the bottom.
     */
    private static final int OPPONENT_BACK_Y = 150;
    private static final int OPPONENT_FRONT_Y = 275;
    private static final int PLAYER_FRONT_Y = 490;
    private static final int PLAYER_BACK_Y = 615;

    private static final int OPPONENT_HAND_Y = 25;
    private static final int PLAYER_HAND_Y = 780;

    /*
     * Cards drawn small enough to fit inside a field zone.
     */
    private static final int FIELD_CARD_WIDTH = 70;
    private static final int FIELD_CARD_HEIGHT = 100;

    /*
     * Cards drawn large enough to read their description.
     */
    private static final int HAND_CARD_WIDTH = 96;
    private static final int HAND_CARD_HEIGHT = 140;
    private static final int HAND_GAP = 8;

    private static final int OPPONENT_HAND_CARD_WIDTH = 60;
    private static final int OPPONENT_HAND_CARD_HEIGHT = 86;
    private static final int OPPONENT_HAND_GAP = 6;

    private static final Color FRONT_ZONE_COLOR = new Color(210, 235, 210);
    private static final Color BACK_ZONE_COLOR = new Color(224, 214, 234);
    private static final Color CARD_BACK_COLOR = new Color(95, 55, 30);
    private static final Color GRAVEYARD_GRAY = new Color(155, 155, 155);

    private List<ClientCard> ownHand = new ArrayList<>();
    private List<ClientUnit> ownFront = new ArrayList<>();
    private List<ClientUnit> opponentFront = new ArrayList<>();
    private List<ClientTrap> ownBack = new ArrayList<>();
    private List<Integer> opponentBackZones = new ArrayList<>();

    private int opponentHandCount = 0;
    private boolean yourTurn = false;
    private boolean gameOver = false;

    /*
     * Only ever a card currently in hand - played cards can't be re-dragged.
     */
    private ClientCard draggedCard;
    private int mouseX;
    private int mouseY;

    private final Consumer<PlayAction> playListener;

    public BoardPanel(Consumer<PlayAction> playListener) {
        this.playListener = playListener;

        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(new Color(235, 235, 235));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                startDragging(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedCard == null) {
                    return;
                }
                mouseX = e.getX();
                mouseY = e.getY();
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                finishDragging(e.getX(), e.getY());
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    public void updateState(
            List<ClientCard> ownHand,
            int opponentHandCount,
            List<ClientUnit> ownFront,
            List<ClientUnit> opponentFront,
            List<ClientTrap> ownBack,
            List<Integer> opponentBackZones,
            boolean yourTurn) {

        this.ownHand = new ArrayList<>(ownHand);
        this.opponentHandCount = opponentHandCount;
        this.ownFront = new ArrayList<>(ownFront);
        this.opponentFront = new ArrayList<>(opponentFront);
        this.ownBack = new ArrayList<>(ownBack);
        this.opponentBackZones = new ArrayList<>(opponentBackZones);
        this.yourTurn = yourTurn;

        /*
         * The server state is authoritative - any in-flight drag is stale.
         */
        draggedCard = null;
        repaint();
    }

    public void resetGame() {
        ownHand = new ArrayList<>();
        ownFront = new ArrayList<>();
        opponentFront = new ArrayList<>();
        ownBack = new ArrayList<>();
        opponentBackZones = new ArrayList<>();
        opponentHandCount = 0;
        yourTurn = false;
        gameOver = false;
        draggedCard = null;
        repaint();
    }

    public void setGameOver() {
        gameOver = true;
        yourTurn = false;
        draggedCard = null;
        repaint();
    }

    /*
     * ==========================================================
     * Dragging
     * ==========================================================
     */

    private void startDragging(int x, int y) {
        if (!yourTurn || gameOver) {
            return;
        }

        for (int i = 0; i < ownHand.size(); i++) {
            Rectangle rectangle = getHandCardRectangle(
                    i, ownHand.size(), HAND_CARD_WIDTH, HAND_CARD_HEIGHT, HAND_GAP, PLAYER_HAND_Y);

            if (rectangle.contains(x, y)) {
                draggedCard = ownHand.get(i);
                mouseX = x;
                mouseY = y;
                repaint();
                return;
            }
        }
    }

    private void finishDragging(int x, int y) {
        if (draggedCard == null) {
            return;
        }

        PlayAction action = resolveAction(x, y);
        if (action != null) {
            playListener.accept(action);
        }

        draggedCard = null;
        repaint();
    }

    /*
     * A drop is resolved in this order: a unit under the cursor means a
     * spell cast; an empty own front zone means a unit placement; an empty
     * own back zone means a trap placement; anything else only works for an
     * untargeted (SPECIAL) spell. Each branch also has to match what kind
     * of card is actually being dragged, or the drop is invalid.
     */
    private PlayAction resolveAction(int x, int y) {
        ClientUnit targetUnit = findUnitAt(x, y);
        if (targetUnit != null) {
            if (draggedCard instanceof ClientSpell spell) {
                return new PlayAction.SpellCast(spell.id(), targetUnit.id());
            }
            return null;
        }

        Integer emptyFrontZone = findEmptyOwnFrontZone(x, y);
        if (emptyFrontZone != null) {
            if (draggedCard instanceof ClientUnit unit) {
                return new PlayAction.UnitPlacement(unit.id(), emptyFrontZone);
            }
            return null;
        }

        Integer emptyBackZone = findEmptyOwnBackZone(x, y);
        if (emptyBackZone != null) {
            if (draggedCard instanceof ClientTrap trap) {
                return new PlayAction.TrapPlacement(trap.id(), emptyBackZone);
            }
            return null;
        }

        if (draggedCard instanceof ClientSpell spell
                && spell.spellType() == SpellType.SPECIAL
                && new Rectangle(0, 0, BOARD_WIDTH, BOARD_HEIGHT).contains(x, y)) {
            return new PlayAction.SpellCast(spell.id(), null);
        }

        return null;
    }

    private ClientUnit findUnitAt(int x, int y) {
        for (ClientUnit unit : ownFront) {
            Rectangle zone = getOwnZoneRectangle(unit.zoneIndex(), PLAYER_FRONT_Y);
            if (getFieldCardRectangle(zone).contains(x, y)) {
                return unit;
            }
        }
        for (ClientUnit unit : opponentFront) {
            Rectangle zone = getOpponentZoneRectangle(unit.zoneIndex(), OPPONENT_FRONT_Y);
            if (getFieldCardRectangle(zone).contains(x, y)) {
                return unit;
            }
        }
        return null;
    }

    private Integer findEmptyOwnFrontZone(int x, int y) {
        for (int zone = 0; zone < 5; zone++) {
            if (isZoneOccupied(ownFront, zone)) {
                continue;
            }
            if (getOwnZoneRectangle(zone, PLAYER_FRONT_Y).contains(x, y)) {
                return zone;
            }
        }
        return null;
    }

    private Integer findEmptyOwnBackZone(int x, int y) {
        for (int zone = 0; zone < 5; zone++) {
            if (isBackZoneOccupied(zone)) {
                continue;
            }
            if (getOwnZoneRectangle(zone, PLAYER_BACK_Y).contains(x, y)) {
                return zone;
            }
        }
        return null;
    }

    private boolean isZoneOccupied(List<ClientUnit> units, int zone) {
        for (ClientUnit unit : units) {
            if (unit.zoneIndex() == zone) {
                return true;
            }
        }
        return false;
    }

    private boolean isBackZoneOccupied(int zone) {
        for (ClientTrap trap : ownBack) {
            if (trap.zoneIndex() == zone) {
                return true;
            }
        }
        return false;
    }

    /*
     * ==========================================================
     * Painting
     * ==========================================================
     */

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D graphics = (Graphics2D) g.create();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBoard(graphics);
        drawOpponentHand(graphics);
        drawOwnHand(graphics);
        drawFrontZones(graphics);
        drawBackZones(graphics);

        if (draggedCard != null) {
            Rectangle rectangle = new Rectangle(
                    mouseX - HAND_CARD_WIDTH / 2, mouseY - HAND_CARD_HEIGHT / 2,
                    HAND_CARD_WIDTH, HAND_CARD_HEIGHT);
            CardRenderer.draw(graphics, rectangle, draggedCard, true);
        }

        graphics.dispose();
    }

    private void drawBoard(Graphics2D graphics) {
        for (int zone = 0; zone < 5; zone++) {
            drawZone(graphics, getOpponentZoneRectangle(zone, OPPONENT_BACK_Y), BACK_ZONE_COLOR);
            drawZone(graphics, getOpponentZoneRectangle(zone, OPPONENT_FRONT_Y), FRONT_ZONE_COLOR);
            drawZone(graphics, getOwnZoneRectangle(zone, PLAYER_FRONT_Y), FRONT_ZONE_COLOR);
            drawZone(graphics, getOwnZoneRectangle(zone, PLAYER_BACK_Y), BACK_ZONE_COLOR);
        }

        drawUtilityZone(graphics, getOpponentUtilityRectangle(OPPONENT_FRONT_Y), GRAVEYARD_GRAY, "GRAVEYARD");
        drawUtilityZone(graphics, getOpponentUtilityRectangle(OPPONENT_BACK_Y), CARD_BACK_COLOR, "DECK");
        drawUtilityZone(graphics, getPlayerUtilityRectangle(PLAYER_FRONT_Y), GRAVEYARD_GRAY, "GRAVEYARD");
        drawUtilityZone(graphics, getPlayerUtilityRectangle(PLAYER_BACK_Y), CARD_BACK_COLOR, "DECK");

        graphics.setColor(Color.BLACK);
        graphics.setStroke(new BasicStroke(2));
        graphics.drawLine(40, 400, BOARD_WIDTH - 40, 400);

        graphics.setFont(new Font("Arial", Font.BOLD, 14));
        graphics.drawString("OPPONENT", 10, 120);
        graphics.drawString("YOU", 10, 770);
    }

    private void drawZone(Graphics2D graphics, Rectangle rectangle, Color color) {
        graphics.setColor(color);
        graphics.fill(rectangle);
        graphics.setColor(Color.GRAY);
        graphics.draw(rectangle);
    }

    private void drawUtilityZone(Graphics2D graphics, Rectangle rectangle, Color background, String text) {
        graphics.setColor(background);
        graphics.fill(rectangle);
        graphics.setColor(Color.BLACK);
        graphics.draw(rectangle);
        graphics.setColor(background == GRAVEYARD_GRAY ? Color.BLACK : Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 11));
        drawCenteredText(graphics, text, rectangle);
    }

    private void drawOpponentHand(Graphics2D graphics) {
        for (int i = 0; i < opponentHandCount; i++) {
            Rectangle rectangle = getHandCardRectangle(
                    i, opponentHandCount, OPPONENT_HAND_CARD_WIDTH, OPPONENT_HAND_CARD_HEIGHT,
                    OPPONENT_HAND_GAP, OPPONENT_HAND_Y);
            drawCardBack(graphics, rectangle);
        }
    }

    private void drawOwnHand(Graphics2D graphics) {
        for (int i = 0; i < ownHand.size(); i++) {
            ClientCard card = ownHand.get(i);
            if (card == draggedCard) {
                continue;
            }
            Rectangle rectangle = getHandCardRectangle(
                    i, ownHand.size(), HAND_CARD_WIDTH, HAND_CARD_HEIGHT, HAND_GAP, PLAYER_HAND_Y);
            CardRenderer.draw(graphics, rectangle, card, true);
        }
    }

    private void drawFrontZones(Graphics2D graphics) {
        for (ClientUnit unit : ownFront) {
            Rectangle zone = getOwnZoneRectangle(unit.zoneIndex(), PLAYER_FRONT_Y);
            CardRenderer.draw(graphics, getFieldCardRectangle(zone), unit, false);
        }
        for (ClientUnit unit : opponentFront) {
            Rectangle zone = getOpponentZoneRectangle(unit.zoneIndex(), OPPONENT_FRONT_Y);
            CardRenderer.draw(graphics, getFieldCardRectangle(zone), unit, false);
        }
    }

    private void drawBackZones(Graphics2D graphics) {
        for (ClientTrap trap : ownBack) {
            Rectangle zone = getOwnZoneRectangle(trap.zoneIndex(), PLAYER_BACK_Y);
            CardRenderer.draw(graphics, getFieldCardRectangle(zone), trap, false);
        }
        for (int zoneIndex : opponentBackZones) {
            Rectangle zone = getOpponentZoneRectangle(zoneIndex, OPPONENT_BACK_Y);
            drawCardBack(graphics, getFieldCardRectangle(zone));
        }
    }

    private void drawCardBack(Graphics2D graphics, Rectangle rectangle) {
        graphics.setColor(CARD_BACK_COLOR);
        graphics.fill(rectangle);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 10));
        drawCenteredText(graphics, "CARD", rectangle);
    }

    private void drawCenteredText(Graphics2D graphics, String text, Rectangle rectangle) {
        FontMetrics metrics = graphics.getFontMetrics();
        int x = rectangle.x + (rectangle.width - metrics.stringWidth(text)) / 2;
        int y = rectangle.y + (rectangle.height - metrics.getHeight()) / 2 + metrics.getAscent();
        graphics.drawString(text, x, y);
    }

    /*
     * ==========================================================
     * Geometry
     * ==========================================================
     */

    private Rectangle getOwnZoneRectangle(int zone, int y) {
        /*
         * Zone 0 sits closest to the player's own graveyard, on the right,
         * so zone 0 is rightmost and zone 4 is leftmost.
         */
        int x = FIELD_LEFT + (4 - zone) * (ZONE_WIDTH + ZONE_GAP);
        return new Rectangle(x, y, ZONE_WIDTH, ZONE_HEIGHT);
    }

    private Rectangle getOpponentZoneRectangle(int zone, int y) {
        /*
         * The opponent's graveyard is on the left from our perspective, so
         * their zone 0 is leftmost and zone 4 is rightmost.
         */
        int x = FIELD_LEFT + zone * (ZONE_WIDTH + ZONE_GAP);
        return new Rectangle(x, y, ZONE_WIDTH, ZONE_HEIGHT);
    }

    private Rectangle getPlayerUtilityRectangle(int y) {
        int x = FIELD_LEFT + 5 * (ZONE_WIDTH + ZONE_GAP) + 8;
        return new Rectangle(x, y, ZONE_WIDTH, ZONE_HEIGHT);
    }

    private Rectangle getOpponentUtilityRectangle(int y) {
        int x = FIELD_LEFT - ZONE_WIDTH - 8;
        return new Rectangle(x, y, ZONE_WIDTH, ZONE_HEIGHT);
    }

    private Rectangle getFieldCardRectangle(Rectangle zone) {
        int x = zone.x + (zone.width - FIELD_CARD_WIDTH) / 2;
        int y = zone.y + (zone.height - FIELD_CARD_HEIGHT) / 2;
        return new Rectangle(x, y, FIELD_CARD_WIDTH, FIELD_CARD_HEIGHT);
    }

    private Rectangle getHandCardRectangle(
            int index, int count, int cardWidth, int cardHeight, int gap, int y) {

        int totalWidth = count * cardWidth + Math.max(0, count - 1) * gap;
        int startX = (BOARD_WIDTH - totalWidth) / 2;
        return new Rectangle(startX + index * (cardWidth + gap), y, cardWidth, cardHeight);
    }
}
