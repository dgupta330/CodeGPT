package housing.codebro.codegpt.toolwindow.conversations.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import housing.codebro.codegpt.conversations.Conversation;
import housing.codebro.codegpt.conversations.ConversationsState;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public class MoveDownAction extends MoveAction {

  public MoveDownAction(Runnable onRefresh) {
    super("Move Down", "Move Down", AllIcons.Actions.MoveDown, onRefresh);
  }

  @Override
  public void update(@NotNull AnActionEvent event) {
    super.update(event);
  }

  @Override
  protected Optional<Conversation> getConversation() {
    return ConversationsState.getInstance().getNextConversation();
  }
}
