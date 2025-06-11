@echo off
echo Starting SonarQube analysis for CT.01 project...

echo Step 1: Starting SonarQube server (if not running)...
docker-compose -f sonar-docker-compose.yml up -d

echo Waiting for SonarQube to be ready...
timeout /t 30

echo Step 2: Running Maven clean and compile...
mvn clean compile

echo Step 3: Running SonarQube analysis...
mvn sonar:sonar ^
  -Dsonar.projectKey=ct01-crypto-tracker ^
  -Dsonar.projectName="CT.01 Cryptocurrency Tracker" ^
  -Dsonar.host.url=http://localhost:9000 ^
  -Dsonar.token=squ_your_token_here

echo Analysis complete! Open http://localhost:9000 to view results
pause 