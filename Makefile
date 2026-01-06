.PHONY: services watch watch_down watch_prune

create_env:
	cat .env* 2>/dev/null | grep -E '^[A-Za-z_][A-Za-z0-9_]*=.+' | awk -F= '!seen[$$1]++' > .env.docker

services:
	make create_env; \
	docker compose -f ./docker-compose.dev.yml --profile services -p shortener up -d

watch:
	make create_env; \
	docker compose -f ./docker-compose.dev.yml -p shortener  up shortener watch_mode ngrok --watch --build --force-recreate --attach shortener; \
	make watch_down

watch_down:
	docker compose -f ./docker-compose.dev.yml -p shortener down --volumes --remove-orphans; \
	docker stop shortener watch_mode ngrok; \
	docker system prune --all --filter "label=com.docker.compose.project=shortener" --force; \
	docker volume rm shortener_root shortener_app; \
	rm -f .env.docker

watch_prune:
	docker stop $$(docker ps -a -q --filter "label=com.docker.compose.project=shortener"); \
	docker system prune -a --filter "label=com.docker.compose.project=shortener" -f

test_oauth:
	# npm i -g live-server
	live-server --port=5050 --host="localhost" --watch=./test-static --entry-file=./test-static/index.html --cors
