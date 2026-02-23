# #######################################################################################
# Service commands
# #######################################################################################
ENVIRONMENT?=dev
MICROSERVICE_NAME=skills-hub
DEBUG_PORT=10020

#include envs/${ENVIRONMENT}.env
#export $(shell sed 's/=.*//' envs/$(ENVIRONMENT).env)

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



# -------------
# 📦 INSTALLATION LOCALE
# -------------
#todo verifier
#install-deps:
#	@echo "Installing system dependencies (ffmpeg for ffprobe)..."
#ifeq ($(shell uname -s),Darwin)
#	brew install ffmpeg
#else
#	sudo apt-get update && sudo apt-get install -y ffmpeg
#endif
#	@echo "✅ ffmpeg (and ffprobe) installed."
# #######################################################################################
# Liquibase commands for course_service (PostgreSQL)
# #######################################################################################

LIQUIBASE_VARIABLES = 	-Dliquibase.changeLogFile=db/changelog/course/changelog-course-master.yaml  \
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

LIQUIBASE_STORAGE = -Dliquibase.changeLogFile=db/changelog/storage/changelog-storage-master.yaml \
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
# Liquibase commands for user_service (PostgreSQL)
# #######################################################################################

LIQUIBASE_USER = -Dliquibase.changeLogFile=db/changelog/user/changelog-user-master.yaml \
				 -Dliquibase.url=jdbc:postgresql://127.0.0.1:5435/user_service \
				 -Dliquibase.username=root \
				 -Dliquibase.password=root \
				 -Dliquibase.contexts=$(CONTEXT)

# ▶️ Apply new migrations
user_migration_up: compile
	mvn ${LIQUIBASE_USER} liquibase:update

# 🔙 Rollback last migration (1 changeset)
user_migration_down: compile
	mvn ${LIQUIBASE_USER} liquibase:rollback -Dliquibase.rollbackCount=1

# 🧩 Mark current state as up-to-date
user_migration_sync: compile
	mvn ${LIQUIBASE_USER} liquibase:changelogSync

# 🧹 Clear checksums
user_migration_clear: compile
	mvn ${LIQUIBASE_USER} liquibase:clearCheckSums



# #######################################################################################
# MinIO commands
# #######################################################################################
create_minio_buckets:
	@echo "Creating initial MinIO buckets..."
	@docker exec -it minio mc alias set local http://localhost:9000 minioadmin minioadmin123
	@docker exec -it minio mc mb local/course-videos || true
	@docker exec -it minio mc mb local/course-thumbnails || true

# #######################################################################################
# Observability (Prometheus + Grafana)
# #######################################################################################
prometheus_up:
	@echo "🚀 Starting Prometheus..."
	docker-compose up -d prometheus

prometheus_logs:
	@docker logs -f prometheus

prometheus_down:
	@echo "🛑 Stopping Prometheus..."
	docker-compose stop prometheus

prometheus_restart:
	@echo "🔁 Restarting Prometheus..."
	docker-compose restart prometheus

# #######################################################################################
# Grafana
# #######################################################################################

grafana_up:
	@echo "🚀 Starting Grafana..."
	docker-compose up -d grafana

grafana_logs:
	@docker logs -f grafana

grafana_down:
	@echo "🛑 Stopping Grafana..."
	docker-compose stop grafana

grafana_restart:
	@echo "🔁 Restarting Grafana..."
	docker-compose restart grafana
# --------------------------------------------------------------------------------
# Loki & Promtail
# --------------------------------------------------------------------------------
loki_up:
	docker-compose up -d loki promtail

loki_logs:
	docker logs -f loki

loki_down:
	docker-compose down loki promtail

#docker run \
#  -e MAX_CONCURRENT=3 \
#  -e MIN_FREE_SPACE_MB=500 \
#  storage-ms:latest