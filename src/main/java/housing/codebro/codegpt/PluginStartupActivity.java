package housing.codebro.codegpt;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import housing.codebro.codegpt.account.AccountDetailsState;
import housing.codebro.codegpt.action.ActionsUtil;
import housing.codebro.codegpt.client.ClientFactory;
import housing.codebro.codegpt.settings.configuration.ConfigurationState;
import org.jetbrains.annotations.NotNull;


/**
 * er.deepak.kr *
 * 23/03/2023 *
 */

public class PluginStartupActivity implements StartupActivity {

  @Override
  public void runActivity(@NotNull Project project) {
    ActionsUtil.refreshActions(ConfigurationState.getInstance().tableData);
    var accountDetails = AccountDetailsState.getInstance();
    if ("User".equals(accountDetails.accountName) || accountDetails.accountName == null) {
      ClientFactory.getBillingClient()
          .getSubscriptionAsync(subscription ->
              accountDetails.accountName = subscription.getAccountName());
    }
  }
}
