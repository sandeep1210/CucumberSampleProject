Feature: Check google search functionality

  Scenario: Enter text in the google search and verify results are populted

  Given open the browser
  And user is on google search
  When user enter a text in the google search
  And hit enter
  Then results will be populated based on the search

