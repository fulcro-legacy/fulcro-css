(ns om-css.core-spec
  (:require #?(:cljs [untangled-spec.core :refer-macros [specification assertions behavior]]
               :clj  [untangled-spec.core :refer [specification assertions behavior]])
            [om-css.core :as css]
            [om.next :as om :refer [defui]]
            [om.dom :as dom]))


(defui ListItem
  static css/localCSS
  (local-css [this] [[:.item {:font-weight "bold"}]])
  Object
  (render [this]
    (dom/li nil "listitem")))

(defui List
  static css/localCSS
  (local-css [this] [[:.items-wrapper {:background-color "blue"}]])
  static css/childrenCSS
  (children-css [this] [ListItem])
  Object
  (render [this]
    (dom/ul nil "list")))


(defui Root
  static css/localCSS
  (local-css [this] [[:.container {:background-color "red"}]])
  static css/globalCSS
  (global-css [this] [[:.text {:color "green"}]])
  static css/childrenCSS
  (children-css [this] [List])
  Object
  (render [this]
    (dom/div nil "root")))

(specification "Obtain CSS from classes"
  (behavior "can be obtained from"
    (assertions
     "a single component"
     (css/get-css ListItem) => '([:.om-css_core-spec_ListItem__item {:font-weight "bold"}])
     "a component with a child"
     (css/get-css List) => '([:.om-css_core-spec_List__items-wrapper {:background-color "blue"}]
                             [:.om-css_core-spec_ListItem__item {:font-weight "bold"}])
     "a component with nested children"
     (css/get-css Root) => '([:.om-css_core-spec_Root__container {:background-color "red"}]
                             [:.text {:color "green"}]
                             [:.om-css_core-spec_List__items-wrapper {:background-color "blue"}]
                             [:.om-css_core-spec_ListItem__item {:font-weight "bold"}]))))


(specification "Generate classnames from CSS"
  (assertions
    "global classnames are untouched"
    (:text (css/get-classnames Root)) => "text"
    "local classnames are transformed"
    (:container (css/get-classnames Root)) => "om-css_core-spec_Root__container"
    "does not generate children-classnames"
    (:items-wrapper (css/get-classnames Root)) => nil))
