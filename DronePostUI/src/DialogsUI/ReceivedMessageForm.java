package DialogsUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

public class ReceivedMessageForm extends JFrame implements WindowListener {
    /* deeply messages */
    private static final String appName = "Drone Post";

    public ReceivedMessageForm(String sender, String text) {
        super(appName);
        JLabel senderLabel = new JLabel("New message from " + sender);
        senderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JTextArea textArea = new JTextArea(3, 30);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(text);
        JScrollPane scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(senderLabel, BorderLayout.NORTH);
        add(scroll, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(this);
        pack();
        setResizable(false);
        setVisible(true);
        System.out.println(text);
    }

    public ReceivedMessageForm(String sender, ArrayList<String> orders) {
        super(appName);
        StringBuilder text = new StringBuilder();
        for (String order: orders){
            text.append(order).append("\n");
        }
        JLabel senderLabel = new JLabel("New message from " + sender);
        senderLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JTextArea textArea = new JTextArea(5, 50);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setText(text.toString());
        JScrollPane scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(senderLabel, BorderLayout.NORTH);
        add(scroll, BorderLayout.SOUTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(this);
        pack();
        setResizable(false);
        setVisible(true);
        System.out.println(text);
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
