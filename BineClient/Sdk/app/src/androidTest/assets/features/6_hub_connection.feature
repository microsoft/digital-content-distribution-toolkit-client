Feature: HomeHub

  @homeHub-feature
  Scenario: Home connect hub
    Given open Home Screen
    Then change ip to hub ip
    Then check is hub connected

  @homeHub-feature
  Scenario: Home check is hub content is visible
    Given open Home Screen
    Then check is hub content displaying
    Then check content is download is working
