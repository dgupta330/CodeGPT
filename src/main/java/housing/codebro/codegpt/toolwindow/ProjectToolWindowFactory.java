package housing.codebro.codegpt.toolwindow;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentManagerEvent;
import com.intellij.ui.content.ContentManagerListener;
import com.intellij.ui.jcef.JBCefBrowser;
import housing.codebro.codegpt.client.ClientFactory;
import housing.codebro.codegpt.account.AccountDetailsState;
import housing.codebro.codegpt.conversations.ConversationsState;
import housing.codebro.codegpt.toolwindow.chat.ChatGptToolWindow;
import housing.codebro.codegpt.toolwindow.conversations.ConversationsToolWindow;
import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;

public class ProjectToolWindowFactory implements ToolWindowFactory, DumbAware {

  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    var chatToolWindow = new ChatGptToolWindow(project);
    var conversationsToolWindow = new ConversationsToolWindow(project);
    var toolWindowService = project.getService(ToolWindowService.class);
    toolWindowService.setChatToolWindow(chatToolWindow);

    var contentManagerService = project.getService(ContentManagerService.class);
    addContent(toolWindow, chatToolWindow.getContent(), "Chat");
    addContent(toolWindow, conversationsToolWindow.getContent(), "Conversation History");
    addContent(toolWindow, new JBCefBrowser("https://chat.openai.com/chat").getComponent(), "Browser");
    toolWindow.addContentManagerListener(new ContentManagerListener() {
      public void selectionChanged(@NotNull ContentManagerEvent event) {
        var content = event.getContent();
        if ("Conversation History".equals(content.getTabName()) && content.isSelected()) {
          conversationsToolWindow.refresh();
        } else if ("Chat".equals(content.getTabName()) && content.isSelected()) {
          ClientFactory.getBillingClient()
              .getCreditUsageAsync(creditUsage -> {
                var accountDetails = AccountDetailsState.getInstance();
                accountDetails.totalAmountGranted = creditUsage.getTotalGranted();
                accountDetails.totalAmountUsed = creditUsage.getTotalUsed();
              });
        }
      }
    });

    if (contentManagerService.isChatTabSelected(toolWindow.getContentManager())) {
      var conversation = ConversationsState.getCurrentConversation();
      if (conversation == null) {
        chatToolWindow.displayLandingView();
      } else {
        chatToolWindow.displayConversation(conversation);
      }
    }
  }

  public void addContent(ToolWindow toolWindow, JComponent panel, String displayName) {
    var contentManager = toolWindow.getContentManager();
    var content = contentManager.getFactory().createContent(panel, displayName, false);
    contentManager.addContent(content);
  }
}
