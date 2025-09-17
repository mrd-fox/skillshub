# #######################################################################################
# Service commands
# #######################################################################################
ENVIRONMENT?=dev
MICROSERVICE_NAME=skills-hub
DEBUG_PORT=10020

include envs/${ENVIRONMENT}.env
export $(shell sed 's/=.*//' envs/$(ENVIRONMENT).env)

compile:
	mvn clean test-compile

install:
	mvn clean install

install_skip_test:
	mvn clean install -DskipTests

install_and_run:
	mvn clean install -DskipTests spring-boot:run

run:
	mvn spring-boot:run

run_dev:
	java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$(DEBUG_PORT) -jar target/$(MICROSERVICE_NAME)*.jar

 # #######################################################################################
 # Extra commands
 # #######################################################################################
install_with_pact_consumer_test:
	mvn clean install -Ddockerfile.skip -Ppact-consumer-test

install_with_diagram_generate: install diagram_generate

diagram_generate:
	mvn uml-generator:generate

# #######################################################################################
# Liquibase commands for course_service (PostgreSQL)
# #######################################################################################

LIQUIBASE_VARIABLES = 	-Dliquibase.changeLogFile=db/changelog/db.changelog-master.yaml \
						-Dliquibase.url=jdbc:postgresql://127.0.0.1:5433/course_service \
						-Dliquibase.username=root \
						-Dliquibase.password=root \
						-Dliquibase.contexts=$(CONTEXT)

migration_up: compile
	mvn ${LIQUIBASE_VARIABLES} liquibase:update

migration_down: compile
	mvn ${LIQUIBASE_VARIABLES} liquibase:rollback -Dliquibase.rollbackCount=1

migration_sync: compile
	mvn ${LIQUIBASE_VARIABLES} liquibase:changelogSync

migration_clear: compile
	mvn ${LIQUIBASE_VARIABLES} liquibase:clearChekSums

# #######################################################################################
# Liquibase commands for storage_service (PostgreSQL)
# #######################################################################################

LIQUIBASE_STORAGE = 	-Dliquibase.changeLogFile=db/changelog/db.changelog-master.yaml \
					-Dliquibase.url=jdbc:postgresql://127.0.0.1:5434/storage_service \
					-Dliquibase.username=root \
					-Dliquibase.password=root \
					-Dliquibase.contexts=$(CONTEXT)

storage_migration_up: compile
	mvn ${LIQUIBASE_STORAGE} liquibase:update

storage_migration_down: compile
	mvn ${LIQUIBASE_STORAGE} liquibase:rollback -Dliquibase.rollbackCount=1

storage_migration_sync: compile
	mvn ${LIQUIBASE_STORAGE} liquibase:changelogSync

storage_migration_clear: compile
	mvn ${LIQUIBASE_STORAGE} liquibase:clearCheckSums

# #######################################################################################
# MinIO commands
# #######################################################################################

# Exemple : créer les buckets initiaux
create_minio_buckets:
	@echo "Creating initial MinIO buckets..."
	@docker exec -it minio mc alias set local http://localhost:9000 minioadmin minioadmin
	@docker exec -it minio mc mb local/course-videos || true
	@docker exec -it minio mc mb local/course-thumbnails || true
