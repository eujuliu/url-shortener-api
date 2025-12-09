.PHONY: watch watch_down

watch:
	cat .env* > .env.docker; \
	docker compose -f ./docker-compose.dev.yaml -p shortener up --watch --build --force-recreate --attach shortener; \
	make watch_down

watch_down:
	docker compose -f ./docker-compose.dev.yaml -p shortener down shortener --volumes --remove-orphans; \
	rm -f .env.docker

watch_prune:
	docker stop $$(docker ps -a -q --filter "label=com.docker.compose.project=shortener"); \
	docker system prune -a --filter "label=com.docker.compose.project=shortener" -f