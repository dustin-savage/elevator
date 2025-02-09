mvn:
	mvn clean install

mvn-skip:
	mvn clean install -pl svc -o -DskipTests

up:
	docker-compose -p elevator -f docker-compose.yml up -d
	make logs

logs:
	docker logs -f elevator-svc-1

down:
	docker-compose -p elevator -f docker-compose.yml down --remove-orphans

build-up:
	make mvn-skip
	make up
	make logs
