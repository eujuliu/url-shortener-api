.PHONY: services watch watch_down watch_prune

services:
	cat .env* 2>/dev/null | grep -E '^[A-Za-z_][A-Za-z0-9_]*=.+' | awk -F= '!seen[$$1]++' > .env.docker; \
	docker compose -f ./docker-compose.dev.yml --profile services -p shortener up -d

watch:
	cat .env* 2>/dev/null | grep -E '^[A-Za-z_][A-Za-z0-9_]*=.+' | awk -F= '!seen[$$1]++' > .env.docker; \
	docker compose -f ./docker-compose.dev.yml -p shortener  up shortener watch_mode --watch --build --force-recreate --attach shortener; \
	docker stop shortener watch_mode; \
	docker system prune --all --volumes --filter "label=com.docker.compose.project=shortener" --force

watch_down:
	docker compose -f ./docker-compose.dev.yml -p shortener down --volumes --remove-orphans; \
	rm -f .env.docker

watch_prune:
	docker stop $$(docker ps -a -q --filter "label=com.docker.compose.project=shortener"); \
	docker system prune -a --filter "label=com.docker.compose.project=shortener" -f
