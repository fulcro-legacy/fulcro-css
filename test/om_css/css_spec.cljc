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
  (children [this] [])
  Object
  (render [this]
          (dom/div nil "test")))

(defui Child2
  static css/CSS
  (local-rules [this] [[:.child2class {:color "blue"}]])
  (children [this] [])
  Object
  (render [this]
          (dom/div nil "test")))

(defui Parent
  static css/CSS
  (local-rules [this] [])
  (children [this] [Child1 Child2])
  Object
  (render [this]
          (dom/div nil "test")))

(defui MyLabel
  static css/CSS
  (local-rules [this] [[:.my-label {:color "green"}]])
  (children [this] []))

(defui MyButton
  static css/CSS
  (local-rules [this] [[:.my-button {:color "black"}]])
  (children [this] [MyLabel])
  Object
  (render [this]
          (dom/div nil "test")))

(defui MyForm
  static css/CSS
  (local-rules [this] [[:.form {:background-color "white"}]])
  (children [this] [MyButton])
  Object
  (render [this]
          (dom/div nil "test")))

(defui MyNavigation
  static css/CSS
  (local-rules [this] [[:.nav {:width "100px"}]])
  (children [this] [MyButton])
  Object
  (render [this]
          (dom/div nil "test")))

(defui MyRoot
  static css/CSS
  (local-rules [this] [])
  (children [this] [MyForm MyNavigation])
  Object
  (render [this]
          (dom/div nil "test")))

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
                               [:.om-css_css-spec_Child2__child2class {:color "blue"}])
     "a component with multiple direct children without duplicating rules"
     (css/get-css MyRoot) => '([:.om-css_css-spec_MyForm__form {:background-color "white"}]
                               [:.om-css_css-spec_MyNavigation__nav {:width "100px"}]
                               [:.om-css_css-spec_MyButton__my-button {:color "black"}]
                               [:.om-css_css-spec_MyLabel__my-label {:color "green"}]))))

(specification "Generate classnames from CSS"
  (assertions
    "global classnames are untouched"
    (:text (css/get-classnames Root)) => "text"
    "local classnames are transformed"
    (:container (css/get-classnames Root)) => "om-css_css-spec_Root__container"
    "does not generate children-classnames"
    (:items-wrapper (css/get-classnames Root)) => nil))
