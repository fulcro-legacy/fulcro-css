(ns fulcro-css.automated-test-main
  (:require fulcro-css.tests-to-run
            [doo.runner :refer-macros [doo-all-tests]]))

(doo-all-tests #"fulcro.css.*-spec")
