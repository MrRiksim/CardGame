package game.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class BoardPanel extends JPanel {

    private static final int BOARD_WIDTH = 600;
    private static final int BOARD_HEIGHT = 400;
    private static final int CARD_SIZE = 60;

    private int myX = 100;
    private int myY = 170;

    private int enemyX = 440;
    private int enemyY = 170;

    private boolean yourTurn = false;
    private boolean gameOver = false;

    private boolean dragging = false;

    private int dragOffsetX;
    private int dragOffsetY;

    private final Consumer<Point> moveListener;

    public BoardPanel(
            Consumer<Point> moveListener) {

        this.moveListener = moveListener;

        setPreferredSize(
                new Dimension(
                        BOARD_WIDTH,
                        BOARD_HEIGHT
                )
        );

        setBackground(
                new Color(220, 220, 220)
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

                        if (!yourTurn || gameOver) {
                            return;
                        }

                        Rectangle ownCard =
                                new Rectangle(
                                        myX,
                                        myY,
                                        CARD_SIZE,
                                        CARD_SIZE
                                );

                        if (!ownCard.contains(e.getPoint())) {
                            return;
                        }

                        dragging = true;

                        dragOffsetX =
                                e.getX() - myX;

                        dragOffsetY =
                                e.getY() - myY;
                    }

                    @Override
                    public void mouseDragged(
                            MouseEvent e) {

                        if (!dragging ||
                                !yourTurn ||
                                gameOver) {

                            return;
                        }

                        int newX =
                                e.getX() - dragOffsetX;

                        int newY =
                                e.getY() - dragOffsetY;

                        newX = Math.max(
                                0,
                                Math.min(
                                        BOARD_WIDTH - CARD_SIZE,
                                        newX
                                )
                        );

                        newY = Math.max(
                                0,
                                Math.min(
                                        BOARD_HEIGHT - CARD_SIZE,
                                        newY
                                )
                        );

                        myX = newX;
                        myY = newY;

                        repaint();

                        /*
                         * Tell the server where we want
                         * our card to move.
                         */
                        moveListener.accept(
                                new Point(myX, myY)
                        );
                    }

                    @Override
                    public void mouseReleased(
                            MouseEvent e) {

                        dragging = false;
                    }
                };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    public void updateState(
            int myX,
            int myY,
            int enemyX,
            int enemyY,
            boolean yourTurn) {

        this.myX = myX;
        this.myY = myY;

        this.enemyX = enemyX;
        this.enemyY = enemyY;

        this.yourTurn = yourTurn;

        repaint();
    }

    public void setGameOver() {

        gameOver = true;
        yourTurn = false;

        repaint();
    }

    public void resetGame() {

        gameOver = false;
        dragging = false;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D graphics =
                (Graphics2D) g.create();

        /*
         * Enemy card: RED
         */
        graphics.setColor(
                Color.RED
        );

        graphics.fillRect(
                enemyX,
                enemyY,
                CARD_SIZE,
                CARD_SIZE
        );

        /*
         * Own card: GREEN
         */
        graphics.setColor(
                Color.GREEN
        );

        graphics.fillRect(
                myX,
                myY,
                CARD_SIZE,
                CARD_SIZE
        );

        /*
         * Labels on the cards.
         */
        graphics.setColor(
                Color.BLACK
        );

        graphics.drawString(
                "YOU",
                myX + 17,
                myY + 34
        );

        graphics.drawString(
                "ENEMY",
                enemyX + 10,
                enemyY + 34
        );

        graphics.dispose();
    }
}