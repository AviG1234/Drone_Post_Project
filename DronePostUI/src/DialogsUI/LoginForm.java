package DialogsUI;

import Enums.CommunicationCode;
import Services.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

public class LoginForm extends JFrame implements ActionListener, WindowListener {
    /* log in form at the beginning of program running, asking user if he usd the program from current machine and if not to register.  */
    private final String mainLabel = "Welcome to Drone Post app";
    private final String welcomeLabel = "Please, authenticate using phone number";
    private final String errorMessage = "User not found";
    private static final ArrayList<String> formButtons = new ArrayList<>(Arrays.asList("Register", "Authenticate"));

    private final JTextField titleLabel = new JTextField(mainLabel);
    private final JTextField welcomeMessage = new JTextField(welcomeLabel);
    private final JTextField clientNotification = new JTextField(20);
    private final JTextField telephone = new JTextField(15);

    private final JPanel titlePanel = new JPanel(new GridLayout(0, 1));
    private final JPanel mainPanel = new JPanel(new GridLayout(0, 1));
    private final JPanel loginPanel = new JPanel(new GridLayout(0, 2));

    private final JButton registerButton = new JButton(formButtons.get(0));
    private final JButton authButton = new JButton(formButtons.get(1));
    private final JPanel loginFormButtons = new JPanel(new FlowLayout());

    public LoginForm(String appName) {
        super(appName);
        addWindowListener(this);
        setLayout((new BorderLayout(20, 15)));

        setFormLabel();
        setWelcomeLabel();
        setFormBody();
        setButtonsPanel(loginFormButtons, new ArrayList<>(Arrays.asList(registerButton, authButton)));

        add(titlePanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(loginFormButtons, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setVisible(true);
        setResizable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String buttonClicked = e.getActionCommand();
        switch (buttonClicked) {
            case "Register" -> {
                System.out.println("Register");
                ClientManager.openRegistrationForm(this);
            }
            case "Authenticate" -> {
                System.out.println("Authenticate");
                ArrayList<String> currentUserData = authenticate();
                if (currentUserData != null)
                    ClientManager.openOrderForm(this, currentUserData);
            }
        }
    }

    private void setFormLabel() {
        titleLabel.setEditable(false);
        titleLabel.setBackground(null);
        titleLabel.setBorder(null);
        titleLabel.setFont(new Font(null, Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.NORTH);
    }

    private void setWelcomeLabel() {
        welcomeMessage.setEditable(false);
        welcomeMessage.setBackground(null);
        welcomeMessage.setBorder(null);
        welcomeMessage.setFont(new Font(null, Font.PLAIN, 11));
        welcomeMessage.setHorizontalAlignment(SwingConstants.LEFT);
        mainPanel.add(welcomeMessage, BorderLayout.NORTH);
    }

    private void setFormBody() {
        setFields();
        String columnLabel = "Phone Number";
        loginPanel.add(new Label(columnLabel));
        loginPanel.add(telephone);
        mainPanel.add(loginPanel, BorderLayout.CENTER);
        setClientNotification(Color.GREEN, "Placeholder", false);
        mainPanel.add(clientNotification, BorderLayout.SOUTH);
    }

    private void setFields() {
        telephone.setText("+972");
        telephone.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                int length = telephone.getText().length();
                if ((length == 4 && e.getKeyChar() == '0') || !Character.isDigit(e.getKeyChar()) || (length == 13))
                    e.setKeyChar(Character.MIN_VALUE);
                if (clientNotification.isVisible()) hideNotification();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if ((e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                        && clientNotification.isVisible())
                    hideNotification();
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    private void setClientNotification(Color color, String message, Boolean show) {
        clientNotification.setBackground(null);
        clientNotification.setBorder(null);
        clientNotification.setText(message);
        clientNotification.setForeground(color);
        clientNotification.setHorizontalAlignment(SwingConstants.CENTER);
        if (show)
            showNotification();
        else hideNotification();
    }

    private void showNotification() {
        clientNotification.setVisible(true);
    }
    private void hideNotification() {
        clientNotification.setVisible(false);
    }
    private void setButtonsPanel(JPanel panel, ArrayList<JButton> buttons) {
        buttons.forEach(button -> {
            panel.add(button);
            button.setPreferredSize(new Dimension(120, 25));
            button.addActionListener(this);
        });
    }

    private ArrayList<String> authenticate() {
        String typedPhone = telephone.getText();
        ArrayList<String> currentUserData;
        if (!ClientManager.validateTelephone(typedPhone)) {
            setClientNotification(Color.RED, errorMessage, true);
            return null;
        } else {
            ClientManager.sendToServer(Parsers.parseMessageToServer(CommunicationCode.GetClientDetails, new ArrayList<>(Arrays.asList(typedPhone))));
            ClientManager.processServerResponse();
            currentUserData = new Services().getClientDetailsFromFile();
        }
        return currentUserData;
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
}


