package game.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import game.engine.Board;
import game.engine.Constants;
import game.engine.Game;
import game.engine.Role;
import game.engine.cards.Card;
import game.engine.cells.CardCell;
import game.engine.cells.Cell;
import game.engine.cells.ContaminationSock;
import game.engine.cells.ConveyorBelt;
import game.engine.cells.DoorCell;
import game.engine.cells.MonsterCell;
import game.engine.monsters.Dasher;
import game.engine.monsters.Dynamo;
import game.engine.monsters.Monster;
import game.engine.monsters.MultiTasker;
import game.engine.monsters.Schemer;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GUITemplate extends Application implements EventHandler<ActionEvent> {

    // ── constants ──────────────────────────────────────────────────────────────
    private static final Color C_BG_DEEP   = Color.web("#080818");
    private static final Color C_BG_MID    = Color.web("#0e0e2a");
    private static final Color C_CYAN      = Color.web("#00f5ff");
    private static final Color C_SCARLET   = Color.web("#ff2d55");
    private static final Color C_GOLD      = Color.web("#ffd60a");
    private static final Color C_PURPLE    = Color.web("#bf5af2");
    private static final Color C_GREEN     = Color.web("#30d158");
    private static final Color C_ORANGE    = Color.web("#ff6b00");
    private static final Color C_TEXT      = Color.web("#dde8ff");

    private static final String S_DARK_PANEL =
        "-fx-background-color: rgba(14,14,42,0.95); " +
        "-fx-border-color: rgba(0,245,255,0.25); " +
        "-fx-border-width: 1; " +
        "-fx-border-radius: 10; " +
        "-fx-background-radius: 10;";

    private static final String S_LABEL =
        "-fx-text-fill: #dde8ff; -fx-font-family: 'Consolas'; -fx-font-size: 11.5;";

    // ── state ──────────────────────────────────────────────────────────────────
    private Stage primaryStage;
    private Game  game;

    private Button scarerButton, laugherButton;
    private Button playTurnButton, usePowerupButton;
    private Button backToStartButton, playAgainButton;
    private Button cheatWinButton, cheatEnergyButton;

    private Label currentTurnLabel;
    private Label playerInfoLabel;
    private Label opponentInfoLabel;
    private Label diceLabel;
    private Label cardLabel;
    private Label pileLabel;
    private Label legendLabel;

    private TextArea messageArea;

    private GridPane boardGrid;
    private Button[][] boardButtons;

    private int     lastDiceRoll;
    private Card    lastDrawnCard;
    private String  lastAction;
    private boolean powerupUsedThisTurn;

    // ── animated dice label ────────────────────────────────────────────────────
    private Label diceRollBigLabel;

    // ── player location highlight animation ───────────────────────────────────
    private ScaleTransition playerCellPulse;
    private Button lastPulsingPlayerCell;

    // ── particle canvas (game screen) ─────────────────────────────────────────
    private Canvas particleCanvas;
    private Timeline particleLoop;

    // tiny inner record for particles
    private double[] px = new double[60];
    private double[] py = new double[60];
    private double[] pvx = new double[60];
    private double[] pvy = new double[60];
    private double[] palpha = new double[60];
    private Color[]  pcolor = new Color[60];

    // ══════════════════════════════════════════════════════════════════════════
    //  Application.start
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("DoorDasH // NEON ARENA EDITION // Visual Upgrade Active");
        showStartScreen();
        this.primaryStage.show();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS — animations
    // ══════════════════════════════════════════════════════════════════════════

    /** Scale-up/down on hover. */
    private void addHoverAnimation(Button button) {
        ScaleTransition si = new ScaleTransition(Duration.millis(140), button);
        si.setToX(1.10); si.setToY(1.10);
        ScaleTransition so = new ScaleTransition(Duration.millis(140), button);
        so.setToX(1.0);  so.setToY(1.0);
        button.setOnMouseEntered(e -> { so.stop(); si.playFromStart(); });
        button.setOnMouseExited (e -> { si.stop(); so.playFromStart(); });
    }

    /** Pulsing glow effect on a label. */
    private void addGlowAnimation(Label label) {
        Glow glow = new Glow(0.0);
        label.setEffect(glow);
        Timeline t = new Timeline(
            new KeyFrame(Duration.ZERO,              new KeyValue(glow.levelProperty(), 0.0)),
            new KeyFrame(Duration.millis(900),       new KeyValue(glow.levelProperty(), 0.9)),
            new KeyFrame(Duration.millis(1800),      new KeyValue(glow.levelProperty(), 0.0))
        );
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
    }

    /** Cyclic gradient-shift timeline on a label's style (title rainbow). */
    private void addRainbowTitle(Label label) {
        String[] colors = { "#00f5ff", "#bf5af2", "#ffd60a", "#ff2d55", "#30d158", "#00f5ff" };
        KeyFrame[] frames = new KeyFrame[colors.length];
        for (int i = 0; i < colors.length; i++) {
            final String c = colors[i];
            frames[i] = new KeyFrame(Duration.millis(i * 600),
                e -> label.setStyle(label.getStyle().replaceAll("-fx-text-fill:[^;]+;",
                    "-fx-text-fill: " + c + ";")));
        }
        Timeline t = new Timeline(frames);
        t.setCycleCount(Timeline.INDEFINITE);
        t.play();
    }

    /** Bounce-in a node. */
    private void bounceIn(javafx.scene.Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(400), node);
        st.setFromX(0.0); st.setFromY(0.0);
        st.setToX(1.0);   st.setToY(1.0);
        st.setInterpolator(javafx.animation.Interpolator.SPLINE(0.34, 1.56, 0.64, 1.0));
        st.play();
    }

    /** Flash a button with a color overlay then restore. */
    private void flashButton(Button btn, String flashColor) {
        String original = btn.getStyle();
        btn.setStyle(original + " -fx-background-color: " + flashColor + ";");
        PauseTransition pt = new PauseTransition(Duration.millis(250));
        pt.setOnFinished(e -> btn.setStyle(original));
        pt.play();
    }

    /** Shake a node (invalid-move feedback). */
    private void shake(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setFromX(-8); tt.setToX(8);
        tt.setCycleCount(5);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS — background builders
    // ══════════════════════════════════════════════════════════════════════════

    /** Deep-space animated background with floating orbs and a starfield canvas. */
    private AnchorPane buildAnimatedBackground(int w, int h) {
        AnchorPane pane = new AnchorPane();

        LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, C_BG_DEEP),
            new Stop(1.0, Color.web("#0a0a30")));
        pane.setBackground(new Background(new BackgroundFill(grad, CornerRadii.EMPTY, Insets.EMPTY)));

        // ── starfield canvas ──
        Canvas stars = new Canvas(w, h);
        GraphicsContext gc = stars.getGraphicsContext2D();
        java.util.Random rng = new java.util.Random(7);
        double[] sx = new double[120], sy = new double[120], sr = new double[120], salpha = new double[120];
        for (int i = 0; i < sx.length; i++) {
            sx[i] = rng.nextDouble() * w;
            sy[i] = rng.nextDouble() * h;
            sr[i] = rng.nextDouble() * 1.5 + 0.3;
            salpha[i] = rng.nextDouble() * 0.7 + 0.1;
        }
        Timeline starTwinkle = new Timeline(new KeyFrame(Duration.millis(80), e -> {
            gc.clearRect(0, 0, w, h);
            for (int i = 0; i < sx.length; i++) {
                double a = salpha[i] * (0.6 + 0.4 * Math.sin(System.currentTimeMillis() * 0.003 + i));
                gc.setFill(Color.color(0.7, 0.85, 1.0, a));
                gc.fillOval(sx[i] - sr[i], sy[i] - sr[i], sr[i] * 2, sr[i] * 2);
            }
        }));
        starTwinkle.setCycleCount(Timeline.INDEFINITE);
        starTwinkle.play();
        pane.getChildren().add(stars);

        // ── floating orbs ──
        Color[] palette = {
            Color.web("#7b2ff7", 0.30), Color.web("#00d4ff", 0.25),
            Color.web("#ff6ec7", 0.22), Color.web("#39ff14", 0.20),
            Color.web("#ffd60a", 0.18)
        };
        for (int i = 0; i < 20; i++) {
            double radius = 16 + rng.nextInt(48);
            double startX = rng.nextInt(w);
            double startY = h + radius;
            double endY   = -radius * 2;
            Circle c = new Circle(radius, palette[i % palette.length]);
            c.setLayoutX(startX); c.setLayoutY(startY);

            // add blur so they look like glowing orbs
            BoxBlur blur = new BoxBlur(radius * 0.5, radius * 0.5, 2);
            c.setEffect(blur);

            pane.getChildren().add(c);

            double dur = 5500 + rng.nextInt(9000);
            Timeline rise = new Timeline(
                new KeyFrame(Duration.ZERO,          new KeyValue(c.layoutYProperty(), startY)),
                new KeyFrame(Duration.millis(dur),   new KeyValue(c.layoutYProperty(), endY)));
            rise.setCycleCount(Timeline.INDEFINITE);
            rise.setDelay(Duration.millis(rng.nextInt(5000)));
            rise.play();

            double wobble = 18 + rng.nextInt(35);
            Timeline wob = new Timeline(
                new KeyFrame(Duration.ZERO,           new KeyValue(c.layoutXProperty(), startX - wobble)),
                new KeyFrame(Duration.millis(2400),   new KeyValue(c.layoutXProperty(), startX + wobble)),
                new KeyFrame(Duration.millis(4800),   new KeyValue(c.layoutXProperty(), startX - wobble)));
            wob.setCycleCount(Timeline.INDEFINITE);
            wob.play();
        }

        return pane;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  START SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    private void showStartScreen() {
        AnchorPane pane = buildAnimatedBackground(960, 640);
        Scene scene = new Scene(pane, 960, 640);

        // ── glowing title ──
        Label title = new Label("⚡ DOORDASH // NEON ARENA ⚡");
        title.setLayoutX(300); title.setLayoutY(30);
        title.setStyle(
            "-fx-font-size: 46; -fx-font-weight: bold; -fx-text-fill: #00f5ff; " +
            "-fx-font-family: 'Consolas';"
        );
        DropShadow ds = new DropShadow(24, C_CYAN);
        ds.setSpread(0.4);
        title.setEffect(ds);
        addGlowAnimation(title);
        addRainbowTitle(title);
        pane.getChildren().add(title);

        Label sub = new Label("SCARE  vs  LAUGH  TOUCHDOWN");
        sub.setLayoutX(318); sub.setLayoutY(90);
        sub.setStyle("-fx-font-size: 14; -fx-text-fill: #a78bfa; -fx-letter-spacing: 4;");
        pane.getChildren().add(sub);

        // scanning line under title
        Rectangle scanLine = new Rectangle(960, 2);
        scanLine.setFill(Color.web("#00f5ff", 0.4));
        scanLine.setLayoutX(0); scanLine.setLayoutY(115);
        Timeline scan = new Timeline(
            new KeyFrame(Duration.ZERO,      new KeyValue(scanLine.opacityProperty(), 0.1)),
            new KeyFrame(Duration.millis(900), new KeyValue(scanLine.opacityProperty(), 0.8)),
            new KeyFrame(Duration.millis(1800), new KeyValue(scanLine.opacityProperty(), 0.1)));
        scan.setCycleCount(Timeline.INDEFINITE); scan.play();
        pane.getChildren().add(scanLine);

        // ── instructions box ──
        TextArea instructions = new TextArea();
        instructions.setEditable(false); instructions.setWrapText(true);
        instructions.setLayoutX(90); instructions.setLayoutY(128);
        instructions.setPrefSize(780, 280);
        instructions.setStyle(
            "-fx-control-inner-background: #08081c; -fx-text-fill: #c4b5fd; " +
            "-fx-font-size: 12.5; -fx-font-family: 'Consolas'; " +
            "-fx-border-color: #7b2ff7; -fx-border-radius: 10; " +
            "-fx-background-radius: 10; -fx-opacity: 0.93;");
        instructions.setText(
            "GAME INSTRUCTIONS\n\n" +
            "  1.  Choose your side: SCARER or LAUGHER.\n" +
            "  2.  Both monsters start at cell 0 with their starting energy.\n" +
            "  3.  Each turn you may activate your powerup FIRST if you have ≥ 500 energy.\n" +
            "  4.  Press Roll Dice / Play Turn to roll 1-6 and move.\n" +
            "  5.  Door cells give or remove energy depending on role match. Used doors are exhausted.\n" +
            "  6.  Card cells draw from the shuffled 25-card pile and apply effects.\n" +
            "  7.  Conveyor belts move you forward; contamination socks move you backward.\n" +
            "  8.  Monster cells can trigger free powerups or energy swaps based on role.\n" +
            "  9.  A move is invalid if the destination is occupied by the opponent — retry.\n" +
            " 10.  WIN: land exactly on cell 99 with at least 1000 energy.\n\n" +
            "CHEAT KEYS (for evaluation):\n" +
            "  W  →  teleport player to cell 99\n" +
            "  E  →  add 1000 energy to player"
        );
        pane.getChildren().add(instructions);

        // ── SCARER button ──
        scarerButton = makeNeonButton("👻  Play as SCARER", "#3b0a9e", "#9b59ff", "#e0d7ff");
        scarerButton.setLayoutX(220); scarerButton.setLayoutY(445);
        scarerButton.setPrefSize(210, 54);
        scarerButton.setOnAction(this);
        addHoverAnimation(scarerButton);
        pane.getChildren().add(scarerButton);

        // ── LAUGHER button ──
        laugherButton = makeNeonButton("😂  Play as LAUGHER", "#00695c", "#26ffbc", "#e0fff8");
        laugherButton.setLayoutX(530); laugherButton.setLayoutY(445);
        laugherButton.setPrefSize(210, 54);
        laugherButton.setOnAction(this);
        addHoverAnimation(laugherButton);
        pane.getChildren().add(laugherButton);

        Label choose = new Label("— choose your side to begin —");
        choose.setLayoutX(358); choose.setLayoutY(518);
        choose.setStyle("-fx-font-size: 12; -fx-text-fill: #a78bfa;");
        pane.getChildren().add(choose);

        // ── fade-in ──
        FadeTransition fi = new FadeTransition(Duration.millis(1000), pane);
        fi.setFromValue(0); fi.setToValue(1); fi.play();

        primaryStage.setScene(scene);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GAME SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    private void showGameScreen() {
        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root, 1320, 750);

        // ── deep-space background ──
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, C_BG_DEEP), new Stop(1, Color.web("#0a0a26")));
        root.setBackground(new Background(new BackgroundFill(bg, CornerRadii.EMPTY, Insets.EMPTY)));

        // ── starfield canvas (game screen) ──
        particleCanvas = new Canvas(1320, 750);
        root.getChildren().add(particleCanvas);
        initParticles();
        startParticleLoop();

        // ── header title ──
        Label headerTitle = new Label("⚡ NEON ARENA // LIVE MATCH ⚡");
        headerTitle.setLayoutX(480); headerTitle.setLayoutY(10);
        headerTitle.setStyle(
            "-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: #00f5ff; " +
            "-fx-font-family: 'Consolas';");
        DropShadow hds = new DropShadow(14, C_CYAN); hds.setSpread(0.3);
        headerTitle.setEffect(hds);
        addGlowAnimation(headerTitle);
        root.getChildren().add(headerTitle);

        // scanline under header
        Rectangle hScan = new Rectangle(1320, 2);
        hScan.setFill(Color.web("#00f5ff", 0.3));
        hScan.setLayoutX(0); hScan.setLayoutY(42);
        Timeline hst = new Timeline(
            new KeyFrame(Duration.ZERO,       new KeyValue(hScan.opacityProperty(), 0.1)),
            new KeyFrame(Duration.millis(950), new KeyValue(hScan.opacityProperty(), 0.7)),
            new KeyFrame(Duration.millis(1900), new KeyValue(hScan.opacityProperty(), 0.1)));
        hst.setCycleCount(Timeline.INDEFINITE); hst.play();
        root.getChildren().add(hScan);

        // ── LEFT PANEL ──────────────────────────────────────────────────────
        VBox leftPanel = new VBox(7);
        leftPanel.setLayoutX(12); leftPanel.setLayoutY(48);
        leftPanel.setPrefWidth(345);
        leftPanel.setPadding(new Insets(12));
        leftPanel.setStyle(S_DARK_PANEL);

        // current-turn label
        currentTurnLabel = new Label();
        currentTurnLabel.setWrapText(true);
        currentTurnLabel.setStyle(S_LABEL + " -fx-font-size: 12.5; -fx-font-weight: bold;");
        DropShadow ctGlow = new DropShadow(10, C_CYAN); ctGlow.setSpread(0.2);
        currentTurnLabel.setEffect(ctGlow);

        // separator
        Rectangle sep1 = makeSep();

        // player info
        playerInfoLabel = new Label();
        playerInfoLabel.setWrapText(true);
        playerInfoLabel.setStyle(S_LABEL + " -fx-text-fill: #80e8ff;");

        Rectangle sep2 = makeSep();

        // opponent info
        opponentInfoLabel = new Label();
        opponentInfoLabel.setWrapText(true);
        opponentInfoLabel.setStyle(S_LABEL + " -fx-text-fill: #ff9faa;");

        Rectangle sep3 = makeSep();

        // dice + card info
        diceLabel = new Label();
        diceLabel.setWrapText(true);
        diceLabel.setStyle(S_LABEL + " -fx-text-fill: #ffd60a;");

        // big animated dice display
        diceRollBigLabel = new Label("🎲");
        diceRollBigLabel.setStyle(
            "-fx-font-size: 42; -fx-text-fill: #ffd60a; " +
            "-fx-effect: dropshadow(gaussian, #ffd60a, 20, 0.5, 0, 0);");

        cardLabel = new Label();
        cardLabel.setWrapText(true);
        cardLabel.setStyle(S_LABEL + " -fx-text-fill: #bf5af2;");

        Rectangle sep4 = makeSep();

        pileLabel = new Label();
        pileLabel.setWrapText(true);
        pileLabel.setStyle(S_LABEL + " -fx-text-fill: #a0a0cc;");

        legendLabel = new Label(
            "LEGEND\n" +
            "D-S = Scarer Door  |  D-L = Laugher Door\n" +
            "R = Ready  |  U = Used  |  C = Card Cell\n" +
            "B = Conveyor Belt  |  Sock = Contamination\n" +
            "M = Monster Cell  |  CYAN YOU = Player  |  RED CPU = Opponent\n" +
            "W = teleport to 99  |  E = +1000 energy"
        );
        legendLabel.setWrapText(true);
        legendLabel.setStyle(S_LABEL + " -fx-text-fill: #606080;");

        Rectangle sep5 = makeSep();

        // ── action buttons ──
        playTurnButton = makeNeonButton("⚡ ROLL DICE", "#003050", "#00f5ff", "#d0f8ff");
        playTurnButton.setPrefSize(155, 36); playTurnButton.setOnAction(this);
        addHoverAnimation(playTurnButton);

        usePowerupButton = makeNeonButton("✨ POWERUP", "#2a005e", "#bf5af2", "#eed8ff");
        usePowerupButton.setPrefSize(155, 36); usePowerupButton.setOnAction(this);
        addHoverAnimation(usePowerupButton);

        HBox actionRow = new HBox(12, playTurnButton, usePowerupButton);

        cheatWinButton = makeNeonButton("W: VICTORY TEST", "#1a1a00", "#ffd60a", "#fffaaa");
        cheatWinButton.setPrefSize(155, 32);
        cheatWinButton.setOnAction(e -> cheatWin());
        addHoverAnimation(cheatWinButton);

        cheatEnergyButton = makeNeonButton("E: +Energy", "#001a00", "#30d158", "#b0ffc0");
        cheatEnergyButton.setPrefSize(155, 32);
        cheatEnergyButton.setOnAction(e -> cheatEnergy());
        addHoverAnimation(cheatEnergyButton);

        HBox cheatRow = new HBox(12, cheatWinButton, cheatEnergyButton);

        backToStartButton = makeNeonButton("↩  BACK TO START", "#200010", "#ff2d55", "#ffccd5");
        backToStartButton.setPrefSize(322, 34); backToStartButton.setOnAction(this);
        addHoverAnimation(backToStartButton);

        leftPanel.getChildren().addAll(
            currentTurnLabel, sep1,
            playerInfoLabel,  sep2,
            opponentInfoLabel, sep3,
            diceLabel, diceRollBigLabel, cardLabel, sep4,
            pileLabel, legendLabel, sep5,
            actionRow, cheatRow, backToStartButton
        );
        root.getChildren().add(leftPanel);

        // ── BOARD (center) ──────────────────────────────────────────────────
        createBoard(root);

        // ── MESSAGE AREA (bottom) ───────────────────────────────────────────
        messageArea = new TextArea();
        messageArea.setEditable(false); messageArea.setWrapText(true);
        messageArea.setLayoutX(365); messageArea.setLayoutY(636);
        messageArea.setPrefSize(950, 96);
        messageArea.setStyle(
            "-fx-control-inner-background: #06060f; " +
            "-fx-text-fill: #b8f0ff; " +
            "-fx-font-family: 'Consolas'; -fx-font-size: 11.5; " +
            "-fx-border-color: rgba(0,245,255,0.3); -fx-border-radius: 8; " +
            "-fx-background-radius: 8;");
        root.getChildren().add(messageArea);

        // ── key bindings ──
        scene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.W) {
                cheatWin();
                ev.consume();
            } else if (ev.getCode() == KeyCode.E) {
                cheatEnergy();
                ev.consume();
            }
        });

        // ── init state ──
        lastDiceRoll         = 0;
        lastDrawnCard        = null;
        lastAction           = "NEON ARENA EDITION ACTIVE ⚡  Track YOU in cyan and CPU in red. Press W to preview the cinematic victory screen.";
        powerupUsedThisTurn  = false;

        refreshGUI();

        // ── fade in ──
        FadeTransition fi = new FadeTransition(Duration.millis(700), root);
        fi.setFromValue(0); fi.setToValue(1); fi.play();

        primaryStage.setScene(scene);
    }

    /** Neon-styled button factory. */
    private Button makeNeonButton(String text, String bg, String border, String fg) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + bg + "; " +
            "-fx-text-fill: " + fg + "; " +
            "-fx-font-size: 12; -fx-font-weight: bold; " +
            "-fx-font-family: 'Consolas'; " +
            "-fx-background-radius: 20; " +
            "-fx-border-color: " + border + "; " +
            "-fx-border-width: 1.5; -fx-border-radius: 20; " +
            "-fx-cursor: hand; -fx-padding: 6 14;");
        DropShadow ds = new DropShadow(8, Color.web(border));
        ds.setSpread(0.15);
        btn.setEffect(ds);
        return btn;
    }

    /** Thin separator line. */
    private Rectangle makeSep() {
        Rectangle r = new Rectangle(320, 1);
        r.setFill(Color.web("#00f5ff", 0.12));
        return r;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BOARD GRID
    // ══════════════════════════════════════════════════════════════════════════
    private void createBoard(AnchorPane pane) {
        boardGrid = new GridPane();
        boardGrid.setLayoutX(365); boardGrid.setLayoutY(48);
        boardGrid.setHgap(3); boardGrid.setVgap(3);

        boardButtons = new Button[Constants.BOARD_ROWS][Constants.BOARD_COLS];

        for (int index = 0; index < Constants.BOARD_SIZE; index++) {
            int[] rc  = indexToRowCol(index);
            int row = rc[0], col = rc[1];

            Button cell = new Button();
            cell.setPrefSize(88, 54);
            cell.setMinSize(88, 54);
            cell.setMaxSize(88, 54);
            cell.setWrapText(true);
            cell.setFocusTraversable(false);
            cell.setOnAction(this);

            boardButtons[row][col] = cell;
            boardGrid.add(cell, col, row);
        }

        pane.getChildren().add(boardGrid);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GAME LIFECYCLE
    // ══════════════════════════════════════════════════════════════════════════
    private void startGame(Role role) {
        try {
            game = new Game(role);
            showGameScreen();
        } catch (IOException e) {
            showStartScreen();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PLAY TURN
    // ══════════════════════════════════════════════════════════════════════════
    private void playTurnAction() {
        if (game == null || game.getWinner() != null) return;

        Monster current  = game.getCurrent();
        Monster opponent = getCurrentOpponent();
        Map<String, MonsterState> before = snapshotAllMonsters();

        lastDrawnCard = null;
        lastDiceRoll  = 0;

        try {
            if (current.isFrozen()) {
                current.setFrozen(false);
                lastAction = current.getName() + " was frozen — turn skipped.";
                switchTurnIfNoWinner();
                powerupUsedThisTurn = false;
                refreshGUIWithChanges(before);
                return;
            }

            lastDiceRoll = rollDice();
            animateDice(lastDiceRoll);

            int oldPos          = current.getPosition();
            int intendedPos     = predictLandingPosition(current, lastDiceRoll);
            Cell intendedCell   = getCellByIndex(intendedPos);

            Card predictedCard = null;
            if (intendedCell instanceof CardCell) {
                if (Board.getCards().size() == 0) Board.reloadCards();
                if (Board.getCards().size() > 0)  predictedCard = Board.getCards().get(0);
            }

            game.getBoard().moveMonster(current, lastDiceRoll, opponent);

            if (predictedCard != null) lastDrawnCard = predictedCard;

            String cellEffect = describeCellEffect(intendedCell);
            lastAction = current.getName()
                + " rolled " + lastDiceRoll
                + " and moved from " + oldPos + " to " + current.getPosition()
                + ". Cell " + intendedPos + " (" + cellEffect + ").";

            if (lastDrawnCard != null)
                lastAction += "\nCard drawn: " + lastDrawnCard.getName()
                    + " — " + lastDrawnCard.getDescription();

            // Neon trail/landing celebration makes movement easy to follow.
            animateLandingImpact(current.getPosition(), current == game.getPlayer());

            if (game.getWinner() == null) {
                switchTurnIfNoWinner();
                powerupUsedThisTurn = false;
            }

            refreshGUIWithChanges(before);

        } catch (Exception ex) {
            lastAction = "Invalid action: " + ex.getMessage()
                + "\nMonster stayed in place. Roll again to retry.";
            shake(playTurnButton);
            refreshGUIWithChanges(before);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  POWERUP
    // ══════════════════════════════════════════════════════════════════════════
    private void usePowerupAction() {
        if (game == null || game.getWinner() != null) return;

        Monster current = game.getCurrent();
        Map<String, MonsterState> before = snapshotAllMonsters();

        if (powerupUsedThisTurn) {
            lastAction = "Invalid: powerup already used this turn. Roll the dice.";
            shake(usePowerupButton);
            refreshGUIWithChanges(before);
            return;
        }
        if (current.getEnergy() < Constants.POWERUP_COST) {
            lastAction = "Invalid: " + current.getName()
                + " needs " + Constants.POWERUP_COST + " energy (has "
                + current.getEnergy() + ").";
            shake(usePowerupButton);
            refreshGUIWithChanges(before);
            return;
        }

        try {
            game.usePowerup();
            powerupUsedThisTurn = true;

            // powerup visual burst
            ScaleTransition burst = new ScaleTransition(Duration.millis(300), usePowerupButton);
            burst.setToX(1.3); burst.setToY(1.3);
            ScaleTransition shrink = new ScaleTransition(Duration.millis(200), usePowerupButton);
            shrink.setToX(1.0); shrink.setToY(1.0);
            new SequentialTransition(burst, shrink).play();

            lastAction = current.getName() + " activated " + powerupName(current) + " (cost 500 energy).";
            refreshGUIWithChanges(before);

        } catch (Exception ex) {
            lastAction = "Powerup error: " + ex.getMessage();
            refreshGUIWithChanges(before);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CHEAT KEYS
    // ══════════════════════════════════════════════════════════════════════════
    private void cheatWin() {
        if (game == null) return;

        // Evaluation showcase: force a valid winning state and open victory mode instantly.
        game.getPlayer().setPosition(Constants.WINNING_POSITION);
        game.getPlayer().setEnergy(Math.max(game.getPlayer().getEnergy(), 1000));
        lastAction = "VICTORY TEST: " + game.getPlayer().getName()
            + " reached the finish portal with " + game.getPlayer().getEnergy() + " energy!";

        if (particleLoop != null) particleLoop.stop();
        showGameOverScreen(game.getPlayer());
    }

    private void cheatEnergy() {
        if (game == null) return;

        int old = game.getPlayer().getEnergy();
        game.getPlayer().setEnergy(old + 1000);
        lastAction = "ENERGY BOOST: " + game.getPlayer().getName()
            + " charged from " + old + " to " + game.getPlayer().getEnergy() + " energy.";

        if (game.getPlayer().getPosition() == Constants.WINNING_POSITION
                && game.getPlayer().getEnergy() >= 1000) {
            if (particleLoop != null) particleLoop.stop();
            showGameOverScreen(game.getPlayer());
        } else {
            refreshGUI();
            energyBurstAnimation();
        }
    }
    /** Cinematic landing impact: pulse, flash and glow the destination tile. */
    private void animateLandingImpact(int position, boolean playerMove) {
        int[] rc = indexToRowCol(position);
        Button landed = boardButtons[rc[0]][rc[1]];
        String flashColor = playerMove ? "#00f5ff" : "#ff2d55";
        flashButton(landed, flashColor);

        ScaleTransition hit = new ScaleTransition(Duration.millis(170), landed);
        hit.setFromX(1.0); hit.setFromY(1.0);
        hit.setToX(1.18); hit.setToY(1.18);
        hit.setAutoReverse(true);
        hit.setCycleCount(2);
        hit.play();

        DropShadow impact = new DropShadow(28, Color.web(flashColor));
        impact.setSpread(0.55);
        landed.setEffect(impact);
        PauseTransition removeImpact = new PauseTransition(Duration.millis(680));
        removeImpact.setOnFinished(e -> refreshBoard());
        removeImpact.play();
    }

    /** Bright energy pulse when E is activated without finishing the game. */
    private void energyBurstAnimation() {
        if (playerInfoLabel == null) return;
        DropShadow charge = new DropShadow(28, C_GREEN);
        charge.setSpread(0.65);
        playerInfoLabel.setEffect(charge);

        ScaleTransition grow = new ScaleTransition(Duration.millis(170), playerInfoLabel);
        grow.setToX(1.08); grow.setToY(1.08);
        ScaleTransition settle = new ScaleTransition(Duration.millis(220), playerInfoLabel);
        settle.setToX(1.0); settle.setToY(1.0);
        SequentialTransition pulse = new SequentialTransition(grow, settle);
        pulse.setOnFinished(e -> playerInfoLabel.setEffect(null));
        pulse.play();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TURN SWITCH
    // ══════════════════════════════════════════════════════════════════════════
    private void switchTurnIfNoWinner() {
        if (game.getWinner() != null) return;
        if (game.getCurrent() == game.getPlayer()) game.setCurrent(game.getOpponent());
        else                                        game.setCurrent(game.getPlayer());
    }

    private Monster getCurrentOpponent() {
        return (game.getCurrent() == game.getPlayer()) ? game.getOpponent() : game.getPlayer();
    }

    private int rollDice() { return (int)(Math.random() * 6) + 1; }

    // ══════════════════════════════════════════════════════════════════════════
    //  DICE ANIMATION
    // ══════════════════════════════════════════════════════════════════════════
    private void animateDice(int finalRoll) {
        String[] faces = { "⚀","⚁","⚂","⚃","⚄","⚅" };
        // spin through random faces then land on the real one
        final int[] count = {0};
        Timeline spin = new Timeline(new KeyFrame(Duration.millis(80), e -> {
            int idx = (int)(Math.random() * 6);
            diceRollBigLabel.setText(faces[idx]);
            count[0]++;
        }));
        spin.setCycleCount(12);
        spin.setOnFinished(e -> {
            diceRollBigLabel.setText(faces[finalRoll - 1]);
            bounceIn(diceRollBigLabel);
        });
        spin.play();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PREDICT LANDING POSITION
    // ══════════════════════════════════════════════════════════════════════════
    private int predictLandingPosition(Monster m, int roll) {
        int movement = roll;
        if (m instanceof Dasher) {
            Dasher d = (Dasher) m;
            movement = (d.getMomentumTurns() > 0) ? roll * 3 : roll * 2;
        } else if (m instanceof MultiTasker) {
            MultiTasker mt = (MultiTasker) m;
            movement = (mt.getNormalSpeedTurns() > 0) ? roll : roll / 2;
        }
        return (m.getPosition() + movement) % Constants.BOARD_SIZE;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REFRESH GUI
    // ══════════════════════════════════════════════════════════════════════════
    private void refreshGUI() {
        if (game == null) return;
        refreshInfoLabels();
        refreshBoard();
        if (messageArea != null) messageArea.setText(lastAction);
        if (game.getWinner() != null) showGameOverScreen(game.getWinner());
    }

    private void refreshGUIWithChanges(Map<String, MonsterState> before) {
        String changes = buildChangeMessage(before);
        if (!changes.isEmpty()) lastAction = lastAction + "\n" + changes;
        refreshGUI();
    }

    private void refreshInfoLabels() {
        Monster current = game.getCurrent();

        // ── whose turn ──
        boolean isPlayer = (current == game.getPlayer());
        String turnColor = isPlayer ? "#00f5ff" : "#ff2d55";
        currentTurnLabel.setStyle(
            S_LABEL + " -fx-font-size: 12.5; -fx-font-weight: bold; -fx-text-fill: " + turnColor + ";");
        currentTurnLabel.setText(
            "▶  " + current.getName() + "'s TURN\n" +
            "Role: " + current.getRole() + "  |  Type: " + getMonsterType(current) + "\n" +
            "Powerup ready this turn: " + (powerupUsedThisTurn ? "No" : "Yes"));

        playerInfoLabel.setText("PLAYER\n" + monsterDetails(game.getPlayer()));
        opponentInfoLabel.setText("OPPONENT\n" + monsterDetails(game.getOpponent()));

        diceLabel.setText("Last roll: " + (lastDiceRoll == 0 ? "—" : lastDiceRoll));
        if (lastDiceRoll > 0) {
            String[] faces = { "","⚀","⚁","⚂","⚃","⚄","⚅" };
            diceRollBigLabel.setText(faces[lastDiceRoll]);
        }

        cardLabel.setText(lastDrawnCard == null
            ? "Last card: none"
            : "Last card: " + lastDrawnCard.getName() + "\n" + lastDrawnCard.getDescription());

        int rem   = Board.getCards()         == null ? 0 : Board.getCards().size();
        int total = Board.getOriginalCards() == null ? 0 : Board.getOriginalCards().size();
        pileLabel.setText("Card pile: " + rem + " / " + total);

        usePowerupButton.setDisable(game.getWinner() != null);
        playTurnButton  .setDisable(game.getWinner() != null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  BOARD RENDER
    // ══════════════════════════════════════════════════════════════════════════
    private void refreshBoard() {
        for (int idx = 0; idx < Constants.BOARD_SIZE; idx++) {
            int[] rc  = indexToRowCol(idx);
            Cell  cell = getCellByIndex(idx);
            Button btn = boardButtons[rc[0]][rc[1]];

            btn.setText(cellButtonText(idx, cell));
            btn.setStyle(cellStyle(cell, idx));
        }

        animatePlayerPosition();
    }

    private String cellButtonText(int idx, Cell cell) {
        boolean playerHere   = game.getPlayer().getPosition() == idx;
        boolean opponentHere = game.getOpponent().getPosition() == idx;

        // Occupied tiles use large labels instead of hiding P/O underneath cell details.
        if (playerHere && opponentHere) {
            return "⚡ YOU + CPU\nCELL " + idx + "\n" + shortCellType(cell);
        }

        if (playerHere) {
            return "▶ YOU\nCELL " + idx + "\n" + shortCellType(cell);
        }

        if (opponentHere) {
            return "● CPU\nCELL " + idx + "\n" + shortCellType(cell);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(idx);

        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            sb.append("\n").append(d.getRole() == Role.SCARER ? "D-S" : "D-L");
            sb.append("\nE:").append(d.getEnergy());
            sb.append(d.isActivated() ? "\nUsed" : "\nReady");
        } else if (cell instanceof CardCell) {
            sb.append("\n🃏 Card");
        } else if (cell instanceof MonsterCell) {
            MonsterCell mc = (MonsterCell) cell;
            sb.append("\n👹 ").append(shortName(mc.getCellMonster().getName()));
        } else if (cell instanceof ConveyorBelt) {
            sb.append("\n⚙ Belt").append("\n").append(signed(((ConveyorBelt) cell).getEffect()));
        } else if (cell instanceof ContaminationSock) {
            sb.append("\n🧦 Sock").append("\n").append(signed(((ContaminationSock) cell).getEffect()));
        } else {
            sb.append("\nNormal");
        }

        return sb.toString();
    }

    private String shortCellType(Cell cell) {
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            return d.getRole() == Role.SCARER ? "SCARER DOOR" : "LAUGH DOOR";
        }
        if (cell instanceof CardCell)          return "CARD";
        if (cell instanceof MonsterCell)       return "MONSTER";
        if (cell instanceof ConveyorBelt)      return "BELT " + signed(((ConveyorBelt) cell).getEffect());
        if (cell instanceof ContaminationSock) return "SOCK " + signed(((ContaminationSock) cell).getEffect());
        return "NORMAL";
    }

    private String cellStyle(Cell cell, int idx) {
        boolean playerHere   = game.getPlayer().getPosition() == idx;
        boolean opponentHere = game.getOpponent().getPosition() == idx;

        String base =
            "-fx-font-family: 'Consolas'; " +
            "-fx-font-weight: bold; " +
            "-fx-text-alignment: center; " +
            "-fx-wrap-text: true; " +
            "-fx-padding: 2; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; ";

        // Occupied tiles get a completely different full-tile color.
        if (playerHere && !opponentHere) {
            return base +
                "-fx-font-size: 13; " +
                "-fx-text-fill: #00151a; " +
                "-fx-background-color: #00f5ff; " +
                "-fx-border-color: #ffffff; " +
                "-fx-border-width: 4; " +
                "-fx-effect: dropshadow(gaussian, #00f5ff, 22, 0.8, 0, 0);";
        }

        if (opponentHere && !playerHere) {
            return base +
                "-fx-font-size: 13; " +
                "-fx-text-fill: white; " +
                "-fx-background-color: #ff2d55; " +
                "-fx-border-color: #ffffff; " +
                "-fx-border-width: 4; " +
                "-fx-effect: dropshadow(gaussian, #ff2d55, 22, 0.8, 0, 0);";
        }

        if (playerHere && opponentHere) {
            return base +
                "-fx-font-size: 11; " +
                "-fx-text-fill: #111111; " +
                "-fx-background-color: #ffd60a; " +
                "-fx-border-color: #ffffff; " +
                "-fx-border-width: 4; " +
                "-fx-effect: dropshadow(gaussian, #ffd60a, 22, 0.8, 0, 0);";
        }

        String bg;
        String border;

        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            if (d.isActivated()) {
                bg = "#1a1a1a";
                border = "#444466";
            } else if (d.getRole() == Role.SCARER) {
                bg = "#0d1a3d";
                border = "#2266dd";
            } else {
                bg = "#0d2a1a";
                border = "#22cc66";
            }
        } else if (cell instanceof CardCell) {
            bg = "#1a0d2e";
            border = "#bf5af2";
        } else if (cell instanceof MonsterCell) {
            bg = "#2a0010";
            border = "#ff2d55";
        } else if (cell instanceof ConveyorBelt) {
            bg = "#001a10";
            border = "#30d158";
        } else if (cell instanceof ContaminationSock) {
            bg = "#2a1500";
            border = "#ff6b00";
        } else {
            bg = "#0a0a18";
            border = "#1e1e40";
        }

        return base +
            "-fx-font-size: 8; " +
            "-fx-text-fill: #dde8ff; " +
            "-fx-background-color: " + bg + "; " +
            "-fx-border-color: " + border + "; " +
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(gaussian, " + border + ", 6, 0.2, 0, 0);";
    }

    private void animatePlayerPosition() {
        if (playerCellPulse != null) {
            playerCellPulse.stop();
        }
        if (lastPulsingPlayerCell != null) {
            lastPulsingPlayerCell.setScaleX(1.0);
            lastPulsingPlayerCell.setScaleY(1.0);
        }

        int[] rc = indexToRowCol(game.getPlayer().getPosition());
        Button playerCell = boardButtons[rc[0]][rc[1]];
        lastPulsingPlayerCell = playerCell;

        playerCell.setScaleX(1.0);
        playerCell.setScaleY(1.0);

        playerCellPulse = new ScaleTransition(Duration.millis(650), playerCell);
        playerCellPulse.setFromX(1.0);
        playerCellPulse.setFromY(1.0);
        playerCellPulse.setToX(1.10);
        playerCellPulse.setToY(1.10);
        playerCellPulse.setAutoReverse(true);
        playerCellPulse.setCycleCount(Timeline.INDEFINITE);
        playerCellPulse.play();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GAME OVER SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    private void showGameOverScreen(Monster winner) {
        if (playerCellPulse != null) playerCellPulse.stop();
        if (particleLoop != null) particleLoop.stop();

        AnchorPane pane = buildAnimatedBackground(980, 650);
        Scene scene = new Scene(pane, 980, 650);

        boolean playerWon = winner == game.getPlayer();
        Color victoryColor = playerWon ? C_CYAN : C_SCARLET;
        String verdict = playerWon ? "YOU WIN!" : "CPU WINS!";
        String icon = playerWon ? "🏆" : "👹";

        // Continuous neon confetti rain
        Canvas celebration = new Canvas(980, 650);
        celebration.setMouseTransparent(true);
        pane.getChildren().add(celebration);
        GraphicsContext gc = celebration.getGraphicsContext2D();
        java.util.Random rng = new java.util.Random();
        final int particles = 120;
        double[] x = new double[particles];
        double[] y = new double[particles];
        double[] speed = new double[particles];
        double[] size = new double[particles];
        Color[] colours = new Color[particles];
        Color[] palette = { C_CYAN, C_GOLD, C_SCARLET, C_PURPLE, C_GREEN, Color.WHITE };

        for (int i = 0; i < particles; i++) {
            x[i] = rng.nextDouble() * 980;
            y[i] = -rng.nextDouble() * 650;
            speed[i] = 2 + rng.nextDouble() * 5;
            size[i] = 3 + rng.nextDouble() * 8;
            colours[i] = palette[rng.nextInt(palette.length)];
        }

        Timeline confetti = new Timeline(new KeyFrame(Duration.millis(28), e -> {
            gc.clearRect(0, 0, 980, 650);
            for (int i = 0; i < particles; i++) {
                y[i] += speed[i];
                x[i] += Math.sin((y[i] + i) * 0.035) * 0.8;
                if (y[i] > 650) {
                    y[i] = -10 - rng.nextDouble() * 110;
                    x[i] = rng.nextDouble() * 980;
                }
                gc.setFill(colours[i].deriveColor(0, 1, 1, 0.8));
                gc.fillRoundRect(x[i], y[i], size[i], size[i] * 1.7, 3, 3);
            }
        }));
        confetti.setCycleCount(Timeline.INDEFINITE);
        confetti.play();

        // Dark glass victory card
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setLayoutX(155);
        card.setLayoutY(50);
        card.setPrefSize(670, 535);
        card.setPadding(new Insets(24));
        card.setStyle(
            "-fx-background-color: rgba(6,8,25,0.92); " +
            "-fx-background-radius: 28; -fx-border-radius: 28; " +
            "-fx-border-color: " + toHex(victoryColor) + "; -fx-border-width: 2.5; " +
            "-fx-effect: dropshadow(gaussian, " + toHex(victoryColor) + ", 38, 0.45, 0, 0);"
        );
        pane.getChildren().add(card);

        Label cup = new Label(icon);
        cup.setStyle("-fx-font-size: 62;");
        ScaleTransition trophyPulse = new ScaleTransition(Duration.millis(700), cup);
        trophyPulse.setFromX(0.94); trophyPulse.setFromY(0.94);
        trophyPulse.setToX(1.12); trophyPulse.setToY(1.12);
        trophyPulse.setAutoReverse(true);
        trophyPulse.setCycleCount(Timeline.INDEFINITE);
        trophyPulse.play();

        Label title = new Label(verdict);
        title.setStyle(
            "-fx-font-family: 'Consolas'; -fx-font-weight: bold; -fx-font-size: 48; " +
            "-fx-text-fill: " + toHex(victoryColor) + "; " +
            "-fx-effect: dropshadow(gaussian, " + toHex(victoryColor) + ", 25, 0.7, 0, 0);"
        );
        addGlowAnimation(title);

        Label champion = new Label("CHAMPION  •  " + winner.getName().toUpperCase());
        champion.setStyle(
            "-fx-font-family: 'Consolas'; -fx-font-size: 18; -fx-font-weight: bold; " +
            "-fx-text-fill: #ffd60a;"
        );

        HBox stats = new HBox(18);
        stats.setAlignment(Pos.CENTER);

        VBox playerCard = endStatCard("YOU", game.getPlayer(), game.getPlayer() == winner, "#00f5ff");
        VBox cpuCard = endStatCard("CPU", game.getOpponent(), game.getOpponent() == winner, "#ff2d55");
        stats.getChildren().addAll(playerCard, cpuCard);

        Label finish = new Label("FINISH CELL 99  •  REQUIRED ENERGY 1000");
        finish.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12; -fx-text-fill: #8a9bbd;");

        playAgainButton = makeNeonButton("↩  PLAY AGAIN", "#21103d", "#bf5af2", "#ffffff");
        playAgainButton.setPrefSize(270, 54);
        playAgainButton.setOnAction(this);
        addHoverAnimation(playAgainButton);

        card.getChildren().addAll(cup, title, champion, stats, finish, playAgainButton);

        FadeTransition fade = new FadeTransition(Duration.millis(650), card);
        fade.setFromValue(0.0); fade.setToValue(1.0);
        ScaleTransition arrive = new ScaleTransition(Duration.millis(650), card);
        arrive.setFromX(0.78); arrive.setFromY(0.78);
        arrive.setToX(1.0); arrive.setToY(1.0);
        new ParallelTransition(fade, arrive).play();

        primaryStage.setScene(scene);
    }

    private VBox endStatCard(String heading, Monster monster, boolean won, String accent) {
        VBox box = new VBox(7);
        box.setAlignment(Pos.CENTER);
        box.setPrefSize(252, 128);
        box.setPadding(new Insets(10));
        box.setStyle(
            "-fx-background-color: rgba(15,18,42,0.94); -fx-background-radius: 16; " +
            "-fx-border-radius: 16; -fx-border-width: " + (won ? "2.5" : "1") + "; " +
            "-fx-border-color: " + accent + ";"
        );

        Label head = new Label((won ? "★ " : "") + heading);
        head.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + accent + ";");
        Label name = new Label(monster.getName());
        name.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12; -fx-text-fill: #e9edff;");
        Label details = new Label("POSITION  " + monster.getPosition() + "ENERGY    " + monster.getEnergy());
        details.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13; -fx-text-fill: #c1cced;");
        box.getChildren().addAll(head, name, details);
        return box;
    }

    private String toHex(Color colour) {
        return String.format("#%02X%02X%02X",
            (int)Math.round(colour.getRed() * 255),
            (int)Math.round(colour.getGreen() * 255),
            (int)Math.round(colour.getBlue() * 255));
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PARTICLE SYSTEM (floating dots on game screen)
    // ══════════════════════════════════════════════════════════════════════════
    private void initParticles() {
        java.util.Random rng = new java.util.Random();
        Color[] palette = { C_CYAN, C_PURPLE, C_GOLD, C_SCARLET, C_GREEN };
        for (int i = 0; i < px.length; i++) {
            px[i] = rng.nextDouble() * 1320;
            py[i] = rng.nextDouble() * 750;
            double angle = rng.nextDouble() * Math.PI * 2;
            double spd   = 0.2 + rng.nextDouble() * 0.5;
            pvx[i]   = Math.cos(angle) * spd;
            pvy[i]   = Math.sin(angle) * spd;
            palpha[i] = rng.nextDouble() * 0.35 + 0.05;
            pcolor[i] = palette[rng.nextInt(palette.length)];
        }
    }

    private void startParticleLoop() {
        GraphicsContext gc = particleCanvas.getGraphicsContext2D();
        particleLoop = new Timeline(new KeyFrame(Duration.millis(40), e -> {
            gc.clearRect(0, 0, 1320, 750);
            for (int i = 0; i < px.length; i++) {
                px[i] += pvx[i]; py[i] += pvy[i];
                if (px[i] < 0) px[i] = 1320;
                if (px[i] > 1320) px[i] = 0;
                if (py[i] < 0) py[i] = 750;
                if (py[i] > 750) py[i] = 0;
                double a = palpha[i] * (0.5 + 0.5 * Math.sin(System.currentTimeMillis() * 0.002 + i));
                gc.setFill(pcolor[i].deriveColor(0, 1, 1, a));
                gc.fillOval(px[i] - 2.5, py[i] - 2.5, 5, 5);
            }
        }));
        particleLoop.setCycleCount(Timeline.INDEFINITE);
        particleLoop.play();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UTILITIES
    // ══════════════════════════════════════════════════════════════════════════
    private int[] indexToRowCol(int index) {
        int row = index / Constants.BOARD_COLS;
        int col = index % Constants.BOARD_COLS;
        if (row % 2 != 0) col = Constants.BOARD_COLS - 1 - col;
        return new int[]{ row, col };
    }

    private Cell getCellByIndex(int index) {
        int[] rc = indexToRowCol(index);
        return game.getBoard().getBoardCells()[rc[0]][rc[1]];
    }

    private String describeCellEffect(Cell cell) {
        if (cell instanceof DoorCell) {
            DoorCell d = (DoorCell) cell;
            return "Door " + d.getRole() + " E:" + d.getEnergy()
                + (d.isActivated() ? " (used)" : " (ready)");
        }
        if (cell instanceof CardCell)     return "Card Cell";
        if (cell instanceof MonsterCell) {
            MonsterCell mc = (MonsterCell) cell;
            return "Monster Cell — " + mc.getCellMonster().getName();
        }
        if (cell instanceof ConveyorBelt)
            return "Conveyor Belt " + signed(((ConveyorBelt) cell).getEffect());
        if (cell instanceof ContaminationSock)
            return "Contamination Sock " + signed(((ContaminationSock) cell).getEffect()) + " / -100 energy";
        return "Normal Cell";
    }

    private void showSelectedCell(Button clicked) {
        for (int idx = 0; idx < Constants.BOARD_SIZE; idx++) {
            int[] rc = indexToRowCol(idx);
            if (boardButtons[rc[0]][rc[1]] == clicked) {
                Cell cell = getCellByIndex(idx);
                lastAction = "Cell " + idx + ": " + fullCellDetails(idx, cell);
                refreshGUI();
                return;
            }
        }
    }

    private String fullCellDetails(int idx, Cell cell) {
        String d = describeCellEffect(cell);
        if (game.getPlayer()  .getPosition() == idx) d += "\n● Player here: "  + game.getPlayer().getName();
        if (game.getOpponent().getPosition() == idx) d += "\n● Opponent here: " + game.getOpponent().getName();
        return d;
    }

    private Map<String, MonsterState> snapshotAllMonsters() {
        Map<String, MonsterState> map = new LinkedHashMap<>();
        addSnapshot(map, game.getPlayer());
        addSnapshot(map, game.getOpponent());
        ArrayList<Monster> stationed = Board.getStationedMonsters();
        if (stationed != null)
            for (Monster m : stationed) addSnapshot(map, m);
        return map;
    }

    private void addSnapshot(Map<String, MonsterState> map, Monster m) {
        if (m != null) map.put(m.getName(), new MonsterState(m));
    }

    private String buildChangeMessage(Map<String, MonsterState> before) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Monster> all = new ArrayList<>();
        all.add(game.getPlayer()); all.add(game.getOpponent());
        if (Board.getStationedMonsters() != null) all.addAll(Board.getStationedMonsters());
        for (Monster m : all) {
            MonsterState old = before.get(m.getName());
            if (old != null) {
                String line = old.compare(m);
                if (!line.isEmpty()) sb.append(line).append("\n");
            }
        }
        if (sb.length() > 0) return "Changes:\n" + sb.toString().trim();
        return "";
    }

    private String monsterDetails(Monster m) {
        return "Name: " + m.getName() + "\n"
            + "Role: " + m.getRole() + (m.getRole() != m.getOriginalRole() ? " (CONFUSED)" : "") + "\n"
            + "Type: " + getMonsterType(m) + "\n"
            + "Energy: " + m.getEnergy() + "\n"
            + "Position: " + m.getPosition() + "\n"
            + "Shielded: " + yn(m.isShielded())
            + "  Frozen: " + yn(m.isFrozen()) + "\n"
            + "Confusion turns: " + m.getConfusionTurns()
            + extraStatus(m);
    }

    private String extraStatus(Monster m) {
        if (m instanceof Dasher)      return "\nMomentum Rush turns: " + ((Dasher)m).getMomentumTurns();
        if (m instanceof MultiTasker) return "\nFocus Mode turns: "    + ((MultiTasker)m).getNormalSpeedTurns();
        if (m instanceof Dynamo)      return "\nPowerup: Energy Freeze (freezes opponent)";
        if (m instanceof Schemer)     return "\nPowerup: Chain Attack (steals from all)";
        return "";
    }

    private String powerupName(Monster m) {
        if (m instanceof Dasher)      return "Momentum Rush";
        if (m instanceof Dynamo)      return "Energy Freeze";
        if (m instanceof MultiTasker) return "Focus Mode";
        if (m instanceof Schemer)     return "Chain Attack";
        return "Powerup";
    }

    private String getMonsterType(Monster m) { return m.getClass().getSimpleName(); }
    private String yn(boolean v)             { return v ? "Yes" : "No"; }
    private String signed(int v)             { return v >= 0 ? "+" + v : "" + v; }

    private String shortName(String s) {
        if (s == null) return "";
        return s.length() <= 7 ? s : s.substring(0, 7);
    }

    private boolean isBoardButton(Object src) {
        if (boardButtons == null) return false;
        for (Button[] row : boardButtons)
            for (Button b : row)
                if (b == src) return true;
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  EVENT HANDLER
    // ══════════════════════════════════════════════════════════════════════════
    @Override
    public void handle(ActionEvent event) {
        Object src = event.getSource();
        if      (src == scarerButton)    startGame(Role.SCARER);
        else if (src == laugherButton)   startGame(Role.LAUGHER);
        else if (src == playTurnButton)  playTurnAction();
        else if (src == usePowerupButton) usePowerupAction();
        else if (src == cheatWinButton)  cheatWin();
        else if (src == cheatEnergyButton) cheatEnergy();
        else if (src == backToStartButton || src == playAgainButton) {
            if (particleLoop != null) particleLoop.stop();
            game = null;
            showStartScreen();
        }
        else if (isBoardButton(src)) showSelectedCell((Button) src);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INNER CLASS — MonsterState snapshot
    // ══════════════════════════════════════════════════════════════════════════
    private class MonsterState {
        private int   energy, position, confusionTurns, momentumTurns, normalSpeedTurns;
        private Role  role;
        private boolean shielded, frozen;

        MonsterState(Monster m) {
            energy         = m.getEnergy();
            position       = m.getPosition();
            role           = m.getRole();
            shielded       = m.isShielded();
            frozen         = m.isFrozen();
            confusionTurns = m.getConfusionTurns();
            momentumTurns  = (m instanceof Dasher)      ? ((Dasher) m).getMomentumTurns()      : 0;
            normalSpeedTurns=(m instanceof MultiTasker) ? ((MultiTasker) m).getNormalSpeedTurns(): 0;
        }

        String compare(Monster m) {
            StringBuilder sb = new StringBuilder();
            if (position       != m.getPosition())   sb.append(m.getName()).append(" position: ").append(position).append("→").append(m.getPosition()).append(". ");
            if (energy         != m.getEnergy())     sb.append(m.getName()).append(" energy: ").append(energy).append("→").append(m.getEnergy()).append(". ");
            if (role           != m.getRole())       sb.append(m.getName()).append(" role→").append(m.getRole()).append(". ");
            if (shielded       != m.isShielded())    {
                if (shielded && !m.isShielded() && energy == m.getEnergy())
                    sb.append(m.getName()).append("'s shield blocked energy loss. ");
                else sb.append(m.getName()).append(" shield→").append(yn(m.isShielded())).append(". ");
            }
            if (frozen         != m.isFrozen())     sb.append(m.getName()).append(" frozen→").append(yn(m.isFrozen())).append(". ");
            if (confusionTurns != m.getConfusionTurns()) sb.append(m.getName()).append(" confusion→").append(m.getConfusionTurns()).append(". ");
            if (m instanceof Dasher) {
                int cur = ((Dasher) m).getMomentumTurns();
                if (momentumTurns != cur) sb.append(m.getName()).append(" momentum→").append(cur).append(". ");
            }
            if (m instanceof MultiTasker) {
                int cur = ((MultiTasker) m).getNormalSpeedTurns();
                if (normalSpeedTurns != cur) sb.append(m.getName()).append(" focus→").append(cur).append(". ");
            }
            return sb.toString().trim();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ══════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) { launch(args); }
}
