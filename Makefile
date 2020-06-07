.PHONY: lint
lint:
		clj -Aformat-check
		clj -Aclj-kondo

.PHONY: format-code
format-code:
		clojure -Aformat
