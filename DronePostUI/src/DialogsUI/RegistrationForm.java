package DialogsUI;

import Enums.CommunicationCode;
import Enums.SubscriptionCode;
import Services.*;
import org.javatuples.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;

public class RegistrationForm extends JFrame implements ActionListener, MouseListener, WindowListener {
    private final ArrayList<String> columnLabel = new ArrayList<>(Arrays.asList("Personal ID", "First name", "Last name", "Date of birth (YYYY-mm-dd)", "Street", "Building", "City", "Zip Code", "E-mail", "Phone number", "Choose your plan"));
    private final ArrayList<String> subscriptionPlan = new ArrayList<>(Arrays.asList("-", "50 shipments for $99", "150 shipments for $179"));
    private static final ArrayList<String> formButtons = new ArrayList<>(Arrays.asList("Back to Login Form", "Clear Form", "Pay & Register"));
    private final ArrayList<Integer> keyEvents = new ArrayList<>(Arrays.asList(KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE));
    private final String registrationLabel = "Client Registration Form";
    private final String successMessageText = "Information saved successfully.";
    private final String clientAlreadyExistsMessageText = "Your phone number is already in the database. Please update client details.";
    private final String savingErrorMessageText = "Error occurred when saving the client information. Please try again.";
    private final String dataErrorMessageText = "Marked fields are empty or contain invalid information.";

    private final JTextField addLabel = new JTextField(registrationLabel);
    private final JTextField personalId = new JTextField(10);
    private final JTextField firstName = new JTextField(15);
    private final JTextField lastName = new JTextField(15);
    private final JTextField dateOfBirth = new JTextField(10);
    private final JTextField street = new JTextField(20);
    private final JTextField building = new JTextField(5);
    private final JTextField city = new JTextField(15);
    private final JTextField zipCode = new JTextField(10);
    private final JTextField email = new JTextField(15);
    private final JTextField telephone = new JTextField(9);
    private final Choice programChoices = new Choice();
    private final JTextField clientNotification = new JTextField("Placeholder");
    private final ArrayList<JTextField> fields = new ArrayList<>(
            Arrays.asList(personalId, firstName, lastName, dateOfBirth, street, building, city, zipCode, email, telephone));

    protected final JButton backToLoginFormButton = new JButton(formButtons.get(0));
    protected final JButton clearButton = new JButton(formButtons.get(1));
    protected final JButton registerButton = new JButton(formButtons.get(2));

    final JPanel titlePanel = new JPanel(new GridLayout(1, 1));
    final JPanel registrationPanel = new JPanel(new GridLayout(0, 2));
    final JPanel regFormButtons = new JPanel(new FlowLayout());

    public RegistrationForm(String appName) {
        super(appName);
        addWindowListener(this);
        setLayout((new BorderLayout(20, 15)));
        setFormLabel();

        //Add Personal Data Form
        setFormBody();

        //Add registration form panel buttons
        setButtonsPanel(regFormButtons, new ArrayList<>(Arrays.asList(backToLoginFormButton, clearButton, registerButton)));

        //Build the Frame
        add(titlePanel, BorderLayout.NORTH);
        add(registrationPanel, BorderLayout.CENTER);
        add(regFormButtons, BorderLayout.SOUTH);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        this.setVisible(true);
        titlePanel.setVisible(true);
        registrationPanel.setVisible(true);
        regFormButtons.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String buttonClicked = e.getActionCommand();
        switch (buttonClicked) {
            case "Back to Login Form" -> processBackToLoginFormRequest();
            case "Clear Form" -> processCleanFieldsRequest();
            case "Pay & Register" -> processPayRegisterRequest();
        }
    }

    private void processBackToLoginFormRequest() {
        this.dispose();
        ClientManager.openLoginForm();
    }

    private void processCleanFieldsRequest() {
        fields.forEach(item -> item.setText(""));
        telephone.setText("+972");
        programChoices.select(0);
    }

