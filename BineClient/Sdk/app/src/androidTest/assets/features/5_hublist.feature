Feature: Hub

  @hub-feature
  Scenario: Hub list Screen
    Given I am seen the content list
    And I want to find a movie booth
    Then I should see hubs list

  @hub-feature
  Scenario: Hub details Screen - Navigate
    Given I see list of hubs near me
    And I want to see a hub details
    Then I should see hubs detailed info
    And Allowed to navigate to hub

  @hub-feature
  Scenario: Hub details Screen - Call
    Given I see list of hubs near me
    And I want to see a hub details
    Then I should see hubs detailed info
    And Allowed to call the hub contact


