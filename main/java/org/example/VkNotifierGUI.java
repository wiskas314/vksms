package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class VkNotifierGUI extends JFrame {
    private final JTextField tokenField = new JTextField(40);
    private final JTextArea templateArea = new JTextArea(8,50);
    private final JButton loadButton = new JButton("Загрузить расписание (.xls)");
    private final JButton sendButton = new JButton("Отправить всем группам");
    private final JTable table = new JTable();
    private final DefaultTableModel tableModel;

    private List<GroupEntry> currentGroups;
    private final ExcelReader excelReader = new ExcelReader();
    private final VkSender vkSender = new VkSender();

    public VkNotifierGUI() {
        setTitle("VK Уведомления о занятиях by wiskas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        // Таблица
        String[] columns = {"Группа", "Время", "Аудитория", "Peer ID (редактируй)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        table.setModel(tableModel);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);

        // Панель управления
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        topPanel.add(new JLabel("Access Token:"), gbc);
        gbc.gridx = 1;
        topPanel.add(tokenField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        topPanel.add(new JLabel("Шаблон сообщения (используй {group}, {time}, {audience}):"), gbc);

        gbc.gridy = 2;
        topPanel.add(new JScrollPane(templateArea), gbc);

        gbc.gridy = 3; gbc.gridwidth = 1;
        topPanel.add(loadButton, gbc);
        gbc.gridx = 1;
        topPanel.add(sendButton, gbc);

        // Сборка
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Обработчики
        loadButton.addActionListener(e -> loadExcel());
        sendButton.addActionListener(e -> sendMessages());

        templateArea.setText("Привет, {group}!\n\nЗанятие сегодня в {time} в аудитории {audience}.\n\nЖду всех на месте!");
        sendButton.setEnabled(false);
    }

    private void loadExcel(){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.
                FileNameExtensionFilter("Excel 97-2003 (*.xls)", "xls"));
        if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            try{
                File file = chooser.getSelectedFile();
                currentGroups = excelReader.readSchedule(file.getAbsolutePath());

                tableModel.setRowCount(0);
                for (GroupEntry g : currentGroups){
                    tableModel.addRow(new Object[]{
                            g.getGroup(),
                            g.getTime(),
                            g.getAudience(),
                            g.getPeerId()
                    });
                }
                sendButton.setEnabled(true);
                JOptionPane.showMessageDialog(this, "Загружено " + currentGroups.size() + " групп");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sendMessages(){
        String token = tokenField.getText().trim();
        if(token.isEmpty()){
            JOptionPane.showMessageDialog(this, "Введите Access Token!",
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String template = templateArea.getText().trim();
        if (template.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите шаблон сообщения!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int success = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String group = (String) tableModel.getValueAt(i, 0);
            String time = (String) tableModel.getValueAt(i, 1);
            String audience = (String) tableModel.getValueAt(i, 2);
            long peerId = Long.parseLong(tableModel.getValueAt(i, 3).toString());

            if (peerId <= 0) continue;

            // Подставляем значения
            String message = template
                    .replace("{group}", group)
                    .replace("{time}", time)
                    .replace("{audience}", audience);

            boolean ok = vkSender.send(token, peerId, message);
            if (ok) {
                success++;
                System.out.println("✅ Отправлено → " + group);
            }

            try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
        }

        JOptionPane.showMessageDialog(this,
                "Отправлено успешно: " + success + " из " + currentGroups.size() + " групп",
                "Результат", JOptionPane.INFORMATION_MESSAGE);
    }

}
