(ns fulcro-css.core-spec
  (:require #?(:cljs [fulcro-spec.core :refer-macros [specification assertions behavior]]
               :clj [fulcro-spec.core :refer [specification assertions behavior]])
                    [fulcro-css.core :as css]
                    [fulcro.client.primitives :as prim :refer [defui]]
                    [fulcro.client.dom :as dom]))

(defui Child
  static css/CSS
  (css [this]
    (let [p (css/local-kw Child :p)]
      [p {:font-weight 'bold}]))
  Object
  (render [this]
    (let [{:keys [id label]} (prim/props this)]
      (dom/div nil "Hello"))))

(defui Child2
  static css/CSS
  (css [this]
    (let [p  (css/local-kw Child2 :p)
          p2 (css/local-kw Child2 :p2)]
      [[p {:font-weight 'bold}] [p2 {:font-weight 'normal}]]))
  Object
  (render [this]
    (let [{:keys [id label]} (prim/props this)]
      (dom/div nil "Hello"))))

(specification "CSS local classes"
  (behavior "can be generated for a class"
    (assertions
      "with a keyword"
      (css/local-class Child :root) => "fulcro-css_core-spec_Child__root"
      "with a string"
      (css/local-class Child "root") => "fulcro-css_core-spec_Child__root"
      "with a symbol"
      (css/local-class Child 'root) => "fulcro-css_core-spec_Child__root")))

