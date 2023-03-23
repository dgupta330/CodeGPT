package housing.codebro.codegpt.toolwindow.chat;

import static java.lang.String.format;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import housing.codebro.codegpt.account.AccountDetailsState;
import housing.codebro.codegpt.conversations.Conversation;
import housing.codebro.codegpt.conversations.ConversationsState;
import housing.codebro.codegpt.settings.SettingsConfigurable;
import housing.codebro.codegpt.settings.SettingsState;
import housing.codebro.codegpt.toolwindow.ToolWindowService;
import housing.codebro.codegpt.toolwindow.chat.actions.CreateNewConversationAction;
import housing.codebro.codegpt.toolwindow.chat.actions.OpenInEditorAction;
import housing.codebro.codegpt.toolwindow.chat.actions.UsageToolbarLabelAction;
import housing.codebro.codegpt.toolwindow.components.GenerateButton;
import housing.codebro.codegpt.toolwindow.components.LandingView;
import housing.codebro.codegpt.toolwindow.components.ScrollPane;
import housing.codebro.codegpt.toolwindow.components.SyntaxTextArea;
import housing.codebro.codegpt.toolwindow.components.TextArea;
import housing.codebro.codegpt.util.SwingUtils;
import icons.Icons;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.jetbrains.annotations.NotNull;

public class ChatGptToolWindow {

  private static final List<SyntaxTextArea> textAreas = new ArrayList<>();
  private final Project project;
  private JPanel chatGptToolWindowContent;
  private ScrollPane scrollPane;
  private ScrollablePanel scrollablePanel;
  private JTextArea textArea;
  private JScrollPane textAreaScrollPane;
  private GenerateButton generateButton;
  private boolean isLandingViewVisible;

  public ChatGptToolWindow(@NotNull Project project) {
    this.project = project;
  }

  public JPanel getContent() {
    SimpleToolWindowPanel panel = new SimpleToolWindowPanel(true);
    panel.setContent(chatGptToolWindowContent);

    var actionGroup = new DefaultActionGroup("TOOLBAR_ACTION_GROUP", false);
    actionGroup.add(new CreateNewConversationAction());
    actionGroup.add(new OpenInEditorAction());
    actionGroup.addSeparator();
    actionGroup.add(new UsageToolbarLabelAction());

    // TODO: Data usage not enabled in stream mode https://community.openai.com/t/usage-info-in-api-responses/18862/11
    // actionGroup.add(new TokenToolbarLabelAction());

    ActionToolbar actionToolbar = ActionManager.getInstance()
        .createActionToolbar("NAVIGATION_BAR_TOOLBAR", actionGroup, false);
    panel.setToolbar(actionToolbar.getComponent());
    return panel;
  }

  public void displayUserMessage(String userMessage) {
    addIconLabel(AllIcons.General.User, AccountDetailsState.getInstance().accountName);

    scrollablePanel.add(SwingUtils.createTextPane(userMessage));
    scrollablePanel.revalidate();
    scrollablePanel.repaint();
  }

  public void displayLandingView() {
    if (!isLandingViewVisible) {
      SwingUtilities.invokeLater(() -> {
        clearWindow();
        isLandingViewVisible = true;

        addSpacing(16);
        var landingView = new LandingView();
        scrollablePanel.add(landingView.createImageIconPanel());
        addSpacing(16);
        landingView.getQuestionPanels().forEach(panel -> {
          scrollablePanel.add(panel);
          addSpacing(16);
        });
        scrollablePanel.revalidate();
        scrollablePanel.repaint();
      });
    }
  }

