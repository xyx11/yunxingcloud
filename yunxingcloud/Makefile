.PHONY: dev build test clean deploy

# ---- 本地开发 ----
dev:
	./mvnw spring-boot:run -pl yunxingcloud-core -Dspring-boot.run.profiles=dev

dev-usercenter:
	./mvnw spring-boot:run -pl yunxingcloud-usercenter -Dspring-boot.run.profiles=dev

dev-gateway:
	./mvnw spring-boot:run -pl yunxingcloud-gateway

# ---- 构建 ----
build:
	./mvnw compile -q
	cd frontend && npm run build

package:
	./mvnw package -pl yunxingcloud-core -DskipTests -q

# ---- 测试 ----
test:
	./mvnw test -pl yunxingcloud-core,yunxingcloud-usercenter

test-core:
	./mvnw test -pl yunxingcloud-core

# ---- 清理 ----
clean:
	./mvnw clean -q
	rm -rf frontend/dist

# ---- Docker ----
docker-build:
	./mvnw package -pl yunxingcloud-core -DskipTests -q
	docker build -t yunxingcloud:latest .

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down

# ---- 部署 ----
deploy:
	./deploy.sh full

deploy-restart:
	./deploy.sh restart

deploy-logs:
	./deploy.sh logs
