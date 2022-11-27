Feature: Home

  @home-feature
  Scenario: Home Screen Tabs
    Given I am on Home Screen
    Then I should switch tabs
    Then I should view settings

  @home-feature
  Scenario: Home Category list content
    Given I am on Home Screen
    Then I should see category list
    Then check category selection

  @home-feature
  Scenario: Home search content
    Given I am on Home Screen
    Then I should see content list
    Then I should see search content