  public void displayConversation(Conversation conversation) {
    clearWindow();
    conversation.getMessages().forEach(message -> {
      displayUserMessage(message.getPrompt());
      addIconLabel(Icons.DefaultImageIcon, "ChatGPT");
      var textArea = new SyntaxTextArea(true, false, SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
      textArea.setText(message.getResponse());
      textArea.displayCopyButton();
      scrollablePanel.add(textArea);
      textAreas.add(textArea);
    });
    scrollToBottom();
    scrollablePanel.revalidate();
    scrollablePanel.repaint();
  }

  public void sendMessage(String prompt, Project project) {
    sendMessage(prompt, project, false);
  }

  public void sendMessage(String prompt, Project project, boolean isRetry) {
    if (!isRetry) {
      addIconLabel(Icons.DefaultImageIcon, "ChatGPT");
    }

    var settings = SettingsState.getInstance();
    if (settings.apiKey.isEmpty()) {
      notifyMissingCredential(project, "API key not provided.");
    } else {
      SyntaxTextArea textArea;
      if (isRetry) {
        textArea = textAreas.get(textAreas.size() - 1);
        textArea.clear();
      } else {
        textArea = new SyntaxTextArea(true, true, SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
        addTextArea(textArea);
      }
      project.getService(ToolWindowService.class)
          .startRequest(prompt, textArea, project, isRetry);
    }
  }

  public void clearWindow() {
    isLandingViewVisible = false;
    generateButton.setVisible(false);
    textAreas.clear();
    scrollablePanel.removeAll();
  }

  public void addSpacing(int height) {
    scrollablePanel.add(Box.createVerticalStrut(height));
  }

  public void addIconLabel(Icon icon, String text) {
    addSpacing(8);
    scrollablePanel.add(SwingUtils.justifyLeft(SwingUtils.createIconLabel(icon, text)));
    addSpacing(8);
  }

  public void notifyMissingCredential(Project project, String text) {
    var label = new JLabel(format("<html>%s <font color='#589df6'><u>Open Settings</u></font> to set one.</html>", text));
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfigurable.class);
      }
    });
    scrollablePanel.add(SwingUtils.justifyLeft(label));
  }

  public void displayGenerateButton(Runnable onClick) {
    generateButton.setVisible(true);
    generateButton.setMode(GenerateButton.Mode.STOP, onClick);
  }

  public void stopGenerating(String prompt, SyntaxTextArea textArea, Project project) {
    generateButton.setMode(GenerateButton.Mode.REFRESH, () -> {
      sendMessage(prompt, project, true);
      scrollToBottom();
    });
    textArea.displayCopyButton();
    textArea.getCaret().setVisible(false);
    scrollToBottom();
  }

  public void scrollToBottom() {
    scrollPane.scrollToBottom();
  }

  public void addTextArea(SyntaxTextArea textArea) {
    scrollablePanel.add(textArea);
    textAreas.add(textArea);
  }

  public void changeStyle() {
    for (var textArea : textAreas) {
      textArea.changeStyleViaThemeXml();
    }
  }

  private void handleSubmit() {
    var searchText = textArea.getText();
    if (isLandingViewVisible || ConversationsState.getCurrentConversation() == null) {
      clearWindow();
    }
    displayUserMessage(searchText);
    sendMessage(searchText, project);
    textArea.setText("");
    scrollToBottom();
    scrollablePanel.revalidate();
    scrollablePanel.repaint();
  }

  private void createUIComponents() {
    textAreaScrollPane = new JBScrollPane() {
      public JScrollBar createVerticalScrollBar() {
        JScrollBar verticalScrollBar = new JScrollPane.ScrollBar(1);
        verticalScrollBar.setPreferredSize(new Dimension(0, 0));
        return verticalScrollBar;
      }
    };
    textAreaScrollPane.setBorder(null);
    textAreaScrollPane.setViewportBorder(null);
    textAreaScrollPane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, JBColor.border()),
        BorderFactory.createEmptyBorder(0, 5, 0, 10)));
    textArea = new TextArea(this::handleSubmit, textAreaScrollPane);
    textAreaScrollPane.setViewportView(textArea);

    scrollablePanel = new ScrollablePanel();
    scrollablePanel.setLayout(new BoxLayout(scrollablePanel, BoxLayout.Y_AXIS));
    scrollPane = new ScrollPane(scrollablePanel);

    generateButton = new GenerateButton();
  }
}
