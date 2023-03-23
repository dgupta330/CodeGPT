package housing.codebro.codegpt.toolwindow.chat.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import housing.codebro.codegpt.conversations.ConversationsState;
import housing.codebro.codegpt.toolwindow.ToolWindowService;
import org.jetbrains.annotations.NotNull;

public class CreateNewConversationAction extends AnAction {

  public CreateNewConversationAction() {
    super("Create New Conversation", "Create new conversation", AllIcons.General.Add);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent event) {
    var project = event.getProject();
    if (project != null) {
      ConversationsState.getInstance().startConversation();
      project.getService(ToolWindowService.class)
          .getChatToolWindow()
          .displayLandingView();
    }
  }
}
