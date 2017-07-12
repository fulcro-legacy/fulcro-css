(defproject fulcrologic/fulcro-css "1.1.0"
  :description "A composable library for co-located CSS on Om/Fulcro UI components"
  :url ""
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]
                 [org.clojure/clojurescript "1.9.671" :scope "provided"]
                 [org.omcljs/om "1.0.0-beta1" :scope "provided"]
                 [garden "1.3.2"]
                 [com.rpl/specter "1.0.2"]
                 [fulcrologic/fulcro-spec "1.0.0-beta2" :scope "test"]]

  :plugins [[lein-cljsbuild "1.1.6"]
            [com.jakemccrary/lein-test-refresh "0.19.0" :exclusions [org.clojure/tools.namespace]]]

  :test-refresh {:report fulcro-spec.reporters.terminal/fulcro-report}

  :source-paths ["src"]
  :test-paths ["test"]
  :resource-paths []
  :clean-targets ^{:protect false} ["resources/public/js" "target"]

  :cljsbuild {:builds [{:id           "test"
                        :source-paths ["src" "test"]
                        :figwheel     {:on-jsload fulcro-css.suite/on-load}
                        :compiler     {:main       fulcro-css.suite
                                       :output-to  "resources/public/js/specs.js"
                                       :output-dir "resources/public/js/specs"
                                       :asset-path "js/specs"}}
                       {:id           "cards"
                        :source-paths ["src" "cards"]
                        :figwheel     {:devcards true}
                        :compiler     {:main       fulcro-css.cards-ui
                                       :output-to  "resources/public/js/cards.js"
                                       :output-dir "resources/public/js/cards"
                                       :asset-path "js/cards"}}]}

  :profiles {:dev {:source-paths   ["src" "dev" "cards" "test"]
                   :resource-paths ["resources"]
                   :dependencies   [[binaryage/devtools "0.9.4"]
                                    [com.cemerick/piggieback "0.2.1"]
                                    [fulcrologic/fulcro "1.0.0-beta2"]
                                    [devcards "0.2.3"]
                                    [figwheel-sidecar "0.5.10" :exclusions [org.clojure/tools.nrepl]]]}}

  :repl-options {:init-ns          user
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]})
