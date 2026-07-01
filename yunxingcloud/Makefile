.PHONY: dev build test clean deploy lint type-check k6-smoke k6-load k6-fullflow test-frontend test-mall test-all

# ---- 本地开发 ----
dev:
	./mvnw spring-boot:run -pl yunxingcloud-core -Dspring-boot.run.profiles=dev

dev-core:
	./mvnw spring-boot:run -pl yunxingcloud-core -Dspring-boot.run.profiles=dev

dev-usercenter:
	./mvnw spring-boot:run -pl yunxingcloud-usercenter -Dspring-boot.run.profiles=dev

dev-payment:
	./mvnw spring-boot:run -pl yunxingcloud-payment -Dspring-boot.run.profiles=dev

dev-order:
	./mvnw spring-boot:run -pl yunxingcloud-order -Dspring-boot.run.profiles=dev

dev-inventory:
	./mvnw spring-boot:run -pl yunxingcloud-inventory -Dspring-boot.run.profiles=dev

dev-gateway:
	./mvnw spring-boot:run -pl yunxingcloud-gateway

dev-all:
	@echo "按依赖顺序启动全部 6 服务..."
	./mvnw spring-boot:run -pl yunxingcloud-usercenter -Dspring-boot.run.profiles=dev & sleep 8
	./mvnw spring-boot:run -pl yunxingcloud-payment -Dspring-boot.run.profiles=dev & sleep 5
	./mvnw spring-boot:run -pl yunxingcloud-order -Dspring-boot.run.profiles=dev & sleep 5
	./mvnw spring-boot:run -pl yunxingcloud-inventory -Dspring-boot.run.profiles=dev & sleep 5
	./mvnw spring-boot:run -pl yunxingcloud-core -Dspring-boot.run.profiles=dev & sleep 8
	./mvnw spring-boot:run -pl yunxingcloud-gateway &
	@echo "全部服务已启动"

# ---- 构建 ----
build:
	./mvnw compile -q
	(cd frontend && npm run build)
	(cd frontend-mall && npm run build)

package:
	./mvnw package -pl yunxingcloud-core,yunxingcloud-usercenter,yunxingcloud-gateway,yunxingcloud-payment,yunxingcloud-order,yunxingcloud-inventory -DskipTests -q

# ---- 测试 ----
test:
	./mvnw test -pl yunxingcloud-core,yunxingcloud-usercenter,yunxingcloud-gateway,yunxingcloud-payment,yunxingcloud-order,yunxingcloud-inventory

test-core:
	./mvnw test -pl yunxingcloud-core

test-gateway:
	./mvnw test -pl yunxingcloud-gateway

test-usercenter:
	./mvnw test -pl yunxingcloud-usercenter

test-payment:
	./mvnw test -pl yunxingcloud-payment

test-order:
	./mvnw test -pl yunxingcloud-order

test-inventory:
	./mvnw test -pl yunxingcloud-inventory


test-frontend:
	cd frontend && npm test

test-mall:
	cd frontend-mall && npm test

test-all:
	./mvnw test
	cd frontend && npm test
	cd frontend-mall && npm test

# ---- 清理 ----
clean:
	./mvnw clean -q
	rm -rf frontend/dist frontend-mall/dist

# ---- Docker ----
docker-build:
	./mvnw package -pl yunxingcloud-core -DskipTests -q
	docker build --build-arg SERVICE=yunxingcloud-core --build-arg PORT=8080 -t yunxingcloud-core:latest .

docker-build-all:
	./mvnw package -pl yunxingcloud-core,yunxingcloud-usercenter,yunxingcloud-gateway,yunxingcloud-payment,yunxingcloud-order,yunxingcloud-inventory -DskipTests -q
	docker build --build-arg SERVICE=yunxingcloud-core --build-arg PORT=8080 -t yunxingcloud-core:latest .
	docker build --build-arg SERVICE=yunxingcloud-usercenter --build-arg PORT=8081 -t yunxingcloud-usercenter:latest .
	docker build --build-arg SERVICE=yunxingcloud-gateway --build-arg PORT=8090 -t yunxingcloud-gateway:latest .
	docker build --build-arg SERVICE=yunxingcloud-payment --build-arg PORT=8083 -t yunxingcloud-payment:latest .
	docker build --build-arg SERVICE=yunxingcloud-order --build-arg PORT=8084 -t yunxingcloud-order:latest .
	docker build --build-arg SERVICE=yunxingcloud-inventory --build-arg PORT=8085 -t yunxingcloud-inventory:latest .

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down

# ---- 代码检查 ----
lint:
	cd frontend && npm run lint
	cd frontend-mall && npm run lint

type-check:
	cd frontend && npx vue-tsc --noEmit
	cd frontend-mall && npx vue-tsc --noEmit

# ---- 压测 ----
k6-smoke:
	k6 run k6-smoke-test.js

k6-load:
	k6 run k6-load-test.js

k6-fullflow:
	k6 run k6-fullflow-test.js

# ---- 部署 ----
deploy:
	./deploy.sh full

deploy-quick:
	./deploy.sh quick

deploy-restart:
	./deploy.sh restart

deploy-logs:
	./deploy.sh logs