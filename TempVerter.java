import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class TempVerter extends JFrame {

    private JTextField celsiusField;
    private JTextField fahrenheitField;
    private JTextField kelvinField;

    private JLabel decimalValueLabel;
    private JButton themeButton;
    private JLabel copyFeedbackLabel;

    private int decimalPlaces = 0;
    private boolean updating = false;
    private boolean darkMode = false;

    private final Color lightBg = new Color(250, 250, 249);
    private final Color lightCard = Color.WHITE;
    private final Color lightBorder = new Color(220, 222, 226);
    private final Color lightMuted = new Color(40, 40, 40);

    private final Color darkBg = new Color(28, 30, 36);
    private final Color darkCard = new Color(38, 41, 50);
    private final Color darkField = new Color(50, 54, 65);
    private final Color darkBorder = new Color(60, 65, 78);
    private final Color darkMuted = new Color(180, 185, 195);

    private final Color[] celsiusBg = {new Color(255, 235, 228), new Color(220, 75, 40)};
    private final Color[] fahrenBg = {new Color(232, 244, 255), new Color(35, 115, 200)};
    private final Color[] kelvinBg = {new Color(238, 249, 218), new Color(85, 145, 35)};

    private final Color invalidLight = new Color(255, 220, 220);
    private final Color invalidDark = new Color(90, 40, 40);

    private JPanel mainPanel;
    private JPanel cardPanel;
    private JPanel[] fieldRows = new JPanel[3];
    private JLabel[] hintLabels = new JLabel[3];

    public TempVerter() {
        setTitle("TempVerter");
        setSize(503, 363);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 15);

        celsiusField = makeField(monoFont);
        fahrenheitField = makeField(monoFont);
        kelvinField = makeField(monoFont);

        mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(28, 8, 18, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel titleLabel = new JLabel("TempVerter");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setIcon(loadIcon("BrandName.png", 22, 22));
        titleLabel.setIconTextGap(10);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        JLabel precisionLabel = new JLabel("Precision");
        precisionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton minusBtn = makeIconButton("minus.png", 44, 34);
        JButton plusBtn = makeIconButton("plus.png", 44, 34);

        decimalValueLabel = new JLabel(String.valueOf(decimalPlaces), SwingConstants.CENTER);
        decimalValueLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        decimalValueLabel.setPreferredSize(new Dimension(18, 28));

        minusBtn.addActionListener(e -> adjustDecimal(-1));
        plusBtn.addActionListener(e -> adjustDecimal(+1));

        themeButton = makeIconButton("dark.png", 50, 34);
        themeButton.addActionListener(e -> {
            darkMode = !darkMode;
            themeButton.setIcon(loadIcon(darkMode ? "light.png" : "dark.png", 18, 18));
            applyTheme();
        });

        rightControls.add(precisionLabel);
        rightControls.add(minusBtn);
        rightControls.add(decimalValueLabel);
        rightControls.add(plusBtn);
        rightControls.add(Box.createHorizontalStrut(8));
        rightControls.add(themeButton);

        header.add(titleLabel, BorderLayout.WEST);
        header.add(rightControls, BorderLayout.EAST);

        cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createLineBorder(lightBorder, 1));

        String[][] meta = {
                {"°C", "Celsius", "Water freezes at 0 °C"},
                {"°F", "Fahrenheit", "Body temp is 98.6 °F"},
                {"K", "Kelvin", "Absolute zero is 0 K"}
        };

        Color[][] accents = {celsiusBg, fahrenBg, kelvinBg};
        JTextField[] fields = {celsiusField, fahrenheitField, kelvinField};
        String[] scales = {"C", "F", "K"};

        for (int i = 0; i < 3; i++) {
            JPanel row = buildFieldRow(meta[i], accents[i], fields[i], scales[i], i);
            fieldRows[i] = row;
            cardPanel.add(row);

            if (i < 2) {
                cardPanel.add(makeDivider());
            }
        }

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JButton clearBtn = new JButton("Clear All");
        clearBtn.setIcon(loadIcon("cancel.png", 13, 13));
        clearBtn.setIconTextGap(6);
        clearBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        clearBtn.setFocusPainted(false);
        clearBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearBtn.setPreferredSize(new Dimension(105, 34));
        clearBtn.addActionListener(e -> clearAll());

        copyFeedbackLabel = new JLabel("Copied!");
        copyFeedbackLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        copyFeedbackLabel.setVisible(false);

        footer.add(clearBtn, BorderLayout.WEST);
        footer.add(copyFeedbackLabel, BorderLayout.EAST);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(footer, BorderLayout.SOUTH);

        add(mainPanel);

        addLiveUpdater(celsiusField, "C");
        addLiveUpdater(fahrenheitField, "F");
        addLiveUpdater(kelvinField, "K");

        applyTheme();
    }

    private JPanel buildFieldRow(String[] meta, Color[] accent, JTextField field, String scale, int idx) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(true);
        row.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel badge = new JLabel(meta[0], SwingConstants.CENTER);
        badge.setName("badgeLabel");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        badge.setOpaque(true);
        badge.setBackground(accent[0]);
        badge.setForeground(accent[1]);
        badge.setPreferredSize(new Dimension(34, 34));

        JPanel metaPanel = new JPanel();
        metaPanel.setLayout(new BoxLayout(metaPanel, BoxLayout.Y_AXIS));
        metaPanel.setOpaque(false);
        metaPanel.setPreferredSize(new Dimension(140, 40));

        JLabel nameLabel = new JLabel(meta[1]);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel hintLabel = new JLabel(meta[2]);
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hintLabels[idx] = hintLabel;

        metaPanel.add(nameLabel);
        metaPanel.add(hintLabel);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(badge);
        left.add(metaPanel);

        JButton copyBtn = makeIconButton("copy.png", 50, 34);
        copyBtn.setToolTipText("Copy value");
        copyBtn.addActionListener(e -> {
            String text = field.getText();

            if (!text.isEmpty()) {
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(text), null);

                showCopyFeedback();
            }
        });

        row.add(left, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.add(copyBtn, BorderLayout.EAST);

        return row;
    }

    private JButton makeIconButton(String fileName, int width, int height) {
        JButton button = new JButton(loadIcon(fileName, 18, 18));
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private ImageIcon loadIcon(String fileName, int width, int height) {
        ImageIcon icon = new ImageIcon("Assets/" + fileName);
        Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(image);
    }

    private JSeparator makeDivider() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return separator;
    }

    private JTextField makeField(Font font) {
        JTextField field = new JTextField();
        field.setFont(font);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 208, 215), 1),
                BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        return field;
    }

    private void addLiveUpdater(JTextField field, String scale) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateFields(field, scale);
            }

            public void removeUpdate(DocumentEvent e) {
                updateFields(field, scale);
            }

            public void changedUpdate(DocumentEvent e) {
                updateFields(field, scale);
            }
        });
    }

    private void updateFields(JTextField sourceField, String sourceScale) {
        if (updating || !sourceField.hasFocus()) {
            return;
        }

        updating = true;

        try {
            String text = sourceField.getText().trim();

            if (isPartialInput(text)) {
                resetFieldColor(sourceField);
                clearOtherFields(sourceScale);
                return;
            }

            double input = Double.parseDouble(text);
            resetFieldColor(sourceField);

            double celsius;

            switch (sourceScale) {
                case "F":
                    celsius = fahrenheitToCelsius(input);
                    break;
                case "K":
                    celsius = kelvinToCelsius(input);
                    break;
                default:
                    celsius = input;
                    break;
            }

            updateConvertedFields(sourceScale, celsius);

        } catch (NumberFormatException ex) {
            sourceField.setBackground(darkMode ? invalidDark : invalidLight);
            clearOtherFields(sourceScale);
        } finally {
            updating = false;
        }
    }

    private boolean isPartialInput(String text) {
        return text.isEmpty()
                || text.equals("-")
                || text.equals(".")
                || text.equals("-.")
                || text.equals("+")
                || text.equals("+.");
    }

    private void updateConvertedFields(String sourceScale, double celsius) {
        double fahrenheit = celsiusToFahrenheit(celsius);
        double kelvin = celsiusToKelvin(celsius);

        if (!sourceScale.equals("C")) {
            celsiusField.setText(fmt(celsius));
        }

        if (!sourceScale.equals("F")) {
            fahrenheitField.setText(fmt(fahrenheit));
        }

        if (!sourceScale.equals("K")) {
            kelvinField.setText(fmt(kelvin));
        }

        resetFieldColor(celsiusField);
        resetFieldColor(fahrenheitField);
        resetFieldColor(kelvinField);
    }

    private void clearOtherFields(String sourceScale) {
        if (!sourceScale.equals("C")) {
            celsiusField.setText("");
        }

        if (!sourceScale.equals("F")) {
            fahrenheitField.setText("");
        }

        if (!sourceScale.equals("K")) {
            kelvinField.setText("");
        }
    }

    private void clearAll() {
        updating = true;

        celsiusField.setText("");
        fahrenheitField.setText("");
        kelvinField.setText("");

        resetFieldColor(celsiusField);
        resetFieldColor(fahrenheitField);
        resetFieldColor(kelvinField);

        updating = false;
    }

    private void adjustDecimal(int direction) {
        decimalPlaces = Math.max(0, Math.min(6, decimalPlaces + direction));
        decimalValueLabel.setText(String.valueOf(decimalPlaces));

        if (fahrenheitField.hasFocus()) {
            updateFields(fahrenheitField, "F");
        } else if (kelvinField.hasFocus()) {
            updateFields(kelvinField, "K");
        } else if (celsiusField.hasFocus()) {
            updateFields(celsiusField, "C");
        }
    }

    private void showCopyFeedback() {
        copyFeedbackLabel.setVisible(true);

        Timer timer = new Timer(1500, e -> copyFeedbackLabel.setVisible(false));
        timer.setRepeats(false);
        timer.start();
    }

    private void resetFieldColor(JTextField field) {
        if (darkMode) {
            field.setBackground(darkField);
            field.setForeground(Color.WHITE);
        } else {
            field.setBackground(Color.WHITE);
            field.setForeground(Color.BLACK);
        }
    }

    private void applyTheme() {
        Color bg = darkMode ? darkBg : lightBg;
        Color card = darkMode ? darkCard : lightCard;
        Color border = darkMode ? darkBorder : lightBorder;
        Color muted = darkMode ? darkMuted : lightMuted;
        Color text = darkMode ? Color.WHITE : Color.BLACK;

        mainPanel.setBackground(bg);
        cardPanel.setBackground(card);
        cardPanel.setBorder(BorderFactory.createLineBorder(border, 1));

        for (int i = 0; i < fieldRows.length; i++) {
            fieldRows[i].setBackground(card);
            hintLabels[i].setForeground(muted);
        }

        copyFeedbackLabel.setForeground(muted);

        applyToChildren(mainPanel, text);

        resetFieldColor(celsiusField);
        resetFieldColor(fahrenheitField);
        resetFieldColor(kelvinField);

        repaint();
    }

    private void applyToChildren(Container container, Color text) {
        for (Component child : container.getComponents()) {
            if (child instanceof JLabel label) {
                if ("badgeLabel".equals(label.getName())) {
                    label.setForeground(Color.BLACK);
                } else if (label != copyFeedbackLabel && !isHintLabel(label)) {
                    label.setForeground(text);
                }
            }

            if (child instanceof JButton button) {
                if ("Clear All".equals(button.getText())) {
                    button.setForeground(Color.BLACK);
                } else {
                    button.setForeground(text);
                }
            }

            if (child instanceof Container nested && !(child instanceof JTextField)) {
                applyToChildren(nested, text);
            }
        }
    }

    private boolean isHintLabel(JLabel label) {
        for (JLabel hint : hintLabels) {
            if (hint == label) {
                return true;
            }
        }

        return false;
    }

    private String fmt(double value) {
        return String.format("%." + decimalPlaces + "f", value);
    }

    private double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * (5.0 / 9.0);
    }

    private double celsiusToFahrenheit(double celsius) {
        return (celsius * (9.0 / 5.0)) + 32;
    }

    private double celsiusToKelvin(double celsius) {
        return celsius + 273.15;
    }

    private double kelvinToCelsius(double kelvin) {
        return kelvin - 273.15;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
            }

            new TempVerter().setVisible(true);
        });
    }
}