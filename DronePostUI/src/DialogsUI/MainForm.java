package DialogsUI;

import Enums.CommunicationCode;
import Services.ClientManager;
import Services.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;


public class MainForm extends JFrame implements WindowListener, ActionListener {
    private final String actionChoiceLabel = "Choose your action";
    private final ArrayList<String> telephoneLabelText = new ArrayList<>(Arrays.asList("Recipient", "Sender"));
    private final ArrayList<String> senderOrReceiverText = new ArrayList<>(Arrays.asList("Send package", "Receive package", "Send message"));
    private final ArrayList<String> orderButtonLabels = new ArrayList<>(Arrays.asList("Send", "Receive", "Order history", "Sign out"));
    private final String orderSent = "Order sent";
    private final String messageSent = "Message sent";
    private final JTextArea textArea = new JTextArea(3,20);

    private final JTextField titleLabel = new JTextField();
    private final JTextField telephone = new JTextField(20);
    private final JTextField notification = new JTextField(20);
    private final JLabel telephoneLabel = new JLabel(telephoneLabelText.get(0));
    private final Choice senderOrReceiverChoices = new Choice();

    private final JButton orderButton = new JButton(orderButtonLabels.get(0));
    private final JButton logOutButton = new JButton(orderButtonLabels.get(3));
    private final JButton orderHistoryButton = new JButton(orderButtonLabels.get(2));

    final JPanel titlePanel = new JPanel(new GridLayout(0, 1));
    final JPanel mainPanel = new JPanel(new GridLayout(0, 1));
    final JPanel dataPanel = new JPanel(new GridLayout(0, 2));
    final JPanel messagePanel = new JPanel(new BorderLayout());
    final JPanel lowerPanel = new JPanel(new BorderLayout());
    final JPanel buttonPanel = new JPanel(new FlowLayout());

    private final ArrayList<String> currentUserData;

    public MainForm(String appName, ArrayList<String> currentUserData) {
        super(appName);
        this.currentUserData = currentUserData;
        addWindowListener(this);
        setLayout(new BorderLayout(20, 20));
        String welcomeMessage = "Welcome, " + currentUserData.get(1) + " " + currentUserData.get(2);

        setFormLabel(welcomeMessage);
        setFormBody();
        setLowerPanel();

        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        this.setVisible(true);
        showPanels(new ArrayList<>(Arrays.asList(titlePanel, mainPanel)));
    }

