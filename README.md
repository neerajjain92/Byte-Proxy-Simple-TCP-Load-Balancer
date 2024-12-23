## Test Setup

1. Start backend servers:
```
docker-compose up -d
```

2. Verify backend servers are running:
```
curl http://localhost:8081
curl http://localhost:8082
curl http://localhost:8083
```
Each should return a unique response with server ID and timestamp

3. Start the load balancer:
```
mvn clean package
java -jar target/byte-proxy-1.0-SNAPSHOT.jar
```

4. Test load balancing:
```
# Run multiple times to see round-robin in action
curl http://localhost:9090
curl http://localhost:9090
curl http://localhost:9090
```

You should see responses rotating between server1, server2, and server3, demonstrating the round-robin load balancing in action.

To stop:
```
# Stop load balancer with Ctrl+C
# Stop backend servers
docker-compose down
```