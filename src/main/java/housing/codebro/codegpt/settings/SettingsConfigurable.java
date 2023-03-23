package housing.codebro.codegpt.settings;

import com.intellij.openapi.options.Configurable;
import housing.codebro.codegpt.conversations.ConversationsState;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class SettingsConfigurable implements Configurable {

  private SettingsComponent settingsComponent;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "CodeGPT: Settings";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return settingsComponent.getPreferredFocusedComponent();
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    var settings = SettingsState.getInstance();
    settingsComponent = new SettingsComponent(settings);
    return settingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    var settings = SettingsState.getInstance();
    return !settingsComponent.getApiKey().equals(settings.apiKey) || isModelChanged(settings) || isClientChanged(settings);
  }

  @Override
  public void apply() {
    var settings = SettingsState.getInstance();

    if (isClientChanged(settings) || isModelChanged(settings)) {
      ConversationsState.getInstance().setCurrentConversation(null);
    }

    settings.apiKey = settingsComponent.getApiKey();
    settings.chatCompletionBaseModel = settingsComponent.getChatCompletionBaseModel().getCode();
    settings.isChatCompletionOptionSelected = settingsComponent.isChatCompletionOptionSelected();
    settings.isTextCompletionOptionSelected = settingsComponent.isTextCompletionOptionSelected();
    settings.textCompletionBaseModel = settingsComponent.getTextCompletionBaseModel().getCode();
  }

  @Override
  public void reset() {
    var settings = SettingsState.getInstance();
    settingsComponent.setUseChatCompletionSelected(settings.isChatCompletionOptionSelected);
    settingsComponent.setUseTextCompletionSelected(settings.isTextCompletionOptionSelected);
    settingsComponent.setApiKey(settings.apiKey);
    settingsComponent.setChatCompletionBaseModel(settings.chatCompletionBaseModel);
    settingsComponent.setTextCompletionBaseModel(settings.textCompletionBaseModel);
  }

  @Override
  public void disposeUIResources() {
    settingsComponent = null;
  }

  private boolean isClientChanged(SettingsState settings) {
    return settingsComponent.isChatCompletionOptionSelected() != settings.isChatCompletionOptionSelected ||
        settingsComponent.isTextCompletionOptionSelected() != settings.isTextCompletionOptionSelected;
  }

  private boolean isModelChanged(SettingsState settings) {
    return !settingsComponent.getChatCompletionBaseModel().getCode().equals(settings.chatCompletionBaseModel) ||
        !settingsComponent.getTextCompletionBaseModel().getCode().equals(settings.textCompletionBaseModel);
  }
}
