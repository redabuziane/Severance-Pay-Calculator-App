import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.DecimalFormat;

public class IndemniteLicenciementApp extends JFrame {
    private final JTextField salaryField = new JTextField();
    private final JSpinner yearsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
    private final JSpinner monthsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 11, 1));
    private final JComboBox<String> categoryBox = new JComboBox<>(new String[]{"Non-cadre", "Cadre"});
    private final JCheckBox abusiveCheck = new JCheckBox("licenciement abusif");
    private final JCheckBox seriousFaultCheck = new JCheckBox("faute grave");
    private final JTextArea resultArea = new JTextArea();
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");

    public IndemniteLicenciementApp() {
        setTitle("Simulateur d'indemnite de licenciement - Maroc");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(760, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Simulateur d'indemnite de licenciement", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(31, 78, 121));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        contentPanel.setOpaque(false);
        contentPanel.add(createInputPanel());
        contentPanel.add(createResultPanel());
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JLabel disclaimer = new JLabel(
                "Estimation informative uniquement. Consultez un professionnel pour valider vos droits.",
                SwingConstants.CENTER);
        disclaimer.setFont(new Font("SansSerif", Font.ITALIC, 12));
        disclaimer.setForeground(Color.DARK_GRAY);
        mainPanel.add(disclaimer, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Informations"));
        panel.setBackground(Color.WHITE);

        panel.add(createFieldLabel("Salaire mensuel brut moyen (MAD)"));
        panel.add(salaryField);
        panel.add(Box.createVerticalStrut(12));

        panel.add(createFieldLabel("Annees d'anciennete"));
        panel.add(yearsSpinner);
        panel.add(Box.createVerticalStrut(12));

        panel.add(createFieldLabel("Mois supplementaires"));
        panel.add(monthsSpinner);
        panel.add(Box.createVerticalStrut(12));

        panel.add(createFieldLabel("Categorie"));
        panel.add(categoryBox);
        panel.add(Box.createVerticalStrut(12));

        panel.add(abusiveCheck);
        panel.add(Box.createVerticalStrut(8));
        panel.add(seriousFaultCheck);
        panel.add(Box.createVerticalStrut(20));

        JButton calculateButton = new JButton("Calculer");
        calculateButton.setFont(new Font("SansSerif", Font.BOLD, 15));
        calculateButton.setBackground(new Color(31, 120, 180));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFocusPainted(false);
        calculateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        calculateButton.addActionListener(e -> calculate());
        panel.add(calculateButton);
        panel.add(Box.createVerticalStrut(10));

        JButton resetButton = new JButton("Reset");
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.addActionListener(e -> resetForm());
        panel.add(resetButton);

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Resultat"));
        panel.setBackground(Color.WHITE);

        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        resultArea.setMargin(new Insets(10, 10, 10, 10));
        resultArea.setText("Entrez les informations, puis cliquez sur Calculer.");

        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void calculate() {
        try {
            String salaryText = salaryField.getText().trim().replace(",", ".");
            if (salaryText.isEmpty()) {
                showError("entrz votre informations");
                return;
            }

            double monthlySalary = Double.parseDouble(salaryText);
            int years = (Integer) yearsSpinner.getValue();
            int months = (Integer) monthsSpinner.getValue();

            if (monthlySalary <= 0) {
                showError("Le salaire doit etre superieur a zero.");
                return;
            }
            if (years == 0 && months < 6) {
                showError("L'anciennete minimale pour l'indemnite legale est de 6 mois.");
                return;
            }

            double seniority = years + (months / 12.0);
            double hourlySalary = monthlySalary / 191.0;
            boolean seriousFault = seriousFaultCheck.isSelected();

            double compensatedHours = seriousFault ? 0 : calculateCompensatedHours(seniority);
            double legalCompensation = compensatedHours * hourlySalary;

            double noticeMonths = seriousFault ? 0 : calculateNoticeMonths(seniority);
            double noticeCompensation = noticeMonths * monthlySalary;

            double damages = 0;
            if (abusiveCheck.isSelected() && !seriousFault) {
                double countedYears = Math.ceil(seniority);
                double damageMonths = Math.min(countedYears * 1.5, 36.0);
                damages = damageMonths * monthlySalary;
            }

            double total = legalCompensation + noticeCompensation + damages;

            StringBuilder result = new StringBuilder();
            result.append("Salaire mensuel : ").append(formatMoney(monthlySalary)).append(" MAD\n");
            result.append("Anciennete      : ").append(years).append(" an(s) et ")
                    .append(months).append(" mois\n");
            result.append("Salaire horaire : ").append(formatMoney(hourlySalary)).append(" MAD\n");
            result.append("--------------------------------\n");

            if (seriousFault) {
                result.append("Faute grave selectionnee.\n");
                result.append("Indemnite legale : 0.00 MAD\n");
                result.append("Preavis           : 0.00 MAD\n");
                result.append("Dommages-interets : 0.00 MAD\n");
            } else {
                result.append("Heures indemnisees: ").append(moneyFormat.format(compensatedHours)).append(" h\n");
                result.append("Indemnite legale  : ").append(formatMoney(legalCompensation)).append(" MAD\n");
                result.append("Preavis (").append(moneyFormat.format(noticeMonths)).append(" mois equivalent) : ")
                        .append(formatMoney(noticeCompensation)).append(" MAD\n");
                result.append("Dommages-interets : ").append(formatMoney(damages)).append(" MAD\n");
            }

            result.append("================================\n");
            result.append("TOTAL ESTIME      : ").append(formatMoney(total)).append(" MAD\n");
            result.append("================================\n");
            result.append("\nCette estimation ne comprend pas les conges non pris, ")
                    .append("le salaire restant ni les regles conventionnelles plus favorables.");

            resultArea.setText(result.toString());
            resultArea.setCaretPosition(0);

        } catch (NumberFormatException ex) {
            showError("Salaire invalide. Exemple valide : 7600 ou 7600.50");
        }
    }

    private double calculateCompensatedHours(double seniority) {
        double remaining = seniority;
        double totalHours = 0;

        double bracket1 = Math.min(remaining, 5.0);
        totalHours += bracket1 * 96;
        remaining -= bracket1;

        if (remaining > 0) {
            double bracket2 = Math.min(remaining, 5.0);
            totalHours += bracket2 * 144;
            remaining -= bracket2;
        }
        if (remaining > 0) {
            double bracket3 = Math.min(remaining, 5.0);
            totalHours += bracket3 * 192;
            remaining -= bracket3;
        }
        if (remaining > 0) {
            totalHours += remaining * 240;
        }
        return totalHours;
    }

    private double calculateNoticeMonths(double seniority) {
        boolean isManager = categoryBox.getSelectedItem().equals("Cadre");

        if (isManager) {
            if (seniority < 1) return 1;
            if (seniority < 5) return 2;
            return 3;
        }

        if (seniority < 1) {
            return 8.0 / 30.0; // Eight days converted to an estimated monthly fraction.
        }
        if (seniority < 5) return 1;
        return 2;
    }

    private String formatMoney(double amount) {
        return moneyFormat.format(amount);
    }

    private void resetForm() {
        salaryField.setText("");
        yearsSpinner.setValue(0);
        monthsSpinner.setValue(0);
        categoryBox.setSelectedIndex(0);
        abusiveCheck.setSelected(false);
        seriousFaultCheck.setSelected(false);
        resultArea.setText("Entrez les informations, puis cliquez sur Calculer.");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            new IndemniteLicenciementApp().setVisible(true);
        });
    }
}
