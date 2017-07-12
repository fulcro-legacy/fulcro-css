(ns fulcro-css.suite
  (:require
    fulcro-css.tests-to-run
    [fulcro-spec.selectors :as sel]
    [fulcro-spec.suite :as suite]))

(enable-console-print!)

(suite/def-test-suite on-load {:ns-regex #"fulcro.css.*-spec"}
  {:default   #{::sel/none :focused}
   :available #{:focused}})

