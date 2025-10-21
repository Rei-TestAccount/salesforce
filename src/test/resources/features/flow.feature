Feature: End-to-end data and metadata flow

  Background:
    Given Salesforce auth works with client credentials

  Scenario: Positive flow - create, export, branch, add phones, update Salesforce
    When I create 10 Accounts with required fields and rules
    And I export Accounts to CSV and commit it to main
    And I create a new branch from main and add Phone column in CSV
    And I push updated CSV to the new branch
    And I update Accounts in Salesforce with phones from the CSV
    Then all updates should be successful

  Scenario: Negative flow - invalid row is handled
    When I create 1 Accounts with required fields and rules
    And I export Accounts to CSV and commit it to main
    And I create a new branch from main and add Phone column in CSV with an invalid row
    And I push updated CSV to the new branch
    And I update Accounts in Salesforce with phones from the CSV handling errors
    Then invalid rows should be reported while valid rows update
