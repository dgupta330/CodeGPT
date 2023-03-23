package housing.codebro.codegpt.util;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBFont;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UI;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

public class SwingUtils {

  public static JTextPane createTextPane(String selectedText) {
    var textPane = new JTextPane();
    textPane.setText(selectedText);
    textPane.setEditable(false);
    textPane.setBackground(JBColor.PanelBackground);
    textPane.setBorder(JBUI.Borders.emptyLeft(4));
    return textPane;
  }

  public static JLabel createIconLabel(Icon icon, String text) {
    var iconLabel = new JLabel(icon);
    iconLabel.setText(text);
    iconLabel.setFont(JBFont.h4());
    iconLabel.setIconTextGap(8);
    iconLabel.setBorder(JBUI.Borders.emptyLeft(4));
    return iconLabel;
  }

  public static JButton createIconButton(Icon icon) {
    var button = new JButton(icon);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.setContentAreaFilled(false);
    button.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
    return button;
  }

  public static Box justifyLeft(Component component) {
    Box box = Box.createHorizontalBox();
    box.add(component);
    box.add(Box.createHorizontalGlue());
    return box;
  }

  public static void addShiftEnterInputMap(JTextArea textArea, Runnable onEnter) {
    var input = textArea.getInputMap();
    var enterStroke = KeyStroke.getKeyStroke("ENTER");
    var shiftEnterStroke = KeyStroke.getKeyStroke("shift ENTER");
    input.put(shiftEnterStroke, "insert-break");
    input.put(enterStroke, "text-submit");

    var actions = textArea.getActionMap();
    actions.put("text-submit", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        onEnter.run();
      }
    });
  }

  public static void setEqualLabelWidths(JPanel firstPanel, JPanel secondPanel) {
    var firstLabel = firstPanel.getComponents()[0];
    var secondLabel = secondPanel.getComponents()[0];
    if (firstLabel instanceof JLabel && secondLabel instanceof JLabel) {
      firstLabel.setPreferredSize(secondLabel.getPreferredSize());
    }
  }

  public static JPanel createPanel(JComponent component, String label, boolean resizeX) {
    return UI.PanelFactory.panel(component)
        .withLabel(label)
        .resizeX(resizeX)
        .createPanel();
  }
}

