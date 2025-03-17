package com.chess.gui;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.chess.engine.board.Board;
import com.chess.engine.board.BoardUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import java.io.FileOutputStream;

import java.io.*;

public class Table {

    private final JFrame gameFrame;

    private final BoardPanel boardPanel;
    private final Board chessBoard;

    private final static Dimension OUTER_FRAME_DIMENSION = new Dimension(600, 600);
    private final static Dimension BOARD_PANEL_DIMENSION = new Dimension(400, 350);
    private final static Dimension TILE_PANEL_DIMENSION = new Dimension(10, 10);

    private final Color lightTileColor = Color.decode("#EEEED2");
    private final Color darkTileColor = Color.decode("#769656");
    private static String defaultPieceImagesPath = "../../../../picture/chess_1Kbyte_gambit";

    public Table() {
        this.gameFrame = new JFrame("JChess");
        this.gameFrame.setLayout(new BorderLayout());
        final JMenuBar tableMenuBar = createTableMenuBar();
        this.gameFrame.setJMenuBar(tableMenuBar);
        this.gameFrame.setSize(OUTER_FRAME_DIMENSION);
        this.chessBoard = Board.createStandardBoard();
        this.boardPanel = new BoardPanel();
        this.gameFrame.add(this.boardPanel, BorderLayout.CENTER);
        this.gameFrame.setVisible(true);
    }

    private JMenuBar createTableMenuBar() {
        final JMenuBar tableMenuBar = new JMenuBar();
        tableMenuBar.add(createFileMenu());
        return tableMenuBar;
    }

    private JMenu createFileMenu() {
        final JMenu fileMenu = new JMenu("File");
        final JMenuItem openPGN = new JMenuItem("Load PGN File");
        openPGN.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Open up that PGN file!");
            }
        });
        fileMenu.add(openPGN);
        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);
        return fileMenu;
    }

    private class BoardPanel extends JPanel {

        final List<TilePanel> boardTiles;

        BoardPanel() {
            super(new GridLayout(8, 8));
            this.boardTiles = new ArrayList<>();
            for (int i = 0; i < BoardUtils.NUM_TILES; i++) {
                final TilePanel tilePanel = new TilePanel(this, i);
                this.boardTiles.add(tilePanel);
                add(tilePanel);
            }
            setPreferredSize(OUTER_FRAME_DIMENSION);
            validate();
        }

    }

    private class TilePanel extends JPanel {

        private final int tileId;

        TilePanel(final BoardPanel boardPanel, final int tileId) {
            super(new GridBagLayout());
            this.tileId = tileId;
            setPreferredSize(TILE_PANEL_DIMENSION);
            assignTileColor();
            assignTilePieceIcon(chessBoard);
            validate();
        }

        private void assignTilePieceIcon(final Board board) {
            this.removeAll();
            if (board.getTile(this.tileId).isTileOccupied()) {
                try {
                    final String pieceCode = board.getTile(this.tileId).getPiece().getPieceAlliance().toString()
                            .substring(0, 1) +
                            board.getTile(this.tileId).getPiece().toString();
                    ;
                    File imageDir = new File("picture/cases");

                    // Check for files in order: PNG → GIF → SVG
                    File pngFile = new File(imageDir, pieceCode + ".png");
                    File gifFile = new File(imageDir, pieceCode + ".gif");
                    File svgFile = new File(imageDir, pieceCode + ".svg");

                    BufferedImage image = null;

                    if (pngFile.exists()) {
                        image = ImageIO.read(pngFile);
                    } else if (gifFile.exists()) {
                        image = ImageIO.read(gifFile);
                    } else if (svgFile.exists()) {
                        image = convertSvgToPng(svgFile, 45, 45);
                    }

                    if (image != null) {
                        add(new JLabel(new ImageIcon(image)));
                    }
                } catch (IOException | TranscoderException e) {
                    e.printStackTrace();
                }
            }
            revalidate();
            repaint();
        }

        private BufferedImage convertSvgToPng(File svgFile, int width, int height)
                throws IOException, TranscoderException {
            try (FileInputStream fis = new FileInputStream(svgFile);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                PNGTranscoder transcoder = new PNGTranscoder();
                transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
                transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);

                TranscoderInput input = new TranscoderInput(fis);
                TranscoderOutput output = new TranscoderOutput(outputStream);

                transcoder.transcode(input, output);
                outputStream.flush();

                byte[] pngData = outputStream.toByteArray();
                return ImageIO.read(new ByteArrayInputStream(pngData));
            }
        }

        private void assignTileColor() {
            if (BoardUtils.FIRST_ROW[this.tileId] ||
                    BoardUtils.THIRD_ROW[this.tileId] ||
                    BoardUtils.FIFTH_ROW[this.tileId] ||
                    BoardUtils.SEVENTH_ROW[this.tileId]) {
                setBackground(this.tileId % 2 == 0 ? lightTileColor : darkTileColor);
            } else if (BoardUtils.SECOND_ROW[this.tileId] ||
                    BoardUtils.FOURTH_ROW[this.tileId] ||
                    BoardUtils.SIXTH_ROW[this.tileId] ||
                    BoardUtils.EIGHTH_ROW[this.tileId]) {
                setBackground(this.tileId % 2 != 0 ? lightTileColor : darkTileColor);

            }
        }
    }
}