    private void processPayRegisterRequest() {
        try {
            ArrayList<String> clientData = retrieveRegData();
            if (clientData == null)
                throw new NullPointerException("No client data received");
            String parsedData;
            if (validateInput())
                parsedData = Parsers.parseMessageToServer(CommunicationCode.RegisterClient, clientData);
            else throw new IllegalArgumentException(dataErrorMessageText);
            ClientManager.sendToServer(parsedData);
            String response = ClientManager.receiveFromServer();
            Pair<CommunicationCode, ArrayList<String>> parsedMessage = Parsers.parseMessageFromServer(response);
            switch (parsedMessage.getValue0()) {
                case ClientRegistered -> {
                    new Services().saveClientDetailsToFile(clientData);
                    showSuccessNotification();
                    Thread.sleep(3000);
                    ClientManager.openOrderForm(this, clientData);
                }
                case ClientAlreadyExists -> showClientAlreadyExistsNotification();
                case ClientSavingError -> showSavingErrorNotification();
            }
        } catch (IllegalArgumentException iae) {
            showDataErrorNotification();
            iae.printStackTrace();
        } catch (InterruptedException io) {
            io.printStackTrace();
        }
    }


    private Boolean validateInput() {
        boolean valid = true;
        String date = dateOfBirth.getText();
        try {
            if (LocalDate.parse(date).isAfter(LocalDate.now())) {
                valid = false;
                colorFieldInvalid(dateOfBirth);
            }
        } catch (DateTimeParseException dtp) {
            showDataErrorNotification();
        }
        String e_mail = email.getText();
        if (e_mail.split("@").length != 2
                || !email.getText().split("@")[1].contains(".")) {
            valid = false;
            colorFieldInvalid(email);
        }
        if (telephone.getText().length() < 12) {
            valid = false;
            colorFieldInvalid(telephone);
        }
        String plan = programChoices.getSelectedItem();
        if (plan.equals(SubscriptionCode.NO_SUBSCRIPTION.getDescription())) {
            valid = false;
            programChoices.setBackground(Color.ORANGE);
        }
        fields.forEach(field -> {
            if (field.getText().equals(""))
                colorFieldInvalid(field);
        });
        return valid;
    }

    private ArrayList<String> retrieveRegData() {
        ArrayList<String> data = new ArrayList<>();
        fields.forEach(field -> data.add(field.getText()));
        //Add subscription plan
        data.add(SubscriptionCode.getCode(programChoices.getSelectedItem()));

        return data.size() != fields.size() + 1 ? null : data;
    }

    private void setFormLabel() {
        addLabel.setEditable(false);
        addLabel.setBackground(null);
        addLabel.setBorder(null);
        addLabel.setFont(new Font(null, Font.BOLD, 14));
        addLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(addLabel);
    }

    private void setFormBody() {
        setFields();
        subscriptionPlan.forEach(programChoices::add);
        for (int i = 0; i < fields.size(); ++i) {
            registrationPanel.add(new Label(columnLabel.get(i)));
            registrationPanel.add(fields.get(i));
        }
        registrationPanel.add(new Label(columnLabel.get(columnLabel.size() - 1)));
        registrationPanel.add(programChoices);
        setClientNotification();
        registrationPanel.add(clientNotification);
    }

