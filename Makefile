tests:
	lein doo chrome automated-tests once
	lein test-refresh :run-once

watch:
	lein doo chrome automated-tests

ci-tests:
	npm install
	lein doo firefox automated-tests once
	lein test-refresh :run-once
