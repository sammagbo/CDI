#!/bin/bash

# Start the app in the background
echo "Starting Spring Boot app..."
mvn spring-boot:run > app.log 2>&1 &
APP_PID=$!

# Wait for the app to start (simple sleep)
echo "Waiting 20 seconds for app to initialize..."
sleep 20

# Function to check if app is responding
check_app() {
    curl -s http://localhost:8081/api/students > /dev/null
    return $?
}

if ! check_app; then
    echo "App not responding after 20s. Showing last 20 lines of log:"
    tail -n 20 app.log
    kill $APP_PID
    exit 1
fi

echo "App is running!"

# 1. Add Student
echo ">>> TEST: Add Student"
ADD_RES=$(curl -s -X POST http://localhost:8081/api/students \
    -H "Content-Type: application/json" \
    -d '{"id": "TEST001", "firstName": "Jean", "lastName": "Test", "studentClass": "2nde B"}')
echo "Response: $ADD_RES"

# 2. Scan (IN)
echo ">>> TEST: Scan IN"
SCAN_IN=$(curl -s -X POST http://localhost:8081/api/scan/TEST001)
echo "Response: $SCAN_IN"

# 3. Scan (OUT)
echo ">>> TEST: Scan OUT"
SCAN_OUT=$(curl -s -X POST http://localhost:8081/api/scan/TEST001)
echo "Response: $SCAN_OUT"

# 4. Get Logs
echo "Get Logs:"
curl -v http://localhost:8081/api/logs
echo -e "\n"

# 5. Update Student (PUT)
echo "Update Student:"
curl -X PUT http://localhost:8081/api/students/TEST001 \
     -H "Content-Type: application/json" \
     -d '{"firstName": "John", "lastName": "Updated", "studentClass": "T A", "present": true}'
echo -e "\n"

# 6. Delete Student (DELETE)
echo "Delete Student:"
curl -X DELETE -v http://localhost:8081/api/students/TEST001
echo -e "\n"

# 7. Check if Deleted (Should be 404 or empty from get)
echo "Verify Deletion:"
curl -v http://localhost:8081/api/scan/TEST001
echo -e "\n"

# Cleanup
echo "Stopping app..."
kill $APP_PID
