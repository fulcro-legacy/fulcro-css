(defproject fulcrologic/fulcro-css "2.0.0-beta1"
  :description "A composable library for co-located CSS on Fulcro UI components"
  :url ""
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.8.0" :scope "provided"]
                 [org.clojure/clojurescript "1.9.946" :scope "provided"]
                 [garden "1.3.3"]
                 [com.rpl/specter "1.0.2"]

                 [lein-doo "0.1.7" :scope "test"]
                 [fulcrologic/fulcro "2.0.0-beta1" :scope "test"]
                 [fulcrologic/fulcro-spec "2.0.0-beta1" :scope "test"]]

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-doo "0.1.8"]
            [com.jakemccrary/lein-test-refresh "0.21.1" :exclusions [org.clojure/tools.namespace]]]

  :test-refresh {:report fulcro-spec.reporters.terminal/fulcro-report}

  :source-paths ["src"]
  :test-paths ["test"]
  :resource-paths []
  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :doo {:build "automated-tests"
        :paths {:karma "node_modules/karma/bin/karma"}}

  :cljsbuild {:builds [{:id           "test"
                        :source-paths ["src" "test"]
                        :figwheel     {:on-jsload fulcro-css.suite/on-load}
                        :compiler     {:main       fulcro-css.suite
                                       :output-to  "resources/public/js/specs.js"
                                       :output-dir "resources/public/js/specs"
                                       :asset-path "js/specs"}}
                       {:id           "automated-tests"
                        :source-paths ["src" "test"]
                        :compiler     {:main       fulcro-css.automated-test-main
                                       :output-to  "resources/public/js/unit-tests.js"
                                       :output-dir "resources/public/js/unit-tests"
                                       :optimizations :none
                                       :asset-path "js/unit-tests"}}
                       {:id           "cards"
                        :source-paths ["src" "cards"]
                        :figwheel     {:devcards true}
                        :compiler     {:main       fulcro-css.cards-ui
                                       :output-to  "resources/public/js/cards.js"
                                       :output-dir "resources/public/js/cards"
                                       :asset-path "js/cards"}}]}

  :profiles {:dev {:source-paths   ["src" "dev" "cards" "test"]
                   :resource-paths ["resources"]
                   :dependencies   [[binaryage/devtools "0.9.7"]
                                    [com.cemerick/piggieback "0.2.2"]
                                    [devcards "0.2.3"]
                                    [figwheel-sidecar "0.5.14" :exclusions [org.clojure/tools.nrepl]]]}}

  :repl-options {:init-ns          user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