    private void setFormLabel(String welcomeMessage) {
        titleLabel.setEditable(false);
        titleLabel.setBackground(null);
        titleLabel.setBorder(null);
        titleLabel.setText(welcomeMessage);
        titleLabel.setFont(new Font(null, Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
    }

    private void setChooseActionPanel() {
        JLabel senderOrReceiverLabel = new JLabel(actionChoiceLabel);
        dataPanel.add(senderOrReceiverLabel);
        senderOrReceiverText.forEach(senderOrReceiverChoices::add);
        senderOrReceiverChoices.addItemListener(e -> {
            String str = (String) e.getItem();
            System.out.println(str);
            if (notification.isVisible())
                notification.setVisible(false);
            if (str.equals(senderOrReceiverText.get(0))) {//"Receiver phone number"
                telephoneLabel.setText(telephoneLabelText.get(0));
                orderButton.setText(orderButtonLabels.get(0));
                messagePanel.setVisible(false);
            } else if (str.equals(senderOrReceiverText.get(1))) {//"Sender phone number"
                telephoneLabel.setText(telephoneLabelText.get(1));
                orderButton.setText(orderButtonLabels.get(1));
                messagePanel.setVisible(false);
            } else { //"Receiver phone number"
                telephoneLabel.setText(telephoneLabelText.get(0));
                orderButton.setText(orderButtonLabels.get(0));
                messagePanel.setVisible(true);
            }
        });
        dataPanel.add(senderOrReceiverChoices);
    }

    private void setTelephone() {
        dataPanel.add(telephoneLabel);
        dataPanel.add(telephone);

        telephone.setText("+972");
        telephone.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                int length = telephone.getText().length();
                if ((length == 4 && e.getKeyChar() == '0') || !Character.isDigit(e.getKeyChar()) || (length == 13))
                    e.setKeyChar(Character.MIN_VALUE);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                        && telephone.getBackground() == Color.ORANGE)
                    telephone.setBackground(Color.WHITE);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
    }

    private void setMessagePanel() {
        messagePanel.add(new JLabel("Your message:"), BorderLayout.NORTH);
        messagePanel.add(textArea, BorderLayout.SOUTH);
        mainPanel.add(messagePanel, BorderLayout.SOUTH);
        messagePanel.setVisible(false);
    }

    private void setNotification(String text, Boolean visible) {
        notification.setText(text);
        notification.setFont(new Font(null, Font.BOLD, 13));
        notification.setVisible(visible);
        notification.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setFormBody() {
        setChooseActionPanel();
        setTelephone();
        dataPanel.setVisible(true);
        mainPanel.add(dataPanel, BorderLayout.NORTH);
        setMessagePanel();
    }

    private void setLowerPanel() {
        ArrayList<JButton> buttons = new ArrayList<>(Arrays.asList(orderButton, orderHistoryButton, logOutButton));
        for (JButton button : buttons) {
            button.setSize(40, 10);
            button.setPreferredSize(new Dimension(120, 25));
            buttonPanel.add(button);
            button.addActionListener(this);
        }
        setNotification("Placeholder", false);
        lowerPanel.add(notification, BorderLayout.NORTH);
        lowerPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new Thread(() -> {
            String selectedAction = senderOrReceiverChoices.getSelectedItem();
            String providedTelephone = telephone.getText();
            String buttonPressed = e.getActionCommand();
            if (!buttonPressed.equals(logOutButton.getText()) && !buttonPressed.equals(orderHistoryButton.getText())) {
                if (!ClientManager.validateTelephone(providedTelephone)) {
                    new ReceivedMessageForm("App", "The provided phone number is not registered in the system.");
                    return;
                }
            }
            switch (buttonPressed) {
                case "Send" -> processSendRequest(selectedAction, providedTelephone);
                case "Receive" -> processReceiveRequest(providedTelephone);
                case "Order history" -> processHistoryRequest();
                case "Sign out" -> {
                    this.dispose();
                    ClientManager.openLoginForm();
                }

            }
        }).start();
    }

    private void processSendRequest(String selectedAction, String providedTelephone) {
        if (selectedAction.equals(senderOrReceiverText.get(0))) {
            String parsedData = Parsers.parseMessageToServer(CommunicationCode.NewOrder, new ArrayList<>(Arrays.asList(currentUserData.get(9), providedTelephone)));
            ClientManager.sendToServer(parsedData);
            ClientManager.processServerResponse();
            setNotification(orderSent, true);
        } else if (selectedAction.equals(senderOrReceiverText.get(2))) {
            String message = textArea.getText();
            if (message.isEmpty()) return;
            String parsedData = Parsers.parseMessageToServer(CommunicationCode.NewMessage, new ArrayList<>(Arrays.asList(currentUserData.get(9), providedTelephone, message)));
            ClientManager.sendToServer(parsedData);
            ClientManager.processServerResponse();
            setNotification(messageSent, true);
        }
    }

    private void processReceiveRequest(String providedTelephone) {
        String parsedData = Parsers.parseMessageToServer(CommunicationCode.NewOrder, new ArrayList<>(Arrays.asList(providedTelephone, currentUserData.get(9))));
        ClientManager.sendToServer(parsedData);
        ClientManager.processServerResponse();
        setNotification(orderSent, true);
    }

    private void processHistoryRequest() {
        String parsedData = Parsers.parseMessageToServer(CommunicationCode.HistoryRequest, new ArrayList<>(Arrays.asList(currentUserData.get(9))));
        ClientManager.sendToServer(parsedData);
        ClientManager.processServerResponse();
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
        this.dispose();
        System.out.println("Window closed");
    }

    @Override
    public void windowIconified(WindowEvent e) {
        System.out.println("Minimized");
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    private void colorFieldInvalid(JTextField field) {
        field.setBackground(Color.ORANGE);
    }

    private void showPanels(ArrayList<JPanel> panels) {
        panels.forEach(panel -> panel.setVisible(true));
    }

    private void hidePanels(ArrayList<JPanel> panels) {
        panels.forEach(panel -> panel.setVisible(false));
    }

}
