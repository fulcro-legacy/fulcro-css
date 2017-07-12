(ns fulcro-css.core-spec
  (:require #?(:cljs [fulcro-spec.core :refer-macros [specification assertions behavior]]
               :clj  [fulcro-spec.core :refer [specification assertions behavior]])
            [fulcro-css.core :as css :refer [localize-classnames]]
            [om.next :as om :refer [defui]]
            [om.dom :as dom]))

(defui Child
  static css/CSS
  (css [this]
    (let [p (css/local-kw Child :p)]
      [p {:font-weight 'bold}]))
  Object
  (render [this]
    (let [{:keys [id label]} (om/props this)]
      (dom/div nil "Hello"))))

(defui Child2
  static css/CSS
  (css [this]
    (let [p (css/local-kw Child2 :p)
          p2 (css/local-kw Child2 :p2)]
      [[p {:font-weight 'bold}] [p2 {:font-weight 'normal}]]))
  Object
  (render [this]
    (let [{:keys [id label]} (om/props this)]
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

(specification "CSS merge"
  (assertions
    "Allows a component to specify a single rule"
    (css/css-merge Child) => [[:.fulcro-css_core-spec_Child__p {:font-weight 'bold}]]
    "Allows a component to specify multiple rules"
    (css/css-merge Child2) => [[:.fulcro-css_core-spec_Child2__p {:font-weight 'bold}]
                               [:.fulcro-css_core-spec_Child2__p2 {:font-weight 'normal}]]
    "Allows component combinations"
    (css/css-merge Child Child2) => [[:.fulcro-css_core-spec_Child__p {:font-weight 'bold}]
                                     [:.fulcro-css_core-spec_Child2__p {:font-weight 'bold}]
                                     [:.fulcro-css_core-spec_Child2__p2 {:font-weight 'normal}]]
    "Merges rules in with component css"
    (css/css-merge Child [:a {:x 1}] Child2) => [[:.fulcro-css_core-spec_Child__p {:font-weight 'bold}]
                                                 [:a {:x 1}]
                                                 [:.fulcro-css_core-spec_Child2__p {:font-weight 'bold}]
                                                 [:.fulcro-css_core-spec_Child2__p2 {:font-weight 'normal}]]))

(defui Boo
  static css/CSS
  (css [this] [:a {:x 1}]))

(specification "apply-css macro"
  (assertions
    "Converts :class entries to localized names for defui types"
    (localize-classnames Boo (pr-str [:a {:b [:c {:d #js {:class :a}}]}])) => #?(:cljs "[:a {:b [:c {:d #js {:className \"fulcro-css_core-spec_Boo__a\"}}]}]"
                                                                                 :clj "[:a {:b [:c {:d {:className \"fulcro-css_core-spec_Boo__a\"}}]}]")))
