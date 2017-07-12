(ns fulcro-css.suite
  (:require-macros
    [fulcro-spec.reporters.suite :as ts])
  (:require
    fulcro-spec.reporters.impl.suite
    fulcro-css.core-spec
    fulcro-css.css-spec
    [devtools.core :as devtools]))

(enable-console-print!)

(devtools/enable-feature! :sanity-hints)
(devtools/install!)

(ts/deftest-all-suite app-specs #".*-spec")

(app-specs)

