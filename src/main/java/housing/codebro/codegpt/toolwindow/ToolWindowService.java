package housing.codebro.codegpt.toolwindow;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.project.Project;
import housing.codebro.codegpt.client.ClientFactory;
import housing.codebro.codegpt.client.ClientRequestFactory;
import housing.codebro.codegpt.client.EventListener;
import housing.codebro.codegpt.conversations.ConversationsState;
import housing.codebro.codegpt.conversations.message.Message;
import housing.codebro.codegpt.settings.SettingsState;
import housing.codebro.codegpt.toolwindow.chat.ChatGptToolWindow;
import housing.codebro.codegpt.toolwindow.components.SyntaxTextArea;
import java.util.List;
import javax.swing.SwingWorker;

import okhttp3.sse.EventSource;
import org.jetbrains.annotations.NotNull;

public class ToolWindowService implements LafManagerListener {

  private ChatGptToolWindow chatToolWindow;

  @Override
  public void lookAndFeelChanged(@NotNull LafManager source) {
    chatToolWindow.changeStyle();
  }

  public void setChatToolWindow(ChatGptToolWindow chatToolWindow) {
    this.chatToolWindow = chatToolWindow;
  }

  public ChatGptToolWindow getChatToolWindow() {
    return chatToolWindow;
  }

  public void startRequest(String prompt, SyntaxTextArea textArea, Project project, boolean isRetry) {
    var conversation = ConversationsState.getInstance().getOrStartNew();
    var conversationMessage = new Message(prompt);

    new SwingWorker<Void, String>() {
      protected Void doInBackground() {
        var eventListener = new EventListener(
            conversationMessage,
            textArea::append,
            () -> chatToolWindow.stopGenerating(prompt, textArea, project),
            isRetry) {
          public void onMessage(String message) {
            publish(message);
          }
        };

        EventSource call;
        var settings = SettingsState.getInstance();
        var requestFactory = new ClientRequestFactory(prompt, conversation);
        if (settings.isChatCompletionOptionSelected) {
          call = ClientFactory.getChatCompletionClient().stream(
              requestFactory.buildChatCompletionRequest(settings), eventListener);
        } else {
          call = ClientFactory.getTextCompletionClient().stream(
              requestFactory.buildTextCompletionRequest(settings), eventListener);
        }
        chatToolWindow.displayGenerateButton(call::cancel);
        return null;
      }

      protected void process(List<String> chunks) {
        for (String text : chunks) {
          try {
            textArea.append(text);
            conversationMessage.setResponse(textArea.getText());
            chatToolWindow.scrollToBottom();
          } catch (Exception e) {
            textArea.append("Something went wrong. Please try again later.");
            throw new RuntimeException(e);
          }
        }
      }
    }.execute();
  }
}