    private void setFields() {
        personalId.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()))
                    e.setKeyChar(Character.MIN_VALUE);
                if (personalId.getText().length() == 10)
                    e.setKeyChar(Character.MIN_VALUE);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (personalId.getBackground() == Color.ORANGE)
                    colorFieldValid(personalId, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        firstName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (firstName.getText().length() == 30)
                    e.setKeyChar(Character.MIN_VALUE);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (firstName.getBackground() == Color.ORANGE)
                    colorFieldValid(firstName, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        lastName.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (lastName.getText().length() == 30)
                    e.setKeyChar(Character.MIN_VALUE);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (lastName.getBackground() == Color.ORANGE)
                    colorFieldValid(lastName, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        dateOfBirth.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                int length = dateOfBirth.getText().length();
                if (Character.isDigit(e.getKeyChar()) && length < 10) {
                    if (length == 4 || length == 7)
                        dateOfBirth.setText(dateOfBirth.getText() + "-");
                } else e.setKeyChar(Character.MIN_VALUE);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (dateOfBirth.getBackground() == Color.ORANGE)
                    colorFieldValid(dateOfBirth, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        street.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (street.getText().length() == 40)
                    e.setKeyChar(Character.MIN_VALUE);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (street.getBackground() == Color.ORANGE)
                    colorFieldValid(street, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        building.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (building.getText().length() == 5)
                    e.setKeyChar(Character.MIN_VALUE);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (building.getBackground() == Color.ORANGE)
                    colorFieldValid(building, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        city.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (city.getText().length() == 30)
                    e.setKeyChar(Character.MIN_VALUE);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (city.getBackground() == Color.ORANGE)
                    colorFieldValid(city, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        zipCode.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (zipCode.getText().length() == 10)
                    e.setKeyChar(Character.MIN_VALUE);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (zipCode.getBackground() == Color.ORANGE)
                    colorFieldValid(zipCode, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        email.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                String text = email.getText();
                if (text.contains("@") && e.getKeyChar() == '@')
                    e.setKeyChar(Character.MIN_VALUE);
                if (text.length() == 45) {
                    e.setKeyChar(Character.MIN_VALUE);
                    if (!email.getText().contains("@") || !email.getText().contains("."))
                        colorFieldInvalid(email);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (email.getBackground() == Color.ORANGE)
                    colorFieldValid(email, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
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
                if (telephone.getBackground() == Color.ORANGE)
                    colorFieldValid(telephone, e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        programChoices.addItemListener(e -> {
            if (programChoices.getBackground() == Color.ORANGE
                    && e.getItem() != subscriptionPlan.get(0))
                programChoices.setBackground(Color.WHITE);
        });
    }

    private void setButtonsPanel(JPanel panel, ArrayList<JButton> buttons) {
        buttons.forEach(item -> {
            panel.add(item);
            item.addActionListener(this);
        });
    }

    private void setClientNotification() {
        clientNotification.setBackground(null);
        clientNotification.setBorder(null);
        clientNotification.setForeground(Color.RED);
        hideNotification();
    }

    private void colorFieldInvalid(JTextField field) {
        field.setBackground(Color.ORANGE);
    }

    private void colorFieldValid(JTextField field, KeyEvent e) {
        ArrayList<Integer> keyEvents = this.keyEvents;
        if (field == email) {
            keyEvents.add(KeyEvent.VK_AT);
            keyEvents.add(KeyEvent.VK_PERIOD);
        }
        if (Character.isDigit(e.getKeyChar())
                || Character.isAlphabetic(e.getKeyChar())
                || keyEvents.contains(e.getKeyCode()))
            field.setBackground(Color.WHITE);
    }

    private void showClientAlreadyExistsNotification() {
        clientNotification.setText(clientAlreadyExistsMessageText);
        clientNotification.setForeground(Color.RED);
        clientNotification.setVisible(true);
    }

    private void showDataErrorNotification() {
        clientNotification.setText(dataErrorMessageText);
        clientNotification.setForeground(Color.RED);
        clientNotification.setVisible(true);
    }

    private void showSavingErrorNotification() {
        clientNotification.setText(savingErrorMessageText);
        clientNotification.setForeground(Color.RED);
        clientNotification.setVisible(true);
    }

    private void showSuccessNotification() {
        clientNotification.setText(successMessageText);
        clientNotification.setForeground(Color.GREEN);
        clientNotification.setVisible(true);
    }

    private void hideNotification() {
        clientNotification.setVisible(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

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
    }

    @Override
    public void windowIconified(WindowEvent e) {
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