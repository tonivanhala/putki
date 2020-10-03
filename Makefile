.PHONY: lint
lint:
		clj -Mformat-check
		clj -Mclj-kondo

.PHONY: format-code
format-code:
		clojure -Mformat

.PHONY: test
test:
		clojure -Mtest -m kaocha.runner
