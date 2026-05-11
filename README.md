# Run Project
## Dev environment
./start-dev.sh

## Requisito local
El proyecto compila con Java 21. Si ejecutas Maven manualmente, verifica que `java -version` muestre 21 antes de correr `./mvnw`.

## Prod environment
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
