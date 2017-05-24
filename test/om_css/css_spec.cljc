(ns om-css.css-spec
  (:require #?(:cljs [untangled-spec.core :refer-macros [specification assertions behavior]]
               :clj  [untangled-spec.core :refer [specification assertions behavior]])
            [om-css.css :as css]
            [om.next :as om :refer [defui]]
            [om.dom :as dom]))

(defui ListItem
  static css/CSS
  (local-rules [this] [[:.item {:font-weight "bold"}]])
  (children [this] [])
  Object
  (render [this]
          (dom/li nil "listitem")))

(defui ListComponent
  static css/CSS
  (local-rules [this] [[:.items-wrapper {:background-color "blue"}]])
  (children [this] [ListItem])
  Object
  (render [this]
    (dom/ul nil "list")))

(defui Root
  static css/CSS
  (local-rules [this] [[:.container {:background-color "red"}]])
  (children [this] [ListComponent])
  static css/Global
  (global-rules [this] [[:.text {:color "green"}]])
  Object
  (render [this]
    (dom/div nil "root")))

(defui Child1
  static css/CSS
  (local-rules [this] [[:.child1class {:color "red"}]])
  (children [this] []))

(defui Child2
  static css/CSS
  (local-rules [this] [[:.child2class {:color "blue"}]])
  (children [this] []))

(defui Parent
  static css/CSS
  (local-rules [this] [])
  (children [this] [Child1 Child2]))

(specification "Obtain CSS from classes"
  (behavior "can be obtained from"
    (assertions
     "a single component"
     (css/get-css ListItem) => '([:.om-css_css-spec_ListItem__item {:font-weight "bold"}])
     "a component with a child"
     (css/get-css ListComponent) => '([:.om-css_css-spec_ListComponent__items-wrapper {:background-color "blue"}]
                             [:.om-css_css-spec_ListItem__item {:font-weight "bold"}])
     "a component with nested children"
     (css/get-css Root) => '([:.om-css_css-spec_Root__container {:background-color "red"}]
                             [:.text {:color "green"}]
                             [:.om-css_css-spec_ListComponent__items-wrapper {:background-color "blue"}]
                             [:.om-css_css-spec_ListItem__item {:font-weight "bold"}])
     "a component with multiple direct children"
     (css/get-css Parent) => '([:.om-css_css-spec_Child1__child1class {:color "red"}]
                               [:.om-css_css-spec_Child2__child2class {:color "blue"}]))))


(specification "Generate classnames from CSS"
  (assertions
    "global classnames are untouched"
    (:text (css/get-classnames Root)) => "text"
    "local classnames are transformed"
    (:container (css/get-classnames Root)) => "om-css_css-spec_Root__container"
    "does not generate children-classnames"
    (:items-wrapper (css/get-classnames Root)) => nil))
