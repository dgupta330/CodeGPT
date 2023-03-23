package housing.codebro.codegpt.settings.advanced;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.net.Proxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "housing.codebro.codegpt.settings.advanced.AdvancedSettingsState",
    storages = @Storage("CodeGPTAdvancedSettings.xml")
)
public class AdvancedSettingsState implements PersistentStateComponent<AdvancedSettingsState> {

  public String proxyHost = "";
  public int proxyPort;
  public Proxy.Type proxyType = Proxy.Type.SOCKS;
  public boolean isProxyAuthSelected;
  public String proxyUsername;
  public String proxyPassword;

  public static AdvancedSettingsState getInstance() {
    return ApplicationManager.getApplication().getService(AdvancedSettingsState.class);
  }

  @Nullable
  @Override
  public AdvancedSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull AdvancedSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
